#!/usr/bin/env python3
import argparse
import csv
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import urllib.request
import xml.etree.ElementTree as ET

sys.path.insert(0, str(Path(__file__).resolve().parent))
import batch_reproflake_chatunitest as B

IDOFT_URL = "https://raw.githubusercontent.com/TestingResearchIllinois/idoft/main/pr-data.csv"
IDOFT_TEST_COL = "Fully-Qualified Test Name (packageName.ClassName.methodName)"
UNIT_FIELDS = ["unit", "project", "commit", "module", "zip", "java", "status", "mvn_exit",
               "build_status", "tests_ran", "passed", "not_passed", "excluded_flaky",
               "nonflaky", "flaky_listed", "flaky_not_seen", "note"]
TEST_FIELDS = ["project", "commit", "module", "zip", "java", "url", "test", "outcome", "excluded"]
NONFLAKY_FIELDS = ["project", "commit", "module", "zip", "java", "url", "test"]
IDENT = re.compile(r"^[A-Za-z_$][\w$]*$")
RANK = {"passed": 0, "skipped": 1, "rerun_passed": 2, "failed": 3}


def norm_url(u):
    return (u or "").strip().rstrip("/").lower()


def norm_module(m):
    m = (m or "").strip().strip("/")
    while m.startswith("./"):
        m = m[2:]
    return m or "."


def load_flaky(matched_rows, idoft_path):
    flaky = {}
    for r in matched_rows:
        key = (norm_url(r["project"]), norm_module(r["module"]), r["flaky_test"].strip())
        flaky.setdefault(key, set()).add("result_matched")
    if idoft_path:
        with open(idoft_path, newline="", encoding="utf-8", errors="replace") as f:
            for r in csv.DictReader(f):
                fq = (r.get(IDOFT_TEST_COL) or "").strip()
                if "." not in fq:
                    continue
                cls, method = fq.rsplit(".", 1)
                key = (norm_url(r.get("Project URL")), norm_module(r.get("Module Path")), f"{cls}#{method}")
                flaky.setdefault(key, set()).add("idoft")
    return flaky


def unit_slug(project, commit, module):
    name = project.rstrip("/").split("/")[-1]
    return f"{name}__{commit[:7]}__{module.replace('/', '+')}"


def archive_available(row, args):
    z = row["zip"]
    return any([(args.archives / z).is_dir(), (args.archives / (z + ".zip")).is_file(),
                (args.out / "extracted" / z / ".batch_extracted").is_file(),
                *[(d / z).is_dir() for d in args.also_look_in]])


def find_archive(row, args):
    for d in args.also_look_in:
        if (d / row["zip"]).is_dir():
            return (d / row["zip"]).resolve()
    return B.get_archive(row, args)


def read_reports(module_dir):
    tests, unmapped = {}, set()
    for report in sorted((module_dir / "target" / "surefire-reports").glob("TEST-*.xml")):
        try:
            root = ET.parse(report).getroot()
        except ET.ParseError:
            continue
        for tc in root.iter("testcase"):
            cls = (tc.get("classname") or "").strip()
            raw = (tc.get("name") or "").strip()
            method = re.split(r"[\[({]", raw, 1)[0].strip()
            if not cls or not IDENT.match(method):
                unmapped.add(f"{cls}#{raw}")
                continue
            tags = {c.tag for c in tc}
            if tags & {"failure", "error"}:
                outcome = "failed"
            elif tags & {"flakyFailure", "flakyError", "rerunFailure", "rerunError"}:
                outcome = "rerun_passed"
            elif "skipped" in tags:
                outcome = "skipped"
            else:
                outcome = "passed"
            key = f"{cls}#{method}"
            if RANK[outcome] >= RANK.get(tests.get(key), -1):
                tests[key] = outcome
    return tests, sorted(unmapped)


def project_root(module_dir, module):
    if module != ".":
        parts = Path(module).parts
        if module_dir.parts[-len(parts):] == parts:
            parent = Path(*module_dir.parts[:-len(parts)])
            if (parent / "pom.xml").is_file():
                return parent, ["-pl", module]
    return module_dir, []


class OriginalPom:

    def __init__(self, module_dir):
        self.pom = module_dir / "pom.xml"
        self.orig = module_dir / "pom.xml.batch_orig"
        self.saved = module_dir / "pom.xml.nonflaky_saved"

    def __enter__(self):
        if self.saved.is_file():
            shutil.copy2(self.saved, self.pom)
            self.saved.unlink()
        if self.orig.is_file():
            shutil.copy2(self.pom, self.saved)
            shutil.copy2(self.orig, self.pom)
        return self

    def __exit__(self, *exc):
        if self.saved.is_file():
            shutil.copy2(self.saved, self.pom)
            self.saved.unlink()


def maven_without_extensions(out):
    mvn = shutil.which("mvn")
    if not mvn:
        return None
    home = Path(mvn).resolve().parent.parent
    if not list((home / "lib" / "ext").glob("*.jar")):
        return None
    copy = out / "maven-noext"
    if not (copy / "bin" / "mvn").is_file():
        print(f"Copying {home} to {copy} without lib/ext/*.jar")
        shutil.copytree(home, copy, symlinks=True, dirs_exist_ok=True,
                        ignore=lambda d, names: [n for n in names
                                                 if Path(d).name == "ext" and n.endswith(".jar")])
    return copy / "bin"


def run_tests(r, env, args, unit_dir, log_name):
    module_dir = Path(r["module_dir"])
    root, pl = project_root(module_dir, r["module"])
    shutil.rmtree(module_dir / "target" / "surefire-reports", ignore_errors=True)
    cmd = ["mvn", "-B", "test", *pl, *[s for s in B.BUILD_SKIPS if s != "-DskipTests"],
           "-Dmaven.test.failure.ignore=true", "-DfailIfNoTests=false",
           "-Dsurefire.failIfNoSpecifiedTests=false", "-Dmaven.repo.local=" + str(args.repo)]
    print("  test: (cd " + str(root) + " && " + " ".join(cmd) + ")", flush=True)
    with (unit_dir / log_name).open("w", encoding="utf-8") as fout:
        try:
            p = subprocess.run(cmd, cwd=root, env=env, stdout=fout, stderr=subprocess.STDOUT,
                               timeout=args.timeout)
        except subprocess.TimeoutExpired:
            fout.write(f"Tests timed out after {args.timeout} seconds.\n")
            return "timeout"
    return p.returncode


def collect_unit(group, flaky, args):
    first = group[0]
    project, commit, module = first["project"].strip(), first["flaky_commit"].strip(), norm_module(first["module"])
    slug = unit_slug(project, commit, module)
    unit_dir = args.out / "units" / slug
    unit_dir.mkdir(parents=True, exist_ok=True)
    row = next((r for r in group if archive_available(r, args)), first)
    rec = {"unit": slug, "project": project, "commit": commit, "module": module,
           "zip": row["zip"], "java": row["java"], "status": "", "mvn_exit": "", "build_status": ""}
    print(f"[{slug}] archive {row['zip']} (JDK {row['java']})", flush=True)

    try:
        root = find_archive(row, args)
        _, module_dir, _ = B.locate_test(root, row["module"], row["flaky_test"])
    except Exception as ex:
        rec.update(status="archive_or_module_not_found", note=str(ex)[:300])
        print(f"  => {rec['status']}: {ex}")
        return rec, []
    r = dict(row, module=module, archive_root=str(root), module_dir=str(module_dir))

    env = os.environ.copy()
    jdk = B.find_jdk(str(row.get("java", "")).strip())
    if jdk:
        env["JAVA_HOME"] = jdk
        env["PATH"] = str(Path(jdk) / "bin") + os.pathsep + env.get("PATH", "")
    else:
        print(f"  no JDK {row['java']} found, using the current java")
    if args.mvn_bin:
        env["PATH"] = str(args.mvn_bin) + os.pathsep + env["PATH"]
        env.pop("M2_HOME", None)
        env.pop("MAVEN_HOME", None)

    with OriginalPom(module_dir):
        attempts = ["", "_archive_m2"] if args.archive_m2 == "fallback" else [""]
        for suffix in attempts:
            if suffix:
                merged = B.merge_archive_m2(r, args.repo)
                if merged is None:
                    break
                print(f"  retrying with the archive's Flakym2/ merged into {args.repo} "
                      f"({merged[0]} files added)")
                (module_dir / ".batch_built").unlink(missing_ok=True)
            build = "not_needed"
            if module != ".":
                build = B.build_module(r, env, args, unit_dir, f"build{suffix}.log")
            rec["build_status"] = build + suffix
            if build in ("build_failed", "build_timeout"):
                continue
            code = run_tests(r, env, args, unit_dir, f"test{suffix}.log")
            rec["mvn_exit"] = code
            tests, unmapped = read_reports(module_dir)
            if tests or code == "timeout":
                break

    if rec["build_status"].startswith(("build_failed", "build_timeout")):
        rec["status"] = rec["build_status"]
        print(f"  => {rec['status']}; logs in {unit_dir}")
        return rec, []
    if rec["mvn_exit"] == "timeout":
        rec["status"] = "test_timeout"
    elif not tests:
        rec["status"] = "no_tests_ran"
    else:
        rec["status"] = "ok"

    key_base = (norm_url(project), module)
    listed = {k[2]: v for k, v in flaky.items() if k[:2] == key_base}
    rows = []
    for test, outcome in sorted(tests.items()):
        excluded = "+".join(sorted(listed[test])) if test in listed else ""
        if not excluded and outcome != "passed":
            excluded = "not_passed"
        rows.append({"project": project, "commit": commit, "module": module, "zip": row["zip"],
                     "java": row["java"], "url": row["url"], "test": test, "outcome": outcome,
                     "excluded": excluded})
    not_seen = sorted(t for t in listed if t not in tests)
    rec.update(tests_ran=len(tests), passed=sum(o == "passed" for o in tests.values()),
               not_passed=sum(o != "passed" for o in tests.values()),
               excluded_flaky=sum(t in listed for t in tests),
               nonflaky=sum(not x["excluded"] for x in rows),
               flaky_listed=len(listed), flaky_not_seen=len(not_seen),
               note=(f"{len(unmapped)} test names not mappable to a method" if unmapped else ""))
    (unit_dir / "result.json").write_text(json.dumps(
        {"record": rec, "flaky_not_seen": not_seen, "unmapped_names": unmapped}, indent=1))
    B.csv_write(unit_dir / "tests.csv", rows, TEST_FIELDS)
    print(f"  => {rec['status']}: {rec['tests_ran']} ran, {rec['excluded_flaky']} known flaky, "
          f"{rec['not_passed']} not passed, {rec['nonflaky']} non-flaky", flush=True)
    return rec, rows


def write_combined(out, units):
    B.csv_write(out / "units.csv", units, UNIT_FIELDS)
    rows = []
    for u in units:
        f = out / "units" / u["unit"] / "tests.csv"
        if u.get("status") == "ok" and f.is_file():
            rows += list(csv.DictReader(f.open(encoding="utf-8")))
    B.csv_write(out / "all_tests.csv", rows, TEST_FIELDS)
    B.csv_write(out / "nonflaky.csv", [r for r in rows if not r["excluded"]], NONFLAKY_FIELDS)
    return rows


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--csv", type=Path, required=True, help="result_matched.csv")
    a.add_argument("--archives", type=Path, default=Path("zips"),
                   help="Folder with <zip>.zip files or extracted <zip>/ folders")
    a.add_argument("--also-look-in", type=Path, action="append", default=[],
                   help="Another folder of already-extracted <zip>/ folders (e.g. out_part1/extracted); repeatable")
    a.add_argument("--out", type=Path, default=Path("nonflaky_out"))
    a.add_argument("--download", action="store_true", help="Download missing archives from Zenodo")
    a.add_argument("--delete-zips", action="store_true", help="Delete each ZIP once it is extracted")
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--timeout", type=int, default=4 * 3600, help="Seconds per Maven run (default 4h)")
    a.add_argument("--archive-m2", choices=["fallback", "never"], default="fallback",
                   help="If the build or test compile fails, merge the archive's Flakym2/ into "
                        "--maven-repo and retry once (default)")
    a.add_argument("--keep-maven-extensions", action="store_true",
                   help="Run Maven with its lib/ext extensions (default: a copy without them)")
    a.add_argument("--idoft", default=IDOFT_URL, help="IDoFT pr-data.csv path or URL")
    a.add_argument("--no-idoft", action="store_true", help="Only exclude tests listed in --csv")
    a.add_argument("--only", action="append", default=[],
                   help="Only units whose project URL, zip or module contains this text; repeatable")
    a.add_argument("--limit", type=int, default=0, help="First N units only (pilot)")
    a.add_argument("--redo", action="store_true", help="Rerun units that already finished")
    args = a.parse_args()
    args.out = args.out.resolve()
    args.archives = args.archives.resolve()
    args.also_look_in = [d.resolve() for d in args.also_look_in]
    args.repo = args.repo.resolve()

    args.mvn_bin = None if args.keep_maven_extensions else maven_without_extensions(args.out)
    matched, _ = B.load_rows(args.csv)
    idoft = None
    if not args.no_idoft:
        idoft = args.out / "idoft_pr_data.csv"
        if re.match(r"https?://", args.idoft):
            if not idoft.is_file():
                args.out.mkdir(parents=True, exist_ok=True)
                print(f"Downloading IDoFT {args.idoft}")
                with urllib.request.urlopen(args.idoft, timeout=120) as src, idoft.open("wb") as dst:
                    shutil.copyfileobj(src, dst)
        else:
            idoft = Path(args.idoft)
    flaky = load_flaky(matched, idoft)

    groups = {}
    for r in matched:
        key = (norm_url(r["project"]), r["flaky_commit"].strip(), norm_module(r["module"]))
        groups.setdefault(key, []).append(r)
    todo = [g for g in groups.values()
            if not args.only or any(s in f"{r['project']} {r['zip']} {r['module']}"
                                    for r in g for s in args.only)]
    if args.limit:
        todo = todo[:args.limit]
    print(f"{len(groups)} (project, commit, module) units in {args.csv}; running {len(todo)}")

    previous = {}
    if (args.out / "units.csv").is_file():
        previous = {u["unit"]: u for u in csv.DictReader((args.out / "units.csv").open(encoding="utf-8"))}
    for i, g in enumerate(todo, 1):
        slug = unit_slug(g[0]["project"].strip(), g[0]["flaky_commit"].strip(), norm_module(g[0]["module"]))
        if not args.redo and previous.get(slug, {}).get("status") == "ok":
            print(f"[{i}/{len(todo)}] {slug}: already done, skipping (--redo to rerun)")
            continue
        print(f"[{i}/{len(todo)}]", end=" ")
        rec, _ = collect_unit(g, flaky, args)
        previous[slug] = rec
        rows = write_combined(args.out, list(previous.values()))

    units = list(previous.values())
    rows = write_combined(args.out, units)
    ok = [u for u in units if u.get("status") == "ok"]
    print(f"\n{len(ok)}/{len(units)} units ok; {sum(not r['excluded'] for r in rows)} non-flaky tests "
          f"-> {args.out / 'nonflaky.csv'}")
    for u in units:
        if u.get("status") != "ok":
            print(f"  {u['unit']}: {u['status']}  {u.get('note', '')}")


if __name__ == "__main__":
    main()

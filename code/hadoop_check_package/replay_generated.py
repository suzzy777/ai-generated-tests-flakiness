#!/usr/bin/env python3
import argparse
import csv
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
import batch_reproflake_chatunitest as B
import collect_nonflaky as C

RESULT_FIELDS = ["id", "part", "row_number", "zip", "module", "java", "flaky_test", "focal",
                 "junit", "overload_match", "test_classes", "build_status", "status", "mvn_exit",
                 "methods", "passed", "failed", "skipped", "archive_m2", "note", "log"]
METHOD_FIELDS = ["id", "zip", "module", "focal", "test", "outcome"]
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)
MOCKITO_STACK = [("org.mockito", "mockito-core", B.MOCKITO_VERSION),
                 ("org.mockito", "mockito-inline", B.MOCKITO_VERSION),
                 ("net.bytebuddy", "byte-buddy", "1.12.19"),
                 ("net.bytebuddy", "byte-buddy-agent", "1.12.19"),
                 ("org.objenesis", "objenesis", "3.3")]
ADDED_MOCKITO = re.compile(r"\s*<dependency>\s*<groupId>org\.mockito</groupId>\s*<artifactId>"
                           r"(mockito-core|mockito-junit-jupiter)</artifactId>\s*<version>"
                           + re.escape(B.MOCKITO_VERSION) + r"</version>\s*<scope>test</scope>\s*</dependency>")


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def generated_rows(parts):
    for part_dir, manifest in parts:
        man = {r["row_number"]: r for r in csv.DictReader(open(manifest, encoding="utf-8"))}
        for g in csv.DictReader(open(part_dir / "generation_results.csv", encoding="utf-8")):
            if g["status"] == "generated":
                yield part_dir, g, man[g["row_number"]]


def test_files(tests_dir):
    out = []
    for f in sorted(Path(tests_dir).rglob("*_Test.java")):
        m = PACKAGE.search(f.read_text(encoding="utf-8", errors="replace"))
        out.append((f, m.group(1) if m else "", f.stem))
    return out


class CopiedTests:

    def __init__(self, module_dir, files, row_dir):
        self.module_dir, self.files, self.ledger = module_dir, files, row_dir / "copied.txt"

    def cleanup(self):
        if not self.ledger.is_file():
            return
        for line in self.ledger.read_text().splitlines():
            pkg_path, name = line.rsplit("|", 1)
            (self.module_dir / "src" / "test" / "java" / pkg_path / (name + ".java")).unlink(missing_ok=True)
            classes = self.module_dir / "target" / "test-classes" / pkg_path
            for c in list(classes.glob(name + ".class")) + list(classes.glob(name + "$*.class")):
                c.unlink()
        self.ledger.unlink()

    def __enter__(self):
        self.cleanup()
        conflicts = []
        for _, pkg, name in self.files:
            dest = self.module_dir / "src" / "test" / "java" / Path(*pkg.split(".")) / (name + ".java")
            if dest.exists():
                conflicts.append(str(dest))
        if conflicts:
            raise FileExistsError("would overwrite " + "; ".join(conflicts))
        with self.ledger.open("w") as led:
            for src, pkg, name in self.files:
                pkg_path = Path(*pkg.split(".")) if pkg else Path()
                dest = self.module_dir / "src" / "test" / "java" / pkg_path / (name + ".java")
                dest.parent.mkdir(parents=True, exist_ok=True)
                led.write(f"{pkg_path}|{name}\n")
                led.flush()
                shutil.copy2(src, dest)
        return self

    def __exit__(self, *exc):
        self.cleanup()


def project_dependencies_open(text):
    close = B.project_dependencies_close(text)
    if close is None:
        return None
    masked = re.sub(r"<!--.*?-->|<!\[CDATA\[.*?\]\]>", lambda m: " " * len(m.group(0)), text, flags=re.S)
    depth = 0
    for m in reversed(list(re.finditer(r"<(/?)dependencies\b[^>]*>", masked[:close]))):
        if m.group(1):
            depth += 1
        elif depth == 0:
            return m.end()
        else:
            depth -= 1
    return None


def mockito_fixed(text, mode):
    text = ADDED_MOCKITO.sub("", text)
    deps = list(MOCKITO_STACK) + ([("org.mockito", "mockito-junit-jupiter", B.MOCKITO_VERSION)]
                                  if mode == "5" else [])
    block = "".join(f"""
        <dependency>
            <groupId>{g}</groupId>
            <artifactId>{a}</artifactId>
            <version>{v}</version>
            <scope>test</scope>
        </dependency>""" for g, a, v in deps)
    at = project_dependencies_open(text)
    if at is None:
        end = text.rindex("</project>")
        return text[:end] + "    <dependencies>" + block + "\n    </dependencies>\n" + text[end:]
    return text[:at] + block + text[at:]


class TempPom:

    def __init__(self, pom, text, row_dir):
        self.pom, self.text, self.saved = pom, text, row_dir / "pom.xml.before"

    def __enter__(self):
        if self.text is not None:
            self.pom.write_text(self.text, encoding="utf-8")
            (self.saved.parent / "pom.xml.used").write_text(self.text, encoding="utf-8")
        return self

    def __exit__(self, *exc):
        if self.text is not None:
            shutil.copy2(self.saved, self.pom)


def run_tests(r, env, args, row_dir, classes, log_name):
    module_dir = Path(r["module_dir"])
    root, pl = C.project_root(module_dir, r["module"])
    shutil.rmtree(module_dir / "target" / "surefire-reports", ignore_errors=True)
    cmd = ["mvn", "-B", "test", *pl, "-Dtest=" + ",".join(classes),
           *[s for s in B.BUILD_SKIPS if s != "-DskipTests"],
           "-Dmaven.test.failure.ignore=true", "-DfailIfNoTests=false",
           "-Dsurefire.failIfNoSpecifiedTests=false", "-Dmaven.repo.local=" + str(args.repo)]
    print("  test: (cd " + str(root) + " && " + " ".join(cmd) + ")", flush=True)
    with (row_dir / log_name).open("w", encoding="utf-8") as fout:
        try:
            p = subprocess.run(cmd, cwd=root, env=env, stdout=fout, stderr=subprocess.STDOUT,
                               timeout=args.timeout)
        except subprocess.TimeoutExpired:
            fout.write(f"Tests timed out after {args.timeout} seconds.\n")
            return "timeout"
    return p.returncode


def keep_reports(module_dir, classes, row_dir):
    dest = row_dir / "surefire-reports"
    shutil.rmtree(dest, ignore_errors=True)
    dest.mkdir()
    for f in (module_dir / "target" / "surefire-reports").glob("*"):
        if any(c in f.name for c in classes):
            shutil.copy2(f, dest / f.name)


def replay(part_dir, g, m, args, env_base):
    rid = f"{part_dir.name}_row_{int(g['row_number']):03d}"
    row_dir = args.out / "rows" / rid
    row_dir.mkdir(parents=True, exist_ok=True)
    tests_dir = Path(g["test_output"])
    if not tests_dir.is_dir():
        tests_dir = part_dir / "runs" / f"row_{int(g['row_number']):03d}" / "tests"
    files = test_files(tests_dir)
    classes = [name for _, _, name in files]
    rec = {"id": rid, "part": part_dir.name, "row_number": g["row_number"], "zip": g["zip"],
           "module": m["module"], "java": m["java"], "flaky_test": g["flaky_test"],
           "focal": g["selected_method"], "junit": g["chatunitest_mode"],
           "overload_match": g["overload_match"], "test_classes": ";".join(classes),
           "build_status": "", "status": "", "mvn_exit": "", "archive_m2": ""}
    print(f"[{rid}] {g['zip']} {g['selected_method']} ({len(files)} test file(s), JDK {m['java']})", flush=True)
    if not files:
        rec.update(status="no_test_files", note="tests/ is empty")
        return rec, []
    try:
        root = (part_dir / "extracted" / g["zip"]).resolve()
        _, module_dir, _ = B.locate_test(root, m["module"], g["flaky_test"])
    except Exception as ex:
        rec.update(status="module_not_found", note=str(ex)[:300])
        return rec, []
    r = dict(m, module=C.norm_module(m["module"]), archive_root=str(root), module_dir=str(module_dir))
    pom = module_dir / "pom.xml"
    used = row_dir / "pom.xml.used"
    if used.is_file() and (row_dir / "pom.xml.before").is_file() and pom.read_bytes() == used.read_bytes():
        print(f"  restoring {pom} left modified by a killed run")
        shutil.copy2(row_dir / "pom.xml.before", pom)
    used.unlink(missing_ok=True)
    shutil.copy2(pom, row_dir / "pom.xml.before")
    pom_sha = sha(pom)
    if "<artifactId>mockito-core</artifactId>" not in pom.read_text(encoding="utf-8"):
        rec["note"] = "pom declares no mockito-core; "

    env = dict(env_base)
    jdk = B.find_jdk(str(m.get("java", "")).strip())
    if jdk:
        env["JAVA_HOME"] = jdk
        env["PATH"] = str(Path(jdk) / "bin") + os.pathsep + env["PATH"]

    tests = {}
    new_pom = mockito_fixed(pom.read_text(encoding="utf-8"), g["chatunitest_mode"]) if args.mockito_fix else None
    try:
        with TempPom(pom, new_pom, row_dir), CopiedTests(module_dir, files, row_dir):
            attempts = ["", "_archive_m2"] if args.archive_m2 == "fallback" else [""]
            for suffix in attempts:
                if suffix:
                    merged = B.merge_archive_m2(r, args.repo)
                    if merged is None:
                        break
                    rec["archive_m2"] = f"{merged[0]} added"
                    print(f"  retrying with Flakym2/ merged into {args.repo} ({merged[0]} files added)")
                    (module_dir / ".batch_built").unlink(missing_ok=True)
                build = "not_needed"
                if r["module"] != ".":
                    build = B.build_module(r, env, args, row_dir, f"build{suffix}.log")
                rec["build_status"] = build + suffix
                if build in ("build_failed", "build_timeout"):
                    continue
                code = run_tests(r, env, args, row_dir, classes, f"test{suffix}.log")
                rec["mvn_exit"] = code
                rec["log"] = str(row_dir / f"test{suffix}.log")
                tests, _ = C.read_reports(module_dir)
                tests = {t: o for t, o in tests.items() if t.split("#")[0].rsplit(".", 1)[-1] in classes}
                if tests or code == "timeout":
                    break
            keep_reports(module_dir, classes, row_dir)
    except FileExistsError as ex:
        rec.update(status="would_overwrite", note=str(ex)[:300])
        return rec, []
    finally:
        if sha(pom) != pom_sha:
            print(f"  WARNING: {pom} changed during the run; original copy in {row_dir / 'pom.xml.before'}")
            rec["note"] = rec.get("note", "") + "POM CHANGED during run; "

    log = Path(rec["log"]).read_text(encoding="utf-8", errors="replace") if rec.get("log") else ""
    if rec["build_status"].startswith(("build_failed", "build_timeout")):
        rec["status"] = rec["build_status"].replace("_archive_m2", "")
    elif rec["mvn_exit"] == "timeout":
        rec["status"] = "test_timeout"
    elif not tests:
        if "COMPILATION ERROR" in log or "Compilation failure" in log:
            rec["status"] = "compile_failed"
        elif "BUILD FAILURE" in log:
            rec["status"] = "maven_failed"
        else:
            rec["status"] = "no_tests_ran"
    else:
        failed = sum(o in ("failed", "rerun_passed") for o in tests.values())
        rec["status"] = "passed" if failed == 0 else ("failed" if failed == len(tests) else "partly_failed")
    rec.update(methods=len(tests), passed=sum(o == "passed" for o in tests.values()),
               failed=sum(o in ("failed", "rerun_passed") for o in tests.values()),
               skipped=sum(o == "skipped" for o in tests.values()))
    print(f"  => {rec['status']}: {rec['passed']}/{rec['methods']} methods passed", flush=True)
    methods = [{"id": rid, "zip": g["zip"], "module": m["module"], "focal": g["selected_method"],
                "test": t, "outcome": o} for t, o in sorted(tests.items())]
    return rec, methods


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--part", action="append", default=[], metavar="OUT_DIR:MANIFEST",
                   help="Generate output folder and its manifest, e.g. out_part1:generate_part1.csv "
                        "(repeatable; default: both parts)")
    a.add_argument("--out", type=Path, default=Path("genreplay_out"))
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--timeout", type=int, default=2 * 3600, help="Seconds per Maven run (default 2h)")
    a.add_argument("--archive-m2", choices=["fallback", "never"], default="fallback")
    a.add_argument("--only", action="append", default=[], help="Only rows whose id or zip contains this text")
    a.add_argument("--limit", type=int, default=0)
    a.add_argument("--redo", action="store_true", help="Rerun rows that already have a result")
    a.add_argument("--mockito-fix", action="store_true",
                   help="Declare Mockito 4.11 + its byte-buddy/objenesis first in the pom for the run")
    args = a.parse_args()
    here = Path(__file__).resolve().parent
    specs = args.part or ["out_part1:generate_part1.csv", "out_part2:generate_part2.csv"]
    parts = [(Path(p).resolve(), Path(mf).resolve()) for p, mf in (s.split(":", 1) for s in specs)]
    args.out = (here / args.out).resolve() if not args.out.is_absolute() else args.out
    args.repo = args.repo.resolve()
    args.out.mkdir(parents=True, exist_ok=True)

    env = os.environ.copy()
    mvn_bin = C.maven_without_extensions(args.out)
    if mvn_bin:
        env["PATH"] = str(mvn_bin) + os.pathsep + env["PATH"]
        env.pop("M2_HOME", None)
        env.pop("MAVEN_HOME", None)

    todo = list(generated_rows(parts))
    todo = [t for t in todo if not args.only or any(
        s in f"{t[0].name}_row_{int(t[1]['row_number']):03d} {t[1]['zip']}" for s in args.only)]
    if args.limit:
        todo = todo[:args.limit]
    prev = {}
    if (args.out / "results.csv").is_file():
        prev = {r["id"]: r for r in csv.DictReader(open(args.out / "results.csv", encoding="utf-8"))}
    methods = {}
    if (args.out / "methods.csv").is_file():
        for r in csv.DictReader(open(args.out / "methods.csv", encoding="utf-8")):
            methods.setdefault(r["id"], []).append(r)
    print(f"{len(todo)} generated rows to replay")
    for i, (part_dir, g, m) in enumerate(todo, 1):
        rid = f"{part_dir.name}_row_{int(g['row_number']):03d}"
        if rid in prev and not args.redo:
            print(f"[{i}/{len(todo)}] {rid}: done ({prev[rid]['status']}), skipping (--redo to rerun)")
            continue
        print(f"[{i}/{len(todo)}]", end=" ")
        rec, ms = replay(part_dir, g, m, args, env)
        prev[rid], methods[rid] = rec, ms
        B.csv_write(args.out / "results.csv", sorted(prev.values(), key=lambda x: x["id"]), RESULT_FIELDS)
        B.csv_write(args.out / "methods.csv", [x for k in sorted(methods) for x in methods[k]], METHOD_FIELDS)

    counts = {}
    for r in prev.values():
        counts[r["status"]] = counts.get(r["status"], 0) + 1
    print(f"\n{len(prev)} rows: " + ", ".join(f"{k} {v}" for k, v in sorted(counts.items()))
          + f" -> {args.out / 'results.csv'}")


if __name__ == "__main__":
    main()

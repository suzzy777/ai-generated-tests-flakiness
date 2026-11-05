#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import re
import shutil
import sys
import xml.etree.ElementTree as ET

Q = Path(__file__).resolve().parent
H = Q.parent
sys.path.insert(0, str(H))
import flaky_check_basic as F

B, C, R = F.B, F.C, F.R
JACOCO = "org.jacoco:jacoco-maven-plugin:0.8.12"
JACOCO_JARS = Path.home() / ".m2" / "repository" / "org" / "jacoco"
JACOCO_AGENT = JACOCO_JARS / "org.jacoco.agent" / "0.8.12" / "org.jacoco.agent-0.8.12-runtime.jar"
JACOCO_CLI = JACOCO_JARS / "org.jacoco.cli" / "0.8.12" / "org.jacoco.cli-0.8.12-nodeps.jar"
PIT_VERSION = "1.15.2"
PIT_JUNIT5 = "1.2.1"
FLAGS = [f for f in F.TEST_FLAGS if f != "-Djacoco.skip"]
RETRY = {"", "no_jacoco_report", "no_pit_report", "error", "build_failed", "build_timeout"}
ITEM_FIELDS = ["id", "set", "zip", "focal_method", "test_class", "tests_csv", "rid"]
RESULT_FIELDS = ITEM_FIELDS + [
    "status", "tests_run", "tests_failed", "line_covered", "line_total", "branch_covered", "branch_total",
    "class_line_covered", "class_line_total", "mut_total", "mut_killed", "mut_survived", "mut_no_coverage",
    "mut_timed_out", "mut_other", "note"]


def read(p):
    return list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []


def save(p, rows, fields):
    p.parent.mkdir(parents=True, exist_ok=True)
    with open(p, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fields)
        w.writeheader()
        w.writerows(rows)


def make_items(args):
    items, dev = [], {}
    for tests_csv, set_name in zip(args.tests, args.set_name):
        targs = argparse.Namespace(tests=tests_csv.resolve(), only=[], archive_dir=args.archive_dir)
        for rid, _, g, m, _ in F.load_tests(targs):
            for f in g["test_files"]:
                pkg = R.PACKAGE.search(f.read_text(encoding="utf-8", errors="replace"))
                cls = (pkg.group(1) + "." if pkg else "") + f.stem
                items.append(dict(id=f"{set_name}_{rid}_{f.stem}", set=set_name, zip=g["zip"],
                                  focal_method=g["selected_method"], test_class=cls,
                                  tests_csv=str(tests_csv.resolve()), rid=rid))
            dcls = g["flaky_test"].split("#")[0]
            dev.setdefault((g["zip"], g["selected_method"], dcls), (str(tests_csv.resolve()), rid))
    for (z, focal, dcls), (tc, rid) in sorted(dev.items()):
        items.append(dict(id=f"developer_{z}_{dcls.rsplit('.', 1)[-1]}", set="developer", zip=z,
                          focal_method=focal, test_class=dcls, tests_csv=tc, rid=rid))
    save(args.items, items, ITEM_FIELDS)
    print(f"{len(items)} items ({len(dev)} developer) -> {args.items}")


def jupiter_version(p, args):
    cp = p.row_dir / "test_classpath.txt"
    p.mvn(["org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath", "-Dmdep.includeScope=test",
           "-Dmdep.outputFile=" + str(cp)], p.row_dir / "classpath.log", offline=args.offline)
    m = re.search(r"junit-jupiter-api-(5\.[\w.-]+?)\.jar", cp.read_text()) if cp.is_file() else None
    return m.group(1) if m else None


def with_junit5_engine(text, version):
    platform = "1." + version.split(".", 1)[1]
    deps = "".join(f"<dependency><groupId>{g}</groupId><artifactId>{a}</artifactId><version>{v}</version>"
                   f"<scope>test</scope></dependency>"
                   for g, a, v in (("org.junit.jupiter", "junit-jupiter-engine", version),
                                   ("org.junit.platform", "junit-platform-launcher", platform)))
    at = B.project_dependencies_close(text)
    if at is None:
        return text.replace("</project>", "<dependencies>" + deps + "</dependencies></project>")
    return text[:at] + deps + text[at:]


def with_pit_junit5(text):
    plugin = (f"<plugin><groupId>org.pitest</groupId><artifactId>pitest-maven</artifactId>"
              f"<version>{PIT_VERSION}</version><dependencies><dependency><groupId>org.pitest</groupId>"
              f"<artifactId>pitest-junit5-plugin</artifactId><version>{PIT_JUNIT5}</version></dependency>"
              f"</dependencies></plugin>")
    masked = re.sub(r"<!--.*?-->|<!\[CDATA\[.*?\]\]>", lambda m: " " * len(m.group(0)), text, flags=re.S)
    stack, build_open, project_close = [], None, None
    for m in re.finditer(r"<(/?)([\w.:-]+)[^>]*?(/?)>", masked):
        closing, name, selfclosing = m.group(1), m.group(2).split(":")[-1], m.group(3)
        if masked[m.start() + 1] in "?!":
            continue
        if closing:
            if stack and stack[-1] == name:
                stack.pop()
            if name == "project" and not stack:
                project_close = m.start()
        elif not selfclosing:
            stack.append(name)
            if stack == ["project", "build"]:
                build_open = m.end()
            if stack == ["project", "build", "plugins"]:
                return text[:m.end()] + plugin + text[m.end():]
    if build_open is not None:
        return text[:build_open] + "<plugins>" + plugin + "</plugins>" + text[build_open:]
    return text[:project_close] + "<build><plugins>" + plugin + "</plugins></build>" + text[project_close:]


class Run(F.Prepared):

    def __enter__(self):
        used = self.row_dir / "pom.xml.used"
        if used.is_file() and (self.row_dir / "pom.xml.before").is_file() and self.pom.read_bytes() == used.read_bytes():
            shutil.copy2(self.row_dir / "pom.xml.before", self.pom)
        used.unlink(missing_ok=True)
        shutil.copy2(self.pom, self.row_dir / "pom.xml.before")
        self.pom_sha = R.sha(self.pom)
        text = self.pom.read_text(encoding="utf-8")
        if self.fix:
            text = R.mockito_fixed(text, self.g["chatunitest_mode"])
        self.stack = [R.TempPom(self.pom, text, self.row_dir), R.CopiedTests(self.module_dir, self.files, self.row_dir)]
        for s in self.stack:
            s.__enter__()
        return self


def outcomes(module_dir, cls):
    tests, _ = C.read_reports(module_dir)
    return {t: o for t, o in tests.items() if t.split("#")[0] == cls}


def jacoco_counts(xml_path, focal_cls, focal_name):
    root = ET.parse(xml_path).getroot()
    want = focal_cls.replace(".", "/")
    m = [0, 0, 0, 0]
    cl = [0, 0]
    for c in root.iter("class"):
        if c.get("name") != want:
            continue
        for meth in c.findall("method"):
            if meth.get("name") != focal_name:
                continue
            for ctr in meth.findall("counter"):
                cov, mis = int(ctr.get("covered")), int(ctr.get("missed"))
                if ctr.get("type") == "LINE":
                    m[0] += cov
                    m[1] += cov + mis
                elif ctr.get("type") == "BRANCH":
                    m[2] += cov
                    m[3] += cov + mis
        for ctr in c.findall("counter"):
            if ctr.get("type") == "LINE":
                cl = [int(ctr.get("covered")), int(ctr.get("covered")) + int(ctr.get("missed"))]
    return m, cl


def pit_counts(xml_path, focal_cls, focal_name):
    counts = {"KILLED": 0, "SURVIVED": 0, "NO_COVERAGE": 0, "TIMED_OUT": 0, "other": 0}
    for mu in ET.parse(xml_path).getroot().iter("mutation"):
        if mu.findtext("mutatedClass") != focal_cls or mu.findtext("mutatedMethod") != focal_name:
            continue
        s = mu.get("status", "")
        counts[s if s in counts else "other"] += 1
    return counts


def run_item(it, args):
    rec = dict(it, status="", note="")
    targs = argparse.Namespace(tests=Path(it["tests_csv"]), only=[it["rid"]], archive_dir=args.archive_dir)
    found = [x for x in F.load_tests(targs) if x[0] == it["rid"]]
    if not found:
        return dict(rec, status="not_found")
    rid, root, g, m, fix = found[0]
    if it["set"] == "developer":
        g = dict(g, test_files=[], methods=[])
        fix = False
    else:
        g = dict(g, test_files=[f for f in g["test_files"] if f.stem == it["test_class"].rsplit(".", 1)[-1]])
    a = argparse.Namespace(**vars(args))
    a.out = args.out
    p = Run(it["id"], root, g, m, fix, a)
    p.row_dir = args.out / "rows" / it["id"]
    p.row_dir.mkdir(parents=True, exist_ok=True)
    focal_cls, focal_name = it["focal_method"].split("#")
    cls = it["test_class"]
    with p:
        b = p.build()
        if b in ("build_failed", "build_timeout"):
            return dict(rec, status=b)
        tgt = p.module_dir / "target"
        if it["set"] == "developer":
            for i in range(1, 4):
                shutil.rmtree(tgt / "surefire-reports", ignore_errors=True)
                p.mvn(["test", "-Dtest=" + cls, *FLAGS], p.row_dir / f"pass_check_{i}.log", offline=args.offline)
                o = outcomes(p.module_dir, cls)
                if not o or any(v != "passed" for v in o.values()):
                    return dict(rec, status="developer_not_passing",
                                note=f"run {i}: {sum(v != 'passed' for v in o.values())} of {len(o)} not passed")
        shutil.rmtree(tgt / "site" / "jacoco", ignore_errors=True)
        for e in tgt.glob("*.exec"):
            e.unlink()
        shutil.rmtree(tgt / "surefire-reports", ignore_errors=True)
        p.mvn(["test", "-Dtest=" + cls, *FLAGS], p.row_dir / "jacoco_own.log", offline=args.offline)
        execs = sorted(tgt.glob("*.exec"))
        if not execs:
            shutil.rmtree(tgt / "surefire-reports", ignore_errors=True)
            p.mvn([f"{JACOCO}:prepare-agent", "test", "-Dtest=" + cls, *FLAGS], p.row_dir / "jacoco.log",
                  offline=args.offline)
            execs = sorted(tgt.glob("*.exec"))
            rec["note"] = "our JaCoCo agent"
            if not execs:
                pkg = focal_cls.rsplit(".", 1)[0]
                env0 = p.env
                p.env = dict(env0, JAVA_TOOL_OPTIONS=f"-javaagent:{JACOCO_AGENT}=destfile={tgt / 'jacoco-tool.exec'},"
                                                     f"append=true,includes={pkg}.*")
                shutil.rmtree(tgt / "surefire-reports", ignore_errors=True)
                try:
                    p.mvn(["test", "-Dtest=" + cls, *FLAGS], p.row_dir / "jacoco_tool.log", offline=args.offline)
                finally:
                    p.env = env0
                execs = sorted(tgt.glob("*.exec"))
                rec["note"] = "our JaCoCo agent (JAVA_TOOL_OPTIONS)"
        else:
            rec["note"] = "project's own JaCoCo"
        if execs:
            p.mvn([f"{JACOCO}:report", "-Djacoco.dataFile=" + str(execs[0]),
                   *[f for f in B.BUILD_SKIPS if f != "-Djacoco.skip"]], p.row_dir / "jacoco_report.log",
                  offline=args.offline)
        if execs and not (tgt / "site" / "jacoco" / "jacoco.xml").is_file():
            java = str(Path(p.env["JAVA_HOME"]) / "bin" / "java") if p.env.get("JAVA_HOME") else "java"
            (tgt / "site" / "jacoco").mkdir(parents=True, exist_ok=True)
            cmd = [java, "-jar", str(JACOCO_CLI), "report", str(execs[0]), "--classfiles", str(tgt / "classes"),
                   "--sourcefiles", str(p.module_dir / "src" / "main" / "java"),
                   "--xml", str(tgt / "site" / "jacoco" / "jacoco.xml")]
            with open(p.row_dir / "jacoco_cli.log", "w", encoding="utf-8") as fout:
                fout.write(" ".join(cmd) + "\n")
                fout.flush()
                F.subprocess.run(cmd, stdout=fout, stderr=F.subprocess.STDOUT, timeout=600)
            rec["note"] += "; report by JaCoCo CLI"
        o = outcomes(p.module_dir, cls)
        rec.update(tests_run=len(o), tests_failed=sum(v != "passed" for v in o.values()))
        jx = tgt / "site" / "jacoco" / "jacoco.xml"
        if not o:
            return dict(rec, status="no_tests_ran")
        if not jx.is_file():
            return dict(rec, status="no_jacoco_report")
        shutil.copy2(jx, p.row_dir / "jacoco.xml")
        (lc, lt, bc, bt), (clc, clt) = jacoco_counts(jx, focal_cls, focal_name)
        rec.update(line_covered=lc, line_total=lt, branch_covered=bc, branch_total=bt,
                   class_line_covered=clc, class_line_total=clt)
        if rec["tests_failed"]:
            return dict(rec, status="tests_fail", note="coverage measured, PIT skipped (needs a green run)")
        if args.no_pit:
            return dict(rec, status="coverage_only")
        shutil.rmtree(tgt / "pit-reports", ignore_errors=True)
        if p.g["chatunitest_mode"] == "5":
            v = jupiter_version(p, args)
            if not v:
                return dict(rec, status="no_pit_report", note="JUnit 5 but no junit-jupiter-api on the test classpath")
            text = with_pit_junit5(with_junit5_engine(p.pom.read_text(encoding="utf-8"), v))
            p.pom.write_text(text, encoding="utf-8")
            rec["note"] += f"; PIT with jupiter {v}"
        pit = ["test-compile", f"org.pitest:pitest-maven:{PIT_VERSION}:mutationCoverage",
               "-DtargetClasses=" + focal_cls, "-DtargetTests=" + cls, "-DoutputFormats=XML",
               "-DtimestampedReports=false", "-DfailWhenNoMutations=false", "-Dthreads=2", "-Dmutators=" + args.mutators,
               *[f for f in FLAGS if not f.startswith("-Dmaven.test.failure.ignore")]]
        p.mvn(pit, p.row_dir / "pit.log", offline=args.offline)
        mx = tgt / "pit-reports" / "mutations.xml"
        undefined = re.findall(r"\^\\\$\{([\w.-]+)\}\$", (p.row_dir / "pit.log").read_text(errors="replace"))
        if not mx.is_file() and undefined:
            p.mvn(pit + [f"-D{undefined[0]}=__no_such_file__"], p.row_dir / "pit_retry.log", offline=args.offline)
            if mx.is_file():
                rec["note"] += f"; PIT with -D{undefined[0]}=__no_such_file__"
        if not mx.is_file():
            logs = "".join(f.read_text(errors="replace") for f in [p.row_dir / "pit.log", p.row_dir / "pit_retry.log"]
                           if f.is_file())
            if "No mutations found" in logs:
                rec.update(mut_total=0, mut_killed=0, mut_survived=0, mut_no_coverage=0, mut_timed_out=0, mut_other=0)
                return dict(rec, status="done", note=rec["note"] + "; PIT: no mutants")
            if "did not pass without mutation" in logs:
                return dict(rec, status="pit_not_green", note=rec["note"] + "; a test fails in PIT's run, see pit.log")
            return dict(rec, status="no_pit_report", note="see pit.log")
        shutil.copy2(mx, p.row_dir / "mutations.xml")
        c = pit_counts(mx, focal_cls, focal_name)
        rec.update(mut_total=sum(c.values()), mut_killed=c["KILLED"], mut_survived=c["SURVIVED"],
                   mut_no_coverage=c["NO_COVERAGE"], mut_timed_out=c["TIMED_OUT"], mut_other=c["other"])
        return dict(rec, status="done")


def run(args):
    items = read(args.items)
    if args.only:
        items = [i for i in items if any(x in f"{i['id']} {i['zip']}" for x in args.only)]
    res_path = args.out / "results.csv"
    done = {r["id"]: r for r in read(res_path)}
    env = os.environ.copy()
    mvn_bin = C.maven_without_extensions(args.out)
    if mvn_bin:
        env["PATH"] = str(mvn_bin) + os.pathsep + env["PATH"]
        env.pop("M2_HOME", None)
        env.pop("MAVEN_HOME", None)
    args.env = env
    for n, it in enumerate(items, 1):
        if it["id"] in done and done[it["id"]]["status"] not in RETRY and not args.redo:
            continue
        print(f"[{n}/{len(items)}] {it['id']}", flush=True)
        try:
            rec = run_item(it, args)
        except Exception as ex:
            rec = dict(it, status="error", note=f"{type(ex).__name__}: {str(ex)[:250]}")
        print(f"  => {rec['status']} lines {rec.get('line_covered', '')}/{rec.get('line_total', '')} "
              f"branches {rec.get('branch_covered', '')}/{rec.get('branch_total', '')} "
              f"killed {rec.get('mut_killed', '')}/{rec.get('mut_total', '')} {rec.get('note', '')}", flush=True)
        done[it["id"]] = rec
        save(res_path, sorted(done.values(), key=lambda r: r["id"]), RESULT_FIELDS)


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("command", choices=["items", "run"])
    a.add_argument("--tests", type=Path, action="append", default=[])
    a.add_argument("--set-name", action="append", default=[])
    a.add_argument("--only", action="append", default=[])
    a.add_argument("--out", type=Path, default=Q / "out")
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--timeout", type=int, default=3 * 3600)
    a.add_argument("--offline", action="store_true")
    a.add_argument("--no-pit", action="store_true")
    a.add_argument("--mutators", default="STRONGER", help="PIT mutator group (default STRONGER)")
    a.add_argument("--redo", action="store_true")
    a.add_argument("--items", type=Path, default=Q / "items.csv", help="Item list (default items.csv)")
    a.add_argument("--archive-dir", type=Path, action="append", default=[],
                   help="Project copies (default extracted/ here); changed in place during a run")
    args = a.parse_args()
    args.out = args.out.resolve()
    args.repo = args.repo.resolve()
    args.items = args.items.resolve()
    args.archive_dir = [d.resolve() for d in args.archive_dir] or [Q / "extracted"]
    args.command_name = args.command
    if args.command == "items":
        if len(args.tests) != len(args.set_name):
            sys.exit("one --set-name per --tests")
        return make_items(args)
    args.command = "coverage"
    run(args)


if __name__ == "__main__":
    main()

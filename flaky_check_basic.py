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

sys.path.insert(0, str(Path(__file__).resolve().parent))
import batch_reproflake_chatunitest as B
import collect_nonflaky as C
import replay_generated as R

HERE = Path(__file__).resolve().parent
NONDEX = "edu.illinois:nondex-maven-plugin:2.1.7"
IDFLAKIES = "edu.illinois.cs:idflakies-maven-plugin:2.0.0"
NIO_FIELDS = ["test", "nio_method", "iterations", "pass", "failure", "error", "not_twice", "nio", "example"]
ROW_FIELDS = ["id", "zip", "module", "java", "focal", "mockito_fix", "test_classes", "build_status",
              "status", "methods", "flaky_generated", "flaky_other", "note", "log"]
RUN_FIELDS = ["id", "run", "test", "outcome"]
SUMMARY_FIELDS = ["project", "sha", "module", "generated_test", "focal_method", "rerun_runs", "rerun_passed", "rerun_failed",
                  "rerun_errors", "rerun_flaky", "nondex_flaky", "idflakies_type", "idflakies_polluter_of", "nio", "nio_method",
                  "any_flaky", "focal_from_test", "focal_from_test_type"]
TEST_FLAGS = [s for s in B.BUILD_SKIPS if s != "-DskipTests"] + [
    "-Dmaven.test.failure.ignore=true", "-DfailIfNoTests=false", "-Dsurefire.failIfNoSpecifiedTests=false",
    "-Dsurefire.rerunFailingTestsCount=0"]

NIO_JUNIT5 = r"""
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class FlakyCheckNio5 {
    public static void main(String[] a) throws Exception {
        for (int i = 1; i <= 2; i++) {
            LauncherDiscoveryRequest req = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(a[0], a[1])).build();
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            launcher.execute(req, listener);
            TestExecutionSummary s = listener.getSummary();
            String why = s.getFailures().isEmpty() ? "" : String.valueOf(s.getFailures().get(0).getException());
            String outcome = s.getTestsStartedCount() == 0 ? "NOTRUN" : s.getTotalFailureCount() > 0 ? "FAIL" : "PASS";
            System.out.println("FLAKYCHECK_NIO " + i + " " + outcome + " " + why.replace('\n', ' '));
        }
        System.exit(0);
    }
}
"""


TESTRUNNER_PLUGIN = "edu.illinois.cs:testrunner-maven-plugin:1.4-SNAPSHOT"
NIO_ARTIFACTS = ["testrunner/1.4-SNAPSHOT", "testrunner-maven-plugin/1.4-SNAPSHOT", "testrunner-core-plugin/1.4-SNAPSHOT",
                 "testrunner-running/1.4-SNAPSHOT", "testrunner-testing/1.4-SNAPSHOT", "idflakies/1.2.0-SNAPSHOT"]
NIO_MVN_OPTS = [
    "-Ddependency-check.skip=true", "-Dgpg.skip=true", "-DfailIfNoTests=false", "-Dskip.installnodenpm",
    "-Dskip.npm", "-Dskip.yarn", "-Dlicense.skip", "-Dcheckstyle.skip", "-Drat.skip", "-Denforcer.skip",
    "-Danimal.sniffer.skip", "-Dmaven.javadoc.skip", "-Dwarbucks.skip", "-Dmodernizer.skip", "-Dimpsort.skip",
    "-Dmdep.analyze.skip", "-Dpgpverify.skip", "-Dxml.skip", "-Dcobertura.skip=true", "-Dfindbugs.skip=true",
    "-P!checkstyle", "-Dcheckstyle.skipExec=true", "-Dmaven.checkstyle.skip=true", "-DskipCheckstyle=true",
    "-Dcheckstyle.config.location=google_checks.xml", "-Dcheckstyle.failsOnError=false", "-Dspotless.skip=true",
    "-Dskip.format=true"]
NIO_DETECTOR_OPTS = [
    "-Ddt.detector.original_order.all_must_pass=false", "-Ddt.randomize.rounds=0",
    "-Ddt.detector.original_order.retry_count=1", "-Dtestplugin.runner.idempotent.num.runs=2",
    "-Dtestplugin.runner.consec.idempotent=true", "-Ddetector.detector_type=original"]


def load_tests(args):
    runs = {}
    for r in csv.DictReader(open(args.tests, encoding="utf-8")):
        src = (HERE / r["test_file"]).resolve()
        run_dir = next((q for q in src.parents if q.name.startswith("row_")), src.parent)
        rid = f"{run_dir.parent.parent.name}_{run_dir.name}"
        if args.only and not any(x in f"{rid} {r['zip']} {r['generated_test']}" for x in args.only):
            continue
        root = next((d / r["zip"] for d in args.archive_dir if (d / r["zip"]).is_dir()), None)
        if root is None:
            sys.exit(f"{r['zip']} not found in " + ", ".join(map(str, args.archive_dir)))
        if rid not in runs:
            runs[rid] = (rid, root.resolve(),
                         {"zip": r["zip"], "flaky_test": r["focal_from_test"], "selected_method": r["focal_method"],
                          "chatunitest_mode": r["junit"], "test_files": [], "methods": [], "csv": r},
                         {"project": r["project"], "flaky_commit": r["sha"], "module": r["module"],
                          "java": r["java"], "zip": r["zip"], "flaky_test": r["focal_from_test"]},
                         r["mockito_fix"].strip().lower() == "true")
        g = runs[rid][2]
        if src not in g["test_files"]:
            g["test_files"].append(src)
        g["methods"].append(r["generated_test"])
    return [runs[k] for k in sorted(runs)]


def test_classes(g):
    out = []
    for f in g["test_files"]:
        m = R.PACKAGE.search(f.read_text(encoding="utf-8", errors="replace"))
        out.append((f, m.group(1) if m else "", f.stem))
    return out


class Prepared:

    def __init__(self, rid, root, g, m, mockito_fix, args):
        self.rid, self.g, self.m, self.fix, self.args = rid, g, m, mockito_fix, args
        self.row_dir = args.out / "rows" / rid
        self.row_dir.mkdir(parents=True, exist_ok=True)
        self.files = test_classes(g)
        self.classes = [name for _, _, name in self.files]
        self.fqcn = [f"{pkg}.{name}" if pkg else name for _, pkg, name in self.files]
        _, self.module_dir, _ = B.locate_test(root, m["module"], g["flaky_test"])
        self.r = dict(m, module=C.norm_module(m["module"]), archive_root=str(root), module_dir=str(self.module_dir))
        self.cwd, self.pl = C.project_root(self.module_dir, self.r["module"])
        self.pom = self.module_dir / "pom.xml"
        self.env = dict(args.env)
        jdk = B.find_jdk(str(m.get("java", "")).strip())
        if jdk:
            self.env["JAVA_HOME"] = jdk
            self.env["PATH"] = str(Path(jdk) / "bin") + os.pathsep + self.env["PATH"]

    def __enter__(self):
        used = self.row_dir / "pom.xml.used"
        if used.is_file() and (self.row_dir / "pom.xml.before").is_file() and self.pom.read_bytes() == used.read_bytes():
            print(f"  restoring {self.pom} left modified by a killed run")
            shutil.copy2(self.row_dir / "pom.xml.before", self.pom)
        used.unlink(missing_ok=True)
        shutil.copy2(self.pom, self.row_dir / "pom.xml.before")
        self.pom_sha = R.sha(self.pom)
        text = R.mockito_fixed(self.pom.read_text(encoding="utf-8"), self.g["chatunitest_mode"]) if self.fix else None
        if self.args.command == "nio" and self.g["chatunitest_mode"] != "5":
            text = with_testrunner(text if text is not None else self.pom.read_text(encoding="utf-8"))
        if self.args.command == "idflakies":
            base = text if text is not None else self.pom.read_text(encoding="utf-8")
            moved = with_surefire_engines(base)
            text = moved if moved != base else text
        self.stack = [R.TempPom(self.pom, text, self.row_dir), R.CopiedTests(self.module_dir, self.files, self.row_dir)]
        for s in self.stack:
            s.__enter__()
        return self

    def __exit__(self, *exc):
        for s in reversed(self.stack):
            s.__exit__(*exc)
        if R.sha(self.pom) != self.pom_sha:
            print(f"  WARNING: {self.pom} changed during the run; original copy in {self.row_dir / 'pom.xml.before'}")

    def build(self):
        if self.r["module"] == ".":
            return "not_needed"
        return B.build_module(self.r, self.env, self.args, self.row_dir, "build.log")

    def mvn(self, goals, log, offline=False, in_module=False):
        cwd, pl = (self.module_dir, []) if in_module else (self.cwd, self.pl)
        cmd = ["mvn", "-B", *(["-o"] if offline else []), *pl, *goals,
               "-Dmaven.repo.local=" + str(self.args.repo)]
        with log.open("w", encoding="utf-8") as fout:
            fout.write("(cd " + str(cwd) + " && " + " ".join(cmd) + ")\n")
            fout.flush()
            try:
                p = subprocess.run(cmd, cwd=cwd, env=self.env, stdout=fout, stderr=subprocess.STDOUT,
                                   timeout=self.args.timeout)
            except subprocess.TimeoutExpired:
                fout.write(f"Timed out after {self.args.timeout} seconds.\n")
                return "timeout"
        return p.returncode

    def reports(self):
        tests, _ = C.read_reports(self.module_dir)
        return {t: o for t, o in tests.items() if t.split("#")[0] in self.fqcn}

    def take_tool_dir(self, name, existed):
        src = self.module_dir / name
        if existed or not src.exists():
            return None
        dest = self.row_dir / name.lstrip(".")
        shutil.rmtree(dest, ignore_errors=True)
        shutil.move(str(src), str(dest))
        return dest


def record(p, mockito_fix):
    return {"id": p.rid, "zip": p.g["zip"], "module": p.m["module"], "java": p.m["java"],
            "focal": p.g["selected_method"], "mockito_fix": str(mockito_fix).lower(),
            "test_classes": ";".join(p.classes), "build_status": "", "status": "", "note": ""}


def generated_name(name, fqcn):
    name = re.split(r"[\[(]", name.strip(), 1)[0]
    cls, sep, method = name.partition("#")
    if not sep:
        cls, _, method = name.rpartition(".")
    return f"{cls}#{method}" if cls in fqcn else None


def do_rerun(p, rec, args):
    runs_csv = args.out / "runs.csv"
    done = {}
    if runs_csv.is_file():
        for r in csv.DictReader(open(runs_csv, encoding="utf-8")):
            if r["id"] == p.rid:
                done[r["test"]] = max(done.get(r["test"], 0), int(r["run"]))
    logs = p.row_dir / "rerun_logs"
    logs.mkdir(exist_ok=True)
    new = not runs_csv.is_file()
    first = True
    with runs_csv.open("a", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, RUN_FIELDS)
        if new:
            w.writeheader()
        for t in row_methods(args, p.rid):
            cls, _, method = t.partition("#")
            name = cls.rsplit(".", 1)[-1] + "#" + method
            for run in range(done.get(t, 0) + 1, args.runs + 1):
                shutil.rmtree(p.module_dir / "target" / "surefire-reports", ignore_errors=True)
                log = logs / f"{method}_{run:03d}.log"
                code = p.mvn(["test", "-Dtest=" + name, *TEST_FLAGS], log, offline=not first and args.offline)
                first = False
                outcome = p.reports().get(t) or ("timeout" if code == "timeout" else "error")
                w.writerow({"id": p.rid, "run": run, "test": t, "outcome": outcome})
                f.flush()
                if outcome == "passed":
                    log.unlink()
                print(f"  {name} run {run}/{args.runs}: {outcome}", flush=True)


def rerun_counts(path):
    counts = {}
    if path.is_file():
        for r in csv.DictReader(open(path, encoding="utf-8")):
            c = counts.setdefault((r["id"], r["test"]), [0, 0, 0, 0])
            c[0] += r["outcome"] == "passed"
            c[1] += r["outcome"] in ("failed", "rerun_passed")
            c[2] += 1
            c[3] += r["outcome"] in ("error", "timeout")
    return counts


def nondex_flagged(log_text):
    out, on = set(), False
    for line in log_text.splitlines():
        if "Across all seeds:" in line:
            on, out = True, set()
            continue
        if on:
            m = re.match(r"\[(?:WARNING|INFO|ERROR)\]\s+(\S+[#.]\S+)\s*$", line)
            if m and not m.group(1).startswith(("-", "*")):
                out.add(m.group(1))
            else:
                on = False
    return out


def do_nondex(p, rec, args):
    existed = (p.module_dir / ".nondex").exists()
    log = p.row_dir / "nondex.log"
    code = p.mvn([NONDEX + ":nondex", "-Dtest=" + ",".join(p.classes), f"-DnondexRuns={args.nondex_runs}",
                  *[f for f in TEST_FLAGS if f != "-Dmaven.test.failure.ignore=true"]], log)
    dest = p.take_tool_dir(".nondex", existed)
    text = log.read_text(encoding="utf-8", errors="replace")
    flagged = nondex_flagged(text)
    gen = sorted({g for g in (generated_name(t, p.fqcn) for t in flagged) if g})
    (p.row_dir / "nondex_flaky.txt").write_text("".join(t + "\n" for t in gen))
    rec.update(log=str(log), flaky_generated=len(gen), flaky_other=len(flagged) - len(gen),
               note=("" if dest else ".nondex existed in the module beforehand, left there; " if existed else ""))
    if code == "timeout":
        rec["status"] = "timeout"
    elif "NonDex SUMMARY" not in text and "Across all seeds" not in text:
        rec["status"] = "compile_failed" if "COMPILATION ERROR" in text else "nondex_did_not_run"
    else:
        rec["status"] = "flaky_found" if gen else "no_flaky_found"


def idflakies_flagged(tool_dir):
    res = tool_dir / "detection-results" if tool_dir else None
    out = {}
    if res and (res / "flaky-lists.json").is_file():
        try:
            data = json.loads((res / "flaky-lists.json").read_text())
            for d in data.get("dts", []):
                out[d.get("name", "")] = d.get("type", "")
        except ValueError:
            pass
    if res and not out and (res / "list.txt").is_file():
        out = {line.strip(): "" for line in (res / "list.txt").read_text().splitlines() if line.strip()}
    return out


def idflakies_polluters(tool_dir, fqcn):
    res = tool_dir / "detection-results" / "flaky-lists.json" if tool_dir else None
    out = {}
    if res and res.is_file():
        try:
            dts = json.loads(res.read_text()).get("dts", [])
        except ValueError:
            dts = []
        for d in dts:
            runs = [d.get(k) or {} for k in ("intended", "revealed")]
            if [r.get("result") == "PASS" for r in runs] == [False, True]:
                runs.reverse()
            if runs[0].get("result", "PASS") != "PASS" or runs[1].get("result") == "PASS":
                continue
            failing = [t for t in runs[1].get("order", []) if generated_name(t, fqcn)]
            passing = set(runs[0].get("order", []))
            out[d.get("name", "")] = sorted(generated_name(t, fqcn) for t in failing if t not in passing)
    return out


def flagged_orders(tool_dir):
    res = tool_dir / "detection-results" / "flaky-lists.json" if tool_dir else None
    if not (res and res.is_file()):
        return []
    try:
        dts = json.loads(res.read_text()).get("dts", [])
    except ValueError:
        return []
    def hashed(t):
        t = re.split(r"[\[(]", t, 1)[0]
        return t if "#" in t else "#".join(t.rsplit(".", 1))
    out = []
    for d in dts:
        name = hashed(d.get("name", ""))
        runs = [d.get(k) or {} for k in ("intended", "revealed")]
        if [r.get("result") == "PASS" for r in runs] == [False, True]:
            runs.reverse()
        if runs[0].get("result", "PASS") != "PASS" or runs[1].get("result") == "PASS":
            continue
        out.append((name, *[[hashed(t) for t in r.get("order", [])] + [name] for r in runs]))
    return out


def write_orders(args, rows):
    dest = args.out / "orders"
    shutil.rmtree(dest, ignore_errors=True)
    dest.mkdir(parents=True)
    seen = {}
    for rid, root, g, m, fix in rows:
        for test, passing, failing in flagged_orders(args.out / "rows" / rid / "dtfixingtools"):
            seen[test] = seen.get(test, 0) + 1
            name = test + (f"-{seen[test]}" if seen[test] > 1 else "")
            (dest / f"{name}.passing.txt").write_text("\n".join(passing) + "\n")
            (dest / f"{name}.failing.txt").write_text("\n".join(failing) + "\n")


def do_idflakies(p, rec, args):
    existed = (p.module_dir / ".dtfixingtools").exists()
    log = p.row_dir / "idflakies.log"
    code = p.mvn(["test-compile", IDFLAKIES + ":detect", "-Ddetector.detector_type=random-class-method",
                  f"-Ddt.randomize.rounds={args.rounds}", "-Ddt.detector.original_order.all_must_pass=false",
                  *[s for s in B.BUILD_SKIPS if s != "-DskipTests"]], log, in_module=True)
    for out in {q.resolve() for q in [*p.cwd.glob("failing-test-output-*"), *p.module_dir.glob("failing-test-output-*")]}:
        shutil.move(str(out), str(p.row_dir / out.name))
    dest = p.take_tool_dir(".dtfixingtools", existed)
    if existed:
        rec["note"] = ".dtfixingtools existed in the module beforehand, left there and read from there; "
        dest = p.module_dir / ".dtfixingtools"
    flagged = idflakies_flagged(dest)
    gen = {g: typ for g, typ in ((generated_name(t, p.fqcn), typ) for t, typ in flagged.items()) if g}
    polluters = idflakies_polluters(dest, p.fqcn)
    with (p.row_dir / "idflakies_flaky.csv").open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["test", "type", "generated", "suspected_generated_polluters"])
        for t, typ in sorted(flagged.items()):
            w.writerow([t, typ, str(generated_name(t, p.fqcn) is not None).lower(), ";".join(polluters.get(t, []))])
    polluted = sum(1 for t, ps in polluters.items() if ps and not generated_name(t, p.fqcn))
    rec.update(log=str(log), flaky_generated=len(gen), flaky_other=len(flagged) - len(gen))
    if polluted:
        rec["note"] = rec.get("note", "") + f"{polluted} existing test(s) fail only after a generated test (suspected polluter); "
    text = log.read_text(encoding="utf-8", errors="replace")
    rounds_dir = dest / "detection-results" / "random-class-method" if dest else None
    rounds = len(list(rounds_dir.glob("round*.json"))) if rounds_dir and rounds_dir.is_dir() else 0
    why = ("mixed JUnit 4/5, not supported by iDFlakies" if "both JUnit 4 and JUnit 5" in text
           else "a test JVM did not exit" if "hasn't exited" in text
           else "iDFlakies' test runner failed, see failing-test-output-* in the row folder" if "Non-zero exit code" in text
           else "")
    rec["note"] = rec.get("note", "") + f"rounds {rounds}/{args.rounds}" + (f" ({why})" if why else "") + "; "
    if code == "timeout":
        rec["status"] = "timeout"
    elif dest is None or not (dest / "detection-results").is_dir():
        rec["status"] = "compile_failed" if "COMPILATION ERROR" in text else "idflakies_did_not_run"
    elif gen:
        rec["status"] = "flaky_found"
    elif polluted:
        rec["status"] = "polluter_found"
    elif rounds == 0:
        rec["status"] = "not_run"
    elif rounds < args.rounds:
        rec["status"] = "incomplete"
    else:
        rec["status"] = "no_flaky_found"


def row_methods(args, rid):
    return sorted(set(args.methods.get(rid, [])))


SUREFIRE_PLUGIN = re.compile(r"<plugin>(?:(?!</plugin>).)*?<artifactId>maven-surefire-plugin</artifactId>.*?</plugin>", re.S)
ENGINE_DEP = re.compile(r"<dependency>(?:(?!</dependency>).)*?<artifactId>(junit-(?:jupiter|vintage)-engine|junit-platform-[\w-]+)"
                        r"</artifactId>.*?</dependency>", re.S)


def with_surefire_engines(text):
    outside = SUREFIRE_PLUGIN.sub("", text)
    add = {}
    for plugin in SUREFIRE_PLUGIN.finditer(text):
        for d in ENGINE_DEP.finditer(plugin.group(0)):
            if f"<artifactId>{d.group(1)}</artifactId>" not in outside:
                dep = re.sub(r"\s*<scope>[^<]*</scope>", "", d.group(0))
                add.setdefault(d.group(1), dep[:-len("</dependency>")] + "<scope>test</scope></dependency>")
    at = R.project_dependencies_open(text)
    if not add or at is None:
        return text
    return text[:at] + "".join("\n        " + d for d in add.values()) + text[at:]


def with_testrunner(text):
    if "<artifactId>testrunner-maven-plugin</artifactId>" in text:
        return text
    import xml.etree.ElementTree as ET
    root = ET.fromstring(text.encode("utf-8"))
    ns = root.tag[1:].split("}")[0] if root.tag.startswith("{") else ""
    if ns:
        ET.register_namespace("", ns)
    q = lambda tag: f"{{{ns}}}{tag}" if ns else tag

    def child(parent, tag, value=None):
        e = ET.SubElement(parent, q(tag))
        if value is not None:
            e.text = value
        return e
    build = root.find(q("build"))
    if build is None:
        build = child(root, "build")
    plugins = build.find(q("plugins"))
    if plugins is None:
        plugins = child(build, "plugins")
    plugin = child(plugins, "plugin")
    group, artifact, version = TESTRUNNER_PLUGIN.split(":")
    child(plugin, "groupId", group), child(plugin, "artifactId", artifact), child(plugin, "version", version)
    dep = child(child(plugin, "dependencies"), "dependency")
    child(dep, "groupId", "edu.illinois.cs"), child(dep, "artifactId", "idflakies"), child(dep, "version", "1.2.0-SNAPSHOT")
    child(child(plugin, "configuration"), "className", "edu.illinois.cs.dt.tools.detection.DetectorPlugin")
    return '<?xml version="1.0" encoding="UTF-8"?>\n' + ET.tostring(root, encoding="unicode") + "\n"


def ensure_testrunner(args):
    cs = Path("edu/illinois/cs")
    if all((args.repo / cs / a).is_dir() for a in NIO_ARTIFACTS):
        return
    for d in args.archive_dir:
        for m2 in sorted(d.glob("*/*/Flakym2/.m2/repository")) + sorted(d.glob("*/Flakym2/.m2/repository")):
            if all((m2 / cs / a).is_dir() for a in NIO_ARTIFACTS):
                for a in NIO_ARTIFACTS:
                    for f in (m2 / cs / a).iterdir():
                        dest = args.repo / cs / a / f.name
                        if f.is_file() and not dest.exists():
                            dest.parent.mkdir(parents=True, exist_ok=True)
                            shutil.copy2(f, dest)
                print(f"  testrunner/iDFlakies copied from {m2}", flush=True)
                return
    raise RuntimeError("no archive Flakym2 with ReproFlake's testrunner 1.4-SNAPSHOT / iDFlakies 1.2.0-SNAPSHOT")


def nio_status(dt):
    results_dir = next((d for d in (dt / "test-runs" / "results", dt / "results") if d.is_dir()), None)
    files = sorted(results_dir.iterdir()) if results_dir else []
    if not files:
        return "error", "no testrunner result", None
    try:
        data = json.loads(files[0].read_text(encoding="utf-8"))
    except ValueError:
        return "error", "unreadable testrunner result", files[0]
    res = data.get("results") or {}
    order = [t for t in (data.get("testOrder") or []) if t in res] or list(res)
    vals = [((res[t] or {}).get("result") or "").upper().replace("SUCCESS", "PASS") for t in order]
    status = "pass" if vals and all(v == "PASS" for v in vals) else ("failure" if "PASS" in vals else "error")
    if len(vals) < 2:
        status = "not_twice"
    return status, ";".join(vals), files[0]


def nio_runner(p, junit, args):
    jdk8 = B.find_jdk("8")
    if not jdk8 or not (Path(jdk8) / "bin" / "javac").exists():
        raise RuntimeError("needs a JDK 8 with javac to compile the small NIO runner")
    name = f"FlakyCheckNio{junit}"
    d = p.row_dir / "nio_runner"
    shutil.rmtree(d, ignore_errors=True)
    d.mkdir()
    (d / (name + ".java")).write_text(NIO_JUNIT5)
    out = subprocess.run([str(Path(jdk8) / "bin" / "javac"), "-source", "8", "-target", "8", "-nowarn",
                          "-cp", p.classpath, "-d", str(d), str(d / (name + ".java"))],
                         stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    if out.returncode != 0:
        raise RuntimeError("could not compile the NIO runner: " + out.stdout[-500:])
    return d, name


def junit_launcher(p, cp, args):
    def version(prefix):
        found = [re.search(prefix + r"-([\w.\-]+)\.jar$", e) for e in cp]
        return next((m.group(1) for m in found if m), None)
    jupiter = version("junit-jupiter-api")
    platform = version("junit-platform-engine") or version("junit-platform-commons") or (
        "1." + jupiter.split(".", 1)[1] if jupiter and jupiter.startswith("5.") else None)
    if not platform:
        raise RuntimeError("no JUnit Platform on the test classpath")
    want = [("org.junit.platform", "junit-platform-launcher", platform)]
    if not version("junit-platform-engine"):
        want.append(("org.junit.platform", "junit-platform-engine", platform))
    if jupiter and not version("junit-jupiter-engine"):
        want.append(("org.junit.jupiter", "junit-jupiter-engine", jupiter))
    jars = []
    for group, artifact, ver in want:
        if version(artifact):
            continue
        jar = args.repo / group.replace(".", "/") / artifact / ver / f"{artifact}-{ver}.jar"
        if not jar.is_file():
            p.mvn(["org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get", "-Dtransitive=false",
                   f"-Dartifact={group}:{artifact}:{ver}"], p.row_dir / f"nio_get_{artifact}.log")
        if not jar.is_file():
            raise RuntimeError(f"could not get {group}:{artifact}:{ver}")
        jars.append(str(jar))
    return jars


def own_junit5_iterations(p, args):
    cp_file = p.row_dir / "test_classpath.txt"
    code = p.mvn(["test-compile", "org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath",
                  "-Dmdep.includeScope=test", "-Dmdep.outputFile=" + str(cp_file),
                  *[s for s in B.BUILD_SKIPS if s != "-DskipTests"]], p.row_dir / "nio_classpath.log")
    if code != 0 or not cp_file.is_file():
        raise RuntimeError(f"test classpath failed, see {p.row_dir / 'nio_classpath.log'}")
    cp = [str(p.module_dir / "target" / "test-classes"), str(p.module_dir / "target" / "classes")]
    cp += [e for e in cp_file.read_text().strip().split(os.pathsep) if e]
    cp += junit_launcher(p, cp, args)
    p.classpath = os.pathsep.join(cp)
    runner_dir, runner = nio_runner(p, "5", args)
    java = str(Path(p.env["JAVA_HOME"]) / "bin" / "java") if p.env.get("JAVA_HOME") else "java"
    out = {}
    for t in row_methods(args, p.rid):
        tdir = p.row_dir / "nio_runs" / t
        tdir.mkdir(parents=True, exist_ok=True)
        done_file = tdir / "statuses.txt"
        done = done_file.read_text().splitlines() if done_file.is_file() else []
        cls, _, method = t.partition("#")
        for i in range(len(done), args.nio_runs):
            try:
                text = subprocess.run([java, "-cp", os.pathsep.join([str(runner_dir), p.classpath]), runner, cls, method],
                                      cwd=p.module_dir, env=p.env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                      text=True, timeout=args.timeout).stdout
            except subprocess.TimeoutExpired:
                text = "FLAKYCHECK_NIO 1 TIMEOUT"
            got = dict(re.findall(r"^FLAKYCHECK_NIO (\d) (\w+)", text, re.M))
            vals = [got.get("1", "ERROR"), got.get("2", "ERROR")]
            status = "pass" if vals == ["PASS", "PASS"] else ("failure" if "PASS" in vals else "error")
            if status != "pass":
                (tdir / f"run_{i:03d}.log").write_text(text)
            with done_file.open("a") as f:
                f.write(f"{status},{';'.join(vals)}\n")
            done.append(f"{status},{';'.join(vals)}")
        out[t] = done
    return out


def do_nio(p, rec, args):
    if p.g["chatunitest_mode"] == "5":
        try:
            runs = own_junit5_iterations(p, args)
        except RuntimeError as ex:
            rec.update(status="nio_setup_failed", note=str(ex)[:300])
            return
        nio_summarize(p, rec, runs, "own_junit5_runner")
        return
    try:
        ensure_testrunner(args)
    except RuntimeError as ex:
        rec.update(status="nio_setup_failed", note=str(ex)[:300])
        return
    dt = p.module_dir / ".dtfixingtools"
    if dt.exists():
        rec.update(status="skipped_dtfixingtools_exists", note=f"{dt} already exists; testrunner would use it")
        return
    log = p.row_dir / "nio_compile.log"
    if p.mvn(["test-compile", *NIO_MVN_OPTS, *[s for s in B.BUILD_SKIPS if s != "-DskipTests"]], log) != 0:
        rec.update(status="compile_failed", log=str(log))
        return
    junit5 = p.g["chatunitest_mode"] == "5"
    force4 = "false" if junit5 else "true"
    results = {}
    try:
        for t in row_methods(args, p.rid):
            tdir = p.row_dir / "nio_runs" / t
            tdir.mkdir(parents=True, exist_ok=True)
            done_file = tdir / "statuses.txt"
            done = done_file.read_text().splitlines() if done_file.is_file() else []
            for i in range(len(done), 0 if done and done[-1].startswith("not_twice") else args.nio_runs):
                dt.mkdir(exist_ok=True)
                (dt / "original-order").write_text((t if junit5 else t.replace("#", ".")) + "\n")
                run_log = tdir / f"run_{i:03d}.log"
                p.mvn([TESTRUNNER_PLUGIN + ":testplugin", *NIO_MVN_OPTS, *NIO_DETECTOR_OPTS,
                       f"-Ddt.detector.forceJUnit4={force4}"], run_log)
                status, vals, result_file = nio_status(dt)
                if result_file:
                    shutil.copy2(result_file, tdir / f"run_{i:03d}.json")
                shutil.rmtree(dt, ignore_errors=True)
                for out in {q.resolve() for q in [*p.cwd.glob("failing-test-output-*"), *p.module_dir.glob("failing-test-output-*")]}:
                    shutil.move(str(out), str(tdir / f"run_{i:03d}.{out.name}"))
                if status == "pass":
                    run_log.unlink()
                with done_file.open("a") as f:
                    f.write(f"{status},{vals}\n")
                done.append(f"{status},{vals}")
                if status == "not_twice":
                    break
            results[t] = done
    finally:
        shutil.rmtree(dt, ignore_errors=True)
    nio_summarize(p, rec, results, "testrunner")


def nio_summarize(p, rec, runs, method):
    results = []
    for t, done in runs.items():
        counts = {k: sum(1 for d in done if d.split(",", 1)[0] == k) for k in ("pass", "failure", "error", "not_twice")}
        example = next((d.split(",", 1)[1] for d in done if not d.startswith("pass")), "")
        nio = "true" if counts["failure"] else ("false" if counts["pass"] else "inconclusive")
        results.append({"test": t, "nio_method": method, "iterations": len(done), **counts, "nio": nio, "example": example})
        print(f"  {t}: pass {counts['pass']}, failure {counts['failure']}, error {counts['error']}, "
              f"not run twice {counts['not_twice']} ({method})" + (" -> NIO" if nio == "true" else ""), flush=True)
    B.csv_write(p.row_dir / "nio.csv", results, NIO_FIELDS)
    gen = [r for r in results if r["nio"] == "true"]
    rec.update(log=str(p.row_dir / "nio.csv"), methods=len(results), flaky_generated=len(gen),
               note=rec.get("note", "") + ("" if method == "testrunner" else f"NIO with {method}; "),
               status="flaky_found" if gen else ("inconclusive" if any(r["nio"] == "inconclusive" for r in results)
                                                  else "no_flaky_found"))


def summary(args):
    base = args.out
    counts = rerun_counts(base / "rerun" / "runs.csv")
    out = []
    for rid, root, g, m, fix in load_tests(args):
        files = test_classes(g)
        fqcn = [f"{pkg}.{name}" if pkg else name for _, pkg, name in files]
        nd_file = base / "nondex" / "rows" / rid / "nondex_flaky.txt"
        nd = set(nd_file.read_text().split()) if nd_file.is_file() else None
        idf, polluter_of = {}, {}
        idf_file = base / "idflakies" / "rows" / rid / "idflakies_flaky.csv"
        idf_rows = base / "idflakies" / "rows.csv"
        idf_status = next((r["status"] for r in csv.DictReader(open(idf_rows, encoding="utf-8")) if r["id"] == rid),
                          "") if idf_rows.is_file() else ""
        idf_none = "-" if idf_status in ("no_flaky_found", "flaky_found", "polluter_found", "") else idf_status
        if idf_file.is_file():
            for r in csv.DictReader(open(idf_file, encoding="utf-8")):
                name = generated_name(r["test"], fqcn)
                if name:
                    idf[name] = r["type"] or "flaky"
                else:
                    for gt in filter(None, (r.get("suspected_generated_polluters") or "").split(";")):
                        polluter_of[gt] = polluter_of.get(gt, 0) + 1
        nio_file = base / "nio" / "rows" / rid / "nio.csv"
        nio_rows = {r["test"]: r for r in csv.DictReader(open(nio_file, encoding="utf-8"))} if nio_file.is_file() else None
        nio = {t: r["nio"] for t, r in nio_rows.items()} if nio_rows is not None else None
        for t in row_methods(args, rid):
            c = counts.get((rid, t), [0, 0, 0, 0])
            errors = c[3]
            row = {"project": m["project"], "sha": m["flaky_commit"], "module": m["module"], "generated_test": t,
                   "focal_method": g["selected_method"], "focal_from_test": g["csv"]["focal_from_test"],
                   "focal_from_test_type": g["csv"].get("focal_from_test_type", ""),
                   "rerun_runs": c[2] or "", "rerun_passed": c[0] if c[2] else "", "rerun_failed": c[1] if c[2] else "",
                   "rerun_errors": errors or "",
                   "rerun_flaky": (str(c[0] > 0 and c[1] > 0).lower() if c[2] else ""),
                   "nondex_flaky": "" if nd is None else str(t in nd).lower(),
                   "idflakies_type": "" if not idf_file.is_file() else idf.get(t, idf_none),
                   "idflakies_polluter_of": "" if not idf_file.is_file() else polluter_of.get(t, 0),
                   "nio": "" if nio is None else nio.get(t, ""),
                   "nio_method": "" if nio_rows is None else (nio_rows.get(t) or {}).get("nio_method", "")}
            row["any_flaky"] = str(row["rerun_flaky"] == "true" or row["nondex_flaky"] == "true"
                                   or t in idf or row["nio"] == "true"
                                   or bool(row["idflakies_polluter_of"])).lower()
            out.append(row)
    B.csv_write(base / "summary.csv", out, SUMMARY_FIELDS)
    flaky = [r for r in out if r["any_flaky"] == "true"]
    print(f"{len(out)} generated test methods, {len(flaky)} flagged flaky by at least one tool -> {base / 'summary.csv'}")
    for r in flaky:
        print(f"  {r['generated_test']}: rerun {r['rerun_failed']}/{r['rerun_runs']} failed, nondex {r['nondex_flaky']}, "
              f"idflakies {r['idflakies_type']} (polluter of {r['idflakies_polluter_of'] or 0}) , nio {r['nio']}")


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("command", choices=["rerun", "nondex", "idflakies", "nio", "summary"])
    a.add_argument("--out", type=Path, help="Default flaky_check_out/<command> (summary: flaky_check_out)")
    a.add_argument("--tests", type=Path, default=HERE / "task_csvs" / "flaky_generated_tests.csv",
                   help="CSV with the generated tests to check (default task_csvs/flaky_generated_tests.csv)")
    a.add_argument("--archive-dir", type=Path, action="append",
                   help="Folder with extracted <zip>/ archives (repeatable; default out_part1/extracted "
                        "and out_part2/extracted)")
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--runs", type=int, default=100, help="rerun: runs per test method (default 100)")
    a.add_argument("--offline", action="store_true", help="rerun: mvn -o after the first run (faster)")
    a.add_argument("--nondex-runs", type=int, default=10, help="nondex: seeds (default 10)")
    a.add_argument("--nio-runs", type=int, default=100,
                   help="nio: iterations per test method, each running it twice in one JVM (default 100)")
    a.add_argument("--rounds", type=int, default=10, help="idflakies: shuffled rounds (default 10)")
    a.add_argument("--timeout", type=int, default=6 * 3600, help="Seconds per Maven call (default 6h)")
    a.add_argument("--only", action="append", default=[], help="Only runs whose id, zip or generated test contains this text")
    a.add_argument("--redo", action="store_true", help="nondex/idflakies: redo rows that have a result")
    args = a.parse_args()
    args.tests = args.tests.resolve()
    args.archive_dir = [d.resolve() for d in (args.archive_dir or [HERE / "out_part1" / "extracted",
                                                                   HERE / "out_part2" / "extracted"])]
    args.methods = {rid: g["methods"] for rid, _, g, _, _ in load_tests(args)}
    args.repo = args.repo.resolve()
    args.out = (args.out or HERE / "flaky_check_out" / ("" if args.command == "summary" else args.command)).resolve()
    if args.command == "summary":
        return summary(args)
    args.out.mkdir(parents=True, exist_ok=True)

    env = os.environ.copy()
    mvn_bin = C.maven_without_extensions(args.out)
    if mvn_bin:
        env["PATH"] = str(mvn_bin) + os.pathsep + env["PATH"]
        env.pop("M2_HOME", None)
        env.pop("MAVEN_HOME", None)
    args.env = env

    rows = load_tests(args)
    only, args.only = args.only, []
    all_rows = load_tests(args)
    args.only = only
    prev = {}
    if (args.out / "rows.csv").is_file():
        prev = {r["id"]: r for r in csv.DictReader(open(args.out / "rows.csv", encoding="utf-8"))}
    print(f"{len(rows)} generation runs, {sum(len(r[2]['methods']) for r in rows)} test methods "
          f"from {args.tests.name} ({args.command})")
    for i, (rid, root, g, m, fix) in enumerate(rows, 1):
        if args.command != "rerun" and rid in prev and prev[rid]["status"] and not args.redo:
            print(f"[{i}/{len(rows)}] {rid}: done ({prev[rid]['status']}), skipping (--redo to rerun)")
            continue
        print(f"[{i}/{len(rows)}] {rid} {g['zip']} {g['selected_method']} (JDK {m['java']}"
              + (", mockito fix" if fix else "") + ")", flush=True)
        p = Prepared(rid, root, g, m, fix, args)
        rec = record(p, fix)
        try:
            with p:
                rec["build_status"] = p.build()
                if rec["build_status"] in ("build_failed", "build_timeout"):
                    rec["status"] = rec["build_status"]
                else:
                    {"rerun": do_rerun, "nondex": do_nondex, "idflakies": do_idflakies, "nio": do_nio}[args.command](p, rec, args)
        except FileExistsError as ex:
            rec.update(status="would_overwrite", note=str(ex)[:300])
        if args.command == "rerun" and not rec["status"]:
            c = {t: v for (r_id, t), v in rerun_counts(args.out / "runs.csv").items() if r_id == rid}
            flaky = [t for t, v in c.items() if v[0] and v[1]]
            rec.update(methods=len(c), flaky_generated=len(flaky),
                       status="flaky_found" if flaky else ("always_failed" if c and all(not v[0] for v in c.values())
                                                           else "no_flaky_found"))
        print(f"  => {rec['status']}" + (f": {rec.get('flaky_generated')} generated test(s) flaky"
                                         if rec.get("flaky_generated") else ""), flush=True)
        prev[rid] = rec
        B.csv_write(args.out / "rows.csv", sorted(prev.values(), key=lambda x: x["id"]), ROW_FIELDS)
        if args.command == "idflakies":
            write_orders(args, all_rows)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
import csv
import json
from pathlib import Path
import re

Q = Path(__file__).resolve().parent
H = Q.parent
ARCHIVES = [H / "out_part1" / "extracted", H / "out_part2" / "extracted", H / "nonflaky_check_package" / "extracted",
            Q / "extracted"]
TEST = re.compile(r"@Test\b")
ASSERT = re.compile(r"\b(assert\w*|fail)\s*\(")
VERIFY = re.compile(r"\bverify\s*\(")
EXC = re.compile(r"\bassertThrows\s*\(|expected\s*=\s*[\w.]+\.class|\bExpectedException\b|\bfail\s*\(")


def method_bodies(src, anno):
    out = {}
    for m in re.finditer(r"@" + anno + r"\b[^{;]*?\bvoid\s+(\w+)\s*\([^)]*\)[^{;]*\{", src):
        d, i = 1, m.end()
        while i < len(src) and d:
            d += {"{": 1, "}": -1}.get(src[i], 0)
            i += 1
        out[m.group(1)] = (m.group(0), src[m.end():i])
    return out


def features(path):
    s = re.sub(r"//[^\n]*|/\*.*?\*/", "", path.read_text(encoding="utf-8", errors="replace"), flags=re.S)
    tests = method_bodies(s, "Test")
    fields = set(re.findall(r"\bField\s+(\w+)\s*[=;,)]", s))
    after = "".join(b for _, b in method_bodies(s, "After").values()) + \
            "".join(b for _, b in method_bodies(s, "AfterEach").values()) + \
            "".join(b for _, b in method_bodies(s, "AfterClass").values()) + \
            "".join(b for _, b in method_bodies(s, "AfterAll").values())
    no_assert = sum(1 for head, b in tests.values() if not (ASSERT.search(b) or VERIFY.search(b) or EXC.search(head + b)))
    exc_only = sum(1 for head, b in tests.values() if EXC.search(head + b) and not re.search(r"\bassert(?!Throws)\w*\s*\(", b))
    return dict(
        loc=sum(1 for l in s.splitlines() if l.strip()),
        tests=len(TEST.findall(s)),
        asserts=len(ASSERT.findall(s)) + len(VERIFY.findall(s)),
        tests_no_assert=no_assert,
        tests_exception_only=exc_only,
        mocks=bool(re.search(r"\bmock\s*\(|@Mock\b|\bspy\s*\(|@Spy\b|\bwhen\s*\(", s)),
        verify=bool(VERIFY.search(s)),
        reflection=bool(re.search(r"setAccessible\s*\(\s*true|getDeclared(Field|Method)", s)),
        reflection_write=any(re.search(r"\b" + f + r"\.set\w*\s*\(", s) for f in fields),
        static_mock=bool(re.search(r"mockStatic\s*\(|PowerMock", s)),
        before=bool(re.search(r"@(Before|BeforeEach|BeforeClass|BeforeAll)\b", s)),
        after=bool(after.strip()),
        sysprop=bool(re.search(r"System\.(setProperty|clearProperty)", s)),
        sleep=bool(re.search(r"Thread\.sleep|TimeUnit\.\w+\.sleep", s)),
        time=bool(re.search(r"currentTimeMillis|nanoTime|new Date\(\)|\.now\(\)", s)),
        random=bool(re.search(r"new Random\(|Math\.random|randomUUID|ThreadLocalRandom", s)),
        files_io=bool(re.search(r"new File\(|Files\.\w+\(|TemporaryFolder|@TempDir|createTemp", s)),
        network=bool(re.search(r"\bSocket\b|ServerSocket|HttpClient|URLConnection|NetUtils|getAvailablePort", s)),
        threads=bool(re.search(r"new Thread\(|ExecutorService|CountDownLatch|CompletableFuture", s)))


def read(p):
    return list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []


def agent_failing(cond):
    bad = set()
    for r in read(H / "agent_experiment" / f"out_{cond}" / "generation_results.csv"):
        t = H / "agent_experiment" / f"out_{cond}" / "runs" / f"row_{int(r['row_number']):03d}" / "trajectory.json"
        if not t.is_file():
            continue
        d = json.load(open(t))
        m = d.get("messages") or d.get("trajectory")
        outs = [x["content"] if isinstance(x["content"], str) else json.dumps(x["content"]) for x in m if x["role"] in ("user", "tool")]
        runs = [re.findall(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+)", o) for o in outs if "Tests run:" in o]
        last = runs[-1][-1] if runs and runs[-1] else None
        if not last or int(last[1]) or int(last[2]) or not int(last[0]):
            bad.add(f"row_{int(r['row_number']):03d}")
    return bad


NOT_OD = {"io.elasticjob.lite.event.rdb.JobEventRdbSearchTest#assertFindJobStatusTraceEventsWithTime"}


VICTIMS = {}
IDOFT = {r["Fully-Qualified Test Name (packageName.ClassName.methodName)"].strip()
         for r in csv.DictReader(open(Path(__file__).resolve().parent / "idoft_pr-data.csv", encoding="utf-8", errors="replace"))}


def status_maps():
    od = [H / "flaky_check_out" / "od_confirm" / "od_confirm.csv",
          H / "nonflaky_check_package" / "flaky_check_out" / "od_confirm" / "od_confirm.csv",
          *(H / "prompt_experiment").glob("out_idflakies/lane*/od_confirm/od_confirm.csv"),
          *(H / "prompt_experiment").glob("machine_b_idflakies_results/out_idflakies/**/od_confirm/od_confirm.csv"),
          *(H / "agent_experiment").glob("out_flaky_[AF]/lane*/od_confirm/od_confirm.csv")]
    st = {}
    for f in od:
        for r in read(f):
            if r["test"] in NOT_OD:
                continue
            cls = lambda t: t.split("#")[0].rsplit(".", 1)[-1]
            if r["category"] == "victim" and r["generated"] != "true" and r["polluter_generated"] == "true":
                st.setdefault((r["id"], cls(r["polluter"])), set()).add("polluter")
                VICTIMS.setdefault((r["id"], cls(r["polluter"])), set()).add(r["test"])
            if r["category"] == "brittle" and r["state_setter_generated"] == "true":
                st.setdefault((r["id"], cls(r["state_setter"])), set()).add("state_setter")
            if r["category"] == "victim" and r["generated"] == "true":
                st.setdefault((r["id"], cls(r["test"])), set()).add("od_victim")
    idf = [H / "flaky_check_out" / "idflakies" / "rows.csv", H / "nonflaky_check_package" / "flaky_check_out" / "idflakies" / "rows.csv",
           *(H / "prompt_experiment").glob("out_idflakies/lane*/idflakies/rows.csv"),
           *(H / "prompt_experiment").glob("machine_b_idflakies_results/out_idflakies/idflakies/rows.csv"),
           *(H / "agent_experiment").glob("out_flaky_[AF]/lane*/idflakies/rows.csv")]
    global IDF_DONE
    confirmed = {r["id"] for f in od for r in read(f)}
    IDF_DONE = {r["id"] for f in idf for r in read(f)
                if r.get("status") and (r["status"] == "no_flaky_found" or r["id"] in confirmed)}
    for f in (H / "od_recheck").glob("*/recheck.csv"):
        for r in read(f):
            if r["category"] == "victim" and r["test"] not in NOT_OD:
                pc = r["confirmed_polluter"].split("#")[0].rsplit(".", 1)[-1]
                st.setdefault((r["id"], pc), set()).add("polluter")
                VICTIMS.setdefault((r["id"], pc), set()).add(r["test"])
    checked = set()
    sums = [("main, flaky side", H / "flaky_check_out" / "summary.csv"),
            ("main, non-flaky side", H / "nonflaky_check_package" / "flaky_check_out" / "summary.csv"),
            *[("agent " + c, f) for c in "AF" for f in (H / "agent_experiment").glob(f"out_flaky_{c}/lane*/summary.csv")]]
    for study, f in sums:
        for r in read(f):
            t = r["generated_test"].split("#")[0].rsplit(".", 1)[-1]
            key = (r.get("id") or "", t)
            flaky = any(r.get(k) == "true" for k in ("rerun_flaky", "nondex_flaky", "nio"))
            checked.add((study, t))
            if flaky:
                st.setdefault((study, t), set()).add("self_flaky")
    for f in sorted((H / "machine_b_other_checks").glob("lane*/*/rows.csv")):
        for r in read(f):
            study = "prompt, current" if r["id"].startswith("out_default_") else "prompt, cleanup"
            for t in r["test_classes"].split(";"):
                checked.add((study, t))
                if r["status"] != "no_flaky_found" or r.get("flaky_generated", "0") not in ("", "0"):
                    st.setdefault((study, t), set()).add("self_flaky")
    return st, checked


def file_id(rel):
    parts = Path(rel).parts
    i = parts.index("runs")
    return f"{(parts[i - 1] if i else 'nonflaky_check_package')}_{parts[i + 1]}"


def find_dev(cls, zip_name):
    rel = Path(*cls.split(".")[:-1]) / (cls.rsplit(".", 1)[-1] + ".java")
    for a in ARCHIVES:
        root = a / zip_name
        if root.is_dir():
            for p in root.rglob(str(rel)):
                if "src/test/java" in str(p) and "/target/" not in str(p):
                    return p
    return None


def main():
    global ST
    global CHECKED
    ST, CHECKED = status_maps()
    sets = [("main, flaky side", H / "task_csvs" / "flaky_generated_tests.csv", H, None),
            ("main, non-flaky side", H / "nonflaky_check_package" / "task_csvs" / "nonflaky_generated_tests.csv",
             H / "nonflaky_check_package", None),
            ("prompt, current", H / "prompt_experiment" / "task_csvs" / "experiment_generated_tests.csv", H, "out_default"),
            ("prompt, cleanup", H / "prompt_experiment" / "task_csvs" / "experiment_generated_tests.csv", H, "out_cleanup"),
            ("agent A", H / "agent_experiment" / "task_csvs" / "agent_A_tests.csv", H, "A"),
            ("agent F", H / "agent_experiment" / "task_csvs" / "agent_F_tests.csv", H, "F")]
    rows, dev = [], {}
    for name, tests_csv, base, sel in sets:
        bad = agent_failing(sel) if name.startswith("agent") else set()
        seen = set()
        for r in read(tests_csv):
            if name.startswith("prompt") and f"/{sel}/" not in r["test_file"]:
                continue
            if any(f"/{b}/" in r["test_file"] for b in bad) or r["test_file"] in seen:
                continue
            seen.add(r["test_file"])
            cls = Path(r["test_file"]).stem
            fid = file_id(r["test_file"])
            tags = ST.get((fid, cls), set())
            done = fid in IDF_DONE
            rows.append(dict(group=name, file=r["test_file"], zip=r["zip"], **features(base / r["test_file"]),
                             polluter=("polluter" in tags) if done else None,
                             state_setter=("state_setter" in tags) if done else None,
                             od_victim=("od_victim" in tags) if done else None,
                             victims=";".join(sorted(VICTIMS.get((fid, cls), set()))),
                             self_flaky=("self_flaky" in ST.get((name, cls), set())) if (name, cls) in CHECKED else None))
            side = "flaky side" if name in ("main, flaky side", "prompt, current", "prompt, cleanup", "agent A", "agent F") \
                and r.get("focal_from_test_type", "") not in ("nonflaky",) else "non-flaky side"
            if name == "main, non-flaky side":
                side = "non-flaky side"
            dev.setdefault((r["focal_from_test"].split("#")[0], r["zip"]), (side, name))
    missing = []
    for (cls, z), (side, src) in sorted(dev.items()):
        p = find_dev(cls, z)
        if p is None:
            missing.append(cls)
            continue
        g = "developer (main, flaky side)" if src == "main, flaky side" else \
            "developer (main, non-flaky side)" if src == "main, non-flaky side" else None
        if g:
            rows.append(dict(group=g, file=str(p.relative_to(H)), zip=z, **features(p), polluter=None, state_setter=None, od_victim=None, victims="", self_flaky=None))
        if src not in ("main, flaky side", "main, non-flaky side") or cls in FIVE:
            pass
    for cls, z in FIVE.items():
        p = find_dev(cls, z)
        if p:
            rows.append(dict(group="developer (5 experiment classes)", file=str(p.relative_to(H)), zip=z, **features(p), polluter=None, state_setter=None, od_victim=None, victims="", self_flaky=None))
    keys = [k for k in rows[0] if k not in ("group", "file", "zip")]
    with open(Q / "static_files.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, ["group", "file", "zip"] + keys)
        w.writeheader()
        w.writerows(rows)

    order = ["developer (main, flaky side)", "main, flaky side", "developer (main, non-flaky side)", "main, non-flaky side",
             "developer (5 experiment classes)", "prompt, current", "prompt, cleanup", "agent A", "agent F"]
    flags = [("mocks", "Mocks"), ("verify", "verify()"), ("reflection", "Reflection"),
             ("reflection_write", "Writes a field by reflection"), ("before", "@Before"), ("after", "@After with code"),
             ("sysprop", "System properties"), ("sleep", "sleep"), ("time", "Clock/time"), ("random", "Randomness"),
             ("files_io", "Files"), ("network", "Network"), ("threads", "Threads")]
    status = [("polluter", "Confirmed polluter"), ("state_setter", "State-setter"), ("od_victim", "Itself an OD victim"),
              ("self_flaky", "Flaky alone (rerun/NonDex/NIO)")]
    head = ["Group", "Files"] + [t for _, t in status] + ["Developer tests broken (in IDoFT / new)", "Test methods/file", "Assertions/test", "Tests without assertion",
            "Tests that only check an exception", "Lines/file"] + [t for _, t in flags]
    out = ["# What the tests contain: generated vs developer", "",
           "Static, regex-based counts on the source (approximate). Generated files: only those that pass alone.",
           "Developer: the class of each focal method's source test. % = share of files, except where per test.",
           "Developer tests broken: distinct developer tests (per project) confirmed (od_confirm) as victims of the group's polluters;",
           "in IDoFT = listed in IDoFT's pr-data.csv (known flaky, matched by name), new = not listed.", "",
           "| " + " | ".join(head) + " |", "|" + "---|" * len(head)]
    for g in order:
        xs = [r for r in rows if r["group"] == g]
        if not xs:
            continue
        nt = sum(r["tests"] for r in xs) or 1
        def st_cell(k):
            vals = [r[k] for r in xs if r[k] is not None]
            if not vals:
                return "-"
            n = f"{sum(bool(v) for v in vals)} of {len(vals)}"
            return n if len(vals) == len(xs) else n + f" ({len(xs) - len(vals)} not checked yet)"
        vic = {(r["zip"], t) for r in xs for t in r["victims"].split(";") if t}
        known = sum(t.replace("#", ".") in IDOFT for _, t in vic)
        vcell = "-" if g.startswith("developer") else (f"{len(vic)} ({known} / {len(vic) - known})" if vic else "0")
        cells = [g, str(len(xs))] + [st_cell(k) for k, _ in status] + [vcell, f"{sum(r['tests'] for r in xs) / len(xs):.1f}", f"{sum(r['asserts'] for r in xs) / nt:.1f}",
                 f"{100 * sum(r['tests_no_assert'] for r in xs) / nt:.0f}%",
                 f"{100 * sum(r['tests_exception_only'] for r in xs) / nt:.0f}%",
                 f"{sum(r['loc'] for r in xs) / len(xs):.0f}"]
        cells += [f"{100 * sum(bool(r[k]) for r in xs) / len(xs):.0f}%" for k, _ in flags]
        out.append("| " + " | ".join(cells) + " |")
    if missing:
        out += ["", f"Developer classes not found in the projects: {', '.join(sorted(set(missing)))}."]
    (Q / "STATIC_FEATURES.md").write_text("\n".join(out) + "\n", encoding="utf-8")
    print("\n".join(out))


FIVE = {"org.apache.dubbo.rpc.protocol.dubbo.telnet.ChangeTelnetHandlerTest": "dubbodubborpcdubborpcdubboaa9f16e",
        "org.apache.dubbo.rpc.protocol.dubbo.telnet.PortTelnetHandlerTest": "dubbodubborpcdubborpcdubbo628ad77",
        "net.sf.marineapi.ais.parser.AISMessageFactoryTest": "marineapi0a1f308",
        "com.j256.ormlite.logger.LoggerFactoryTest": "ormlitecore59309e5",
        "com.sap.scimono.client.GroupRequestTest": "scimono=scimono-client=7487ec5"}

if __name__ == "__main__":
    main()

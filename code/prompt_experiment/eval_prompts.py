#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import re
import subprocess
import sys

E = Path(__file__).resolve().parent
H = E.parent
VARIANTS = ("default", "cleanup")
POLLUTER_ZIPS = {"dubbodubborpcdubborpcdubboaa9f16e", "dubbodubborpcdubborpcdubbo628ad77", "marineapi0a1f308"}


def read(p):
    return list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []


def save(p, rows, fields):
    with open(p, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fields)
        w.writeheader()
        w.writerows(rows)


def replay(v):
    part = f"{E / ('out_' + v)}:{E / 'manifest.csv'}"
    env = dict(os.environ)
    subprocess.run([sys.executable, str(E / "replay_generated.py"), "--part", part, "--out", str(E / f"replay_{v}")], env=env)
    bad = [r["id"] for r in read(E / f"replay_{v}" / "results.csv") if r["status"] != "passed"]
    if bad:
        subprocess.run([sys.executable, str(E / "replay_generated.py"), "--part", part, "--out", str(E / f"replay_{v}_fix"),
                        "--mockito-fix", *sum((["--only", b] for b in bad), [])], env=env)


def method_bodies(src, annos):
    out = []
    for m in re.finditer(r"@(" + "|".join(annos) + r")\b[^{]*\{", src):
        depth, i = 1, m.end()
        while i < len(src) and depth:
            depth += {"{": 1, "}": -1}.get(src[i], 0)
            i += 1
        out.append(src[m.end():i])
    return out


def code_facts(src):
    sets = re.findall(r"\.set(?:Int|Long|Boolean|Double|Float)?\(\s*([^,()]+?)\s*,", src)
    refl = "setAccessible(true)" in src and bool(sets)
    after = method_bodies(src, ["After", "AfterEach", "AfterAll", "AfterClass"])
    return {"sets_field_by_reflection": refl,
            "sets_static_field": refl and any(s == "null" for s in sets),
            "uses_getInstance": "getInstance()" in src,
            "has_after": bool(after),
            "after_sets_field_back": any(re.search(r"\.set(?:Int|Long|Boolean|Double|Float)?\(", b) for b in after)}


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--skip-replay", action="store_true")
    a.add_argument("--skip-pollution", action="store_true")
    args = a.parse_args()
    (E / "eval").mkdir(exist_ok=True)
    man = {r["row_number"]: r for r in read(E / "manifest.csv")}

    files = []
    for v in VARIANTS:
        if not args.skip_replay:
            replay(v)
        plain = {r["row_number"]: r for r in read(E / f"replay_{v}" / "results.csv")}
        fixed = {r["row_number"]: r for r in read(E / f"replay_{v}_fix" / "results.csv")}
        gen = {r["row_number"]: r for r in read(E / f"out_{v}" / "generation_results.csv")}
        for rn, m in sorted(man.items(), key=lambda x: int(x[0])):
            g = gen.get(rn, {})
            tests = sorted((E / f"out_{v}" / "runs" / f"row_{int(rn):03d}" / "tests").rglob("*_Test.java"))
            status = plain.get(rn, {}).get("status", "")
            mockito_fix = False
            if status != "passed" and fixed.get(rn, {}).get("status") == "passed":
                status, mockito_fix = "passed", True
            base = {"variant": v, "row": rn, "rep": m["rep"], "zip": m["zip"], "focal": m["selected_method"],
                    "junit": m["junit"], "generation": g.get("status", "not_run"), "replay": status or "-",
                    "mockito_fix": mockito_fix}
            if not tests:
                files.append(dict(base, file=""))
            for f in tests:
                files.append(dict(base, file=str(f.relative_to(E)), **code_facts(f.read_text(encoding="utf-8", errors="replace"))))
    ff = ["variant", "row", "rep", "zip", "focal", "junit", "generation", "replay", "mockito_fix", "file",
          "sets_field_by_reflection", "sets_static_field", "uses_getInstance", "has_after", "after_sets_field_back"]
    save(E / "eval" / "files.csv", files, ff)

    pol = read(E / "eval" / "pollution.csv")
    if not args.skip_pollution and not pol:
        pol = pollution(files)
        if pol:
            save(E / "eval" / "pollution.csv", pol, list(pol[0].keys()))
    report(files, pol)


def pollution(files):
    sys.path.insert(0, str(H))
    import flaky_check_basic as F
    import collect_nonflaky as C
    import od_confirm as O
    victims = {}
    for p in read(H / "flaky_check_out" / "od_pairs_generated.csv"):
        if p["role"] == "polluter":
            victims.setdefault(p["project"], set()).add(p["developer_test"])
    proj = {r["zip"]: r for r in read(H / "task_csvs" / "flaky_generated_tests.csv")}
    rows = []
    for f in files:
        if f["zip"] in POLLUTER_ZIPS and f["replay"] == "passed" and f["file"]:
            p = proj[f["zip"]]
            rows.append({"project": p["project"], "sha": p["sha"], "module": p["module"],
                         "generated_test": "", "test_file": str((E / f["file"]).relative_to(H)),
                         "focal_method": f["focal"], "zip": f["zip"], "java": p["java"], "junit": f["junit"],
                         "mockito_fix": str(f["mockito_fix"]).lower(), "focal_from_test": p["focal_from_test"],
                         "focal_from_test_type": "experiment"})
    if not rows:
        return []
    tests_csv = E / "eval" / "pollution_tests.csv"
    save(tests_csv, rows, list(rows[0].keys()))

    class A:
        pass
    args = A()
    args.command, args.tests, args.archive_dir = "od_confirm", tests_csv, [E / "extracted"]
    args.only, args.test, args.offline, args.timeout = [], [], True, 1800
    args.repo = (Path.home() / ".m2" / "repository").resolve()
    args.out = E / "eval" / "pollution"
    args.env = os.environ.copy()
    out = []
    for rid, root, g, m, fix in F.load_tests(args):
        p = F.Prepared(rid, root, g, m, fix, args)
        variant = rid.split("_row_")[0].replace("out_", "")
        with p:
            if p.build() in ("build_failed", "build_timeout"):
                print(f"{rid}: build failed")
                continue
            run = O.Runner(p, args)
            for cls in p.fqcn:
                for v in sorted(victims.get(g["zip"], ())):
                    short = v.rsplit(".", 1)[-1]
                    r = run.run([cls, v], f"after_{cls.rsplit('.', 1)[-1]}_{short}")
                    tests, _ = C.read_reports(p.module_dir)
                    gen_ok = all(o == "passed" for t, o in tests.items() if t.startswith(cls + "#")) and any(t.startswith(cls + "#") for t in tests)
                    out.append({"variant": variant, "row": rid.rsplit("_", 1)[-1], "zip": g["zip"], "generated_class": cls,
                                "victim": v, "victim_after_generated": r, "generated_tests_passed": gen_ok})
                    print(f"  {variant} {rid} {cls.rsplit('.', 1)[-1]} -> {short}: {r}", flush=True)
    return out


def report(files, pol):
    lines = ["# Prompt experiment: default vs cleanup prompt", "",
             "Made by eval_prompts.py. Per generated test file; a row (generation run) can have several files.", ""]
    for junit in ("4", "5"):
        lines += [f"## JUnit {junit}", "",
                  "| | default | cleanup |", "|---|---|---|"]
        sel = {v: [f for f in files if f["variant"] == v and f["junit"] == junit] for v in VARIANTS}
        runs = {v: {f["row"] for f in sel[v]} for v in VARIANTS}
        withf = {v: [f for f in sel[v] if f["file"]] for v in VARIANTS}

        def c(v, pred):
            return sum(1 for f in withf[v] if pred(f))
        stats = [
            ("Generation runs", lambda v: len(runs[v])),
            ("Runs with tests generated", lambda v: len({f["row"] for f in withf[v]})),
            ("Generated test files", lambda v: len(withf[v])),
            ("...passing the replay", lambda v: c(v, lambda f: f["replay"] == "passed")),
            ("...setting a field by reflection", lambda v: c(v, lambda f: f["sets_field_by_reflection"])),
            ("...setting a static field by reflection", lambda v: c(v, lambda f: f["sets_static_field"])),
            ("...with an @After/@AfterEach", lambda v: c(v, lambda f: f["has_after"])),
            ("...setting by reflection AND setting it back in @After", lambda v: c(v, lambda f: f["sets_field_by_reflection"] and f["after_sets_field_back"])),
        ]
        lines += [f"| {n} | {fn('default')} | {fn('cleanup')} |" for n, fn in stats]
        lines.append("")
    if pol:
        lines += ["## Pollution of the known victims (JUnit 4 polluter focal methods)", "",
                  "Each known victim of the project run right after the whole generated class (M8).", "",
                  "| | default | cleanup |", "|---|---|---|"]
        vals = {}
        for v in VARIANTS:
            by = {}
            for r in pol:
                if r["variant"] == v:
                    by.setdefault((r["row"], r["generated_class"]), []).append(r["victim_after_generated"])
            runs_v = [r for r in pol if r["variant"] == v]
            vals[v] = (len(by), sum(1 for x in by.values() if "failed" in x),
                       f"{sum(1 for r in runs_v if r['victim_after_generated'] == 'failed')} of {len(runs_v)}")
        for i, n in enumerate(("Generated classes checked", "...that make at least one victim fail", "Victim runs that failed")):
            lines.append(f"| {n} | {vals['default'][i]} | {vals['cleanup'][i]} |")
    (E / "eval" / "REPORT.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))


if __name__ == "__main__":
    main()

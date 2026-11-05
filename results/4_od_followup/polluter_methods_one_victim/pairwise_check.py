#!/usr/bin/env python3
import argparse
import csv
import glob
import os
import sys
from pathlib import Path

D = Path(__file__).resolve().parent
H = D.parent
sys.path.insert(0, str(H))
import flaky_check_basic as F
import od_confirm as O

A = H / "agent_experiment"
P = H / "prompt_experiment"
SETS = {
    "main": (H / "task_csvs" / "flaky_generated_tests.csv", [H / "out_part1" / "extracted", H / "out_part2" / "extracted"],
             [H / "flaky_check_out"], ["out_part1_row_055", "out_part2_row_043", "out_part2_row_045"]),
    "prompt_default": (P / "task_csvs" / "experiment_generated_tests.csv", [P / "extracted"],
                       [P / "out_idflakies", P / "machine_b_idflakies_results", H / "od_recheck" / "pilot"],
                       ["out_default_row_002", "out_default_row_004", "out_default_row_011", "out_default_row_012"]),
    "agent_A": (A / "task_csvs" / "agent_A_tests.csv", [A / "extracted"], [A / "out_flaky_A"],
                [f"out_A_row_{n:03d}" for n in (1, 2, 3, 4, 7, 8)]),
    "agent_F": (A / "task_csvs" / "agent_F_tests.csv", [A / "extracted"], [A / "out_flaky_F"],
                [f"out_F_row_{n:03d}" for n in (2, 4, 5)]),
    "agent_FC": (A / "task_csvs" / "agent_FC_tests.csv", [A / "extracted"], [A / "out_flaky_FC"], ["out_FC_row_002"]),
}
RUNS = 2
FIELDS = ["experiment", "id", "zip", "file", "method", "victim", "victim_alone", "after_1", "after_2", "polluter",
          "named_by_od_confirm"]


def victims(dirs, rid):
    out = {}
    for d in dirs:
        for f in glob.glob(str(d / "**" / "od_confirm" / "od_confirm.csv"), recursive=True):
            for x in csv.DictReader(open(f, encoding="utf-8")):
                if x["id"] == rid and x["category"] == "victim" and x["generated"] != "true" \
                        and x["polluter_generated"] == "true":
                    out.setdefault(x["test"], x["polluter"])
        for f in glob.glob(str(d / "**" / "recheck.csv"), recursive=True):
            for x in csv.DictReader(open(f, encoding="utf-8")):
                if x["id"] == rid and x.get("category") == "victim":
                    out.setdefault(x["test"], x["confirmed_polluter"])
    return out


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--lane", choices=["dubbo", "other"], required=True)
    a.add_argument("--out", type=Path)
    args = a.parse_args()
    out = (args.out or D / args.lane).resolve()
    out.mkdir(parents=True, exist_ok=True)
    out_csv = out / "results.csv"
    res = list(csv.DictReader(open(out_csv, encoding="utf-8"))) if out_csv.is_file() else []
    done = {(r["id"], r["method"]) for r in res}

    for exp, (tests, archives, dirs, ids) in SETS.items():
        ns = argparse.Namespace(tests=tests, archive_dir=archives, out=out, repo=Path.home() / ".m2" / "repository",
                                offline=True, timeout=1800, only=ids, command="od_confirm", env=os.environ.copy(),
                                methods=[], redo=False)
        for rid, root, g, m, fix in F.load_tests(ns):
            if rid not in ids or ("dubbo" in g["zip"]) != (args.lane == "dubbo"):
                continue
            vic = victims(dirs, rid)
            if not vic:
                print(f"{rid}: no confirmed victim found, skipped", flush=True)
                continue
            p = F.Prepared(rid, root, g, m, fix, ns)
            methods = [F.generated_name(t, p.fqcn) or t for t in g["methods"]]
            if all((rid, t) in done for t in methods):
                print(f"{rid}: done before", flush=True)
                continue
            with p:
                if p.build() in ("build_failed", "build_timeout"):
                    print(f"{rid}: build failed", flush=True)
                    continue
                run = O.Runner(p, ns)
                v, alone = None, ""
                for i, cand in enumerate(sorted(vic)[:5]):
                    alone = run.run([cand], f"victim{i}_alone")
                    if alone == "passed":
                        v = cand
                        break
                    print(f"{rid}: victim {cand} alone {alone}, trying the next", flush=True)
                if v is None:
                    print(f"{rid}: no victim passes alone, skipped", flush=True)
                    continue
                print(f"{rid} {g['zip']}: victim {v}, {len(methods)} methods", flush=True)
                for t in methods:
                    if (rid, t) in done:
                        continue
                    k = t.split("#")[-1]
                    after = [run.run([t, v], f"{k}_then_victim_{i}") for i in range(1, RUNS + 1)]
                    pol = "yes" if all(x == "failed" for x in after) else (
                        "no" if all(x == "passed" for x in after) else "mixed")
                    print(f"  {k}: {', '.join(after)} -> polluter {pol}", flush=True)
                    res.append({"experiment": exp, "id": rid, "zip": g["zip"], "file": t.split("#")[0].split(".")[-1],
                                "method": t, "victim": v, "victim_alone": alone, "after_1": after[0],
                                "after_2": after[1], "polluter": pol,
                                "named_by_od_confirm": "yes" if t in set(vic.values()) else "no"})
                    with open(out_csv, "w", newline="", encoding="utf-8") as f:
                        w = csv.DictWriter(f, FIELDS)
                        w.writeheader()
                        w.writerows(res)
    print("done", flush=True)


if __name__ == "__main__":
    main()

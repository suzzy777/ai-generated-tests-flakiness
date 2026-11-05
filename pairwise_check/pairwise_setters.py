#!/usr/bin/env python3
import argparse
import csv
import os
import sys
from pathlib import Path

D = Path(__file__).resolve().parent
H = D.parent
sys.path.insert(0, str(H))
import flaky_check_basic as F
import od_confirm as O

SETS = {
    "main_flaky": (H / "task_csvs" / "flaky_generated_tests.csv", [H / "out_part1" / "extracted", H / "out_part2" / "extracted"],
                   H / "flaky_check_out" / "od_confirm" / "od_confirm.csv", "out_part1_row_070"),
    "main_nonflaky": (H / "od_recheck" / "nonflaky_tests.csv", [H / "nonflaky_check_package" / "extracted"],
                      H / "nonflaky_check_package" / "flaky_check_out" / "od_confirm" / "od_confirm.csv",
                      "nonflaky_check_package_row_106"),
}
RUNS = 2
FIELDS = ["experiment", "id", "method", "brittle", "brittle_alone", "after_1", "after_2", "state_setter",
          "named_by_od_confirm"]


def main():
    out = D / "setters"
    out.mkdir(exist_ok=True)
    res = []
    for exp, (tests, archives, odc, rid) in SETS.items():
        named = {}
        for x in csv.DictReader(open(odc, encoding="utf-8")):
            if x["id"] == rid and x["category"] == "brittle" and x["state_setter_generated"] == "true":
                named.setdefault(x["test"], x["state_setter"])
        ns = argparse.Namespace(tests=tests, archive_dir=archives, out=out, repo=Path.home() / ".m2" / "repository",
                                offline=True, timeout=1800, only=[rid], command="od_confirm", env=os.environ.copy(),
                                methods=[], redo=False)
        for r, root, g, m, fix in F.load_tests(ns):
            if r != rid:
                continue
            p = F.Prepared(rid, root, g, m, fix, ns)
            methods = [F.generated_name(t, p.fqcn) or t for t in g["methods"]]
            with p:
                if p.build() in ("build_failed", "build_timeout"):
                    print(f"{rid}: build failed", flush=True)
                    continue
                run = O.Runner(p, ns)
                b, alone = None, ""
                for i, cand in enumerate(sorted(named)):
                    alone = run.run([cand], f"brittle{i}_alone")
                    if alone == "failed":
                        b = cand
                        break
                if b is None:
                    print(f"{rid}: no brittle test fails alone, skipped", flush=True)
                    continue
                print(f"{rid}: brittle {b}, {len(methods)} methods", flush=True)
                for t in methods:
                    k = t.split("#")[-1]
                    after = [run.run([t, b], f"{k}_then_brittle_{i}") for i in range(1, RUNS + 1)]
                    s = "yes" if all(x == "passed" for x in after) else ("no" if all(x == "failed" for x in after) else "mixed")
                    print(f"  {k}: {', '.join(after)} -> state-setter {s}", flush=True)
                    res.append({"experiment": exp, "id": rid, "method": t, "brittle": b, "brittle_alone": alone,
                                "after_1": after[0], "after_2": after[1], "state_setter": s,
                                "named_by_od_confirm": "yes" if t in set(named.values()) else "no"})
                    with open(out / "results.csv", "w", newline="", encoding="utf-8") as f:
                        w = csv.DictWriter(f, FIELDS)
                        w.writeheader()
                        w.writerows(res)
    print("done", flush=True)


if __name__ == "__main__":
    main()

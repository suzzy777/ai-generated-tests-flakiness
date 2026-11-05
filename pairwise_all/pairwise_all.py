#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import sys

D = Path(__file__).resolve().parent
H = D.parent
sys.path.insert(0, str(H))
sys.path.insert(0, str(H / "pairwise_check"))
import flaky_check_basic as F
import od_confirm as O
import pairwise_check as PC

C = F.C
RUNS = 2
FIELDS = ["experiment", "id", "zip", "file", "method", "victims", "victims_pass_baseline", "failed_1", "failed_2",
          "failed_both", "polluter", "pairwise_one_victim"]


class AllRunner(O.Runner):
    def run_all(self, order, tag):
        self.run(order, tag)
        tests, _ = C.read_reports(self.p.module_dir)
        return {t: tests.get(t, "missing") for t in order}


def old_pairwise():
    out = {}
    for f in (H / "pairwise_check" / "dubbo" / "results.csv", H / "pairwise_check" / "other" / "results.csv"):
        if f.is_file():
            for r in csv.DictReader(open(f, encoding="utf-8")):
                out[(r["id"], r["method"])] = r["polluter"]
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
    old = old_pairwise()

    for exp, (tests, archives, dirs, ids) in PC.SETS.items():
        ns = argparse.Namespace(tests=tests, archive_dir=archives, out=out, repo=Path.home() / ".m2" / "repository",
                                offline=True, timeout=3600, only=ids, command="od_confirm", env=os.environ.copy(),
                                methods=[], redo=False)
        for rid, root, g, m, fix in F.load_tests(ns):
            if rid not in ids or ("dubbo" in g["zip"]) != (args.lane == "dubbo"):
                continue
            vic = sorted(PC.victims(dirs, rid))
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
                run = AllRunner(p, ns)
                base = run.run_all(vic, "baseline")
                ok = [v for v in vic if base[v] == "passed"]
                print(f"{rid} {g['zip']}: {len(vic)} victims, {len(ok)} pass in the baseline, "
                      f"{len(methods)} methods", flush=True)
                if not ok:
                    continue
                for t in methods:
                    if (rid, t) in done:
                        continue
                    k = t.split("#")[-1]
                    fails = []
                    for i in range(1, RUNS + 1):
                        r = run.run_all([t] + ok, f"{k}_then_all_{i}")
                        fails.append({v for v in ok if r[v] == "failed"})
                    both = fails[0] & fails[1]
                    pol = "yes" if both else ("no" if not (fails[0] | fails[1]) else "mixed")
                    print(f"  {k}: {len(fails[0])}, {len(fails[1])} of {len(ok)} failed, both {len(both)} "
                          f"-> polluter {pol}", flush=True)
                    res.append({"experiment": exp, "id": rid, "zip": g["zip"],
                                "file": t.split("#")[0].split(".")[-1], "method": t, "victims": len(vic),
                                "victims_pass_baseline": len(ok), "failed_1": len(fails[0]),
                                "failed_2": len(fails[1]), "failed_both": len(both), "polluter": pol,
                                "pairwise_one_victim": old.get((rid, t), "")})
                    with open(out_csv, "w", newline="", encoding="utf-8") as f:
                        w = csv.DictWriter(f, FIELDS)
                        w.writeheader()
                        w.writerows(res)
    print("done", flush=True)


if __name__ == "__main__":
    main()

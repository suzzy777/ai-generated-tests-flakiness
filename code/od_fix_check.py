#!/usr/bin/env python3
import csv
import os
from pathlib import Path
import sys

H = Path(__file__).resolve().parent
sys.path.insert(0, str(H))
import flaky_check_basic as F
import collect_nonflaky as C
import od_confirm as O


class Args:
    pass


args = Args()
args.command = "od_confirm"
args.tests = H / "od_fix_check" / "fixed_tests.csv"
args.archive_dir = [H / "out_part1" / "extracted", H / "out_part2" / "extracted"]
args.only, args.test, args.offline, args.timeout = [], [], True, 1800
args.repo = (Path.home() / ".m2" / "repository").resolve()
args.out = H / "od_fix_check" / "out"
args.env = os.environ.copy()
if (args.out / "fix_check.csv").exists():
    sys.exit(f"{args.out / 'fix_check.csv'} exists already; not overwriting it")
args.out.mkdir(parents=True, exist_ok=True)

pairs = [p for p in csv.DictReader(open(H / "flaky_check_out" / "od_pairs_generated.csv", encoding="utf-8"))
         if p["side"] == "flaky" and p["role"] == "polluter"]
out = []
for rid, root, g, m, fix in F.load_tests(args):
    todo = [p for p in pairs if p["id"] == rid]
    print(f"{rid}: {len(todo)} victim(s)", flush=True)
    p = F.Prepared(rid, root, g, m, fix, args)
    with p:
        if p.build() in ("build_failed", "build_timeout"):
            sys.exit("build failed")
        run = O.Runner(p, args)
        for q in todo:
            v, pol = q["developer_test"], q["generated_test"]
            cls = pol.split("#")[0]
            short = v.rsplit(".", 1)[-1]
            rec = {"id": rid, "generated_test": pol, "victim": v,
                   "before_fix_alone": q["developer_test_alone"], "before_fix_after_polluter": q["developer_test_after_generated"]}
            rec["alone"] = run.run([v], "alone_" + short)
            rec["after_polluter"] = run.run([pol, v], "after_polluter_" + short)
            tests, _ = C.read_reports(p.module_dir)
            rec["polluter_itself"] = tests.get(pol, "missing")
            rec["after_whole_class"] = run.run([cls, v], "after_class_" + short)
            out.append(rec)
            print(f"  {short}: alone {rec['alone']}, after polluter {rec['after_polluter']} "
                  f"(polluter {rec['polluter_itself']}), after whole class {rec['after_whole_class']}", flush=True)
F.B.csv_write(args.out / "fix_check.csv", out, list(out[0].keys()))
fixed = sum(1 for r in out if r["alone"] == r["after_polluter"] == r["after_whole_class"] == "passed")
print(f"{fixed}/{len(out)} victims pass alone, after the polluter and after the whole class with the @After fix")

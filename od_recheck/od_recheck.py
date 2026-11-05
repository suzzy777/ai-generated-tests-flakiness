#!/usr/bin/env python3
import argparse
import csv
import os
from pathlib import Path
import shutil
import sys

H = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(H))
import flaky_check_basic as F
import od_confirm as O

C = F.C
FIELDS = ["id", "zip", "test", "candidates", "batch_failed_after", "confirmed_polluter", "confirm_1", "confirm_2",
          "alone", "category"]


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--name", required=True)
    a.add_argument("--od-confirm", type=Path, required=True)
    a.add_argument("--idflakies-out", type=Path, required=True)
    a.add_argument("--tests", type=Path, required=True)
    a.add_argument("--archive-dir", type=Path, action="append", required=True)
    a.add_argument("--out", type=Path, required=True)
    a.add_argument("--skip-zip", action="append", default=[])
    a.add_argument("--only", dest="only_ids", action="append", default=[], help="Only these row ids")
    a.add_argument("--maven-repo", dest="repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--timeout", type=int, default=3600)
    args = a.parse_args()
    args.command, args.offline, args.only = "od_confirm", True, []
    args.tests, args.out, args.repo = args.tests.resolve(), args.out.resolve(), args.repo.resolve()
    args.archive_dir = [d.resolve() for d in args.archive_dir]
    args.out.mkdir(parents=True, exist_ok=True)
    args.env = os.environ.copy()

    rejected = {}
    for r in csv.DictReader(open(args.od_confirm, encoding="utf-8")):
        if r["category"] == "not_by_generated" and r["alone"] == "passed" and r["generated"] != "true":
            if not any(z in r["zip"] for z in args.skip_zip) and (not args.only_ids or r["id"] in args.only_ids):
                rejected.setdefault(r["id"], []).append(r["test"])
    out_csv = args.out / "recheck.csv"
    done = {(r["id"], r["test"]): r for r in csv.DictReader(open(out_csv, encoding="utf-8"))} if out_csv.is_file() else {}
    rows = {x[0]: x for x in F.load_tests(args)}
    for rid, tests in sorted(rejected.items()):
        if rid not in rows:
            print(f"{rid}: not in --tests, skipped", flush=True)
            continue
        _, root, g, m, fix = rows[rid]
        p = F.Prepared(rid, root, g, m, fix, args)
        p.row_dir = args.out / "rows" / rid
        p.row_dir.mkdir(parents=True, exist_ok=True)
        dt = args.idflakies_out / "rows" / rid / "dtfixingtools"
        orders = {t: (ps, fl) for t, ps, fl in F.flagged_orders(dt)}
        cands = {t: O.candidates(t, orders[t][1], orders[t][0], p.fqcn) for t in tests if t in orders}
        todo = {t: c for t, c in cands.items() if len(c) >= 2 and (rid, t) not in done}
        print(f"{rid} {g['zip']}: {len(tests)} rejected, {sum(len(c) >= 2 for c in cands.values())} with >= 2 "
              f"generated candidates, {len(todo)} left", flush=True)
        if not todo:
            continue
        with p:
            if p.build() in ("build_failed", "build_timeout"):
                print("  build failed, skipped", flush=True)
                continue
            run = O.Runner(p, args)
            methods = sorted({x for c in todo.values() for x in c})
            failed_after = {t: [] for t in todo}
            for i, meth in enumerate(methods, 1):
                ts = [t for t, c in todo.items() if meth in c]
                run.run([meth] + ts, f"batch{i}")
                res, _ = C.read_reports(p.module_dir)
                fails = [t for t in ts if res.get(t) in O.FAILED]
                print(f"  batch {meth.split('#')[-1]} -> {len(ts)} victims: {len(fails)} failed", flush=True)
                for t in fails:
                    failed_after[t].append(meth)
            for t, c in todo.items():
                rec = dict(id=rid, zip=g["zip"], test=t, candidates=";".join(c),
                           batch_failed_after=";".join(failed_after[t]), category="not_by_generated")
                for meth in failed_after[t]:
                    tag = t.rsplit(".", 1)[-1][:60]
                    c1 = run.run([meth, t], f"confirm1_{tag}")
                    c2 = run.run([meth, t], f"confirm2_{tag}") if c1 in O.FAILED else ""
                    al = run.run([t], f"alone_{tag}") if c2 in O.FAILED else ""
                    rec.update(confirm_1=c1, confirm_2=c2, alone=al)
                    if c1 in O.FAILED and c2 in O.FAILED and al == "passed":
                        rec.update(confirmed_polluter=meth, category="victim")
                        break
                done[(rid, t)] = rec
                F.B.csv_write(out_csv, sorted(done.values(), key=lambda r: (r["id"], r["test"])), FIELDS)
            n = sum(1 for r in done.values() if r["id"] == rid and r["category"] == "victim")
            print(f"  {rid}: {n} confirmed victims", flush=True)
    print(f"all done ({args.name})", flush=True)


if __name__ == "__main__":
    main()

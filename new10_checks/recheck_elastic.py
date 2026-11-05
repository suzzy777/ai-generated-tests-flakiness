#!/usr/bin/env python3
import argparse
import csv
import os
import sys
from pathlib import Path

H = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(H))
import flaky_check_basic as F
import od_confirm as O

V = "io.elasticjob.lite.event.rdb.JobEventRdbSearchTest#"
CASES = [
    ("agent", "out_FCV_row_017", V + "assertFindJobStatusTraceEventsWithTime"),
    ("agent", "out_FCV_row_018", V + "assertFindJobExecutionEventsWithTime"),
    ("prompt", "out_default_row_017", V + "assertFindJobExecutionEventsWithTime"),
    ("prompt", "out_cleanup_row_018", V + "assertFindJobExecutionEventsWithTime"),
]
CONTROL = ["io.elasticjob.lite.util.env.IpUtilsTest#assertGetIp",
           "io.elasticjob.lite.internal.util.SensitiveInfoUtilsTest#assertFilterContentWithoutIp",
           "io.elasticjob.lite.internal.schedule.JobRegistryTest#assertRegisterJob"]
CSVS = {"prompt": H / "prompt_experiment" / "task_csvs" / "new10_tests.csv",
        "agent": H / "agent_experiment" / "task_csvs" / "new10_agent_tests.csv"}


def main():
    a = argparse.ArgumentParser()
    a.add_argument("--runs", type=int, default=10)
    a.add_argument("--control", action="store_true",
                   help="Instead: the victims after unrelated developer tests (timing vs. state), results_control.csv")
    x = a.parse_args()
    out = Path(__file__).resolve().parent / "recheck_elastic"
    out.mkdir(exist_ok=True)
    res = []
    cases = CASES
    if x.control:
        cases = [("agent", "out_FCV_row_017", V + "assertFindJobStatusTraceEventsWithTime"),
                 ("agent", "out_FCV_row_017", V + "assertFindJobExecutionEventsWithTime")]
    for exp, rid, victim in cases:
        args = argparse.Namespace(tests=CSVS[exp], archive_dir=[H / f"{exp}_experiment" / "extracted"],
                                  out=out, repo=Path.home() / ".m2" / "repository", offline=True,
                                  timeout=1800, only=[rid], command="od_confirm", env=os.environ.copy(),
                                  methods=[], redo=False)
        rows = [r for r in F.load_tests(args) if r[0] == rid]
        rid, root, g, m, fix = rows[0]
        dt = H / "new10_checks" / f"elasticjob_{exp}" / "idflakies" / "rows" / rid / "dtfixingtools"
        p = F.Prepared(rid, root, g, m, fix, args)
        orders = [(pa, fa) for t, pa, fa in F.flagged_orders(dt) if t == victim]
        cands = O.candidates(victim, orders[0][1], orders[0][0], p.fqcn)
        print(f"{rid}: {victim}, candidates {cands}", flush=True)
        with p:
            if p.build() in ("build_failed", "build_timeout"):
                print("  build failed")
                continue
            run = O.Runner(p, args)
            run.first = False
            todo = (("alone", [victim]), ("after_candidates", cands + [victim]))
            if x.control:
                todo = tuple((f"after_{c.split('.')[-1]}", [c, victim]) for c in CONTROL)
            for kind, order in todo:
                outs = [run.run(order, (victim.split("#")[1] + "_" if x.control else "") + f"{kind}_{i}") for i in range(1, x.runs + 1)]
                n_fail = sum(o == "failed" for o in outs)
                print(f"  {kind}: {n_fail}/{x.runs} failed ({','.join(outs)})", flush=True)
                res.append({"id": rid, "victim": victim, "order": kind, "candidates": ";".join(cands),
                            "runs": x.runs, "failed": n_fail, "outcomes": ";".join(outs)})
                with open(out / ("results_control.csv" if x.control else "results.csv"), "w", newline="") as f:
                    w = csv.DictWriter(f, list(res[0]))
                    w.writeheader()
                    w.writerows(res)
    print("done")


if __name__ == "__main__":
    main()

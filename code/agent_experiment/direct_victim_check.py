#!/usr/bin/env python3
import argparse
import csv
import os
import sys
from pathlib import Path

E = Path(__file__).resolve().parent
sys.path.insert(0, str(E.parent))
import flaky_check_basic as F
import od_confirm as O

POLLUTER = "net.sf.marineapi.nmea.parser.SentenceFactoryTest#testRegisterParserWithAlternativeBeginChar"
RUNS = 2


def main():
    out = E / "direct_victim_check"
    out.mkdir(exist_ok=True)
    res = []
    for c in ("FC", "FCV"):
        for n in (7, 8, 9):
            rid = f"out_{c}_row_{n:03d}"
            args = argparse.Namespace(tests=E / "task_csvs" / f"agent_{c}_tests.csv", archive_dir=[E / "extracted"],
                                      out=out, repo=Path.home() / ".m2" / "repository", offline=True,
                                      timeout=1800, only=[rid], command="od_confirm", env=os.environ.copy(),
                                      methods=[], redo=False)
            rows = [r for r in F.load_tests(args) if r[0] == rid]
            if not rows:
                print(f"{rid}: not in the test list")
                continue
            rid, root, g, m, fix = rows[0]
            p = F.Prepared(rid, root, g, m, fix, args)
            with p:
                if p.build() in ("build_failed", "build_timeout"):
                    print(f"{rid}: build failed")
                    continue
                run = O.Runner(p, args)
                for t in g["methods"]:
                    t = F.generated_name(t, p.fqcn) or t
                    k = t.split("#")[1]
                    alone = [run.run([t], f"{k}_alone_{i}") for i in range(1, RUNS + 1)]
                    after = [run.run([POLLUTER, t], f"{k}_after_{i}") for i in range(1, RUNS + 1)]
                    print(f"{rid} {k}: alone {','.join(alone)}; after polluter {','.join(after)}", flush=True)
                    res.append({"condition": c, "row": n, "id": rid, "test": t,
                                "alone": ";".join(alone), "after_polluter": ";".join(after)})
                    with open(out / "results.csv", "w", newline="") as f:
                        w = csv.DictWriter(f, list(res[0]))
                        w.writeheader()
                        w.writerows(res)
    print("done")


if __name__ == "__main__":
    main()

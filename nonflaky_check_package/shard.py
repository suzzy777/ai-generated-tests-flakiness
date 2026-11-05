#!/usr/bin/env python3
import argparse
import csv
from pathlib import Path
import shutil

HERE = Path(__file__).resolve().parent
IDF_MINUTES = {"elastic-job-lite-core": 12, "plugins/network-elements/tungsten": 7, "dubbo-rpc/dubbo-rpc-dubbo": 180,
               "components/camel-reactor": 15, "karate-core": 10, ".": 10, "scimono-client": 1, "crane4j-core": 1,
               "store": 1, "foundations/foundation-vertx": 1}


def split(runs, weight, n):
    shards = [[0, []] for _ in range(n)]
    for rid in sorted(runs, key=weight, reverse=True):
        s = min(shards, key=lambda x: x[0])
        s[0] += weight(rid)
        s[1].append(rid)
    return [s[1] for s in shards if s[1]]


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--tests", type=Path, required=True)
    a.add_argument("--idflakies-shards", type=int, default=2)
    a.add_argument("--rerun-shards", type=int, default=3)
    args = a.parse_args()
    runs = {}
    for r in csv.DictReader(open(args.tests, encoding="utf-8")):
        src = (HERE / r["test_file"]).resolve()
        run_dir = next((q for q in src.parents if q.name.startswith("row_")), src.parent)
        rid = f"{run_dir.parent.parent.name}_{run_dir.name}"
        x = runs.setdefault(rid, {"zip": r["zip"], "module": r["module"], "methods": 0})
        x["methods"] += 1

    jobs = [("nio", "nio", HERE / "extracted", list(runs)), ("nondex", "nondex", HERE / "par" / "nondex", list(runs))]
    for i, s in enumerate(split(runs, lambda rid: IDF_MINUTES.get(runs[rid]["module"], 5), args.idflakies_shards), 1):
        jobs.append((f"idflakies_{i}", "idflakies", HERE / "par" / f"idflakies_{i}", s))
    for i, s in enumerate(split(runs, lambda rid: runs[rid]["methods"], args.rerun_shards), 1):
        jobs.append((f"rerun_{i}", "rerun", HERE / "par" / f"rerun_{i}", s))

    for name, check, adir, rids in jobs:
        if adir != HERE / "extracted":
            adir.mkdir(parents=True, exist_ok=True)
            for z in sorted({runs[rid]["zip"] for rid in rids}):
                if not (adir / z).exists():
                    shutil.copytree(HERE / "extracted" / z, adir / z, symlinks=True)
        print(name, check, adir, *sorted(rids))


if __name__ == "__main__":
    main()

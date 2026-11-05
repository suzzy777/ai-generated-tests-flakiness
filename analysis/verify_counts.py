#!/usr/bin/env python3
import csv
import glob
from pathlib import Path

H = Path(__file__).resolve().parent.parent
E = H / "agent_experiment"
DIRS = {"A": [E / "out_flaky_A"], "F": [E / "out_flaky_F"], "FC": [E / "out_flaky_FC"],
        "FCV": [E / "out_flaky_FCV", H / "machine_C_FCVdubbo_results"]}
DIRECT = E / "direct_victim_check" / "results.csv"


def rd(p):
    return list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []


def files(dirs, pattern):
    out = []
    for d in dirs:
        out += glob.glob(str(d / pattern), recursive=True)
    return [f for f in out if ".empty_on_A" not in f and "SKIP_HERE" not in f]


def main():
    direct = {}
    for r in rd(DIRECT):
        if "passed" in r["alone"] and "failed" in r["after_polluter"]:
            direct.setdefault((r["condition"], int(r["row"])), True)
    for c, dirs in DIRS.items():
        gen = {int(g["row_number"]): g for g in rd(E / f"out_{c}" / "generation_results.csv")}
        broken, seen = set(), set()
        for f in files(dirs, "**/rerun/runs.csv"):
            runs = rd(f)
            if not runs:
                continue
            k = next(x for x in ("outcome", "status", "result") if x in runs[0])
            t = next(x for x in ("test", "method", "generated_test") if x in runs[0])
            by = {}
            for x in runs:
                by.setdefault((x["id"], x[t]), set()).add(x[k])
                seen.add(x["id"])
            broken |= {i for (i, _), s in by.items() if "passed" not in s}
        pol, vic = set(), set()
        for f in files(dirs, "**/od_confirm/od_confirm.csv"):
            for x in rd(f):
                if x["category"] == "victim" and x["generated"] != "true" and x["polluter_generated"] == "true":
                    pol.add(x["id"])
                if x["category"] == "victim" and x["generated"] == "true":
                    vic.add(x["id"])
        for f in files(dirs, "**/od_recheck/recheck.csv"):
            for x in rd(f):
                if x.get("category") == "victim":
                    pol.add(x["id"])
        out = {"polluter": [], "victim": [], "clean": [], "unusable": []}
        for n in range(1, 16):
            i = f"out_{c}_row_{n:03d}"
            g = gen.get(n)
            if not g or g["status"] != "generated" or i not in seen or i in broken:
                out["unusable"].append(n)
            elif i in pol:
                out["polluter"].append(n)
            elif i in vic or direct.get((c, n)) or (c == "F" and n in (7, 8)):
                out["victim"].append(n)
            else:
                out["clean"].append(n)
        print(c, {k: f"{len(v)} {v}" for k, v in out.items()})


if __name__ == "__main__":
    main()

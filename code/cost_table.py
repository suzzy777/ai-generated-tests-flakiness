#!/usr/bin/env python3
import csv
import glob
import json
from pathlib import Path

H = Path(__file__).resolve().parent
IN, CACHED, OUT = 2.50, 1.25, 10.00
USABLE = {"ChatUniTest default": 8 + 12, "ChatUniTest cleanup": 12 + 9, "A": 13 + 12, "F": 11 + 12,
          "FC": 12 + 10, "FCV": 9 + 12}


def chatunitest(v):
    runs = []
    for run in sorted(glob.glob(str(H / f"prompt_experiment/out_{v}/runs/row_*"))):
        p = c = n = 0
        for f in glob.glob(f"{run}/tmp/**/records.json", recursive=True):
            for r in json.load(open(f)):
                p += int(r.get("promptToken") or 0)
                c += int(r.get("responseToken") or 0)
                n += 1
        runs.append((p, c, 0, n, (p * IN + c * OUT) / 1e6))
    return runs


def agent(cond):
    runs = []
    for f in sorted(glob.glob(str(H / f"agent_experiment/out_{cond}/runs/row_*/trajectory.json"))):
        t = json.load(open(f))
        p = c = ca = n = 0
        for m in t["messages"]:
            u = ((m.get("extra") or {}).get("response") or {}).get("usage")
            if u:
                p += u["prompt_tokens"]
                c += u["completion_tokens"]
                ca += (u.get("prompt_tokens_details") or {}).get("cached_tokens", 0)
                n += 1
        runs.append((p, c, ca, n, t["info"]["model_stats"]["instance_cost"]))
    return runs


def main():
    rows = []
    for name, runs in [("ChatUniTest default", chatunitest("default")), ("ChatUniTest cleanup", chatunitest("cleanup"))] + \
                      [(c, agent(c)) for c in ("A", "F", "FC", "FCV")]:
        k = len(runs)
        cost = sum(r[4] for r in runs)
        rows.append({"condition": name, "runs": k, "api_calls_per_run": round(sum(r[3] for r in runs) / k, 1),
                     "input_tokens_per_run": round(sum(r[0] for r in runs) / k),
                     "cached_input_per_run": round(sum(r[2] for r in runs) / k) if name[0] != "C" else "not recorded",
                     "output_tokens_per_run": round(sum(r[1] for r in runs) / k),
                     "cost_total_usd": round(cost, 2), "cost_per_run_usd": round(cost / k, 3),
                     "usable_runs": USABLE[name], "cost_per_usable_run_usd": round(cost / USABLE[name], 3),
                     "cost_basis": "list price, upper bound" if name[0] == "C" else "recorded (cache discount)"})
    with open(H / "cost_table.csv", "w", newline="") as f:
        w = csv.DictWriter(f, list(rows[0]))
        w.writeheader()
        w.writerows(rows)
    for r in rows:
        print(r)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
import csv
import shutil
import sys
from pathlib import Path

H = Path(__file__).resolve().parent
SIDES = [("flaky", H / "flaky_check_out" / "od_confirm" / "od_confirm.csv"),
         ("nonflaky", H / "nonflaky_check_package" / "flaky_check_out" / "od_confirm" / "od_confirm.csv")]


def load(path):
    rows = list(csv.DictReader(open(path, encoding="utf-8")))
    return rows, list(rows[0].keys()) if rows else []


def save(path, rows, fields):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fields)
        w.writeheader()
        w.writerows(rows)


if "--drop-inconclusive" in sys.argv:
    for side, path in SIDES:
        rows, fields = load(path)
        shutil.copy2(path, str(path) + ".before_redo")
        keep = [r for r in rows if r["category"] != "inconclusive"]
        save(path, keep, fields)
        print(f"{side}: dropped {len(rows) - len(keep)} inconclusive line(s) for the redo")
    sys.exit()

all_rows = []
for side, path in SIDES:
    rows, fields = load(path)
    for r in rows:
        if r["category"] == "not_by_generated" and not r["with_candidates"] and "also after" in r["note"]:
            victim = r["alone"] == "passed"
            r["note"] = (f"T {'passed' if victim else 'failed'} alone; no generated test ran before it only in "
                         f"iDFlakies' {'failing' if victim else 'passing'} order")
    save(path, rows, fields)
    all_rows += [dict(side=side, **r) for r in rows]
fields = ["side"] + fields
save(H / "flaky_check_out" / "od_confirm_all.csv", all_rows, fields)
pairs = [r for r in all_rows if r["category"] in ("victim", "brittle")]
pf = ["side", "id", "zip", "module", "victim", "polluter", "brittle", "state_setter", "polluter_generated",
      "state_setter_generated", "generated", "alone", "with_candidates", "repeat", "full_order", "note"]
save(H / "flaky_check_out" / "od_pairs.csv", [{k: r.get(k, "") for k in pf} for r in pairs], pf)
from collections import Counter
for side in ("flaky", "nonflaky"):
    c = Counter(r["category"] for r in all_rows if r["side"] == side)
    print(side, dict(c))
print(f"{len(pairs)} victim/brittle line(s) in flaky_check_out/od_pairs.csv")

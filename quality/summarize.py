#!/usr/bin/env python3
import csv
from collections import defaultdict
from pathlib import Path

Q = Path(__file__).resolve().parent
GROUPS = ["developer", "current (main study)", "current", "cleanup", "agent A", "agent F"]
SUBJECT = {"ChangeTelnetHandler#telnet": "dubbo ChangeTelnetHandler", "PortTelnetHandler#telnet": "dubbo PortTelnetHandler",
           "AISMessageFactory#create": "marineapi", "LoggerFactory#setLogBackendType": "ormlite",
           "GroupRequest#createGroup": "scimono"}


def group(r):
    if r["set"] == "developer":
        return "developer"
    if r["set"] == "main":
        return "current (main study)"
    if r["set"] == "prompt":
        return "current" if "out_default_" in r["rid"] else "cleanup"
    return {"agentA": "agent A", "agentF": "agent F"}[r["set"]]


def pct(a, b):
    return 100.0 * a / b if b else None


def load(g):
    out = {}
    for f in sorted(Q.glob(f"out_{g}/*/results.csv")):
        for r in csv.DictReader(open(f, encoding="utf-8")):
            out[r["id"]] = r
    return out


def mean(xs):
    xs = [x for x in xs if x is not None]
    return sum(xs) / len(xs) if xs else None


def fmt(x):
    return "-" if x is None else f"{x:.0f}%"


def main():
    st, al = load("stronger"), load("all")
    rows, unstable = [], []
    for i, r in st.items():
        a = al.get(i, {})
        if a and r["status"] == a.get("status") == "done":
            if any(r[k] != a[k] for k in ("line_covered", "line_total", "branch_covered", "branch_total")):
                unstable.append(i)
        subj = SUBJECT[r["focal_method"].rsplit(".", 1)[-1]]
        rows.append(dict(id=i, subject=subj, group=group(r), status=r["status"],
                         line=pct(int(r["line_covered"] or 0), int(r["line_total"] or 0)) if r["line_total"] else None,
                         branch=pct(int(r["branch_covered"] or 0), int(r["branch_total"] or 0)) if r["branch_total"] not in ("", "0") else None,
                         mut_stronger=pct(int(r["mut_killed"] or 0), int(r["mut_total"] or 0)) if r["mut_total"] not in ("", "0") else None,
                         mut_all=pct(int(a.get("mut_killed") or 0), int(a.get("mut_total") or 0)) if a.get("mut_total") not in (None, "", "0") else None,
                         mutants_stronger=r["mut_total"], mutants_all=a.get("mut_total", "")))
    with open(Q / "summary_items.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)

    agg = defaultdict(list)
    for r in rows:
        if r["status"] == "done":
            agg[(r["subject"], r["group"])].append(r)
    lines = ["# Coverage and mutation of the focal method (generated vs developer)", "",
             "Mean over each group's test files that pass alone (n = files). Focal method only.",
             "Mutation score = killed / mutants inside the focal method; mutants per method in brackets.", "",
             "| Subject | Group | n | Line cov | Branch cov | Mutation STRONGER | Mutation ALL |",
             "|---|---|---|---|---|---|---|"]
    for subj in dict.fromkeys(SUBJECT.values()):
        for g in GROUPS:
            xs = agg.get((subj, g))
            if not xs:
                continue
            ms = sorted({x["mutants_stronger"] for x in xs})
            ma = sorted({x["mutants_all"] for x in xs})
            lines.append(f"| {subj} | {g} | {len(xs)} | {fmt(mean(x['line'] for x in xs))} | "
                         f"{fmt(mean(x['branch'] for x in xs))} | {fmt(mean(x['mut_stronger'] for x in xs))} ({'/'.join(ms)}) | "
                         f"{fmt(mean(x['mut_all'] for x in xs))} ({'/'.join(ma)}) |")
    left = [r for r in rows if r["status"] != "done"]
    lines += ["", f"Left out (fail alone): {', '.join(r['id'] for r in left) or 'none'}.",
              f"JaCoCo counts identical in both runs of every item: {'yes' if not unstable else 'NO: ' + ', '.join(unstable)}."]
    (Q / "SUMMARY.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))


if __name__ == "__main__":
    main()

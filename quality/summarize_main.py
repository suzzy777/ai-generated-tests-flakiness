#!/usr/bin/env python3
import csv
from collections import defaultdict
from pathlib import Path

Q = Path(__file__).resolve().parent
SIDE = {"main_flaky": "generated, flaky side", "main_nonflaky": "generated, non-flaky side", "developer": "developer"}
COVERED = ("done", "pit_not_green")
METRICS = [("line", "Line cov"), ("branch", "Branch cov"), ("mut_stronger", "Mutation STRONGER"), ("mut_all", "Mutation ALL")]


def load(g):
    return {r["id"]: r for f in sorted(Q.glob(f"out_main_{g}/*/results.csv")) for r in csv.DictReader(open(f, encoding="utf-8"))}


def pct(a, b):
    return 100.0 * int(a) / int(b) if b not in (None, "", "0") else None


def mean(xs):
    xs = [x for x in xs if x is not None]
    return sum(xs) / len(xs) if xs else None


def fmt(x, n=None):
    s = "-" if x is None else f"{x:.0f}%"
    return s if n is None else f"{s} ({n})"


def project(z):
    return z.split("=")[0].replace("shardingsphereelasticjobelasticjoblitecore", "shardingsphere ")


def main():
    st, al = load("stronger"), load("all")
    rows, unstable = [], []
    for i, r in st.items():
        a = al.get(i, {})
        if "developer_not_passing" in (r["status"], a.get("status")) or not (r["status"] in COVERED and a.get("status") in COVERED):
            rows.append(dict(id=i, side=SIDE[r["set"]], project=project(r["zip"]), zip=r["zip"], focal=r["focal_method"],
                             status=r["status"] if r["status"] not in COVERED else a.get("status", ""), line=None,
                             branch=None, mut_stronger=None, mut_all=None, mutants_stronger="", mutants_all=""))
            continue
        if any(r[k] != a[k] for k in ("line_covered", "line_total", "branch_covered", "branch_total")):
            unstable.append(i)
        rows.append(dict(id=i, side=SIDE[r["set"]], project=project(r["zip"]), zip=r["zip"], focal=r["focal_method"],
                         status="done" if r["status"] == a["status"] == "done" else "pit_not_green",
                         line=pct(r["line_covered"], r["line_total"]), branch=pct(r["branch_covered"], r["branch_total"]),
                         mut_stronger=pct(r["mut_killed"], r["mut_total"]), mut_all=pct(a["mut_killed"], a["mut_total"]),
                         mutants_stronger=r["mut_total"], mutants_all=a["mut_total"]))
    with open(Q / "summary_main_items.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, list(rows[0].keys()))
        w.writeheader()
        w.writerows(sorted(rows, key=lambda x: (x["project"], x["focal"], x["side"])))
    ok = [r for r in rows if r["status"] in COVERED]

    L = ["# Main study: coverage and mutation of the focal method, generated vs developer tests", "",
         "Focal method only; one test class per run. Values are means over test files (n in brackets).",
         "Mutation score = killed / mutants in the focal method, over files whose focal method has at least 1 mutant.", ""]

    L += ["## 1. All files", "", "| Tests | Files | " + " | ".join(h for _, h in METRICS) + " |", "|---|---|" + "---|" * len(METRICS)]
    for s in SIDE.values():
        xs = [r for r in ok if r["side"] == s]
        L.append(f"| {s} | {len(xs)} | " + " | ".join(fmt(mean(x[k] for x in xs), sum(x[k] is not None for x in xs))
                                                     for k, _ in METRICS) + " |")

    gen, dev = defaultdict(list), {}
    for r in ok:
        if r["side"] == "developer":
            dev[(r["zip"], r["focal"])] = r
        else:
            gen[(r["zip"], r["focal"])].append(r)
    pairs = [(k, gen[k], dev[k]) for k in sorted(dev) if k in gen]
    L += ["", "## 2. Paired: same focal method, generated vs developer", "",
          f"{len(pairs)} focal methods with both a generated file and a developer class measured. Per metric, only the pairs "
          "where both sides have a value; generated = mean of that method's generated files.", "",
          "| Metric | Pairs | Generated | Developer | Generated higher | Same | Developer higher |", "|---|---|---|---|---|---|---|"]
    per_project = defaultdict(lambda: defaultdict(list))
    for k, h in METRICS:
        g_, d_, win = [], [], [0, 0, 0]
        for key, gs, d in pairs:
            gv = mean(x[k] for x in gs)
            if gv is None or d[k] is None:
                continue
            g_.append(gv)
            d_.append(d[k])
            win[0 if gv > d[k] + 0.5 else 2 if d[k] > gv + 0.5 else 1] += 1
            per_project[d["project"]][k].append((gv, d[k]))
        L.append(f"| {h} | {len(g_)} | {fmt(mean(g_))} | {fmt(mean(d_))} | {win[0]} | {win[1]} | {win[2]} |")

    L += ["", "## 3. Paired, per project (generated / developer, pairs in brackets)", "",
          "| Project | " + " | ".join(h for _, h in METRICS) + " |", "|---|" + "---|" * len(METRICS)]
    for p in sorted(per_project):
        cells = []
        for k, _ in METRICS:
            v = per_project[p][k]
            cells.append(f"{fmt(mean(a for a, _ in v))} / {fmt(mean(b for _, b in v))} ({len(v)})" if v else "-")
        L.append(f"| {p} | " + " | ".join(cells) + " |")

    left = [r for r in rows if r["status"] not in COVERED]
    nomut = [r for r in ok if r["status"] == "pit_not_green"]
    L += ["", "## Notes", "",
          f"- Not in the tables at all ({len(left)}): " + ", ".join(f"{r['id']} ({r['status']})" for r in left) + ".",
          f"- Coverage only, no mutation score (a test fails in PIT's own run, `pit_not_green`; {len(nomut)}): "
          + ", ".join(r["id"] for r in nomut) + ".",
          f"- {sum(1 for k in gen if k not in dev)} focal methods have generated files but no developer item "
          "(in sections 1 only).",
          "- Not run yet: dubbo (after the agent dubbo flaky check) and hadoop (YARN-9405, too slow).",
          f"- JaCoCo counts identical in the STRONGER and ALL runs of every item: "
          f"{'yes' if not unstable else 'NO: ' + ', '.join(unstable)}."]
    (Q / "SUMMARY_MAIN.md").write_text("\n".join(L) + "\n", encoding="utf-8")
    print("\n".join(L))


if __name__ == "__main__":
    main()

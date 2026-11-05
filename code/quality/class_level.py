#!/usr/bin/env python3
import csv
import xml.etree.ElementTree as ET
from pathlib import Path

Q = Path(__file__).resolve().parent
GROUPS = ["stronger", "all", "new_stronger", "new_all", "main_stronger", "main_all"]
FIELDS = ["id", "set", "zip", "focal_method", "mutators", "results", "status",
          "line_covered", "line_total", "branch_covered", "branch_total", "mut_killed", "mut_total",
          "class_line_covered", "class_line_total", "class_branch_covered", "class_branch_total",
          "class_mut_killed", "class_mut_total", "class_mut_survived", "class_mut_no_coverage", "class_mut_timed_out"]


def in_class(name, cls):
    return name == cls or name.startswith(cls + "$")


def jacoco_class(xml_path, cls):
    want = cls.replace(".", "/")
    lc = lt = bc = bt = 0
    for c in ET.parse(xml_path).getroot().iter("class"):
        if not in_class(c.get("name", ""), want):
            continue
        for ctr in c.findall("counter"):
            cov, mis = int(ctr.get("covered")), int(ctr.get("missed"))
            if ctr.get("type") == "LINE":
                lc, lt = lc + cov, lt + cov + mis
            elif ctr.get("type") == "BRANCH":
                bc, bt = bc + cov, bt + cov + mis
    return lc, lt, bc, bt


def pit_class(xml_path, cls):
    n = {"KILLED": 0, "SURVIVED": 0, "NO_COVERAGE": 0, "TIMED_OUT": 0, "total": 0}
    for mu in ET.parse(xml_path).getroot().iter("mutation"):
        if not in_class(mu.findtext("mutatedClass") or "", cls):
            continue
        n["total"] += 1
        s = mu.get("status", "")
        if s in n:
            n[s] += 1
    return n


def main():
    out = []
    for g in GROUPS:
        for res in sorted(Q.glob(f"out_{g}/*/results.csv")):
            for r in csv.DictReader(open(res, encoding="utf-8")):
                cls = r["focal_method"].split("#")[0]
                d = res.parent / "rows" / r["id"]
                rec = {k: r.get(k, "") for k in FIELDS}
                rec.update(mutators=g.split("_")[-1].upper(), results=str(res.relative_to(Q)))
                if (d / "jacoco.xml").is_file():
                    lc, lt, bc, bt = jacoco_class(d / "jacoco.xml", cls)
                    rec.update(class_line_covered=lc, class_line_total=lt, class_branch_covered=bc,
                               class_branch_total=bt)
                if (d / "mutations.xml").is_file() and r["status"] == "done":
                    n = pit_class(d / "mutations.xml", cls)
                    rec.update(class_mut_killed=n["KILLED"], class_mut_total=n["total"],
                               class_mut_survived=n["SURVIVED"], class_mut_no_coverage=n["NO_COVERAGE"],
                               class_mut_timed_out=n["TIMED_OUT"])
                out.append(rec)
    with open(Q / "class_level.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, FIELDS)
        w.writeheader()
        w.writerows(out)
    print(f"{len(out)} rows -> {Q / 'class_level.csv'}")


if __name__ == "__main__":
    main()

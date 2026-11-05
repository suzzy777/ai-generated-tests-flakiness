#!/usr/bin/env python3
import csv
import xml.etree.ElementTree as ET
from pathlib import Path

Q = Path(__file__).resolve().parent
CLS = "com.j256.ormlite.table.SchemaUtils"
DESC = "(Lcom/j256/ormlite/support/ConnectionSource;Ljava/lang/String;)I"
HELPERS = {"doCreateSchema", "addCreateSchemaStatements", "doStatements", "doCreateTestQueries"}


def wanted(name, desc):
    return name in HELPERS or (name == "createSchema" and desc == DESC)


def main():
    out = []
    for g in ("stronger", "all", "new_stronger", "new_all"):
        for res in sorted(Q.glob(f"out_{g}/*/results.csv")):
            for r in csv.DictReader(open(res, encoding="utf-8")):
                if r["focal_method"] != CLS + "#createSchema":
                    continue
                d = res.parent / "rows" / r["id"]
                rec = {"id": r["id"], "set": r["set"], "mutators": g.split("_")[-1].upper(), "status": r["status"],
                       "line_covered": 0, "line_total": 0, "branch_covered": 0, "branch_total": 0,
                       "selected_overload_lines": "", "mut_killed": "", "mut_total": ""}
                for c in ET.parse(d / "jacoco.xml").getroot().iter("class"):
                    if c.get("name") != CLS.replace(".", "/"):
                        continue
                    for m in c.findall("method"):
                        if not wanted(m.get("name"), m.get("desc")):
                            continue
                        for t in m.findall("counter"):
                            cov, mis = int(t.get("covered")), int(t.get("missed"))
                            if t.get("type") == "LINE":
                                rec["line_covered"] += cov
                                rec["line_total"] += cov + mis
                                if m.get("name") == "createSchema":
                                    rec["selected_overload_lines"] = f"{cov}/{cov + mis}"
                            elif t.get("type") == "BRANCH":
                                rec["branch_covered"] += cov
                                rec["branch_total"] += cov + mis
                if r["status"] == "done" and (d / "mutations.xml").is_file():
                    k = n = 0
                    for mu in ET.parse(d / "mutations.xml").getroot().iter("mutation"):
                        if mu.findtext("mutatedClass") != CLS or not wanted(mu.findtext("mutatedMethod"),
                                                                           mu.findtext("methodDescription")):
                            continue
                        n += 1
                        k += mu.get("status") == "KILLED"
                    rec.update(mut_killed=k, mut_total=n)
                out.append(rec)
    with open(Q / "createschema_selected.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, list(out[0]))
        w.writeheader()
        w.writerows(out)
    for r in out:
        print(f"{r['mutators']:8} {r['id'][:58]:58} overload {r['selected_overload_lines']:5} "
              f"lines {r['line_covered']}/{r['line_total']} branches {r['branch_covered']}/{r['branch_total']} "
              f"killed {r['mut_killed']}/{r['mut_total']}")


if __name__ == "__main__":
    main()

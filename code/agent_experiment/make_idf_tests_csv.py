#!/usr/bin/env python3
import csv
from pathlib import Path
import re
import sys

E = Path(__file__).resolve().parent
H = E.parent
FIELDS = ["project", "sha", "module", "generated_test", "test_file", "focal_method", "zip", "java",
          "junit", "mockito_fix", "focal_from_test", "focal_from_test_type"]
TEST = re.compile(r"@Test\b[^{;]*?\bvoid\s+(\w+)\s*\(")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)


def main():
    cond = sys.argv[1]
    proj = {}
    for r in csv.DictReader(open(H / "task_csvs" / "flaky_generated_tests.csv", encoding="utf-8")):
        proj.setdefault(r["zip"], r)
    man = {r["row_number"]: r for r in csv.DictReader(open(E / "manifest.csv", encoding="utf-8"))}
    out = []
    for g in csv.DictReader(open(E / f"out_{cond}" / "generation_results.csv", encoding="utf-8")):
        if g["status"] != "generated":
            continue
        rn = g["row_number"]
        p, m = proj[g["zip"]], man[rn]
        for f in sorted((E / f"out_{cond}" / "runs" / f"row_{int(rn):03d}" / "tests").rglob("*.java")):
            src = f.read_text(encoding="utf-8", errors="replace")
            pkg = PACKAGE.search(src)
            fqcn = (pkg.group(1) + "." if pkg else "") + f.stem
            for name in TEST.findall(src):
                out.append({"project": p["project"], "sha": p["sha"], "module": p["module"],
                            "generated_test": f"{fqcn}#{name}", "test_file": str(f.relative_to(H)),
                            "focal_method": m["selected_method"], "zip": g["zip"], "java": p["java"],
                            "junit": m["junit"], "mockito_fix": "false", "focal_from_test": m["flaky_test"],
                            "focal_from_test_type": f"agent_{cond}"})
    (E / "task_csvs").mkdir(exist_ok=True)
    dest = E / "task_csvs" / f"agent_{cond}_tests.csv"
    with open(dest, "w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, FIELDS)
        w.writeheader()
        w.writerows(out)
    print(f"{dest}: {len(out)} test methods, {len({r['test_file'] for r in out})} files")


if __name__ == "__main__":
    main()

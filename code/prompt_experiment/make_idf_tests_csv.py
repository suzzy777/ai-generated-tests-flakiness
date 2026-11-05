#!/usr/bin/env python3
import csv
from pathlib import Path

E = Path(__file__).resolve().parent
H = E.parent
FIELDS = ["project", "sha", "module", "generated_test", "test_file", "focal_method", "zip", "java",
          "junit", "mockito_fix", "focal_from_test", "focal_from_test_type"]


def read(p):
    return list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []


def main():
    proj = {}
    for r in read(H / "task_csvs" / "flaky_generated_tests.csv"):
        proj.setdefault(r["zip"], r)
    man = {r["row_number"]: r for r in read(E / "manifest.csv")}
    out = []
    for v in ("default", "cleanup"):
        for name, fix in ((f"replay_{v}", False), (f"replay_{v}_fix", True)):
            passed = {r["id"] for r in read(E / name / "results.csv") if r["status"] == "passed"}
            if not fix:
                done = passed
            else:
                passed -= done
            for m in read(E / name / "methods.csv"):
                if m["id"] not in passed:
                    continue
                rn = str(int(m["id"].rsplit("_", 1)[-1]))
                cls = m["test"].split("#")[0].rsplit(".", 1)[-1]
                files = sorted((E / f"out_{v}" / "runs" / f"row_{int(rn):03d}" / "tests").rglob(cls + ".java"))
                p, g = proj[m["zip"]], man[rn]
                out.append({"project": p["project"], "sha": p["sha"], "module": p["module"],
                            "generated_test": m["test"], "test_file": str(files[0].relative_to(H)),
                            "focal_method": m["focal"], "zip": m["zip"], "java": p["java"], "junit": g["junit"],
                            "mockito_fix": str(fix).lower(), "focal_from_test": g["flaky_test"],
                            "focal_from_test_type": f"experiment_{v}"})
    (E / "task_csvs").mkdir(exist_ok=True)
    with open(E / "task_csvs" / "experiment_generated_tests.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, FIELDS)
        w.writeheader()
        w.writerows(out)
    print(f"{len(out)} generated test methods, {len({(r['test_file']) for r in out})} files")


if __name__ == "__main__":
    main()

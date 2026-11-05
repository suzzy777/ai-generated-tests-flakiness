#!/usr/bin/env python3
import argparse
import csv
from pathlib import Path

HERE = Path(__file__).resolve().parent
FIELDS = ["project", "sha", "module", "generated_test", "test_file", "focal_method", "zip", "java",
          "junit", "mockito_fix", "focal_from_test", "focal_from_test_type"]


def read(name):
    p = HERE / name
    return list(csv.DictReader(open(p, encoding="utf-8"))) if p.is_file() else []


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--not-passed", action="store_true")
    args = a.parse_args()
    plain = {r["id"]: r for r in read("genreplay_out/results.csv")}
    if args.not_passed:
        print(" ".join(sorted(k for k, r in plain.items() if r["status"] != "passed")))
        return
    fix = {r["id"]: r for r in read("genreplay_fix_out/results.csv")}
    methods = {}
    for name in ("genreplay_out/methods.csv", "genreplay_fix_out/methods.csv"):
        for r in read(name):
            methods.setdefault((name.split("/")[0], r["id"]), []).append(r)
    man = {r["row_number"]: r for r in read("task_csvs/nonflaky_generate_sample.csv")}

    out = []
    for rid in sorted(set(plain) | set(fix)):
        if plain.get(rid, {}).get("status") == "passed":
            res, src, mockito_fix = plain[rid], "genreplay_out", "false"
        elif fix.get(rid, {}).get("status") == "passed":
            res, src, mockito_fix = fix[rid], "genreplay_fix_out", "true"
        else:
            continue
        m = man[res["row_number"]]
        tests_dir = HERE / "runs" / f"row_{int(res['row_number']):03d}" / "tests"
        for t in methods.get((src, rid), []):
            cls = t["test"].split("#")[0].rsplit(".", 1)[-1]
            f = next(tests_dir.rglob(cls + ".java"))
            out.append({"project": m["project"], "sha": m["flaky_commit"], "module": m["module"],
                        "generated_test": t["test"], "test_file": str(f.relative_to(HERE)),
                        "focal_method": res["focal"], "zip": res["zip"], "java": m["java"],
                        "junit": res["junit"], "mockito_fix": mockito_fix,
                        "focal_from_test": m["test"], "focal_from_test_type": "nonflaky"})
    dest = HERE / "task_csvs" / "nonflaky_generated_tests.csv"
    with open(dest, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, FIELDS)
        w.writeheader()
        w.writerows(out)
    print(f"{len(out)} test methods in {len({r['test_file'] for r in out})} files -> {dest}")


if __name__ == "__main__":
    main()

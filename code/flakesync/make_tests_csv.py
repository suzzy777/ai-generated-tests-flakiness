#!/usr/bin/env python3
import argparse
import csv
import hashlib
import re
import shutil
import sys
from collections import Counter
from pathlib import Path

OUT_COLS = ["project", "sha", "module", "generated_test", "test_file", "focal_method", "zip", "java", "junit",
            "mockito_fix", "focal_from_test", "focal_from_test_type", "file_here"]
INFO_COLS = ["row", "project", "sha", "module", "zip", "java", "junit", "test_file", "test_class", "test_method",
             "focal_method", "focal_from_test"]


def md5(p):
    return hashlib.md5(Path(p).read_bytes()).hexdigest()


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("src", type=Path, help="e.g. .../flaky_checked_tests_new10 (read only)")
    ap.add_argument("dst", type=Path, help="NEW folder to create (must not exist)")
    ap.add_argument("--mockito-csv", type=Path)
    ap.add_argument("--assume-mockito-fix", choices=["true", "false"])
    a = ap.parse_args()

    if bool(a.mockito_csv) == bool(a.assume_mockito_fix):
        sys.exit("ERROR: give exactly one of --mockito-csv FILE or --assume-mockito-fix true|false")
    src, dst = a.src.resolve(), a.dst.resolve()
    if not src.is_dir():
        sys.exit(f"ERROR: source folder not found: {src}")
    if dst.exists():
        sys.exit(f"ERROR: {dst} already exists. Refusing to touch it. Pick a new name.")
    if dst == src or src in dst.parents or dst in src.parents:
        sys.exit("ERROR: dst must be a separate folder, not inside / around src")

    folders = sorted(p for p in src.iterdir() if p.is_dir() and (p / "tests_info.csv").is_file())
    if not folders:
        sys.exit(f"ERROR: no <folder>/tests_info.csv under {src}")

    fixmap = {}
    if a.mockito_csv:
        for r in csv.DictReader(open(a.mockito_csv, newline="", encoding="utf-8")):
            for c in ("zip", "generated_test", "mockito_fix"):
                if c not in r:
                    sys.exit(f"ERROR: {a.mockito_csv} has no column {c!r}")
            v = r["mockito_fix"].strip().lower()
            if v not in ("true", "false"):
                sys.exit(f"ERROR: mockito_fix={r['mockito_fix']!r} in {a.mockito_csv}")
            k = (r["zip"].strip(), r["generated_test"].strip())
            if k in fixmap and fixmap[k] != v:
                sys.exit(f"ERROR: {a.mockito_csv} gives two different mockito_fix values for {k}")
            fixmap[k] = v

    problems, plan, seen_fix = [], [], Counter()
    for d in folders:
        with open(d / "tests_info.csv", newline="", encoding="utf-8") as fh:
            rd = csv.DictReader(fh)
            if rd.fieldnames != INFO_COLS:
                problems.append(f"{d.name}/tests_info.csv: unexpected columns {rd.fieldnames}")
                continue
            rows = list(rd)
        for n, r in enumerate(rows, 2):
            where = f"{d.name}/tests_info.csv line {n}"
            r = {k: (v or "").strip() for k, v in r.items()}
            if any(not r[c] for c in INFO_COLS):
                problems.append(f"{where}: empty column(s) {[c for c in INFO_COLS if not r[c]]}")
                continue
            f = d / r["test_file"]
            if not f.is_file() or d.resolve() not in f.resolve().parents or f.suffix != ".java":
                problems.append(f"{where}: test file missing/outside folder: {r['test_file']}")
                continue
            text = f.read_text(encoding="utf-8", errors="replace")
            pk = re.search(r"^\s*package\s+([\w.]+)\s*;", text, re.M)
            fq = (pk.group(1) + "." if pk else "") + f.stem
            if r["test_class"] != fq:
                problems.append(f"{where}: test_class {r['test_class']} != package/class of file ({fq})")
            gen = f"{r['test_class']}#{r['test_method']}"
            if a.mockito_csv:
                fix = fixmap.get((r["zip"], gen))
                if fix is None:
                    problems.append(f"{where}: {r['zip']} {gen} not found in {a.mockito_csv}")
                    continue
            else:
                fix = a.assume_mockito_fix
            seen_fix[(d.name, fix)] += 1
            plan.append((d, {
                "project": r["project"], "sha": r["sha"], "module": r["module"], "generated_test": gen,
                "test_file": "", "focal_method": r["focal_method"], "zip": r["zip"], "java": r["java"],
                "junit": r["junit"], "mockito_fix": fix, "focal_from_test": r["focal_from_test"],
                "focal_from_test_type": re.sub(r"_\d+$", "", d.name), "file_here": r["test_file"]}))
    if problems:
        print(f"{len(problems)} PROBLEM(S) - nothing was written. First {min(len(problems), 30)}:")
        for p in problems[:30]:
            print("  ", p)
        sys.exit(1)

    dst.mkdir(parents=True)
    n_files = 0
    for d in folders:
        out = dst / d.name
        out.mkdir()
        wanted = sorted({r["file_here"] for dd, r in plan if dd == d})
        for rel in wanted:
            tgt = out / rel
            tgt.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(d / rel, tgt)
            if md5(d / rel) != md5(tgt):
                sys.exit(f"ERROR: copy differs: {rel}")
            n_files += 1
        with open(out / "tests.csv", "w", newline="", encoding="utf-8") as fh:
            w = csv.DictWriter(fh, fieldnames=OUT_COLS, lineterminator="\n")
            w.writeheader()
            for dd, r in plan:
                if dd == d:
                    w.writerow(r)
        shutil.copy2(d / "tests_info.csv", out / "tests_info.csv")

    print(f"OK: wrote {dst}")
    print(f"  folders: {len(folders)}  java files copied (md5 verified): {n_files}  method rows: {len(plan)}")
    for (fo, fx), n in sorted(seen_fix.items()):
        print(f"  {fo:<16} mockito_fix={fx:<5} {n} methods")
    if a.assume_mockito_fix:
        print("  NOTE: mockito_fix was NOT read from the flaky-check data. You asked for ALL rows = "
              f"{a.assume_mockito_fix} (an assumption).")


if __name__ == "__main__":
    main()

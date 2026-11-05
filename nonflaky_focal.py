#!/usr/bin/env python3
import argparse
import csv
import json
import os
from pathlib import Path
import random
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
import batch_reproflake_chatunitest as B

INPUT_COLS = ["test_kind", "project", "flaky_commit", "module", "zip", "java", "url", "test", "flaky_test"]
SAVE_EVERY = 200


def load_nonflaky(paths):
    units = {}
    for p in paths:
        per_file = {}
        for r in csv.DictReader(open(p, encoding="utf-8")):
            per_file.setdefault((r["project"], r["commit"], r["module"]), []).append(r)
        for key, rows in per_file.items():
            tests = {r["test"] for r in rows}
            if key in units:
                units[key]["tests"] &= tests
            else:
                units[key] = {"tests": tests, "rows": {r["test"]: r for r in rows}}
    return units


def pick(units, per_module, seed):
    out = []
    for key in sorted(units):
        tests = sorted(units[key]["tests"])
        if per_module:
            tests = sorted(random.Random(f"{seed}|{'|'.join(key)}").sample(tests, min(per_module, len(tests))))
        for t in tests:
            r = units[key]["rows"][t]
            cls, _, method = t.partition("#")
            out.append({"test_kind": "nonflaky", "project": r["project"], "flaky_commit": r["commit"],
                        "module": r["module"], "zip": r["zip"], "java": r["java"], "url": r["url"],
                        "test": t, "flaky_test": cls.split("$")[0] + "#" + method})
    return out


def load_helpers(focal_scripts):
    import javalang
    extractor = B.load_module(focal_scripts, "focal_extract.py", "repro_focal_extract")
    finder = B.load_module(focal_scripts, "focal_method_finder_batch.py", "repro_focal_jaccard")
    llm = B.load_module(focal_scripts, "llm_focal.py", "repro_focal_llm")
    extractor.junit_annotated = extractor.is_test_method
    llm.junit_annotated = llm.is_test
    return extractor, finder, llm, javalang


def main():
    a = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--nonflaky", type=Path, action="append", required=True,
                   help="nonflaky.csv to take tests from (repeatable)")
    a.add_argument("--archive-dir", type=Path, action="append", required=True,
                   help="Folder with extracted <zip>/ archives (repeatable), e.g. out_part1/extracted")
    a.add_argument("--focal-scripts", type=Path, default=Path.home() / "focal_scripts")
    a.add_argument("--out", type=Path, default=Path("nonflaky_focal_out"))
    a.add_argument("--per-module", type=int, default=20, help="Tests sampled per module (default 20)")
    a.add_argument("--all", action="store_true", help="Every non-flaky test instead of a sample")
    a.add_argument("--seed", default="1", help="Sampling seed (same seed, same sample)")
    a.add_argument("--run-llm", action="store_true", help="Make the OpenAI calls (needs OPENAI_API_KEY)")
    a.add_argument("--model", default="gpt-4o")
    a.add_argument("--force", action="store_true", help="Redo rows that already have a result")
    a.add_argument("--limit", type=int, default=0, help="First N sampled tests only (pilot)")
    args = a.parse_args()
    args.archive_dir = [d.resolve() for d in args.archive_dir]
    args.out = args.out.resolve()
    args.out.mkdir(parents=True, exist_ok=True)

    units = load_nonflaky(args.nonflaky)
    rows = pick(units, 0 if args.all else args.per_module, args.seed)
    if args.limit:
        rows = rows[:args.limit]
    print(f"{sum(len(u['tests']) for u in units.values())} non-flaky tests in {len(units)} modules; "
          f"{len(rows)} selected", flush=True)

    if args.run_llm and not os.getenv("OPENAI_API_KEY"):
        sys.exit("Set OPENAI_API_KEY before --run-llm")
    modules = load_helpers(args.focal_scripts) if args.run_llm else None

    manifest_path = args.out / "nonflaky_focal_manifest.csv"
    key = lambda r: (r["project"], r["flaky_commit"], r["module"], r.get("test") or r["flaky_test"])
    previous = {}
    if manifest_path.is_file():
        previous = {key(r): r for r in csv.DictReader(open(manifest_path, encoding="utf-8"))}
    try:
        llm_cache = json.loads((args.out / "llm_cache.json").read_text())
    except (OSError, ValueError):
        llm_cache = {}
    cache = {"extract": {}, "production": {}, "src_main": {}, "llm": llm_cache}
    ident = argparse.Namespace(run_llm=args.run_llm, force=args.force, model=args.model,
                               out=args.out, download=False, archives=None)

    columns = INPUT_COLS + [c for c in B.EXTRA if c not in INPUT_COLS]

    def save(done):
        rest = [previous[key(r)] for r in rows[len(done):] if key(r) in previous]
        tmp = manifest_path.with_suffix(".csv.tmp")
        B.csv_write(tmp, done + rest, columns)
        tmp.replace(manifest_path)

    manifest = []
    try:
        for i, row in enumerate(rows, 1):
            old = previous.get(key(row), {})
            if old and not args.force and old.get("status") not in ("", "awaiting_llm", "input_error"):
                entry = old
            else:
                ident.archives = next((d for d in args.archive_dir if (d / row["zip"]).is_dir()),
                                      args.archive_dir[0])
                entry = B.run_identification(row, ident, modules, cache, old)
            entry.update(row_number=i, test=row["test"], test_kind=row["test_kind"])
            manifest.append(entry)
            if i % SAVE_EVERY == 0:
                save(manifest)
            print(f"[{i}/{len(rows)}] {row['test']} -> {entry.get('selected_method') or '-'} "
                  f"({entry.get('status')})", flush=True)
    finally:
        save(manifest)

    counts = {}
    for e in manifest:
        counts[e.get("status")] = counts.get(e.get("status"), 0) + 1
    print(f"\n{len(manifest)} tests: " + ", ".join(f"{k} {v}" for k, v in sorted(counts.items(), key=str))
          + f"\nManifest: {manifest_path}")


if __name__ == "__main__":
    main()

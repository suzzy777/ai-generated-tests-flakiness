#!/usr/bin/env python3
import csv
from pathlib import Path
import shutil
import subprocess
import sys

OUT = Path(__file__).resolve().parent / "flaky_check_out"


def merge(check):
    shards = sorted(OUT.glob(f"{check}_[0-9]*"))
    busy = [s for s in shards if subprocess.run(["pgrep", "-f", "--", f"--out {s}( |$)"], stdout=subprocess.DEVNULL).returncode == 0]
    if busy:
        print(f"{check}: not merged, still running: " + ", ".join(s.name for s in busy))
        return
    if not shards:
        return
    dest = OUT / check
    (dest / "rows").mkdir(parents=True, exist_ok=True)
    for name in ("rows.csv", "runs.csv"):
        header, lines = None, []
        for f in [dest / name] + [s / name for s in shards]:
            if f.is_file():
                with open(f, encoding="utf-8", newline="") as fh:
                    r = csv.reader(fh)
                    header = next(r, header)
                    lines += list(r)
        if header:
            if name == "rows.csv":
                lines = sorted({ln[0]: ln for ln in lines}.values())
            with open(dest / name, "w", encoding="utf-8", newline="") as fh:
                csv.writer(fh).writerows([header] + lines)
    for s in shards:
        for d in (s / "rows").glob("*") if (s / "rows").is_dir() else []:
            shutil.rmtree(dest / "rows" / d.name, ignore_errors=True)
            shutil.move(str(d), str(dest / "rows" / d.name))
        for f in (s / "orders").glob("*.txt") if (s / "orders").is_dir() else []:
            (dest / "orders").mkdir(exist_ok=True)
            target, k = dest / "orders" / f.name, 2
            stem = f.name.rsplit(".", 2)[0]
            while target.exists():
                target, k = dest / "orders" / f"{stem}-{k}.{'.'.join(f.name.rsplit('.', 2)[1:])}", k + 1
            shutil.copy2(f, target)
        s.rename(s.with_name("merged_" + s.name))
    print(f"{check}: merged {len(shards)} shard(s) into {dest}")


if __name__ == "__main__":
    for c in sys.argv[1:] or ["idflakies", "rerun"]:
        merge(c)

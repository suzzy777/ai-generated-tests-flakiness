#!/usr/bin/env python3
import argparse
import csv
import os
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent

def stressor_cmd(name, a):
    if name == "cpu":
        return ["stress-ng", "--cpu", str(a.cpu_workers)]
    if name == "memory":
        return ["stress-ng", "--vm", "2", "--vm-bytes", a.vm_bytes]
    return None


def rerun_counts(path):
    counts = {}
    if path.is_file():
        for r in csv.DictReader(open(path, encoding="utf-8")):
            c = counts.setdefault((r["id"], r["test"]), [0, 0, 0, 0])
            c[0] += r["outcome"] == "passed"
            c[1] += r["outcome"] in ("failed", "rerun_passed")
            c[2] += 1
            c[3] += r["outcome"] in ("error", "timeout")
    return counts


def run_regime(name, a):
    reg_out = (a.out / name).resolve()
    reg_out.mkdir(parents=True, exist_ok=True)
    sp = None
    cmd = stressor_cmd(name, a)
    if cmd:
        print(f"\n=== {name} stress: starting `{' '.join(cmd)}` ===", flush=True)
        sp = subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        time.sleep(2)
        if sp.poll() is not None:
            print(f"!! {name} stressor died immediately; skipping regime", file=sys.stderr)
            return reg_out / "runs.csv"
    else:
        print(f"\n=== {name} (no stress) ===", flush=True)
    try:
        rr = [sys.executable, str(HERE / "flaky_check.py"), "rerun",
              "--tests", str(a.tests), "--runs", str(a.runs), "--out", str(reg_out),
              "--maven-repo", str(a.maven_repo)]
        for d in a.archive_dir:
            rr += ["--archive-dir", str(d)]
        for o in a.only:
            rr += ["--only", o]
        if a.offline:
            rr += ["--offline"]
        subprocess.run(rr, check=False)
    finally:
        if sp:
            sp.terminate()
            try:
                sp.wait(timeout=10)
            except subprocess.TimeoutExpired:
                sp.kill()
            subprocess.run(["pkill", "-x", "stress-ng"], stderr=subprocess.DEVNULL)
    return reg_out / "runs.csv"


def main():
    a = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    a.add_argument("--tests", type=Path, required=True)
    a.add_argument("--archive-dir", type=Path, action="append", default=[])
    a.add_argument("--runs", type=int, default=10, help="runs per method per regime (default 10)")
    a.add_argument("--out", type=Path, default=HERE / "flaky_check_out" / "stress")
    a.add_argument("--only", action="append", default=[])
    a.add_argument("--maven-repo", type=Path, default=Path.home() / ".m2" / "repository")
    a.add_argument("--regimes", default="cpu,memory", help="comma list of baseline,cpu,memory")
    a.add_argument("--cpu-workers", type=int, default=os.cpu_count() or 2)
    a.add_argument("--vm-bytes", default="256M")
    a.add_argument("--offline", action="store_true", help="pass --offline to rerun after first build")
    args = a.parse_args()
    if not (HERE / "flaky_check.py").is_file():
        sys.exit(f"{HERE} is not the package folder (flaky_check.py missing). cd into it.")
    if not args.archive_dir:
        args.archive_dir = [HERE / "extracted"]
    args.out = args.out.resolve()
    args.out.mkdir(parents=True, exist_ok=True)

    regs = [r.strip() for r in args.regimes.split(",") if r.strip()]
    counts = {}
    for name in regs:
        counts[name] = rerun_counts(run_regime(name, args))

    keys = set()
    for c in counts.values():
        keys |= set(c)
    fields = ["id", "test"]
    for r in regs:
        fields += [f"{r}_pass", f"{r}_fail", f"{r}_runs", f"{r}_flaky"]
    fields += ["any_stress_flaky"]
    res = args.out / "stress_results.csv"
    nflaky = 0
    with res.open("w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fields)
        w.writeheader()
        for k in sorted(keys):
            row = {"id": k[0], "test": k[1]}
            anyf = False
            for r in regs:
                p, fl, ru, er = counts[r].get(k, [0, 0, 0, 0])
                flaky = p > 0 and fl > 0
                anyf = anyf or flaky
                row[f"{r}_pass"], row[f"{r}_fail"], row[f"{r}_runs"], row[f"{r}_flaky"] = p, fl, ru, flaky
            row["any_stress_flaky"] = anyf
            nflaky += anyf
            w.writerow(row)
    print(f"\nwrote {res}")
    print(f"regimes={regs} runs={args.runs}  methods={len(keys)}  stress-flaky={nflaky}")


if __name__ == "__main__":
    main()

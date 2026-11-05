#!/bin/bash
H="$(cd "$(dirname "$0")" && pwd)"
P="$H/prompt_experiment"
G="$H/agent_experiment"
O="$H/new10_checks"
mkdir -p "$O"
cd "$H"

ROWS=(); for n in $(seq 16 30); do ROWS+=(--only "_row_0$n"); done

echo "== 1. replay $(date)"
for v in default cleanup; do
  python3 replay_generated.py --part "$P/out_$v:$P/manifest.csv" --out "$P/replay_${v}_new" "${ROWS[@]}" \
    >> "$O/replay.log" 2>&1
  bad=$(python3 -c "
import csv
print(' '.join('--only '+r['id'] for r in csv.DictReader(open('$P/replay_${v}_new/results.csv')) if r['status']!='passed'))")
  if [ -n "$bad" ]; then
    python3 replay_generated.py --part "$P/out_$v:$P/manifest.csv" --out "$P/replay_${v}_new_fix" --mockito-fix $bad \
      >> "$O/replay.log" 2>&1
  fi
done

echo "== 2. test lists $(date)"
python3 - "$H" <<'EOF' || exit 1
import csv, re, sys
from pathlib import Path
H = Path(sys.argv[1]); P = H / "prompt_experiment"; G = H / "agent_experiment"
FIELDS = ["project", "sha", "module", "generated_test", "test_file", "focal_method", "zip", "java",
          "junit", "mockito_fix", "focal_from_test", "focal_from_test_type"]
read = lambda p: list(csv.DictReader(open(p, encoding="utf-8"))) if Path(p).is_file() else []
proj = {}
for r in read(H / "task_csvs" / "flaky_generated_tests.csv"):
    proj.setdefault(r["zip"], r)
def save(dest, out):
    with open(dest, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, FIELDS); w.writeheader(); w.writerows(out)
    print(f"{dest}: {len(out)} test methods, {len({r['test_file'] for r in out})} files")
man = {r["row_number"]: r for r in read(P / "manifest.csv")}
out = []
for v in ("default", "cleanup"):
    done = set()
    for name, fix in ((f"replay_{v}_new", False), (f"replay_{v}_new_fix", True)):
        passed = {r["id"] for r in read(P / name / "results.csv") if r["status"] == "passed"} - done
        done |= passed
        for m in read(P / name / "methods.csv"):
            rn = str(int(m["id"].rsplit("_", 1)[-1]))
            if m["id"] not in passed or int(rn) < 16:
                continue
            cls = m["test"].split("#")[0].rsplit(".", 1)[-1]
            files = sorted((P / f"out_{v}" / "runs" / f"row_{int(rn):03d}" / "tests").rglob(cls + ".java"))
            p, g = proj[m["zip"]], man[rn]
            out.append({"project": p["project"], "sha": p["sha"], "module": p["module"],
                        "generated_test": m["test"], "test_file": str(files[0].relative_to(H)),
                        "focal_method": m["focal"], "zip": m["zip"], "java": p["java"], "junit": g["junit"],
                        "mockito_fix": str(fix).lower(), "focal_from_test": g["flaky_test"],
                        "focal_from_test_type": f"experiment_{v}"})
save(P / "task_csvs" / "new10_tests.csv", out)
TEST = re.compile(r"@Test\b[^{;]*?\bvoid\s+(\w+)\s*\(")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.M)
man = {r["row_number"]: r for r in read(G / "manifest.csv")}
out = []
for c in ("A", "F", "FC", "FCV"):
    for g in read(G / f"out_{c}" / "generation_results.csv"):
        rn = g["row_number"]
        if g["status"] != "generated" or int(rn) < 16:
            continue
        p, m = proj[g["zip"]], man[rn]
        for f in sorted((G / f"out_{c}" / "runs" / f"row_{int(rn):03d}" / "tests").rglob("*.java")):
            src = f.read_text(encoding="utf-8", errors="replace")
            pkg = PACKAGE.search(src)
            fqcn = (pkg.group(1) + "." if pkg else "") + f.stem
            for name in TEST.findall(src):
                out.append({"project": p["project"], "sha": p["sha"], "module": p["module"],
                            "generated_test": f"{fqcn}#{name}", "test_file": str(f.relative_to(H)),
                            "focal_method": m["selected_method"], "zip": g["zip"], "java": p["java"],
                            "junit": m["junit"], "mockito_fix": "false", "focal_from_test": m["flaky_test"],
                            "focal_from_test_type": f"agent_{c}"})
save(G / "task_csvs" / "new10_agent_tests.csv", out)
EOF

echo "== 3. checks $(date)"
checks() {
  local N=$1 T=$2 X=$3 Z=$4 D="$O/$1"
  mkdir -p "$D"
  local A=(--tests "$T" --archive-dir "$X" --only "$Z")
  {
    python3 flaky_check_basic.py idflakies "${A[@]}" --rounds 20 --out "$D/idflakies"
    python3 od_confirm.py --offline "${A[@]}" --idflakies-out "$D/idflakies" --out "$D/od_confirm"
    python3 od_recheck/od_recheck.py --name "new10_$N" --od-confirm "$D/od_confirm/od_confirm.csv" \
      --idflakies-out "$D/idflakies" --tests "$T" --archive-dir "$X" --out "$D/od_recheck"
    python3 flaky_check_basic.py nio "${A[@]}" --nio-runs 100 --out "$D/nio"
    python3 flaky_check_basic.py nondex "${A[@]}" --nondex-runs 100 --out "$D/nondex"
    python3 flaky_check_basic.py rerun "${A[@]}" --runs 100 --offline --out "$D/rerun"
    python3 flaky_check_basic.py summary "${A[@]}" --out "$D"
    echo "$N done $(date)"
  } >> "$D/lane.log" 2>&1
}
lane() {
  checks "$2_prompt" "$P/task_csvs/new10_tests.csv" "$P/extracted" "$1"
  checks "$2_agent" "$G/task_csvs/new10_agent_tests.csv" "$G/extracted" "$1"
}
lane elasticjobf0d elasticjob &
lane ormlitecore59309e5 ormlite &
lane marineapi0a1f308 marineapi &
wait
echo "all done (run_new10_A.sh) $(date)"

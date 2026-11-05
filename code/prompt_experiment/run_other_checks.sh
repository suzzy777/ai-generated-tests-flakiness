#!/bin/bash
D="$(cd "$(dirname "$0")" && pwd)"
REPO="${MAVEN_REPO:-$HOME/.m2/repository}"
T="$D/task_csvs/experiment_generated_tests.csv"
mkdir -p "$REPO" "$D/out_checks"
for a in "$D"/extracted/*/; do z=$(basename "$a"); cp -rn "$a/$z/Flakym2/.m2/repository/." "$REPO/"; done
echo "Flakym2 artifacts copied into $REPO (existing files kept)"
lane() {
  n=$1; shift
  O="$D/out_checks/lane$n"
  mkdir -p "$O"
  (
    cd "$D"
    A=(--tests "$T" --archive-dir "$D/extracted" --maven-repo "$REPO")
    python3 flaky_check_basic.py nio "${A[@]}" --nio-runs 100 --out "$O/nio" "$@"
    python3 flaky_check_basic.py nondex "${A[@]}" --nondex-runs 100 --out "$O/nondex" "$@"
    python3 flaky_check_basic.py rerun "${A[@]}" --runs 100 --offline --out "$O/rerun" "$@"
    echo "lane$n done"
  ) >> "$O/lane.log" 2>&1
}
lane 1 --only dubbodubborpcdubborpcdubbo &
lane 2 --only marineapi0a1f308 &
lane 3 --only ormlitecore59309e5 &
lane 4 --only scimono &
wait
echo "all done (machine_b_other_checks)"

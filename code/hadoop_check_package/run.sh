#!/bin/bash
D="$(cd "$(dirname "$0")" && pwd)"
REPO="${MAVEN_REPO:-$HOME/.m2/repository}"
PART="$D:$D/task_csvs/nonflaky_generate_sample.csv"
(
  protoc --version 2>/dev/null | grep -q "2.5.0" || { echo "hadoop needs protoc 2.5.0 on the PATH (protoc --version)"; exit 1; }
  mkdir -p "$REPO"
  cp -rn "$D/extracted/YARN-9405/YARN-9405/Flakym2/." "$REPO/"
  python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_out"
  IDS=$(python3 "$D/make_tests_csv.py" --not-passed)
  if [ -n "$IDS" ]; then
    ONLY=""; for i in $IDS; do ONLY="$ONLY --only $i"; done
    python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_fix_out" --mockito-fix $ONLY
  fi
  python3 "$D/make_tests_csv.py" || exit 1
  S="$D/flaky_check_basic.py"
  A="--tests $D/task_csvs/nonflaky_generated_tests.csv --archive-dir $D/extracted"
  python3 $S nio    $A --nio-runs 10
  python3 $S nondex $A --nondex-runs 100
  python3 $S rerun  $A --runs 100 --offline
  python3 $S summary $A
  echo "all done (hadoop_check_package)"
) 2>&1 | tee -a "$D/run.log"

#!/bin/bash
D="$(cd "$(dirname "$0")" && pwd)"
PART="$D:$D/task_csvs/nonflaky_generate_sample.csv"

python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_out" 2>&1 | tee -a "$D/replay.log"
IDS=$(python3 "$D/make_tests_csv.py" --not-passed)
if [ -n "$IDS" ]; then
  ONLY=""; for i in $IDS; do ONLY="$ONLY --only $i"; done
  python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_fix_out" --mockito-fix $ONLY 2>&1 | tee -a "$D/replay_fix.log"
fi
python3 "$D/make_tests_csv.py" || exit 1

S="$D/flaky_check_basic.py"
A="--tests $D/task_csvs/nonflaky_generated_tests.csv --archive-dir $D/extracted"
python3 $S nio       $A --nio-runs 10           2>&1 | tee -a "$D/flaky_nio.log"
python3 $S nondex    $A --nondex-runs 100       2>&1 | tee -a "$D/flaky_nondex.log"
python3 $S idflakies $A --rounds 20             2>&1 | tee -a "$D/flaky_idflakies.log"
python3 $S rerun     $A --runs 100 --offline    2>&1 | tee -a "$D/flaky_rerun.log"
python3 $S summary   $A

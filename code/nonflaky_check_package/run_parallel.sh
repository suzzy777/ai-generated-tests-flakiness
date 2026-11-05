#!/bin/bash
set -o pipefail
D=./nonflaky_check_package
H=.
PART="$D:$D/task_csvs/nonflaky_generate_sample.csv"
TESTS=$D/task_csvs/nonflaky_generated_tests.csv
S=$D/flaky_check_basic.py
LOG=$D/run_parallel.log
L=$D/logs_parallel
mkdir -p $L
log() { echo "$(date '+%F %T') $*" | tee -a "$LOG"; }

T="--tests $H/task_csvs/flaky_generated_tests.csv"
(
  python3 $H/flaky_check_basic.py idflakies $T --rounds 20 --redo \
    --only out_part1_row_017 --only out_part1_row_046 --only out_part1_row_049 --only out_part1_row_053 \
    --only out_part1_row_070 --only out_part1_row_071 --only out_part1_row_072 --only out_part2_row_016 \
    --only out_part2_row_119 --only out_part2_row_121 > $H/flaky_idflakies_rerun.log 2>&1
  echo "$(date '+%F %T') flaky side: idflakies rerun exit $?" >> "$LOG"
  python3 $H/flaky_check_basic.py summary $T >> $H/flaky_idflakies_rerun.log 2>&1
  echo "$(date '+%F %T') flaky side: summary exit $?" >> "$LOG"
) &
FLAKY_PID=$!
log "flaky side idflakies rerun started (pid $FLAKY_PID, log $H/flaky_idflakies_rerun.log)"

log "replay"
python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_out" 2>&1 | tee -a "$D/replay.log" > /dev/null
IDS=$(python3 "$D/make_tests_csv.py" --not-passed)
if [ -n "$IDS" ]; then
  ONLY=""; for i in $IDS; do ONLY="$ONLY --only $i"; done
  log "replay --mockito-fix of $(echo $IDS | wc -w) rows"
  python3 "$D/replay_generated.py" --part "$PART" --out "$D/genreplay_fix_out" --mockito-fix $ONLY 2>&1 \
    | tee -a "$D/replay_fix.log" > /dev/null
fi
python3 "$D/make_tests_csv.py" 2>&1 | tee -a "$LOG" || exit 1

log "copying the projects for the parallel jobs"
python3 $D/shard.py --tests $TESTS > $D/par_jobs.txt || exit 1
cat $D/par_jobs.txt | cut -c1-200 >> "$LOG"

PIDS=""
while read -r name check adir rids; do
  ONLY=""; for r in $rids; do ONLY="$ONLY --only $r"; done
  case $check in
    nio)       EXTRA="--nio-runs 10";               OUTDIR=$D/flaky_check_out/nio ;;
    nondex)    EXTRA="--nondex-runs 100";           OUTDIR=$D/flaky_check_out/nondex ;;
    idflakies) EXTRA="--rounds 20";                 OUTDIR=$D/flaky_check_out/$name ;;
    rerun)     EXTRA="--runs 100 --offline";        OUTDIR=$D/flaky_check_out/$name ;;
  esac
  python3 $S $check --tests $TESTS --archive-dir $adir --archive-dir $D/extracted --out $OUTDIR $EXTRA $ONLY \
    < /dev/null > $L/$name.log 2>&1 &
  PIDS="$PIDS $!"
  log "started $name (pid $!, $(echo $rids | wc -w) runs)"
done < $D/par_jobs.txt

for p in $PIDS; do wait $p; log "job pid $p ended, exit $?"; done

python3 $D/merge_shards.py 2>&1 | tee -a "$LOG"
python3 $S summary --tests $TESTS --archive-dir $D/extracted 2>&1 | tee -a "$LOG"
wait $FLAKY_PID
log "all done"

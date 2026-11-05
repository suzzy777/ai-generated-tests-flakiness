#!/bin/bash
D="$(cd "$(dirname "$0")" && pwd)"; H="$(dirname "$D")"
R="python3 $D/od_recheck.py --skip-zip dubbo"
( $R --name main_flaky --od-confirm $H/flaky_check_out/od_confirm/od_confirm.csv --idflakies-out $H/flaky_check_out/idflakies \
    --tests $H/task_csvs/flaky_generated_tests.csv --archive-dir $H/out_part1/extracted --archive-dir $H/out_part2/extracted --out $D/main_flaky ) > $D/main_flaky.log 2>&1 &
( $R --name main_nonflaky --od-confirm $H/nonflaky_check_package/flaky_check_out/od_confirm/od_confirm.csv \
    --idflakies-out $H/nonflaky_check_package/flaky_check_out/idflakies --tests $D/nonflaky_tests.csv \
    --archive-dir $H/nonflaky_check_package/extracted --out $D/main_nonflaky ) > $D/main_nonflaky.log 2>&1 &
( for c in A F; do $R --name agent_$c --od-confirm $H/agent_experiment/out_flaky_$c/lane2/od_confirm/od_confirm.csv \
    --idflakies-out $H/agent_experiment/out_flaky_$c/lane2/idflakies --tests $H/agent_experiment/task_csvs/agent_${c}_tests.csv \
    --archive-dir $H/quality/extracted --out $D/agent_$c; done ) > $D/agent.log 2>&1 &
( while kill -0 2173805 2>/dev/null; do sleep 30; done
  $R --name prompt --od-confirm $H/prompt_experiment/out_idflakies/lane3/od_confirm/od_confirm.csv --idflakies-out $H/prompt_experiment/out_idflakies/lane3/idflakies \
    --tests $H/prompt_experiment/task_csvs/experiment_generated_tests.csv --archive-dir $H/prompt_experiment/extracted --out $D/pilot ) > $D/prompt.log 2>&1 &
wait
echo "all done (od_recheck run_lanes.sh)"

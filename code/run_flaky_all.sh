#!/bin/bash
TESTS=./task_csvs/flaky_generated_tests.csv
S=./flaky_check_basic.py
L=.

python3 $S nio       --tests $TESTS --nio-runs 100          2>&1 | tee $L/flaky_nio.log
python3 $S nondex    --tests $TESTS --nondex-runs 100     2>&1 | tee $L/flaky_nondex.log
python3 $S idflakies --tests $TESTS --rounds 20           2>&1 | tee $L/flaky_idflakies.log
python3 $S rerun     --tests $TESTS --runs 100 --offline  2>&1 | tee $L/flaky_rerun.log
python3 $S summary   --tests $TESTS

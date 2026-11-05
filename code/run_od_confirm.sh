#!/bin/bash
H=.
D=$H/nonflaky_check_package
FIRST="--only dubbo --only marineapi --only elasticjob --only shardingsphere --only scimono --only crane4j --only cloudstack --only camel --only CAMEL"
(
  cd $H
  python3 od_confirm.py --offline $FIRST
  python3 od_confirm.py --offline
  echo "flaky side done"
) > $H/od_confirm_flaky.log 2>&1 &
(
  cd $D
  A="--tests task_csvs/nonflaky_generated_tests.csv --archive-dir extracted --idflakies-out flaky_check_out/idflakies"
  python3 od_confirm.py --offline $A $FIRST
  python3 od_confirm.py --offline $A
  echo "non-flaky side done"
) > $D/od_confirm_nonflaky.log 2>&1 &
wait
echo "all done (run_od_confirm.sh)"

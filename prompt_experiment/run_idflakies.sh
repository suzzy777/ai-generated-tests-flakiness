#!/bin/bash
E="$(cd "$(dirname "$0")" && pwd)"
H="$(dirname "$E")"
T="$E/task_csvs/experiment_generated_tests.csv"
lane() {
  n=$1; shift
  O="$E/out_idflakies/lane$n"
  mkdir -p "$O"
  (
    cd "$H"
    python3 flaky_check_basic.py idflakies --tests "$T" --archive-dir "$E/extracted" --rounds 20 \
      --out "$O/idflakies" "$@"
    python3 od_confirm.py --offline --tests "$T" --archive-dir "$E/extracted" \
      --idflakies-out "$O/idflakies" --out "$O/od_confirm" "$@"
    echo "lane$n done"
  ) > "$O/lane.log" 2>&1
}
lane 1 --only out_default_row_001 --only out_default_row_002 --only out_default_row_003 --only out_default_row_005 &
lane 2 --only marineapi0a1f308 --only scimono &
lane 3 --only ormlitecore59309e5 &
wait
echo "all done (run_idflakies.sh)"

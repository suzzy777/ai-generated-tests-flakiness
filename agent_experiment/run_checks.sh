#!/bin/bash
E="$(cd "$(dirname "$0")" && pwd)"
H="$(dirname "$E")"
COND="${1:-F}"
if [ -e "$E/out_flaky_$COND/SKIP_HERE" ]; then echo "out_flaky_$COND/SKIP_HERE exists: not run here"; cat "$E/out_flaky_$COND/SKIP_HERE"; exit 0; fi
T="$E/task_csvs/agent_${COND}_tests.csv"
python3 "$E/make_idf_tests_csv.py" "$COND" || exit 1
lane() {
  n=$1; shift
  O="$E/out_flaky_$COND/lane$n"
  mkdir -p "$O"
  (
    cd "$H"
    A=(--tests "$T" --archive-dir "$E/extracted")
    python3 flaky_check_basic.py idflakies "${A[@]}" --rounds 20 --out "$O/idflakies" "$@"
    python3 od_confirm.py --offline "${A[@]}" --idflakies-out "$O/idflakies" --out "$O/od_confirm" "$@"
    python3 flaky_check_basic.py nio "${A[@]}" --nio-runs 100 --out "$O/nio" "$@"
    python3 flaky_check_basic.py nondex "${A[@]}" --nondex-runs 100 --out "$O/nondex" "$@"
    python3 flaky_check_basic.py rerun "${A[@]}" --runs 100 --offline --out "$O/rerun" "$@"
    python3 flaky_check_basic.py summary "${A[@]}" --out "$O" "$@"
    echo "lane$n done"
  ) >> "$O/lane.log" 2>&1
}
only=([1]=marineapi0a1f308 [2]=ormlitecore59309e5 [3]=dubbodubborpcdubborpcdubbo [4]=scimono)
for n in ${LANES:-1 2 3 4}; do lane $n --only "${only[$n]}" & done
wait
echo "all done (run_idflakies_F.sh $COND)"

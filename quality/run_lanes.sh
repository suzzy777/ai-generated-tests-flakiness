#!/bin/bash
Q="$(cd "$(dirname "$0")" && pwd)"
for z in "$@"; do
  (
    for g in STRONGER ALL; do
      o="$Q/out_$(echo $g | tr A-Z a-z)/${z%%=*}"
      python3 "$Q/covmut.py" run --mutators $g --out "$o" --only "$z"
    done
  ) >> "$Q/lane_${z%%=*}.log" 2>&1 &
done
wait
echo "all done (run_lanes.sh $*)"

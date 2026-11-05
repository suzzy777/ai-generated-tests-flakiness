#!/bin/bash
Q="$(cd "$(dirname "$0")" && pwd)"
lane() {
  local n=$1; shift
  for z in "$@"; do
    for g in STRONGER ALL; do
      python3 "$Q/covmut.py" run --items "$Q/items_new.csv" --mutators $g \
        --out "$Q/out_new_$(echo $g | tr A-Z a-z)/${z%%=*}" --only "$z"
    done
  done >> "$Q/lane_new_$n.log" 2>&1
}
lane elastic elasticjobf0d shardingsphereelasticjobelasticjoblitecore4b9afa4 &
lane dubbo dubbodubborpcdubborpcdubboaa9f16e dubbodubborpcdubborpcdubbo628ad77 &
lane marineapi marineapi0a1f308 &
lane ormlite ormlitecore59309e5 &
lane scimono scimono=scimono-client=7487ec5 &
wait
echo "all done (run_new.sh) $(date)"

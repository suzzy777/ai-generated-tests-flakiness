#!/bin/bash
Q="$(cd "$(dirname "$0")" && pwd)"
for z in elasticjobf0d shardingsphereelasticjobelasticjoblitecore4b9afa4; do
  (
    for g in STRONGER ALL; do
      python3 "$Q/covmut.py" run --items "$Q/items_new.csv" --mutators $g \
        --out "$Q/out_new_$(echo $g | tr A-Z a-z)/$z" --only "$z"
    done
  ) >> "$Q/lane_new_elastic_$z.log" 2>&1 &
done
wait
echo "all done (run_new_elastic.sh) $(date)"

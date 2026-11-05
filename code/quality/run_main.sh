#!/bin/bash
Q="$(cd "$(dirname "$0")" && pwd)"; H="$(dirname "$Q")"
ARCH=(--archive-dir "$H/out_part1/extracted" --archive-dir "$H/out_part2/extracted" --archive-dir "$H/nonflaky_check_package/extracted")
declare -A L
L[1]="scimono crane4j CAMEL-12025"
L[2]="cloudstack karate eco-schema-catalog"
L[3]="elasticjobf0d shardingsphereelasticjobelasticjoblitecore23a2ab5 shardingsphereelasticjobelasticjoblitecore4b9afa4 shardingsphereelasticjobelasticjoblitecore90e3a7f SCB-2692"
L[4]="ormlitecore59309e5 marineapi0a1f308"
L[dubbo]="dubbodubborpcdubborpcdubboaa9f16e dubbodubborpcdubborpcdubbo628ad77"
for n in ${LANES:-1 2 3 4}; do
  (
    for g in STRONGER ALL; do
      for z in ${L[$n]}; do
        python3 "$Q/covmut.py" run --items "$Q/items_main.csv" "${ARCH[@]}" --mutators $g \
          --out "$Q/out_main_$(echo $g | tr A-Z a-z)/lane$n" --only "$z"
      done
    done
    echo "lane$n done"
  ) >> "$Q/main_lane$n.log" 2>&1 &
done
wait
echo "all done (run_main.sh ${LANES:-1 2 3 4})"

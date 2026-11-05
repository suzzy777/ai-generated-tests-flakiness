#!/bin/bash
cd ./quality
for g in STRONGER ALL; do
  python3 covmut.py run --items hadoop_nonflaky/items.csv --archive-dir ./out_part1/extracted --mutators $g --timeout 7200 --out hadoop_nonflaky/out_$(echo $g | tr A-Z a-z)
done
echo "all done (hadoop_nonflaky)"

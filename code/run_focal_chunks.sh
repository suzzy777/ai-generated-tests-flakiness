#!/bin/bash
cd . || exit 1
if [ -z "$OPENAI_API_KEY" ]; then echo "Set OPENAI_API_KEY first"; exit 1; fi
for k in 01 02 03 04 05 06 07 08 09 10; do
  python3 nonflaky_focal.py --all \
    --nonflaky nonflaky_chunks/nonflaky_chunk$k.csv \
    --archive-dir out_part1/extracted --archive-dir out_part2/extracted \
    --out nonflaky_focal_out/chunk$k --run-llm 2>&1 | tee focal_chunk$k.log
done

#!/bin/bash
D="$(cd "$(dirname "$0")" && pwd)"
cd "$D" || exit 1
if [ -z "$OPENAI_API_KEY" ]; then echo "Set OPENAI_API_KEY first"; exit 1; fi
python3 "$D/batch_reproflake_chatunitest.py" generate \
  --manifest "$D/task_csvs/nonflaky_generate_sample.csv" \
  --junit4-dir "$D/chatunitest" \
  --out "$D/out" --archives "$D/zips" --download \
  --model gpt-4o --resume --execute "$@" 2>&1 | tee -a "$D/generate.log"

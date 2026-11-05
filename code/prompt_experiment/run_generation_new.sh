#!/bin/bash
set -e
E="$(cd "$(dirname "$0")" && pwd)"
[ -n "$OPENAI_API_KEY" ] || { echo "export OPENAI_API_KEY first"; exit 1; }
for v in default cleanup; do
  python3 "$E/batch_reproflake_chatunitest.py" generate --manifest "$E/manifest_new.csv" --execute \
    --junit4-dir ~/chatunitest-junit4 --prompts-junit4 "$E/prompts/junit4_$v" \
    --prompts-junit5 "$E/prompts/junit5_$v" --out "$E/out_${v}_new" --resume 2>&1 | tee -a "$E/generation_${v}_new.log"
done
echo "all done (run_generation_new.sh)"

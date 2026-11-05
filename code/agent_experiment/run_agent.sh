#!/bin/bash
E="$(cd "$(dirname "$0")" && pwd)"
[ -n "$OPENAI_API_KEY" ] || { echo "export OPENAI_API_KEY first"; exit 1; }
ONLY=(); for r in "$@"; do ONLY+=(--only "$r"); done
[ -n "$REDO" ] && ONLY+=(--redo)
for c in ${CONDITIONS:-A}; do
  "$E/venv/bin/python" "$E/run_agent.py" --condition $c "${ONLY[@]}" 2>&1 | tee -a "$E/agent_$c.log"
done
echo "all done (run_agent.sh)"

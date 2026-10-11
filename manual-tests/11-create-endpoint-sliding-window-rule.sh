#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "manual-endpoint-sliding-window",
    "algorithm": "SLIDING_WINDOW_LOG",
    "scopeType": "ENDPOINT",
    "scopeValue": "/manual/sliding-window",
    "limit": 2,
    "windowSize": 1,
    "windowUnit": "MINUTE"
  }'
echo

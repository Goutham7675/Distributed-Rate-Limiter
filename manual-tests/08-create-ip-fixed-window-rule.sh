#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "manual-ip-fixed-window",
    "algorithm": "FIXED_WINDOW_COUNTER",
    "scopeType": "IP_ADDRESS",
    "scopeValue": "198.51.100.10",
    "limit": 1,
    "windowSize": 1,
    "windowUnit": "MINUTE"
  }'
echo

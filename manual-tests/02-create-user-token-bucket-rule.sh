#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "manual-user-token-bucket",
    "algorithm": "TOKEN_BUCKET",
    "scopeType": "USER_ID",
    "scopeValue": "manual-user-001",
    "limit": 2,
    "windowSize": 30,
    "windowUnit": "SECOND",
    "burstCapacity": 2
  }'
echo

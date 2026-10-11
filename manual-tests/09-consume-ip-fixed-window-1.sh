#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/consume" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "manual-user-ip",
    "ipAddress": "198.51.100.10",
    "endpoint": "/manual/fixed-window"
  }'
echo

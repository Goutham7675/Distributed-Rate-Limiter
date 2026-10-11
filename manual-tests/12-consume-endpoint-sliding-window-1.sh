#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/consume" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "manual-user-endpoint",
    "ipAddress": "203.0.113.77",
    "endpoint": "/manual/sliding-window"
  }'
echo

#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X POST "${BASE_URL}/consume" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "manual-user-001",
    "ipAddress": "203.0.113.10",
    "endpoint": "/manual/token-bucket"
  }'
echo

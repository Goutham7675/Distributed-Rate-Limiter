#!/usr/bin/env bash
set -euo pipefail

echo "Flushing Redis DB 15 for a clean manual test run..."
redis-cli -n 15 FLUSHDB
echo "Done."

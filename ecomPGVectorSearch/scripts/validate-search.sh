#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:9204}"
QUERY="${2:-wireless headphones with noise cancellation}"
LIMIT="${3:-5}"

curl -sS --get "$BASE_URL/api/products/search" \
  --data-urlencode "query=$QUERY" \
  --data-urlencode "limit=$LIMIT"

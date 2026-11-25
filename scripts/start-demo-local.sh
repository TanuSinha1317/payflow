#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export DEMO_ENABLED=true
export LAB_ENABLED=true
exec "$ROOT_DIR/scripts/start-backend-local.sh"

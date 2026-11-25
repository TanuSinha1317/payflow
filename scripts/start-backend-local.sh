#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

PG_ISREADY="/opt/homebrew/opt/postgresql@18/bin/pg_isready"
if [[ ! -x "$PG_ISREADY" ]]; then
  echo "PostgreSQL 18 was not found. Install/start it before PayFlow." >&2
  exit 1
fi
if ! "$PG_ISREADY" -h localhost -p 5432 -d payflow >/dev/null; then
  echo "PayFlow PostgreSQL is not ready. Run: brew services start postgresql@18" >&2
  exit 1
fi

if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then
  if curl -fsS http://localhost:8080/actuator/health | grep -q '"UP"'; then
    echo "PayFlow backend is already healthy at http://localhost:8080"
    exit 0
  fi
  echo "Port 8080 is occupied by another process; nothing was terminated." >&2
  lsof -nP -iTCP:8080 -sTCP:LISTEN >&2
  exit 1
fi

cd "$ROOT_DIR/backend"
exec mvn spring-boot:run


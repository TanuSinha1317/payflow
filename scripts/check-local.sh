#!/usr/bin/env bash
set -euo pipefail

printf 'Java: '
/usr/libexec/java_home -v 21 >/dev/null
"$(/usr/libexec/java_home -v 21)/bin/java" -version 2>&1 | head -1
printf 'PostgreSQL: '
/opt/homebrew/opt/postgresql@18/bin/pg_isready -h localhost -p 5432 -d payflow
printf 'Backend: '
curl -fsS http://localhost:8080/actuator/health
printf '\nSwagger: '
curl -sS -o /dev/null -w '%{http_code}\n' http://localhost:8080/swagger-ui/index.html
printf 'Frontend: '
curl -sS -o /dev/null -w '%{http_code}\n' http://localhost:5173/

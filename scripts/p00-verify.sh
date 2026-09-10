#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
COMPOSE_FILE="$ROOT_DIR/infra/compose.yml"
ENV_FILE="$ROOT_DIR/infra/env/.env.example"

if [ -f "$ROOT_DIR/infra/env/.env" ]; then
  ENV_FILE="$ROOT_DIR/infra/env/.env"
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "P00 verification failed: Docker is not available" >&2
  exit 1
fi

compose() {
  docker compose --project-name gl-operations-p00 --env-file "$ENV_FILE" \
    --file "$COMPOSE_FILE" "$@"
}

env_value() {
  key=$1
  value=$(sed -n "s/^${key}=//p" "$ENV_FILE" | tail -n 1)
  if [ -z "$value" ]; then
    echo "P00 verification failed: missing ${key} in env file" >&2
    exit 1
  fi
  printf '%s' "$value"
}

BACKEND_PORT=$(env_value BACKEND_PORT)
FRONTEND_PORT=$(env_value FRONTEND_PORT)
LIVENESS_URL="http://127.0.0.1:${BACKEND_PORT}/actuator/health/liveness"
READINESS_URL="http://127.0.0.1:${BACKEND_PORT}/actuator/health/readiness"
FRONTEND_URL="http://127.0.0.1:${FRONTEND_PORT}/"

wait_for_success() {
  label=$1
  url=$2
  attempts=${3:-30}
  count=1
  while [ "$count" -le "$attempts" ]; do
    if curl --fail --silent --show-error --output /dev/null --max-time 3 "$url"; then
      echo "PASS: ${label}"
      return 0
    fi
    count=$((count + 1))
    sleep 2
  done
  echo "P00 verification failed: ${label}" >&2
  return 1
}

wait_for_failure() {
  label=$1
  url=$2
  attempts=${3:-20}
  count=1
  while [ "$count" -le "$attempts" ]; do
    if ! curl --fail --silent --output /dev/null --max-time 3 "$url"; then
      echo "PASS: ${label}"
      return 0
    fi
    count=$((count + 1))
    sleep 2
  done
  echo "P00 verification failed: ${label}" >&2
  return 1
}

migration_count() {
  compose exec --no-TTY postgres sh -c \
    'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT count(*) FROM flyway_schema_history WHERE script = '\''V0001__bootstrap.sql'\'' AND success"'
}

assert_single_migration() {
  count=$(migration_count | tr -d '[:space:]')
  if [ "$count" != "1" ]; then
    echo "P00 verification failed: expected one successful V0001, found ${count}" >&2
    return 1
  fi
  echo "PASS: Flyway V0001 recorded exactly once"
}

wait_for_success "backend liveness" "$LIVENESS_URL"
wait_for_success "backend readiness with database" "$READINESS_URL"
wait_for_success "frontend HTTP" "$FRONTEND_URL"
assert_single_migration

compose restart backend
wait_for_success "backend readiness after restart" "$READINESS_URL"
assert_single_migration

compose stop postgres
wait_for_failure "readiness fails without database" "$READINESS_URL"
wait_for_success "liveness remains healthy without database" "$LIVENESS_URL"

compose start postgres
wait_for_success "readiness recovers with database" "$READINESS_URL"
assert_single_migration

echo "P00 verification passed"

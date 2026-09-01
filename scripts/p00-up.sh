#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
COMPOSE_FILE="$ROOT_DIR/infra/compose.yml"
ENV_FILE="$ROOT_DIR/infra/env/.env.example"

if [ -f "$ROOT_DIR/infra/env/.env" ]; then
  ENV_FILE="$ROOT_DIR/infra/env/.env"
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "P00 startup failed: Docker is not available" >&2
  exit 1
fi

docker compose --project-name gl-operations-p00 --env-file "$ENV_FILE" \
  --file "$COMPOSE_FILE" up --detach --build --wait

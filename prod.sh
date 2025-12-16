#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

timestamp() { date --iso-8601=seconds; }

on_exit() {
  rc=$?
  if [ $rc -eq 0 ]; then
    echo ">>> prod.sh: FINISHED SUCCESS rc=0 $(timestamp)"
  else
    echo ">>> prod.sh: FINISHED FAILED rc=$rc $(timestamp)" >&2
  fi
}
trap on_exit EXIT

echo ">>> prod.sh: START $(timestamp) args=$*"

BUILD_OPTS="${2:-}"

case "${1:-}" in
  down)
    echo ">>> prod.sh: stopping images $(timestamp)"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop || true
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    echo ">>> prod.sh: stop complete $(timestamp)"
    ;;

  build)
    echo ">>> prod.sh: Building images (no attach) $(timestamp)"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $BUILD_OPTS
    echo ">>> prod.sh: Build complete $(timestamp)"
    ;;

  restart)
    echo ">>> Restarting service (down + up -d) $(timestamp)"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    echo ">>> Restart finished $(timestamp)"
    ;;

  deploy)
    echo ">>> FULL DEPLOY START $(timestamp)"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $BUILD_OPTS
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    echo ">>> FULL DEPLOY SUCCESS $(timestamp)"
    ;;

  *)
    echo "Usage: ./prod.sh {build|restart|deploy|down} [--no-cache]"
    exit 1
    ;;
esac

#!/usr/bin/env bash

# 에러 발생 시 즉시 종료
set -e

COMPOSE_FILE="docker-compose.dev.yml"
ENV_FILE=".env.dev"

case "$1" in
  up)
    echo "Starting DEV containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up
    ;;

  build)
    echo "Building DEV containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up --build
    ;;

  detach)
    echo "Starting DEV containers in detached mode..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    ;;

  down)
    echo "Stopping DEV containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop || true

    echo "Removing DEV containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    ;;

  *)
    echo "Usage: ./dev.sh {build|up|detach|down}"
    exit 1
    ;;
esac

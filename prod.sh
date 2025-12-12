#!/usr/bin/env bash

set -e

COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

case "$1" in
  up)
    echo "Starting PROD containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up
    ;;

  build)
    echo "Building PROD containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up --build
    ;;

  detach)
    echo "Starting PROD containers in detached mode..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    ;;

  down)
    echo "Stopping PROD containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop || true

    echo "Removing PROD containers..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    ;;

  *)
    echo "Usage: ./prod.sh {build|up|detach|down}"
    exit 1
    ;;
esac

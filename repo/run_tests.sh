#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export MSYS_NO_PATHCONV=1
RUN_E2E="${RUN_E2E:-0}"
E2E_ONLY="${E2E_ONLY:-0}"
E2E_COMPOSE_PROJECT="${E2E_COMPOSE_PROJECT:-campusfit-e2e}"
E2E_WEB_PORT="${E2E_WEB_PORT:-3300}"
E2E_API_PORT="${E2E_API_PORT:-38080}"
E2E_MYSQL_PORT="${E2E_MYSQL_PORT:-33306}"

if [ "${E2E_ONLY}" != "1" ]; then
    echo "=== Backend Tests ==="
    docker run --rm \
        -v "${REPO_DIR}/services/api:/app" \
        -w /app \
        maven:3.9-eclipse-temurin-17 \
        bash -c "mvn clean test -B -Dspring.profiles.active=test"

    echo "=== Frontend Tests ==="
    docker run --rm \
        -v "${REPO_DIR}/apps/web:/app" \
        -w /app \
        node:20-alpine \
        sh -c "npm ci && npm test"
fi

if [ "${RUN_E2E}" != "1" ]; then
    exit 0
fi

echo "=== E2E Tests ==="
# Spin up the full stack, run Playwright, then tear down regardless of outcome
docker compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || \
    docker-compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || true

WEB_PORT="${E2E_WEB_PORT}" API_PORT="${E2E_API_PORT}" MYSQL_PORT="${E2E_MYSQL_PORT}" \
docker compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" up -d --build 2>/dev/null || \
    WEB_PORT="${E2E_WEB_PORT}" API_PORT="${E2E_API_PORT}" MYSQL_PORT="${E2E_MYSQL_PORT}" \
    docker-compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" up -d --build

for _ in $(seq 1 60); do
    WEB_STATUS="$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:${E2E_WEB_PORT}/sign-in" || true)"
    API_STATUS="$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:${E2E_API_PORT}/api/auth/sign-in" || true)"
    if [ "${WEB_STATUS}" = "200" ] && [ "${API_STATUS}" != "000" ] && [ -n "${API_STATUS}" ]; then
        break
    fi
    sleep 2
done

if [ "${WEB_STATUS:-000}" != "200" ] || [ "${API_STATUS:-000}" = "000" ]; then
    echo "E2E environment failed to become ready"
    docker compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" logs --tail=200 2>/dev/null || \
        docker-compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" logs --tail=200 2>/dev/null || true
    docker compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || \
        docker-compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || true
    exit 1
fi

E2E_EXIT=0
docker run --rm \
    -v "${REPO_DIR}/apps/e2e:/app" \
    -w /app \
    --network=host \
    -e APP_URL="http://localhost:${E2E_WEB_PORT}" \
    -e API_URL="http://localhost:${E2E_API_PORT}" \
    mcr.microsoft.com/playwright:v1.44.0-jammy \
    sh -c "npm install && npx playwright install chromium && npx playwright test" || E2E_EXIT=$?

docker compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || \
    docker-compose -p "${E2E_COMPOSE_PROJECT}" -f "${REPO_DIR}/docker-compose.yml" down -v 2>/dev/null || true

if [ $E2E_EXIT -ne 0 ]; then
    echo "E2E tests failed (exit $E2E_EXIT)"
    exit $E2E_EXIT
fi

echo "=== All tests passed ==="

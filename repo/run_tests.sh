#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export MSYS_NO_PATHCONV=1
RUN_E2E="${RUN_E2E:-0}"

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

if [ "${RUN_E2E}" != "1" ]; then
    exit 0
fi

echo "=== E2E Tests ==="
# Spin up the full stack, run Playwright, then tear down regardless of outcome
docker compose -f "${REPO_DIR}/docker-compose.yml" up -d --wait 2>/dev/null || \
    docker-compose -f "${REPO_DIR}/docker-compose.yml" up -d

E2E_EXIT=0
docker run --rm \
    -v "${REPO_DIR}/apps/e2e:/app" \
    --network=host \
    -e APP_URL="http://localhost:5173" \
    -e API_URL="http://localhost:8080" \
    mcr.microsoft.com/playwright:v1.44.0-jammy \
    sh -c "npm ci && npx playwright install chromium && npx playwright test" || E2E_EXIT=$?

docker compose -f "${REPO_DIR}/docker-compose.yml" down 2>/dev/null || \
    docker-compose -f "${REPO_DIR}/docker-compose.yml" down

if [ $E2E_EXIT -ne 0 ]; then
    echo "E2E tests failed (exit $E2E_EXIT)"
    exit $E2E_EXIT
fi

echo "=== All tests passed ==="

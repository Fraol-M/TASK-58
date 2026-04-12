#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export MSYS_NO_PATHCONV=1

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

echo "=== All tests passed ==="

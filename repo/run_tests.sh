#!/usr/bin/env bash
# =============================================================================
# run_tests.sh — Run CampusFit tests inside Docker containers
#
# Requires: Docker (no local Java, Maven, or Node installation needed)
#
# Backend  : JUnit 5 + Spring Boot @SpringBootTest with H2 in-memory DB
# Frontend : Vitest (Vue 3 + TypeScript)
#
# Usage:
#   ./run_tests.sh                          # run all suites
#   ./run_tests.sh --backend-only           # Java/Maven tests only
#   ./run_tests.sh --frontend-only          # Vitest tests only
#   ./run_tests.sh --test "*Integration*"   # filter by pattern (both suites)
#   ./run_tests.sh --backend-only --test "AuthService#signIn*"
#   ./run_tests.sh --frontend-only --test "operations"
# =============================================================================

set -euo pipefail

# ── Config ─────────────────────────────────────────────────────────────────────
REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAVEN_IMAGE="maven:3.9-eclipse-temurin-17"
NODE_IMAGE="node:20-alpine"

# Named Docker volumes — auto-created on first run, reused for speed
M2_CACHE_VOLUME="campusfit-m2-cache"           # Maven ~/.m2 repository
NPM_CACHE_VOLUME="campusfit-npm-cache"         # npm download cache
WEB_MODULES_VOLUME="campusfit-web-node-modules" # node_modules (Linux binaries)

# ── Colours ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

# ── Argument parsing ───────────────────────────────────────────────────────────
RUN_BACKEND=true
RUN_FRONTEND=true
TEST_FILTER=""

usage() {
    cat <<EOF

$(echo -e "${BOLD}run_tests.sh${NC}") — Run CampusFit tests in Docker (no local toolchain needed)

$(echo -e "${BOLD}Usage:${NC}")
  ./run_tests.sh [OPTIONS]

$(echo -e "${BOLD}Options:${NC}")
  --backend-only       Run only Java/Maven backend tests
  --frontend-only      Run only Vitest frontend tests
  --test PATTERN       Filter tests by pattern
                         Backend  : Maven -Dtest syntax
                                    e.g. "AuthService", "AuthService#signIn*",
                                         "*Integration*", "InboundWorkflow*"
                         Frontend : passed to vitest run as a path/name filter
                                    e.g. "operations", "useWorkflow"
  -h, --help           Show this help

$(echo -e "${BOLD}Docker volumes (auto-created, persist between runs):${NC}")
  ${M2_CACHE_VOLUME}           Maven local repository
  ${NPM_CACHE_VOLUME}          npm download cache
  ${WEB_MODULES_VOLUME}   node_modules (Linux binaries)

$(echo -e "${BOLD}Examples:${NC}")
  ./run_tests.sh                                     # all tests
  ./run_tests.sh --backend-only                      # backend only
  ./run_tests.sh --frontend-only                     # frontend only
  ./run_tests.sh --test "*Integration*"              # integration tests only
  ./run_tests.sh --backend-only --test "AuthService" # single backend class
  ./run_tests.sh --frontend-only --test "operations" # frontend module filter

EOF
    exit 0
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --backend-only)  RUN_FRONTEND=false; shift ;;
        --frontend-only) RUN_BACKEND=false;  shift ;;
        --test)
            [[ $# -lt 2 ]] && { echo -e "${RED}Error: --test requires a pattern argument${NC}"; exit 1; }
            TEST_FILTER="$2"; shift 2 ;;
        -h|--help) usage ;;
        *) echo -e "${RED}Unknown option: $1${NC}"; usage ;;
    esac
done

# ── Helpers ────────────────────────────────────────────────────────────────────
banner() {
    local title="$1"
    local width=50
    local line
    line=$(printf '═%.0s' $(seq 1 $width))
    echo -e "\n${BOLD}${BLUE}╔${line}╗${NC}"
    printf "${BOLD}${BLUE}║  %-${width}s║${NC}\n" "$title"
    echo -e "${BOLD}${BLUE}╚${line}╝${NC}\n"
}

log_info()    { echo -e "  ${BLUE}▶${NC}  $1"; }
log_filter()  { echo -e "  ${YELLOW}⚙${NC}  Filter: ${YELLOW}${1}${NC}\n"; }
log_pass()    { echo -e "  ${GREEN}✓${NC}  $1  ${GREEN}PASSED${NC}"; }
log_fail()    { echo -e "  ${RED}✗${NC}  $1  ${RED}FAILED${NC}  (exit $2)"; }

check_docker() {
    if ! command -v docker &>/dev/null; then
        echo -e "\n${RED}Error: Docker is not installed or not in PATH.${NC}"
        echo    "       Install Docker Desktop: https://docs.docker.com/get-docker/"
        exit 1
    fi
    if ! docker info &>/dev/null 2>&1; then
        echo -e "\n${RED}Error: Docker daemon is not running.${NC}"
        echo    "       Start Docker Desktop and try again."
        exit 1
    fi
}

# Git Bash on Windows auto-converts Unix-style paths — disable that
export MSYS_NO_PATHCONV=1

# ── Pre-flight ─────────────────────────────────────────────────────────────────
check_docker
echo ""
log_info "Repository: ${REPO_DIR}"
log_info "Suites    : $([ "$RUN_BACKEND" = true ] && echo -n "backend " || true)$([ "$RUN_FRONTEND" = true ] && echo -n "frontend" || true)"
[ -n "$TEST_FILTER" ] && log_filter "$TEST_FILTER"

BACKEND_EXIT=0
FRONTEND_EXIT=0

# ══════════════════════════════════════════════════════════════════════════════
# BACKEND TESTS
# JUnit 5 + Mockito + Spring Boot @SpringBootTest
# H2 in-memory DB (application-test.yml: ddl-auto=create-drop, flyway disabled)
# Test classes: 24 files across unit, controller, and integration suites
# ══════════════════════════════════════════════════════════════════════════════
if [ "$RUN_BACKEND" = true ]; then
    banner "Backend Tests  (JUnit 5 / Maven)"

    MVN_CMD="mvn test -B -Dspring.profiles.active=test"
    if [ -n "$TEST_FILTER" ]; then
        MVN_CMD="${MVN_CMD} -Dtest=${TEST_FILTER}"
        log_filter "$TEST_FILTER"
    fi

    log_info "Image  : ${MAVEN_IMAGE}"
    log_info "Command: ${MVN_CMD}"
    echo ""

    docker run --rm \
        -v "${REPO_DIR}/services/api:/app" \
        -v "${M2_CACHE_VOLUME}:/root/.m2" \
        -w /app \
        "${MAVEN_IMAGE}" \
        bash -c "${MVN_CMD}" \
    || BACKEND_EXIT=$?
fi

# ══════════════════════════════════════════════════════════════════════════════
# FRONTEND TESTS
# Vitest 1.x — Vue 3 + TypeScript + @vue/test-utils + happy-dom
# Test files: 23 spec files across modules (auth, dashboard, fitness, study,
#             operations, master-data, exports, notifications, components)
# ══════════════════════════════════════════════════════════════════════════════
if [ "$RUN_FRONTEND" = true ]; then
    banner "Frontend Tests  (Vitest)"

    if [ -n "$TEST_FILTER" ]; then
        VITEST_CMD="npx vitest run ${TEST_FILTER}"
        log_filter "$TEST_FILTER"
    else
        VITEST_CMD="npm test"
    fi

    log_info "Image  : ${NODE_IMAGE}"
    log_info "Command: npm ci && ${VITEST_CMD}"
    echo ""

    docker run --rm \
        -v "${REPO_DIR}/apps/web:/app" \
        -v "${WEB_MODULES_VOLUME}:/app/node_modules" \
        -v "${NPM_CACHE_VOLUME}:/root/.npm" \
        -w /app \
        "${NODE_IMAGE}" \
        sh -c "npm ci --cache /root/.npm && ${VITEST_CMD}" \
    || FRONTEND_EXIT=$?
fi

# ══════════════════════════════════════════════════════════════════════════════
# SUMMARY
# ══════════════════════════════════════════════════════════════════════════════
banner "Test Results"

OVERALL=0

if [ "$RUN_BACKEND" = true ]; then
    if [ $BACKEND_EXIT -eq 0 ]; then
        log_pass "Backend tests "
    else
        log_fail "Backend tests " "$BACKEND_EXIT"
        OVERALL=1
    fi
fi

if [ "$RUN_FRONTEND" = true ]; then
    if [ $FRONTEND_EXIT -eq 0 ]; then
        log_pass "Frontend tests"
    else
        log_fail "Frontend tests" "$FRONTEND_EXIT"
        OVERALL=1
    fi
fi

echo ""
if [ $OVERALL -eq 0 ]; then
    echo -e "  ${GREEN}${BOLD}All suites passed.${NC}"
else
    echo -e "  ${RED}${BOLD}One or more suites failed — scroll up to see the error output.${NC}"
fi
echo ""

exit $OVERALL

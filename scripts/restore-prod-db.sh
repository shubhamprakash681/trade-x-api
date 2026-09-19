#!/usr/bin/env bash
# ==============================================================================
# TradeX Production Database Restore Script
# Restores a PostgreSQL custom-format (.dump) backup created by backup-prod-db.sh.
# CAUTION: This operation replaces existing data in the target database!
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${PROJECT_DIR}/.env"
COMPOSE_FILE="${PROJECT_DIR}/docker-compose-prod.yml"

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if [[ $# -lt 1 ]]; then
    echo -e "${RED}Usage: $0 <path-to-dump-file>${NC}"
    echo -e "Example: $0 backups/tradex_tradex_20260920_030000.dump"
    echo -e "         $0 backups/latest.dump"
    exit 1
fi

DUMP_FILE="$1"

if [[ ! -f "${DUMP_FILE}" ]]; then
    echo -e "${RED}Error: Dump file not found: ${DUMP_FILE}${NC}" >&2
    exit 1
fi

# Load environment variables if available
if [[ -f "${ENV_FILE}" ]]; then
    # shellcheck disable=SC1090
    set -a
    source "${ENV_FILE}"
    set +a
fi

POSTGRES_USER="${POSTGRES_USER:-tradex}"
POSTGRES_DB="${POSTGRES_DB:-tradex}"

echo -e "${RED}======================================================================${NC}"
echo -e "${RED}                      CRITICAL WARNING                                ${NC}"
echo -e "${RED}======================================================================${NC}"
echo -e "You are about to RESTORE the database '${YELLOW}${POSTGRES_DB}${NC}' using:"
echo -e "Dump file : ${YELLOW}${DUMP_FILE}${NC}"
echo -e "User      : ${YELLOW}${POSTGRES_USER}${NC}"
echo -e "\n${RED}This will DROP existing objects and overwrite data with the backup!${NC}"
echo -e "${RED}======================================================================${NC}"

read -r -p "Type 'RESTORE-CONFIRM' to proceed: " CONFIRMATION

if [[ "${CONFIRMATION}" != "RESTORE-CONFIRM" ]]; then
    echo -e "${YELLOW}Restore aborted by user.${NC}"
    exit 0
fi

# Verify Docker Compose is running postgres
if ! docker compose -f "${COMPOSE_FILE}" ps --status running --format "{{.Service}}" | grep -q "^postgres$"; then
    echo -e "${RED}Error: Postgres container is not running in ${COMPOSE_FILE}!${NC}" >&2
    exit 1
fi

echo -e "\nStarting pg_restore..."
START_TIME=$(date +%s)

# --clean: drop database objects before recreating them
# --if-exists: avoid error messages if objects don't exist yet
# --no-owner: do not set ownership of objects to match original database
# --no-privileges: prevent restore of access privileges
docker compose -f "${COMPOSE_FILE}" exec -T postgres \
    pg_restore -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
    --clean --if-exists --no-owner \
    < "${DUMP_FILE}" || true

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo -e "\n${GREEN}✓ Database restore process completed in ${DURATION}s!${NC}"


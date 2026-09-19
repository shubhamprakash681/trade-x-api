#!/usr/bin/env bash
# ==============================================================================
# TradeX Production Database Backup Script
# Creates a transactionally consistent, compressed custom-format (.dump)
# backup of the PostgreSQL database running in Docker Compose.
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKUP_DIR="${PROJECT_DIR}/backups"
ENV_FILE="${PROJECT_DIR}/.env"
COMPOSE_FILE="${PROJECT_DIR}/docker-compose-prod.yml"

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== TradeX Production Database Backup ===${NC}"

# Load environment variables if available
if [[ -f "${ENV_FILE}" ]]; then
    # shellcheck disable=SC1090
    set -a
    source "${ENV_FILE}"
    set +a
else
    echo -e "${YELLOW}Warning: .env file not found at ${ENV_FILE}. Using defaults.${NC}"
fi

POSTGRES_USER="${POSTGRES_USER:-tradex}"
POSTGRES_DB="${POSTGRES_DB:-tradex}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"

# Ensure backup directory exists with restricted permissions
mkdir -p "${BACKUP_DIR}"
chmod 700 "${BACKUP_DIR}"

TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"
BACKUP_FILE="${BACKUP_DIR}/tradex_${POSTGRES_DB}_${TIMESTAMP}.dump"

echo -e "Database : ${YELLOW}${POSTGRES_DB}${NC}"
echo -e "User     : ${YELLOW}${POSTGRES_USER}${NC}"
echo -e "Target   : ${YELLOW}${BACKUP_FILE}${NC}"

# Verify Docker Compose is running postgres
if ! docker compose -f "${COMPOSE_FILE}" ps --status running --format "{{.Service}}" | grep -q "^postgres$"; then
    echo -e "${RED}Error: Postgres container is not running in ${COMPOSE_FILE}!${NC}" >&2
    exit 1
fi

echo -e "\nStarting pg_dump (format: custom compressed)..."
START_TIME=$(date +%s)

# Execute pg_dump directly inside the postgres container
# -Fc = Custom format (compressed, supports pg_restore parallel/selective restore)
docker compose -f "${COMPOSE_FILE}" exec -T postgres \
    pg_dump -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -Fc \
    > "${BACKUP_FILE}"

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# Validate backup file was created and is not empty
if [[ ! -s "${BACKUP_FILE}" ]]; then
    echo -e "${RED}Error: Backup file is empty or was not created!${NC}" >&2
    rm -f "${BACKUP_FILE}"
    exit 1
fi

FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
SHA256=$(sha256sum "${BACKUP_FILE}" | cut -d' ' -f1)
echo "${SHA256}  $(basename "${BACKUP_FILE}")" > "${BACKUP_FILE}.sha256"

echo -e "${GREEN}✓ Backup successfully completed in ${DURATION}s!${NC}"
echo -e "  File size : ${GREEN}${FILE_SIZE}${NC}"
echo -e "  SHA-256   : ${GREEN}${SHA256}${NC}"

# Create a symlink to latest backup
ln -sf "$(basename "${BACKUP_FILE}")" "${BACKUP_DIR}/latest.dump"

# Cleanup old backups exceeding retention policy
echo -e "\nApplying retention policy (keeping last ${RETENTION_DAYS} days)..."
find "${BACKUP_DIR}" -name "tradex_*.dump*" -type f -mtime +"${RETENTION_DAYS}" -delete
echo -e "${GREEN}✓ Retention cleanup complete.${NC}"
echo -e "${BLUE}===========================================${NC}"


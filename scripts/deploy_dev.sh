#!/bin/bash
set -e

APP_DIR="/opt/billage/backend"
JAR_PATH="${APP_DIR}/app.jar"
ENV_FILE="${APP_DIR}/.env"
TEMP_JAR="/home/${USER}/deploy/app.jar"
LOG_FILE="/var/log/billage/backend/app.log"

echo "[1/5] Validating files..."
[ ! -f "${TEMP_JAR}" ] && echo "JAR not found: ${TEMP_JAR}" && exit 1
[ ! -f "${ENV_FILE}" ] && echo ".env not found: ${ENV_FILE}" && exit 1

echo "[2/5] Loading environment variables..."
set -a
source ${ENV_FILE}
set +a

echo "[3/5] Stopping existing process..."
pkill -f "app.jar" && sleep 5 || true

echo "[4/5] Deploying..."
mv ${TEMP_JAR} ${JAR_PATH}

echo "[5/5] Starting application..."
nohup java -jar ${JAR_PATH} > ${LOG_FILE} 2>&1 &
sleep 5

pgrep -f "app.jar" > /dev/null && echo "✓ Deployed (PID: $(pgrep -f app.jar))" || exit 1
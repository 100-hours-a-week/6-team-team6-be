#!/bin/bash
set -e

APP_DIR="/opt/billage/backend"
TEMP_JAR="/home/${USER}/deploy/app.jar"

echo "[1/3] Validating..."
[ ! -f "${TEMP_JAR}" ] && echo "JAR not found" && exit 1

echo "[2/3] Deploying..."
pm2 stop billage-backend || true
mv ${TEMP_JAR} ${APP_DIR}/app.jar

echo "[3/3] Starting..."
pm2 start ${APP_DIR}/ecosystem.config.js
pm2 save

echo "Done"
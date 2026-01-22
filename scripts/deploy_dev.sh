#!/bin/bash

JAR_PATH="/home/${USER}/deploy/app.jar"

# 프로세스 확인 및 종료 (있으면 죽이고, 없으면 넘어감)
# pgrep -f: 프로세스 이름으로 ID 찾기
CURRENT_PID=$(pgrep -f app.jar)

if [ -n "$CURRENT_PID" ]; then
  kill -9 $CURRENT_PID
  sleep 2
fi

# 실행
nohup java -jar $JAR_PATH > /dev/null 2>&1 &
#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env.local"
ENV_EXAMPLE_FILE="$ROOT_DIR/.env.local.example"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE_FILE" "$ENV_FILE"
  echo ".env.local 파일을 생성했습니다. OPENAI_API_KEY 값을 채운 뒤 다시 실행하세요." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

if [[ -z "${OPENAI_API_KEY:-}" || "$OPENAI_API_KEY" == "여기에_OpenAI_API_KEY를_넣으세요" ]]; then
  echo ".env.local의 OPENAI_API_KEY 값을 실제 OpenAI API Key로 바꾼 뒤 다시 실행하세요." >&2
  exit 1
fi

export OPENAI_MODEL="${OPENAI_MODEL:-gpt-5-nano}"
export SERVER_PORT="${SERVER_PORT:-3001}"
JAVA_21_HOME="${JAVA_21_HOME:-/Users/rsy/Library/Java/JavaVirtualMachines/ms-21.0.9/Contents/Home}"
if [[ ! -x "$JAVA_21_HOME/bin/java" ]]; then
  JAVA_21_HOME="$(/usr/libexec/java_home -v 21)"
fi
export JAVA_HOME="$JAVA_21_HOME"

cd "$ROOT_DIR/apps/server"

for _ in 1 2 3; do
  existing_port_pids="$(lsof -tiTCP:"$SERVER_PORT" -sTCP:LISTEN 2>/dev/null || true)"
  existing_backend_pids="$(
    ps -axww -o pid=,command= |
      awk '/GradleWrapperMain :modules:bootstrap:mymentalcare:bootRun|mymentalcare-0.0.1-SNAPSHOT.jar|com.mymentalcare.server.bootstrap.MyMentalCareApplicationKt|org.gradle.launcher.daemon.bootstrap.GradleDaemon 8.14.3/ && !/awk/ {print $1}'
  )"

  if [[ -z "$existing_port_pids" && -z "$existing_backend_pids" ]]; then
    break
  fi

  echo "기존 myMentalCare 백엔드 프로세스를 종료합니다: ${existing_port_pids} ${existing_backend_pids}" >&2
  kill -9 $existing_port_pids $existing_backend_pids 2>/dev/null || true
  sleep 1
done

if lsof -tiTCP:"$SERVER_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "${SERVER_PORT} 포트를 정리하지 못했습니다. lsof -nP -iTCP:${SERVER_PORT} -sTCP:LISTEN 으로 확인하세요." >&2
  exit 1
fi

./gradlew --stop >/dev/null 2>&1 || true
./gradlew --no-daemon :modules:bootstrap:mymentalcare:bootJar

JAR_PATH="$ROOT_DIR/apps/server/modules/bootstrap/mymentalcare/build/libs/mymentalcare-0.0.1-SNAPSHOT.jar"
exec "$JAVA_HOME/bin/java" \
  -jar "$JAR_PATH" \
  --server.port="$SERVER_PORT"

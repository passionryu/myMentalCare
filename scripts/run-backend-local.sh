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

cd "$ROOT_DIR/apps/server"
exec ./gradlew :modules:bootstrap:mymentalcare:bootRun

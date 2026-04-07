#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$ROOT_DIR/.env.hom"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Arquivo .env.hom nao encontrado em $ROOT_DIR" >&2
  exit 1
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line#"${line%%[![:space:]]*}"}"
  line="${line%"${line##*[![:space:]]}"}"

  [[ -z "$line" || "$line" == \#* ]] && continue
  [[ "$line" == export\ * ]] && line="${line#export }"

  if [[ "$line" != *=* ]]; then
    continue
  fi

  name="${line%%=*}"
  value="${line#*=}"
  name="${name%"${name##*[![:space:]]}"}"
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"

  if [[ "$value" == \"*\" && "$value" == *\" ]]; then
    value="${value:1:${#value}-2}"
  elif [[ "$value" == \'*\' && "$value" == *\' ]]; then
    value="${value:1:${#value}-2}"
  fi

  export "$name=$value"
done < "$ENV_FILE"

export SPRING_PROFILES_ACTIVE=hom

cd "$ROOT_DIR"
echo "[benchmark-hom] executando benchmark Oracle com profile hom"
./gradlew --no-daemon --no-configuration-cache :backend:test --tests sgc.integracao.ProcessoSubprocessoPerformanceOracleIntegrationTest "$@"

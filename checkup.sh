#!/usr/bin/env bash

set -euo pipefail

CYAN='\033[0;36m'
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

function imprimir_passo() {
    local label="$1"
    echo -e "\n${CYAN}==> $label${NC}"
}

function falhar_passo() {
    local label="$1"
    local exit_code="$2"
    echo -e "${RED}FALHA: $label (exit $exit_code)${NC}"
    exit "$exit_code"
}

function invoke_passo() {
    local label="$1"
    shift
    imprimir_passo "$label"
    set +e
    "$@"
    local exit_code=$?
    set -e
    if [ "$exit_code" -ne 0 ]; then
        falhar_passo "$label" "$exit_code"
    fi
}

GRADLE_CMD="./gradlew"
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || -n "${WINDIR:-}" ]]; then
    if [[ -f "./gradlew.bat" ]]; then
        GRADLE_CMD="./gradlew.bat"
    fi
fi

if [ -t 1 ]; then
    clear
fi

invoke_passo 'Atualizar branch local' git pull
invoke_passo 'npm install' npm install --silent
invoke_passo 'Lint raiz' npm run lint
invoke_passo 'Typecheck raiz' npm run typecheck
invoke_passo 'Testes scripts' npm --prefix toolkit run test
invoke_passo 'Testes frontend' npm --prefix frontend run test
invoke_passo 'Testes backend' "$GRADLE_CMD" backend:test
invoke_passo 'Testes e2e' npx playwright test --project=chromium

echo -e "\n${GREEN}Tudo certo!${NC}"
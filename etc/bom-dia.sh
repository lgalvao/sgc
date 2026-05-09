#!/usr/bin/env bash

set -euo pipefail

CYAN='\033[0;36m'
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

function invoke_passo() {
    local label="$1"
    shift
    echo -e "\n${CYAN}==> $label${NC}"
    
    # Executa o comando e captura erro
    set +e
    "$@"
    local exit_code=$?
    set -e
    
    if [ $exit_code -ne 0 ]; then
        echo -e "${RED}FALHA: $label (exit $exit_code)${NC}"
        exit $exit_code
    fi
}

# Detectar comando Gradle adequado
GRADLE_CMD="./gradlew"
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || -n "${WINDIR:-}" ]]; then
    if [[ -f "./gradlew.bat" ]]; then
        GRADLE_CMD="./gradlew.bat"
    fi
fi

clear

invoke_passo 'git pull' git pull
invoke_passo 'Testes backend' "$GRADLE_CMD" backend:test

invoke_passo 'Atualizar npm' npm install -g npm@latest
invoke_passo 'Atualizar globais' npm update -g
invoke_passo 'Atualizar raiz' npm update

invoke_passo 'typecheck' npm run typecheck
invoke_passo 'lint' npm run lint

# Frontend
if [ -d "frontend" ]; then
    pushd frontend > /dev/null
    invoke_passo 'Frontend deps' npm update
    invoke_passo 'Testes frontend' npx vitest run
    popd > /dev/null
fi

invoke_passo 'Testes e2e mínimos' npx playwright test captura jornada

echo -e "\n${GREEN}Tudo certo!${NC}"

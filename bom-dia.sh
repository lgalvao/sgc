#!/usr/bin/env bash

# SGC Good Morning Script - Bash version for macOS/Linux/GitBash
set -euo pipefail

# Configuração de cores
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

# Limpar tela
clear

invoke_passo 'git pull' git pull
invoke_passo 'Testes backend' "$GRADLE_CMD" backend:test

# Garantir que pnpm está instalado
if ! command -v pnpm &> /dev/null; then
    invoke_passo 'Instalar pnpm' npm install -g pnpm
fi

invoke_passo 'Atualizar pnpm' pnpm self-update --silent || true
invoke_passo 'Atualizar globais' pnpm update -g
invoke_passo 'Atualizar raiz' pnpm update

invoke_passo 'Typecheck' pnpm run typecheck
invoke_passo 'Lint' pnpm run lint

# Frontend
if [ -d "frontend" ]; then
    pushd frontend > /dev/null
    invoke_passo 'Frontend deps' pnpm update
    invoke_passo 'Testes frontend' pnpm exec vitest run
    popd > /dev/null
fi

invoke_passo 'Testes e2e mínimos' pnpm exec playwright test captura jornada

echo -e "\n${GREEN}Tudo certo!${NC}"

#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

# Configuração de cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Suprimir saída do e2e/lifecycle.js
export SGC_SILENT_LIFECYCLE=true

# Variável para armazenar o resumo final
SUMMARY_MSG=""

# Garante que o cursor volte a aparecer e limpa arquivos temporários se o script for abortado (Ctrl+C)
trap 'tput cnorm; rm -f /tmp/smoke_test_*.log; echo -e "\n${RED}Execução abortada!${NC}"; exit 1' INT TERM

# Função para exibir o spinner de carregamento
function spinner() {
  local pid=$1
  local delay=0.1
  local spinstr='|/-\'
  
  # Esconde o cursor
  tput civis
  
  # Enquanto o processo com o PID informado estiver rodando
  while kill -0 "$pid" 2>/dev/null; do
    local temp=${spinstr#?}
    printf " [%c] " "$spinstr"
    local spinstr=$temp${spinstr%"$temp"}
    sleep $delay
    printf "\b\b\b\b\b"
  done
  
  # Limpa o espaço ocupado pelo spinner
  printf "     \b\b\b\b\b"
  
  # Mostra o cursor novamente
  tput cnorm
}

# Função auxiliar para rodar um passo
function run_step() {
  local step_name="$1"
  shift
  
  echo -ne "${YELLOW}${step_name}... ${NC}"
  
  # Arquivo temporário para guardar o log
  local tmp_log=$(mktemp /tmp/smoke_test_XXXXXX.log)
  
  # Roda o comando em background jogando a saída para o temp_log
  "$@" > "$tmp_log" 2>&1 &
  local cmd_pid=$!
  
  # Inicia o spinner atrelado ao PID do comando em background
  spinner "$cmd_pid"
  
  # Aguarda o comando terminar e captura o código de saída
  # Precisamos desativar o 'set -e' temporariamente para que o wait não mate o script se falhar
  set +e
  wait "$cmd_pid"
  local exit_code=$?
  set -e
  
  # Lê o log e exclui o arquivo temporário
  local output
  output=$(cat "$tmp_log")
  rm -f "$tmp_log"
  
  if [ $exit_code -eq 0 ]; then
      echo -e "${GREEN}OK${NC}"
      
      # Tentar extrair resumos dos outputs
      if [[ "$step_name" == "junit" ]]; then
          local passed=$(echo "$output" | grep -E "\+ Passed:" | tail -n 1 | awk '{print $3}')
          local total=$(echo "$output" | grep -E "Total:" | tail -n 1 | awk '{print $2}')
          if [[ -n "$passed" && -n "$total" ]]; then
              SUMMARY_MSG+="${CYAN}- Backend JUnit:${NC} $passed/$total testes passando\n"
          else
              # Se falhar o grep (ex: UP-TO-DATE)
              SUMMARY_MSG+="${CYAN}- Backend JUnit:${NC} Cache UP-TO-DATE ou 0 testes executados\n"
          fi
      elif [[ "$step_name" == "vitest" ]]; then
          local passed=$(echo "$output" | grep -E "Tests.*passed" | tail -n 1 | awk '{print $2}')
          if [[ -n "$passed" ]]; then
              SUMMARY_MSG+="${CYAN}- Frontend Vitest:${NC} $passed testes passando\n"
          fi
      elif [[ "$step_name" == "e2e fluxo geral" ]]; then
          local passed=$(echo "$output" | grep -oE "[0-9]+ passed" | awk '{print $1}')
          if [[ -n "$passed" ]]; then
              SUMMARY_MSG+="${CYAN}- E2E Playwright:${NC} $passed fluxos validados\n"
          fi
      fi
  else
      echo -e "${RED}FALHOU!${NC}"
      echo -e "\n${RED}=== Detalhes do erro (${step_name}) ===${NC}"
      echo "$output"
      echo -e "${RED}====================================${NC}\n"
      
      if [[ "$step_name" == "e2e fluxo geral" ]]; then
          echo -e "${YELLOW}Obs.: Os logs de inicialização do backend e frontend e2e estão em e2e/server.log${NC}"
      fi
      
      exit 1
  fi
}

echo "Iniciando bateria de smoke tests..."
echo "-----------------------------------"

run_step "junit" ./gradlew :backend:test
run_step "typecheck" npm run typecheck -s
run_step "lint" npm run lint -s
run_step "vitest" bash -c "cd frontend && npx vitest run --reporter=dot"
run_step "e2es fluxo geral" npx playwright test captura jornada --reporter=dot

echo "-----------------------------------"
if [[ -n "$SUMMARY_MSG" ]]; then
    echo -e "${CYAN}=== Resumo da Execução ===${NC}"
    echo -e "$SUMMARY_MSG"
    echo "-----------------------------------"
fi
echo -e "${GREEN}Finalizado com sucesso! Tudo em ordem.${NC}"

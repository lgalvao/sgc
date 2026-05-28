#!/bin/bash
# Script utilitário para obter o token JWT de teste para o WuppieFuzz no Linux/WSL (Bash)

BASE_URL=${1:-"http://localhost:10000"}
TITULO=${2:-"191919"}
SENHA=${3:-"senha"}

echo "--------------------------------------------------------"
echo "SGC - Obtendo token de autenticação para o WuppieFuzz"
echo "Alvo: $BASE_URL"
echo "Usuário de Teste: $TITULO"
echo "--------------------------------------------------------"

# Caminho temporário para armazenar os cookies
COOKIE_JAR=$(mktemp)

# Realiza a chamada HTTP salvando os cookies recebidos
RESPONSE=$(curl -s -i -X POST "$BASE_URL/api/usuarios/login" \
  -H "Content-Type: application/json" \
  -c "$COOKIE_JAR" \
  -d "{\"tituloEleitoral\":\"$TITULO\",\"senha\":\"$SENHA\"}")

# Extrai o cookie 'jwtToken'
if [ -f "$COOKIE_JAR" ]; then
  TOKEN=$(grep "jwtToken" "$COOKIE_JAR" | awk '{print $7}')
  rm -f "$COOKIE_JAR"
fi

if [ -n "$TOKEN" ]; then
  echo -e "\n\033[0;32m[Sucesso] Autenticação concluída e JWT recuperado!\033[0m"
  echo -e "\033[0;33mInsira o cabeçalho a seguir nas requisições do seu fuzzer:\033[0m"
  echo -e "Authorization: Bearer $TOKEN"
  echo -e "\n\033[0;37mToken bruto para cópia rápida:\033[0m"
  echo -e "\033[0;32m$TOKEN\033[0m"
else
  echo -e "\n\033[0;31m[Falha] Não foi possível recuperar o token JWT.\033[0m"
  echo -e "Certifique-se de que o backend do SGC está rodando no perfil 'e2e' em $BASE_URL."
  echo -e "Comando recomendado: ./gradlew :backend:bootRun -PENV=e2e"
  echo -e "\nResposta bruta do servidor:"
  echo "$RESPONSE"
  exit 1
fi

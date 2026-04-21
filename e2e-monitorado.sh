#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

ARQUIVO_SAIDA="/tmp/e2e-monitorado.log"
NIVEL_LOG="warn"
MODO_MONITORAMENTO="sim"
TEMPO_MINIMO_HTTP_MS="5"
TEMPO_MINIMO_JAVA_MS="5"
SPEC=""
ARGS_PLAYWRIGHT=()

uso() {
  cat <<EOF_USO
Uso: ./e2e-monitorado.sh <spec_playwright> [opcoes_playwright]

Opcoes:
  --saida <arquivo>         Arquivo de saida do log monitorado (padrao: $ARQUIVO_SAIDA)
  --nivel-log <nivel>       Nivel de log normal da aplicacao (padrao: $NIVEL_LOG)
  --monitoramento <sim|nao> Liga ou desliga o monitoramento (padrao: $MODO_MONITORAMENTO)
  --tempo-http-ms <ms>      Limite minimo para log HTTP (padrao: $TEMPO_MINIMO_HTTP_MS)
  --tempo-java-ms <ms>      Limite minimo para log Java (padrao: $TEMPO_MINIMO_JAVA_MS)
  -h, --help                Mostra esta ajuda

Exemplo:
  ./e2e-monitorado.sh e2e/jornada.spec.ts --workers=1
EOF_USO
}

if [[ $# -eq 0 ]]; then
  uso
  exit 1
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --saida)
      ARQUIVO_SAIDA="$2"
      shift 2
      ;;
    --nivel-log)
      NIVEL_LOG="$2"
      shift 2
      ;;
    --monitoramento)
      MODO_MONITORAMENTO="$2"
      shift 2
      ;;
    --tempo-http-ms)
      TEMPO_MINIMO_HTTP_MS="$2"
      shift 2
      ;;
    --tempo-java-ms)
      TEMPO_MINIMO_JAVA_MS="$2"
      shift 2
      ;;
    -h|--help)
      uso
      exit 0
      ;;
    --)
      shift
      ARGS_PLAYWRIGHT+=("$@")
      break
      ;;
    *)
      if [[ -z "$SPEC" ]]; then
        SPEC="$1"
      else
        ARGS_PLAYWRIGHT+=("$1")
      fi
      shift
      ;;
  esac
done

if [[ -z "$SPEC" ]]; then
  echo "ERRO: informe o spec do Playwright."
  uso
  exit 1
fi

mkdir -p "$(dirname "$ARQUIVO_SAIDA")"

export SGC_MONITORAMENTO="$MODO_MONITORAMENTO"
export SGC_MONITORAMENTO_NIVEL_LOG="$NIVEL_LOG"
export SGC_MONITORAMENTO_TEMPO_MINIMO_HTTP_MS="$TEMPO_MINIMO_HTTP_MS"
export SGC_MONITORAMENTO_TEMPO_MINIMO_JAVA_MS="$TEMPO_MINIMO_JAVA_MS"

echo "Executando E2E monitorado..."
echo "Spec: $SPEC"
echo "Log: $ARQUIVO_SAIDA"
echo "Monitoramento: $SGC_MONITORAMENTO"
echo "Nivel de log: $SGC_MONITORAMENTO_NIVEL_LOG"
echo "Limites: http=${SGC_MONITORAMENTO_TEMPO_MINIMO_HTTP_MS}ms java=${SGC_MONITORAMENTO_TEMPO_MINIMO_JAVA_MS}ms"

set -o pipefail
npx playwright test "$SPEC" --workers=1 "${ARGS_PLAYWRIGHT[@]}" 2>&1 | tee "$ARQUIVO_SAIDA"

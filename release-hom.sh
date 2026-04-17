#!/usr/bin/env bash
set -euo pipefail

NOME_SISTEMA="sgc"
CONTAINER_CLI="${CONTAINER_CLI:-podman}"
REGISTRY="${REGISTRY:-registry.tre-pe.gov.br/sesel}"
PORTA_HOST="${PORTA_HOST:-8980}"
PORTA_CONTAINER="${PORTA_CONTAINER:-10000}"
DB_URL="${DB_URL:-jdbc:oracle:thin:@desenvolvimentobd.tre-pe.gov.br:1521:admdes2}"
TAG="${TAG:-}"
SEM_BUILD=false
SEM_IMAGEM=false
SEM_PUSH=false
SEM_DEPLOY=false
SEM_CACHE=false
SEM_PULL=false
TIMEOUT_SUBIDA_SEGUNDOS="${TIMEOUT_SUBIDA_SEGUNDOS:-120}"
ARQUIVO_JAR_DOCKER="sgc.jar"
ARQUIVO_ENV=".env.hom"
ARQUIVO_COMPOSE="compose.hom.yaml"

uso() {
  cat <<EOF_USO
Uso: ./release-hom.sh [opcoes]

Opcoes:
  --container-cli <podman|docker>  CLI de container (padrao: $CONTAINER_CLI)
  --registry <url>                 Registry da imagem (padrao: $REGISTRY)
  --porta-host <porta>             Porta publicada no host (padrao: $PORTA_HOST)
  --porta-container <porta>        Porta interna da aplicacao (padrao: $PORTA_CONTAINER)
  --db-url <url>                   URL JDBC usada no compose
  --tag <tag>                      Tag da imagem; por padrao usa a versao Gradle
  --sem-build                      Nao executa build Gradle
  --sem-imagem                     Nao constroi imagem
  --sem-push                       Nao envia imagem ao registry
  --sem-deploy                     Nao executa compose down/up
  --sem-cache                      Executa build da imagem sem cache
  --sem-pull                       Nao atualiza a imagem base no build
  --timeout-subida <segundos>      Tempo maximo para aguardar subida (padrao: $TIMEOUT_SUBIDA_SEGUNDOS)
  -h, --help                       Mostra esta ajuda
EOF_USO
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --container-cli)
      CONTAINER_CLI="$2"
      shift 2
      ;;
    --registry)
      REGISTRY="$2"
      shift 2
      ;;
    --porta-host)
      PORTA_HOST="$2"
      shift 2
      ;;
    --porta-container)
      PORTA_CONTAINER="$2"
      shift 2
      ;;
    --db-url)
      DB_URL="$2"
      shift 2
      ;;
    --tag)
      TAG="$2"
      shift 2
      ;;
    --sem-build)
      SEM_BUILD=true
      shift
      ;;
    --sem-imagem)
      SEM_IMAGEM=true
      shift
      ;;
    --sem-push)
      SEM_PUSH=true
      shift
      ;;
    --sem-deploy)
      SEM_DEPLOY=true
      shift
      ;;
    --sem-cache)
      SEM_CACHE=true
      shift
      ;;
    --sem-pull)
      SEM_PULL=true
      shift
      ;;
    --timeout-subida)
      TIMEOUT_SUBIDA_SEGUNDOS="$2"
      shift 2
      ;;
    -h|--help)
      uso
      exit 0
      ;;
    *)
      echo "ERRO: opcao desconhecida: $1"
      uso
      exit 1
      ;;
  esac
done

CONTAINER_CLI_NOME="$(basename "$CONTAINER_CLI")"
if [[ "$CONTAINER_CLI_NOME" == "podman" || "$CONTAINER_CLI_NOME" == "podman.exe" ]]; then
  export PODMAN_COMPOSE_WARNING_LOGS=false
fi

limpar_artefato_temporario() {
  rm -f "$ARQUIVO_JAR_DOCKER"
}

obter_valor_container() {
  local formato="$1"
  "$CONTAINER_CLI" inspect "$NOME_SISTEMA" --format "$formato" 2>/dev/null || true
}

aguardar_subida() {
  local inicio agora limite status reiniciando codigo_saida logs
  inicio="$(date +%s)"
  limite=$((inicio + TIMEOUT_SUBIDA_SEGUNDOS))
  echo "==> Monitorando subida do container por ate $TIMEOUT_SUBIDA_SEGUNDOS segundos"

  while true; do
    agora="$(date +%s)"
    if (( agora >= limite )); then
      echo "==> Logs recentes do container"
      "$CONTAINER_CLI" logs --tail 120 "$NOME_SISTEMA" || true
      echo "ERRO: timeout aguardando a subida do container $NOME_SISTEMA."
      exit 1
    fi

    status="$(obter_valor_container '{{.State.Status}}')"
    reiniciando="$(obter_valor_container '{{.State.Restarting}}')"
    codigo_saida="$(obter_valor_container '{{.State.ExitCode}}')"

    if [[ "$status" == "exited" || "$status" == "dead" || "$reiniciando" == "true" ]]; then
      echo "==> Logs recentes do container"
      "$CONTAINER_CLI" logs --tail 120 "$NOME_SISTEMA" || true
      echo "ERRO: container $NOME_SISTEMA ficou em estado invalido: status=$status restarting=$reiniciando exitCode=$codigo_saida"
      exit 1
    fi

    logs="$("$CONTAINER_CLI" logs --tail 120 "$NOME_SISTEMA" 2>/dev/null || true)"
    if grep -q "Started Sgc" <<< "$logs"; then
      echo "==> Aplicacao iniciada com sucesso"
      return
    fi

    sleep 5
  done
}

trap limpar_artefato_temporario EXIT

if [[ ! -f "$ARQUIVO_ENV" ]]; then
  echo "ERRO: arquivo $ARQUIVO_ENV nao encontrado."
  exit 1
fi
if [[ ! -f "$ARQUIVO_COMPOSE" ]]; then
  echo "ERRO: arquivo $ARQUIVO_COMPOSE nao encontrado."
  exit 1
fi

if [[ "$SEM_BUILD" == false ]]; then
  echo "==> Executando build do frontend e do backend"
  ./gradlew --no-daemon clean copyFrontend :backend:bootJar -x test
else
  echo "==> Build Gradle ignorado por parametro"
fi

if [[ -n "$TAG" ]]; then
  VERSAO="$TAG"
else
  VERSAO="$(./gradlew --no-daemon -q properties --property version | awk -F': ' '/^version:/ { print $2; exit }')"
  if [[ -z "$VERSAO" ]]; then
    echo "ERRO: nao foi possivel identificar a versao do projeto."
    exit 1
  fi
fi

TAG_VERSAO="$REGISTRY/$NOME_SISTEMA:$VERSAO"
TAG_LATEST="$REGISTRY/$NOME_SISTEMA:latest"

if [[ "$SEM_IMAGEM" == false ]]; then
  JAR_ORIGEM="$(find backend/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | head -n 1)"
  if [[ -z "$JAR_ORIGEM" ]]; then
    echo "ERRO: jar do backend nao encontrado em backend/build/libs. Rode sem --sem-build ou execute o build antes."
    exit 1
  fi

  echo "==> Preparando artefato Docker: $JAR_ORIGEM"
  cp "$JAR_ORIGEM" "$ARQUIVO_JAR_DOCKER"

  ARGS_BUILD=(build -t "$TAG_VERSAO" -t "$TAG_LATEST")
  if [[ "$SEM_CACHE" == true ]]; then
    ARGS_BUILD+=(--no-cache)
  fi
  if [[ "$SEM_PULL" == false ]]; then
    ARGS_BUILD+=(--pull)
  fi
  ARGS_BUILD+=(.)

  echo "==> Construindo imagem com $CONTAINER_CLI: $TAG_VERSAO"
  "$CONTAINER_CLI" "${ARGS_BUILD[@]}"
else
  echo "==> Build da imagem ignorado por parametro"
fi

if [[ "$SEM_PUSH" == false ]]; then
  echo "==> Enviando imagem para o registry"
  "$CONTAINER_CLI" push "$TAG_VERSAO"
  "$CONTAINER_CLI" push "$TAG_LATEST"
else
  echo "==> Push ignorado por parametro"
fi

export SGC_IMAGE="$TAG_VERSAO"
export PORTA_HOST
export PORTA_CONTAINER
export DB_URL

if [[ "$SEM_DEPLOY" == false ]]; then
  echo "==> Subindo homologacao com a versao $VERSAO"
  "$CONTAINER_CLI" compose -f "$ARQUIVO_COMPOSE" down --remove-orphans
  "$CONTAINER_CLI" compose -f "$ARQUIVO_COMPOSE" up --detach --remove-orphans
  aguardar_subida
else
  echo "==> Deploy do compose ignorado por parametro"
fi

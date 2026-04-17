#!/usr/bin/env bash
set -euo pipefail

NOME_SISTEMA="sgc"
CONTAINER_CLI="${CONTAINER_CLI:-docker}"
REGISTRY="${REGISTRY:-registry.tre-pe.gov.br/sesel}"
PORTA_HOST="${PORTA_HOST:-8980}"
PORTA_CONTAINER="${PORTA_CONTAINER:-10000}"
ARQUIVO_JAR_DOCKER="sgc.jar"
ARQUIVO_ENV=".env.hom"

limpar_artefato_temporario() {
  rm -f "$ARQUIVO_JAR_DOCKER"
}

trap limpar_artefato_temporario EXIT

if [[ ! -f "$ARQUIVO_ENV" ]]; then
  echo "ERRO: arquivo $ARQUIVO_ENV nao encontrado."
  exit 1
fi

echo "==> Executando build do frontend e do backend"
./gradlew --no-daemon clean copyFrontend :backend:bootJar -x test

VERSAO="$(./gradlew --no-daemon -q properties --property version | awk -F': ' '/^version:/ { print $2; exit }')"
if [[ -z "$VERSAO" ]]; then
  echo "ERRO: nao foi possivel identificar a versao do projeto."
  exit 1
fi

JAR_ORIGEM="$(find backend/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | head -n 1)"
if [[ -z "$JAR_ORIGEM" ]]; then
  echo "ERRO: jar do backend nao encontrado em backend/build/libs."
  exit 1
fi

TAG_VERSAO="$REGISTRY/$NOME_SISTEMA:$VERSAO"
TAG_LATEST="$REGISTRY/$NOME_SISTEMA:latest"

echo "==> Preparando artefato Docker: $JAR_ORIGEM"
cp "$JAR_ORIGEM" "$ARQUIVO_JAR_DOCKER"

echo "==> Construindo imagem com $CONTAINER_CLI: $TAG_VERSAO"
"$CONTAINER_CLI" build --no-cache --pull -t "$TAG_VERSAO" -t "$TAG_LATEST" .

echo "==> Enviando imagem para o registry"
"$CONTAINER_CLI" push "$TAG_VERSAO"
"$CONTAINER_CLI" push "$TAG_LATEST"

echo "==> Gerando compose.yaml para homologacao"
cat > compose.yaml <<EOF_COMPOSE
services:
  $NOME_SISTEMA:
    image: $TAG_VERSAO
    container_name: $NOME_SISTEMA
    restart: unless-stopped
    ports:
      - "$PORTA_HOST:$PORTA_CONTAINER"
    env_file:
      - $ARQUIVO_ENV
    environment:
      TZ: America/Recife
      SPRING_PROFILES_ACTIVE: hom
      SERVER_PORT: "$PORTA_CONTAINER"
      LOGGING_FILE_NAME: /var/log/$NOME_SISTEMA/$NOME_SISTEMA.log
    volumes:
      - log-data:/var/log/$NOME_SISTEMA

volumes:
  log-data:
EOF_COMPOSE

echo "==> Subindo homologacao com a versao $VERSAO"
"$CONTAINER_CLI" compose down --remove-orphans
"$CONTAINER_CLI" compose up -d --remove-orphans

#!/bin/bash

set -euo pipefail

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPTS_DIR/../deploy" && pwd)"

if command -v keytool >/dev/null 2>&1; then
    KEYTOOL_BIN="keytool"
elif [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/keytool" ]; then
    KEYTOOL_BIN="$JAVA_HOME/bin/keytool"
else
    echo "Erro: keytool nao encontrado no PATH nem em JAVA_HOME/bin/keytool."
    exit 1
fi

CERT_TRE="$DEPLOY_DIR/cert-tre.cer"
CERT_FOR="$DEPLOY_DIR/cert-for.cer"

if [ ! -f "$CERT_TRE" ]; then
    echo "Erro: certificado nao encontrado: $CERT_TRE"
    exit 1
fi

if [ ! -f "$CERT_FOR" ]; then
    echo "Erro: certificado nao encontrado: $CERT_FOR"
    exit 1
fi

"$KEYTOOL_BIN" -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-tre -file "$CERT_TRE"
"$KEYTOOL_BIN" -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-for -file "$CERT_FOR"

echo "Certificados importados com sucesso."

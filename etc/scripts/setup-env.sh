#!/bin/bash
set -e

echo "----------------------------------------------------------------"
echo "Inicializando Configuração do Ambiente (Linux/macOS)"
echo "----------------------------------------------------------------"

# --- 1. Node.js ---
if ! command -v node &> /dev/null; then
    echo "[!] Node.js não encontrado. Por favor, instale via nvm, brew ou apt."
    exit 1
else
    echo "[✔] Node.js já está instalado."
fi

node -v
npm -v

# --- 2. Java ---
if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ]; then
    echo "[✔] JAVA_HOME já definido: $JAVA_HOME"
else
    echo "[!] AVISO: JAVA_HOME não está definido em seu ambiente. Verifique a instalação do seu JDK."
fi

# --- 3. Certificados SSL ---
echo "----------------------------------------------------------------"
echo "Importando Certificados Locais para o Java (Keystore)"
echo "----------------------------------------------------------------"

if [ -n "$JAVA_HOME" ]; then
    CACERTS="$JAVA_HOME/lib/security/cacerts"
    if [ ! -f "$CACERTS" ]; then
        CACERTS="$JAVA_HOME/jre/lib/security/cacerts"
    fi

    KEYTOOL="$JAVA_HOME/bin/keytool"
    CERTDIR="./backend/etc/deploy"

    import_cert() {
        local certName=$1
        local alias=$2
        local certPath="$CERTDIR/$certName"

        if [ -f "$certPath" ]; then
            echo "[*] Processando $alias..."
            
            # Remove o alias se existir (ignora erro)
            $KEYTOOL -delete -alias "$alias" -keystore "$CACERTS" -storepass changeit -noprompt 2> /dev/null || true

            # Importa com sudo pois certs do java requere previlegio root no linux/mac
            echo "[*] Senha de sudo pode ser solicitada para importar certificado no java cacerts."
            if sudo $KEYTOOL -import -trustcacerts -alias "$alias" -file "$certPath" -keystore "$CACERTS" -storepass changeit -noprompt; then
                echo "[✔] $alias importado."
            else
                echo "[!] Falha ao importar $alias."
            fi
        else
            echo "[!] Arquivo não encontrado: $certPath"
        fi
    }

    if [ -f "$KEYTOOL" ]; then
        import_cert "cert-for.cer" "cert-fortinet"
        import_cert "cert-tre.cer" "cert-tre-pe"
    else
        echo "[!] Keytool não encontrado em $KEYTOOL"
    fi
else
    echo "[!] JAVA_HOME não definido, pulando importação de certificados."
fi

# --- 4. Dependências do Projeto ---
echo "----------------------------------------------------------------"
echo "Instalando Dependências do Projeto"
echo "----------------------------------------------------------------"

echo "[*] Configurando npm para ignorar erros estritos de SSL..."
npm config set strict-ssl false

echo "[*] Executando 'npm install' na raiz..."
npm install

if [ -d "frontend" ]; then
    echo "[*] Executando 'npm install' no frontend..."
    cd frontend
    npm install
    cd ..
else
    echo "[!] Pasta 'frontend' não encontrada."
fi

echo "[*] Instalando Playwright (Chromium)..."
export NODE_TLS_REJECT_UNAUTHORIZED=0
npx playwright install chromium

echo "----------------------------------------------------------------"
echo "Configuração Concluída!"
echo "----------------------------------------------------------------"
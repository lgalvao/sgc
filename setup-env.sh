#!/bin/bash

# setup-env.sh
# Configura o ambiente de desenvolvimento com NVM, Node.js, SDKMAN e Java.
# Otimizado para sistemas Ubuntu/Unix-like.
# INCLUI: Importação de certificados locais para Java/Gradle.

# Parar no primeiro erro
set -e

echo "----------------------------------------------------------------"
echo "Inicializando Configuração do Ambiente"
echo "----------------------------------------------------------------"

# Garantir que caminhos comuns de binários estejam no PATH
export PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Verificação robusta de ferramentas essenciais
verificar_ferramenta() {
    local ferramenta=$1
    if ! command -v "$ferramenta" &> /dev/null && [ ! -x "/usr/bin/$ferramenta" ] && [ ! -x "/bin/$ferramenta" ] && [ ! -x "/usr/local/bin/$ferramenta" ]; then
        return 1
    fi
    return 0
}

for ferramenta in curl unzip zip; do
    if ! verificar_ferramenta "$ferramenta"; then
        echo "[!] Erro: '$ferramenta' não foi encontrado. Por favor, instale-o antes de continuar."
        exit 1
    fi
done

# --- Configuração para Aceitar Certificados Auto-assinados (Curl & Wget) ---
CURLRC="$HOME/.curlrc"
CURLRC_BAK="$HOME/.curlrc.bak.$(date +%s)"
WGETRC="$HOME/.wgetrc"
WGETRC_BAK="$HOME/.wgetrc.bak.$(date +%s)"

limpar_configs_ssl() {
    if [ -f "$CURLRC_BAK" ]; then mv "$CURLRC_BAK" "$CURLRC"; elif [ -f "$CURLRC" ]; then rm "$CURLRC"; fi
    if [ -f "$WGETRC_BAK" ]; then mv "$WGETRC_BAK" "$WGETRC"; elif [ -f "$WGETRC" ]; then rm "$WGETRC" ; fi
}

trap limpar_configs_ssl EXIT

echo "[*] Configurando curl e wget para aceitar certificados auto-assinados..."
if [ -f "$CURLRC" ]; then cp "$CURLRC" "$CURLRC_BAK"; fi
echo "insecure" >> "$CURLRC"

if [ -f "$WGETRC" ]; then cp "$WGETRC" "$WGETRC_BAK"; fi
echo "check_certificate = off" >> "$WGETRC"


# --- 1. Instalação do NVM ---
export NVM_DIR="$HOME/.nvm"
export NVM_NODEJS_ORG_MIRROR=https://nodejs.org/dist

if [ -d "$NVM_DIR" ]; then
    echo "[✔] Diretório do NVM já existe."
else
    echo "[*] Instalando NVM (v0.40.1)..."
    curl -k -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
fi

[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"
if ! command -v nvm &> /dev/null; then
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
fi

# --- 2. Instalação do Node.js ---
echo "[*] Instalando Node.js (LTS)..."

if ! nvm install --lts; then
    echo "[!] 'nvm install --lts' falhou. Tentando versão explícita v22.13.0..."
    nvm install v22.13.0
fi

nvm use --lts || nvm use v22.13.0
nvm alias default "lts/*" || nvm alias default v22.13.0

# VERIFICAR EXECUÇÃO DO NODE
echo "[*] Verificando instalação do Node.js..."
if ! node -e 'console.log("Node is working")' &> /dev/null; then
    echo "[!] ERRO CRÍTICO: Node.js falhou ao executar."
    node -v 2>&1 || true
    if node -v 2>&1 | grep -q "libatomic"; then
        echo ""
        echo "    [DEPENDÊNCIA FALTANDO]: libatomic.so.1"
        echo "    Peça a um admin para rodar: sudo apt-get install -y libatomic1"
    fi
    exit 1
fi
echo "[✔] Node.js $(node -v) e npm $(npm -v) prontos."

# --- 3. Instalação do SDKMAN ---
export SDKMAN_DIR="$HOME/.sdkman"
if [ -d "$SDKMAN_DIR" ]; then
    echo "[✔] SDKMAN! já está instalado."
else
    echo "[*] Instalando SDKMAN!..."
    curl -k -s "https://get.sdkman.io" | bash
fi

if [[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]]; then
    source "$SDKMAN_DIR/bin/sdkman-init.sh"
fi

SDKMAN_CONFIG="$SDKMAN_DIR/etc/config"
if [ -f "$SDKMAN_CONFIG" ]; then
    set_sdkman_config() {
        local key=$1
        local value=$2
        if grep -q "$key" "$SDKMAN_CONFIG"; then
            sed -i "s/$key=.*/$key=$value/g" "$SDKMAN_CONFIG"
        else
            echo "$key=$value" >> "$SDKMAN_CONFIG"
        fi
    }
    set_sdkman_config "sdkman_insecure_ssl" "true"
    set_sdkman_config "sdkman_auto_answer" "true"
fi


# --- 4. Instalação do Java ---

obter_versao_java_recente() {
    local version=$1
    local vendor=$2
    sdk list java 2>/dev/null | grep -E " $version\.[0-9.]*-$vendor " | head -n 1 | awk '{print $NF}'
}

echo "[*] Detectando Java 21 (Corretto)..."
JAVA_21_ID=$(obter_versao_java_recente "21" "amzn") || echo ""
[ -z "$JAVA_21_ID" ] && JAVA_21_ID="21.0.9-amzn"

if sdk list java | grep -q "installed" | grep -q "$JAVA_21_ID"; then
    echo "[✔] Java $JAVA_21_ID já instalado."
else
    echo "    Instalando: $JAVA_21_ID"
    echo "Y" | sdk install java "$JAVA_21_ID" || true
fi

echo "[*] Detectando Java 25 (Corretto)..."
JAVA_25_ID=$(obter_versao_java_recente "25" "amzn") || echo ""
if [ -n "$JAVA_25_ID" ]; then
    if sdk list java | grep -q "installed" | grep -q "$JAVA_25_ID"; then
        echo "[✔] Java $JAVA_25_ID já instalado."
    else
        echo "    Instalando: $JAVA_25_ID"
        echo "n" | sdk install java "$JAVA_25_ID" || true
    fi
else
     echo "    Java 25 (amzn) não encontrado."
fi

echo "[*] Definindo Java 21 como padrão..."
sdk default java "$JAVA_21_ID" || true


# --- CORREÇÃO DE CERTIFICADOS LOCAIS PARA GRADLE/JAVA ---
echo "----------------------------------------------------------------"
echo "Importando Certificados Locais para o Java (Fix Gradle)"
echo "----------------------------------------------------------------"

# Encontrar o local do cacerts
JAVA_HOME_ATUAL=$(sdk home java "$JAVA_21_ID")
KEYSTORE="$JAVA_HOME_ATUAL/lib/security/cacerts"

if [ ! -f "$KEYSTORE" ]; then
    KEYSTORE="$JAVA_HOME_ATUAL/conf/security/cacerts"
fi

CERT_DIR="backend/etc/deploy"

if [ -w "$KEYSTORE" ]; then
    importar_cert_local() {
        local cert_file=$1
        local alias=$2
        
        if [ -f "$cert_file" ]; then
            echo "[*] Importando $cert_file como $alias..."
            
            # Remove o alias se já existir para evitar erro
            "$JAVA_HOME_ATUAL/bin/keytool" -delete -alias "$alias" -keystore "$KEYSTORE" -storepass changeit -noprompt 2>/dev/null || true
            
            "$JAVA_HOME_ATUAL/bin/keytool" -import -trustcacerts -alias "$alias" -file "$cert_file" -keystore "$KEYSTORE" -storepass changeit -noprompt 2>/dev/null
            
            if [ $? -eq 0 ]; then
                echo "[✔] Sucesso."
            else
                echo "[!] Falha ao importar $alias."
            fi
        else
            echo "[!] Arquivo de certificado não encontrado: $cert_file"
        fi
    }

    # Importa os certificados fornecidos
    importar_cert_local "$CERT_DIR/cert-for.cer" "cert-fortinet"
    importar_cert_local "$CERT_DIR/cert-tre.cer" "cert-tre-pe"

else
    echo "[!] Não encontrei o keystore em $KEYSTORE ou sem permissão de escrita."
    echo "    O Gradle pode falhar. Tente rodar como root se necessário, ou verifique permissões."
fi


# --- 5. Dependências do Projeto ---
echo "----------------------------------------------------------------"
echo "Instalando Dependências do Projeto"
echo "----------------------------------------------------------------"

npm config set strict-ssl false
echo "[*] Executando 'npm install' na raiz..."
npm install

echo "[*] Executando 'npm install' no frontend..."
if [ -d "frontend" ]; then
    (cd frontend && npm install)
else
    echo "[!] Diretório 'frontend' não encontrado."
fi

echo "[*] Instalando Playwright (Apenas Chromium)..."
export NODE_TLS_REJECT_UNAUTHORIZED=0
npx playwright install chromium

echo "----------------------------------------------------------------"
echo "Configuração Concluída!"
echo "----------------------------------------------------------------"

if [ -f "$HOME/.bashrc" ]; then
    source "$HOME/.bashrc" || true
elif [ -f "$HOME/.zshrc" ]; then
     source "$HOME/.zshrc" || true
fi

echo "[✔] Ambiente atualizado."
echo "----------------------------------------------------------------"
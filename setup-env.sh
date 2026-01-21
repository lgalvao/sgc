#!/bin/bash

# setup-env.sh
# Configura o ambiente de desenvolvimento com NVM, Node.js, SDKMAN e Java.
# Otimizado para sistemas Ubuntu/Unix-like (Não-interativo & Certificados Auto-assinados).

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
    # echo "[*] Configurações temporárias limpas."
}

trap limpar_configs_ssl EXIT

echo "[*] Configurando curl e wget para aceitar certificados auto-assinados..."
# Curl: 'insecure' permite conexões SSL sem verificar a cadeia de certificados
if [ -f "$CURLRC" ]; then cp "$CURLRC" "$CURLRC_BAK"; fi
echo "insecure" >> "$CURLRC"

# Wget: 'check_certificate = off' permite conexões sem verificar a cadeia
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

# Carregar NVM
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"

if ! command -v nvm &> /dev/null; then
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
fi

# --- 2. Instalação do Node.js ---
echo "[*] Instalando Node.js (LTS)..."

# Preferir LTS (v22) ao invés do "node" (latest/v25) por estabilidade
if ! nvm install --lts; then
    echo "[!] 'nvm install --lts' falhou. Tentando versão explícita v22.13.0..."
    nvm install v22.13.0
fi

nvm use --lts || nvm use v22.13.0
nvm alias default "lts/*" || nvm alias default v22.13.0

# VERIFICAR EXECUÇÃO DO NODE
echo "[*] Verificando instalação do Node.js..."
if ! node -e 'console.log("Node is working")' &> /dev/null; then
    echo "[!] ERRO CRÍTICO: Node.js foi instalado mas falhou ao executar."
    echo "    Erro detectado:"
    node -v 2>&1 || true
    
    # Checar especificamente pelo erro da libatomic
    if node -v 2>&1 | grep -q "libatomic"; then
        echo ""
        echo "    [DEPENDÊNCIA FALTANDO]: libatomic.so.1"
        echo "    Seu sistema não possui uma biblioteca obrigatória."
        echo "    Como este script não usa sudo, peça a um admin para rodar:"
        echo "      sudo apt-get install -y libatomic1"
        echo "    Ou se você tiver acesso sudo, execute você mesmo e reinicie este script."
    fi
    exit 1
fi

echo "[✔] Node.js $(node -v) e npm $(npm -v) estão prontos."

# --- 3. Instalação do SDKMAN ---
export SDKMAN_DIR="$HOME/.sdkman"
if [ -d "$SDKMAN_DIR" ]; then
    echo "[✔] SDKMAN! já está instalado."
else
    echo "[*] Instalando SDKMAN!..."
    curl -k -s "https://get.sdkman.io" | bash
fi

# Carregar SDKMAN
if [[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]]; then
    source "$SDKMAN_DIR/bin/sdkman-init.sh"
fi

# Configurar SDKMAN
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
    # Permite certificados auto-assinados (não verifica a cadeia)
    set_sdkman_config "sdkman_insecure_ssl" "true"
    set_sdkman_config "sdkman_auto_answer" "true"
fi


# --- 4. Instalação do Java ---

obter_versao_java_recente() {
    local version=$1
    local vendor=$2
    # Tenta listar versões (ignora erros se estiver offline/bloqueado)
    sdk list java 2>/dev/null | grep -E " $version\.[0-9.]*-$vendor " | head -n 1 | awk '{print $NF}'
}

echo "[*] Detectando Java 21 mais recente (Corretto/amzn)..."
JAVA_21_ID=$(obter_versao_java_recente "21" "amzn") || echo ""
[ -z "$JAVA_21_ID" ] && JAVA_21_ID="21.0.9-amzn"

# Verificar se já está instalado para evitar output redundante
if sdk list java | grep -q "installed" | grep -q "$JAVA_21_ID"; then
    echo "[✔] Java $JAVA_21_ID já está instalado."
else
    echo "    Instalando: $JAVA_21_ID"
    echo "Y" | sdk install java "$JAVA_21_ID" || true
fi

echo "[*] Detectando Java 25 mais recente (Corretto/amzn)..."
JAVA_25_ID=$(obter_versao_java_recente "25" "amzn") || echo ""
if [ -n "$JAVA_25_ID" ]; then
    if sdk list java | grep -q "installed" | grep -q "$JAVA_25_ID"; then
        echo "[✔] Java $JAVA_25_ID já está instalado."
    else
        echo "    Instalando: $JAVA_25_ID"
        echo "n" | sdk install java "$JAVA_25_ID" || true
    fi
else
     echo "    Java 25 (amzn) não encontrado."
fi

echo "[*] Definindo Java 21 ($JAVA_21_ID) como padrão..."
sdk default java "$JAVA_21_ID" || true

# --- 5. Dependências do Projeto ---
echo "----------------------------------------------------------------"
echo "Instalando Dependências do Projeto"
echo "----------------------------------------------------------------"

# Permite certificados auto-assinados no npm (não desabilita SSL, apenas a validação estrita)
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
# Permite certificados auto-assinados no Node para o download do browser
export NODE_TLS_REJECT_UNAUTHORIZED=0
npx playwright install chromium

echo "----------------------------------------------------------------"
echo "Configuração Concluída!"
echo "----------------------------------------------------------------"

# Tentar atualizar o ambiente atual
if [ -f "$HOME/.bashrc" ]; then
    echo "[*] Carregando (source) ~/.bashrc..."
    source "$HOME/.bashrc" || true
elif [ -f "$HOME/.zshrc" ]; then
     echo "[*] Carregando (source) ~/.zshrc..."
     source "$HOME/.zshrc" || true
fi

echo "[✔] Ambiente atualizado. Você deve conseguir rodar 'node', 'java', etc."
echo "    Se os comandos não forem encontrados, reinicie seu terminal manualmente."
echo "----------------------------------------------------------------"
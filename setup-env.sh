#!/bin/bash

# setup-env.sh
# Sets up the development environment with NVM, Node.js, SDKMAN, and Java.
# Optimized for Ubuntu/Unix-like systems with SSL workarounds (curl & wget).

# Stop on first error
set -e

echo "----------------------------------------------------------------"
echo "Initializing Environment Setup"
echo "----------------------------------------------------------------"

# Ensure common binary paths are in the PATH
export PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Robust check for essential tools
check_tool() {
    local tool=$1
    if ! command -v "$tool" &> /dev/null && [ ! -x "/usr/bin/$tool" ] && [ ! -x "/bin/$tool" ] && [ ! -x "/usr/local/bin/$tool" ]; then
        return 1
    fi
    return 0
}

for tool in curl unzip zip; do
    if ! check_tool "$tool"; then
        echo "[!] Error: '$tool' is not found. Please install it."
        exit 1
    fi
done

# --- SSL Workaround Setup (Curl & Wget) ---
CURLRC="$HOME/.curlrc"
CURLRC_BAK="$HOME/.curlrc.bak.$(date +%s)"
WGETRC="$HOME/.wgetrc"
WGETRC_BAK="$HOME/.wgetrc.bak.$(date +%s)"

cleanup_rc() {
    # Restore curlrc
    if [ -f "$CURLRC_BAK" ]; then
        mv "$CURLRC_BAK" "$CURLRC"
    elif [ -f "$CURLRC" ]; then
        rm "$CURLRC"
    fi
    
    # Restore wgetrc
    if [ -f "$WGETRC_BAK" ]; then
        mv "$WGETRC_BAK" "$WGETRC"
    elif [ -f "$WGETRC" ]; then
        rm "$WGETRC"
    fi
    echo "[*] Cleaned up temporary SSL configurations."
}

trap cleanup_rc EXIT

echo "[*] Configuring temporary insecure SSL for curl and wget..."
# Curl
if [ -f "$CURLRC" ]; then cp "$CURLRC" "$CURLRC_BAK"; fi
echo "insecure" >> "$CURLRC"

# Wget
if [ -f "$WGETRC" ]; then cp "$WGETRC" "$WGETRC_BAK"; fi
echo "check_certificate = off" >> "$WGETRC"


# --- 1. NVM Setup ---
export NVM_DIR="$HOME/.nvm"
export NVM_NODEJS_ORG_MIRROR=https://nodejs.org/dist

if [ -d "$NVM_DIR" ]; then
    echo "[✔] NVM directory exists."
else
    echo "[*] Installing NVM (v0.40.1)..."
    curl -k -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
fi

# Load NVM
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"

if ! command -v nvm &> /dev/null; then
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
fi

# --- 2. Node.js Setup ---
echo "[*] Debug: Checking NVM remote connectivity..."
# Allow failure for this check so we don't exit script
nvm ls-remote --lts | tail -n 3 || echo "[!] nvm ls-remote failed"

echo "[*] Installing latest Node.js..."

# Try explicit version if alias fails
if ! nvm install node; then
    echo "[!] 'nvm install node' failed. Trying explicit LTS installation (v22.13.0)..."
    if ! nvm install v22.13.0; then
        echo "[!] Installation failed. NVM cannot reach the node mirror."
        exit 1
    fi
fi

nvm use node || nvm use v22.13.0
nvm alias default node || nvm alias default v22.13.0
echo "[✔] Node.js $(node -v) and npm $(npm -v) are ready."

# --- 3. SDKMAN Setup ---
export SDKMAN_DIR="$HOME/.sdkman"
if [ -d "$SDKMAN_DIR" ]; then
    echo "[✔] SDKMAN! is already installed."
else
    echo "[*] Installing SDKMAN!..."
    curl -k -s "https://get.sdkman.io" | bash
fi

# Load SDKMAN
if [[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]]; then
    source "$SDKMAN_DIR/bin/sdkman-init.sh"
fi

# Permanent SDKMAN SSL workaround
SDKMAN_CONFIG="$SDKMAN_DIR/etc/config"
if [ -f "$SDKMAN_CONFIG" ]; then
    if grep -q "sdkman_insecure_ssl" "$SDKMAN_CONFIG"; then
        sed -i 's/sdkman_insecure_ssl=false/sdkman_insecure_ssl=true/g' "$SDKMAN_CONFIG"
    else
        echo "sdkman_insecure_ssl=true" >> "$SDKMAN_CONFIG"
    fi
    echo "[*] Configured SDKMAN to allow insecure SSL."
fi

# --- 4. Java Setup ---
get_latest_java_version() {
    local version=$1
    local vendor=$2
    sdk list java | grep -E " $version\.[0-9.]*-$vendor " | head -n 1 | awk '{print $NF}'
}

echo "[*] Detecting latest Java 21 (Corretto/amzn)..."
JAVA_21_ID=$(get_latest_java_version "21" "amzn") || echo ""
[ -z "$JAVA_21_ID" ] && JAVA_21_ID="21.0.6-amzn"

echo "    Installing: $JAVA_21_ID"
sdk install java "$JAVA_21_ID" || true

echo "[*] Detecting latest Java 25 (Corretto/amzn)..."
JAVA_25_ID=$(get_latest_java_version "25" "amzn") || echo ""
if [ -n "$JAVA_25_ID" ]; then
    echo "    Installing: $JAVA_25_ID"
    sdk install java "$JAVA_25_ID" || true
fi

sdk default java "$JAVA_21_ID" || true

# --- 5. Project Dependencies ---
echo "----------------------------------------------------------------"
echo "Installing Project Dependencies"
echo "----------------------------------------------------------------"

npm config set strict-ssl false
echo "[*] Running 'npm install' in root..."
npm install

if [ -d "frontend" ]; then
    echo "[*] Running 'npm install' in frontend..."
    (cd frontend && npm install)
fi

echo "[*] Installing Playwright (Chromium only)..."
export NODE_TLS_REJECT_UNAUTHORIZED=0
npx playwright install chromium

echo "----------------------------------------------------------------"
echo "Setup Complete!"
echo "----------------------------------------------------------------"

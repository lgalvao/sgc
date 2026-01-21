#!/bin/bash

# setup-env.sh
# Sets up the development environment with NVM, Node.js, SDKMAN, and Java.
# Optimized for Ubuntu/Unix-like systems (Non-interactive & SSL workarounds).

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
    if [ -f "$CURLRC_BAK" ]; then mv "$CURLRC_BAK" "$CURLRC"; elif [ -f "$CURLRC" ]; then rm "$CURLRC"; fi
    if [ -f "$WGETRC_BAK" ]; then mv "$WGETRC_BAK" "$WGETRC"; elif [ -f "$WGETRC" ]; then rm "$WGETRC" ; fi
    # echo "[*] Cleaned up temporary SSL configurations."
}

trap cleanup_rc EXIT

echo "[*] Configuring temporary insecure SSL for curl and wget..."
if [ -f "$CURLRC" ]; then cp "$CURLRC" "$CURLRC_BAK"; fi
echo "insecure" >> "$CURLRC"
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
echo "[*] Installing Node.js (LTS)..."

# Prefer LTS (v22) over "node" (latest/v25) for stability
if ! nvm install --lts; then
    echo "[!] 'nvm install --lts' failed. Trying explicit v22.13.0..."
    nvm install v22.13.0
fi

nvm use --lts || nvm use v22.13.0
nvm alias default "lts/*" || nvm alias default v22.13.0

# VERIFY NODE EXECUTION
echo "[*] Verifying Node.js installation..."
if ! node -e 'console.log("Node is working")' &> /dev/null; then
    echo "[!] CRITICAL ERROR: Node.js is installed but failed to run."
    echo "    Error detected:"
    node -v 2>&1 || true
    
    # Check specifically for libatomic error
    if node -v 2>&1 | grep -q "libatomic"; then
        echo ""
        echo "    [MISSING DEPENDENCY]: libatomic.so.1"
        echo "    Your system is missing a required library."
        echo "    Since this script cannot use sudo, please ask an admin to run:"
        echo "      sudo apt-get install -y libatomic1"
        echo "    Or if you have sudo access, run it yourself and restart this script."
    fi
    exit 1
fi

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

# Configure SDKMAN
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


# --- 4. Java Setup ---

get_latest_java_version() {
    local version=$1
    local vendor=$2
    # Check if we can list versions (ignore errors if offline/blocked)
    sdk list java 2>/dev/null | grep -E " $version\.[0-9.]*-$vendor " | head -n 1 | awk '{print $NF}'
}

echo "[*] Detecting latest Java 21 (Corretto/amzn)..."
JAVA_21_ID=$(get_latest_java_version "21" "amzn") || echo ""
[ -z "$JAVA_21_ID" ] && JAVA_21_ID="21.0.9-amzn"

# Check if already installed to avoid redundant output
if sdk list java | grep -q "installed" | grep -q "$JAVA_21_ID"; then
    echo "[✔] Java $JAVA_21_ID is already installed."
else
    echo "    Installing: $JAVA_21_ID"
    echo "Y" | sdk install java "$JAVA_21_ID" || true
fi

echo "[*] Detecting latest Java 25 (Corretto/amzn)..."
JAVA_25_ID=$(get_latest_java_version "25" "amzn") || echo ""
if [ -n "$JAVA_25_ID" ]; then
    if sdk list java | grep -q "installed" | grep -q "$JAVA_25_ID"; then
        echo "[✔] Java $JAVA_25_ID is already installed."
    else
        echo "    Installing: $JAVA_25_ID"
        echo "n" | sdk install java "$JAVA_25_ID" || true
    fi
else
     echo "    Java 25 (amzn) not found."
fi

echo "[*] Setting Java 21 ($JAVA_21_ID) as default..."
sdk default java "$JAVA_21_ID" || true

# --- 5. Project Dependencies ---
echo "----------------------------------------------------------------"
echo "Installing Project Dependencies"
echo "----------------------------------------------------------------"

npm config set strict-ssl false
echo "[*] Running 'npm install' in root..."
npm install

echo "[*] Running 'npm install' in frontend..."
if [ -d "frontend" ]; then
    (cd frontend && npm install)
else
    echo "[!] 'frontend' directory not found."
fi

echo "[*] Installing Playwright (Chromium only)..."
export NODE_TLS_REJECT_UNAUTHORIZED=0
npx playwright install chromium

echo "----------------------------------------------------------------"
echo "Setup Complete!"
echo "Please restart your terminal or run: source ~/.bashrc"
echo "----------------------------------------------------------------"

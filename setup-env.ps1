# setup-env.ps1
# Configura o ambiente de desenvolvimento no Windows.
# Equivalente ao setup-env.sh, adaptado para PowerShell.

$ErrorActionPreference = "Stop"

Write-Host "----------------------------------------------------------------"
Write-Host "Inicializando Configuração do Ambiente (Windows)"
Write-Host "----------------------------------------------------------------"

# --- 1. Node.js ---
# Verifica se o Node.js está instalado
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "[*] Node.js não encontrado. Tentando instalar via winget..."
    try {
        winget install OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements --silent
        
        # Atualiza o PATH da sessão atual
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
        Write-Host "[✔] Node.js instalado."
    } catch {
        Write-Host "[!] Falha ao instalar Node.js via winget. Por favor, instale manualmente."
        exit 1
    }
} else {
    Write-Host "[✔] Node.js já está instalado."
}

# Exibe versões
node -v
npm -v

# --- 2. Java (Amazon Corretto 21) ---
# Tenta localizar JAVA_HOME ou instalar
$javaFound = $false

if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
    Write-Host "[✔] JAVA_HOME já definido: $env:JAVA_HOME"
    $javaFound = $true
} else {
    # Tenta caminho padrão do Corretto 21
    $corretto21Path = "C:\Program Files\Amazon Corretto\jdk21"
    
    if (-not (Test-Path $corretto21Path)) {
        Write-Host "[*] Instalando Java 21 (Corretto) via winget..."
        try {
            winget install Amazon.Corretto.21 --accept-source-agreements --accept-package-agreements --silent
            Write-Host "[✔] Instalação concluída."
        } catch {
            Write-Host "[!] Falha ao instalar Java 21. Tente instalar manualmente."
        }
    }
    
    if (Test-Path $corretto21Path) {
        Write-Host "[*] Definindo JAVA_HOME para $corretto21Path (Usuário)"
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $corretto21Path, "User")
        $env:JAVA_HOME = $corretto21Path
        
        # Adiciona bin ao PATH se necessário
        $binPath = Join-Path $corretto21Path "bin"
        if ($env:Path -notlike "*$binPath*") {
            [System.Environment]::SetEnvironmentVariable("Path", $env:Path + ";$binPath", "User")
            $env:Path += ";$binPath"
        }
        $javaFound = $true
    }
}

if (-not $javaFound) {
    Write-Host "[!] AVISO: Não foi possível configurar o Java automaticamente. Verifique a instalação do JDK 21."
}

# --- 3. Certificados SSL ---
Write-Host "----------------------------------------------------------------"
Write-Host "Importando Certificados Locais para o Java (Keystore)"
Write-Host "----------------------------------------------------------------"

if ($env:JAVA_HOME) {
    $cacerts = Join-Path $env:JAVA_HOME "lib\security\cacerts"
    if (-not (Test-Path $cacerts)) {
        $cacerts = Join-Path $env:JAVA_HOME "conf\security\cacerts"
    }
    
    $keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    $certDir = Join-Path (Get-Location) "backend\etc\deploy"

    if (Test-Path $keytool) {
        function Import-Cert($certName, $alias) {
            $certPath = Join-Path $certDir $certName
            if (Test-Path $certPath) {
                Write-Host "[*] Processando $alias..."
                # Remove alias antigo (silenciosamente)
                & $keytool -delete -alias $alias -keystore $cacerts -storepass changeit -noprompt 2>$null
                
                # Importa novo
                $proc = Start-Process -FilePath $keytool -ArgumentList "-import","-trustcacerts","-alias",$alias,"-file",$certPath,"-keystore",$cacerts,"-storepass","changeit","-noprompt" -Wait -PassThru -NoNewWindow
                
                if ($proc.ExitCode -eq 0) {
                    Write-Host "[✔] $alias importado com sucesso."
                } else {
                    Write-Host "[!] Falha ao importar $alias. (Requer Admin?)"
                }
            } else {
                Write-Host "[!] Arquivo não encontrado: $certPath"
            }
        }

        Import-Cert "cert-for.cer" "cert-fortinet"
        Import-Cert "cert-tre.cer" "cert-tre-pe"
    } else {
        Write-Host "[!] Keytool não encontrado em $keytool"
    }
} else {
    Write-Host "[!] JAVA_HOME não definido, pulando importação de certificados."
}

# --- 4. Dependências do Projeto ---
Write-Host "----------------------------------------------------------------"
Write-Host "Instalando Dependências do Projeto"
Write-Host "----------------------------------------------------------------"

Write-Host "[*] Configurando npm para ignorar erros estritos de SSL..."
npm config set strict-ssl false

Write-Host "[*] Executando 'npm install' na raiz..."
npm install

if (Test-Path "frontend") {
    Write-Host "[*] Executando 'npm install' no frontend..."
    Push-Location frontend
    npm install
    Pop-Location
} else {
    Write-Host "[!] Pasta 'frontend' não encontrada."
}

Write-Host "[*] Instalando Playwright (Chromium)..."
$env:NODE_TLS_REJECT_UNAUTHORIZED = 0
npx playwright install chromium

Write-Host "----------------------------------------------------------------"
Write-Host "Configuração Concluída!"
Write-Host "Reinicie seu terminal (PowerShell) para garantir que as alterações no PATH tenham efeito."
Write-Host "----------------------------------------------------------------"

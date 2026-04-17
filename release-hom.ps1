param(
    [string]$ContainerCli = $(if ($env:CONTAINER_CLI) { $env:CONTAINER_CLI } else { "docker" }),
    [string]$Registry = $(if ($env:REGISTRY) { $env:REGISTRY } else { "registry.tre-pe.gov.br/sesel" }),
    [string]$PortaHost = $(if ($env:PORTA_HOST) { $env:PORTA_HOST } else { "8980" }),
    [string]$PortaContainer = $(if ($env:PORTA_CONTAINER) { $env:PORTA_CONTAINER } else { "10000" }),
    [switch]$PularImagem,
    [switch]$PularPush,
    [switch]$PularCompose
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$NomeSistema = "sgc"
$ArquivoJarDocker = "sgc.jar"
$ArquivoEnv = ".env.hom"

function Remover-ArtefatoTemporario {
    if (Test-Path $ArquivoJarDocker) {
        Remove-Item -LiteralPath $ArquivoJarDocker -Force
    }
}

function Invocar-Comando {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Comando,
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Argumentos
    )

    & $Comando @Argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "Comando falhou ($LASTEXITCODE): $Comando $($Argumentos -join ' ')"
    }
}

try {
    if (-not (Test-Path $ArquivoEnv)) {
        throw "Arquivo $ArquivoEnv nao encontrado."
    }

    $Gradle = if (Test-Path ".\gradlew.bat") { ".\gradlew.bat" } else { "./gradlew" }

    Write-Host "==> Executando build do frontend e do backend"
    Invocar-Comando $Gradle --no-daemon clean copyFrontend ":backend:bootJar" -x test

    $SaidaVersao = & $Gradle --no-daemon -q properties --property version
    if ($LASTEXITCODE -ne 0) {
        throw "Comando falhou ($LASTEXITCODE): $Gradle --no-daemon -q properties --property version"
    }
    $LinhaVersao = $SaidaVersao | Where-Object { $_ -match "^version:\s*(.+)$" } | Select-Object -First 1
    if (-not $LinhaVersao) {
        throw "Nao foi possivel identificar a versao do projeto."
    }
    $Versao = [regex]::Match($LinhaVersao, "^version:\s*(.+)$").Groups[1].Value.Trim()

    $JarOrigem = Get-ChildItem -Path "backend\build\libs" -Filter "*.jar" |
        Where-Object { $_.Name -notlike "*-plain.jar" } |
        Sort-Object Name |
        Select-Object -First 1

    if (-not $JarOrigem) {
        throw "Jar do backend nao encontrado em backend\build\libs."
    }

    $TagVersao = "$Registry/${NomeSistema}:$Versao"
    $TagLatest = "$Registry/${NomeSistema}:latest"

    Write-Host "==> Preparando artefato Docker: $($JarOrigem.FullName)"
    Copy-Item -LiteralPath $JarOrigem.FullName -Destination $ArquivoJarDocker -Force

    if (-not $PularImagem) {
        Write-Host "==> Construindo imagem com ${ContainerCli}: $TagVersao"
        Invocar-Comando $ContainerCli build --no-cache --pull -t $TagVersao -t $TagLatest .

        if (-not $PularPush) {
            Write-Host "==> Enviando imagem para o registry"
            Invocar-Comando $ContainerCli push $TagVersao
            Invocar-Comando $ContainerCli push $TagLatest
        } else {
            Write-Host "==> Push ignorado por parametro"
        }
    } else {
        Write-Host "==> Build da imagem ignorado por parametro"
    }

    Write-Host "==> Gerando compose.yaml para homologacao"
    @"
services:
  ${NomeSistema}:
    image: $TagVersao
    container_name: $NomeSistema
    restart: unless-stopped
    ports:
      - "$PortaHost`:$PortaContainer"
    env_file:
      - $ArquivoEnv
    environment:
      TZ: America/Recife
      SPRING_PROFILES_ACTIVE: hom
      SERVER_PORT: "$PortaContainer"
      LOGGING_FILE_NAME: /var/log/$NomeSistema/$NomeSistema.log
    volumes:
      - log-data:/var/log/$NomeSistema

volumes:
  log-data:
"@ | Set-Content -Path "compose.yaml" -Encoding UTF8

    if (-not $PularCompose) {
        Write-Host "==> Subindo homologacao com a versao $Versao"
        Invocar-Comando $ContainerCli compose down --remove-orphans
        Invocar-Comando $ContainerCli compose up -d --remove-orphans
    } else {
        Write-Host "==> Compose ignorado por parametro"
    }
} finally {
    Remover-ArtefatoTemporario
}

param(
    [string]$ContainerCli = $(if ($env:CONTAINER_CLI) { $env:CONTAINER_CLI } else { "podman" }),
    [string]$Registry = $(if ($env:REGISTRY) { $env:REGISTRY } else { "registry.tre-pe.gov.br/sesel" }),
    [string]$PortaHost = $(if ($env:PORTA_HOST) { $env:PORTA_HOST } else { "8980" }),
    [string]$PortaContainer = $(if ($env:PORTA_CONTAINER) { $env:PORTA_CONTAINER } else { "10000" }),
    [string]$DbUrl = $(if ($env:DB_URL) { $env:DB_URL } else { "jdbc:oracle:thin:@desenvolvimentobd.tre-pe.gov.br:1521:admdes2" }),
    [string]$Tag,
    [switch]$SemBuild,
    [switch]$SemImagem,
    [switch]$SemPush,
    [switch]$SemDeploy,
    [switch]$SemCache,
    [switch]$SemPull,
    [int]$TimeoutSubidaSegundos = $(if ($env:TIMEOUT_SUBIDA_SEGUNDOS) { [int]$env:TIMEOUT_SUBIDA_SEGUNDOS } else { 120 })
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$NomeSistema = "sgc"
$ArquivoJarDocker = "sgc.jar"
$ArquivoEnv = ".env.hom"
$ArquivoCompose = "compose.hom.yaml"

function Remove-ArtefatoTemporario {
    if (Test-Path $ArquivoJarDocker) {
        Remove-Item -LiteralPath $ArquivoJarDocker -Force
    }
}

function Invoke-Comando {
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

function Get-VersaoProjeto {
    param([string]$Gradle)

    $SaidaVersao = & $Gradle -q properties --property version
    if ($LASTEXITCODE -ne 0) {
        throw "Comando falhou ($LASTEXITCODE): $Gradle -q properties --property version"
    }

    $LinhaVersao = $SaidaVersao | Where-Object { $_ -match "^version:\s*(.+)$" } | Select-Object -First 1
    if (-not $LinhaVersao) {
        throw "Nao foi possivel identificar a versao do projeto."
    }

    return [regex]::Match($LinhaVersao, "^version:\s*(.+)$").Groups[1].Value.Trim()
}

function Get-JarBackend {
    $JarOrigem = Get-ChildItem -Path "backend\build\libs" -Filter "*.jar" |
        Where-Object { $_.Name -notlike "*-plain.jar" } |
        Sort-Object Name |
        Select-Object -First 1

    if (-not $JarOrigem) {
        throw "Jar do backend nao encontrado em backend\build\libs. Rode sem -SemBuild ou execute o build antes."
    }

    return $JarOrigem
}

function Get-ValorContainer {
    param(
        [string]$Formato
    )

    $Valor = & $ContainerCli inspect $NomeSistema --format $Formato 2>$null
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return ($Valor | Select-Object -First 1)
}

function Wait-Subida {
    $Inicio = Get-Date
    $Limite = $Inicio.AddSeconds($TimeoutSubidaSegundos)
    Write-Host "==> Monitorando subida do container por ate $TimeoutSubidaSegundos segundos"

    while ((Get-Date) -lt $Limite) {
        $Status = Get-ValorContainer "{{.State.Status}}"
        $Reiniciando = Get-ValorContainer "{{.State.Restarting}}"
        $CodigoSaida = Get-ValorContainer "{{.State.ExitCode}}"

        if ($Status -in @("exited", "dead") -or $Reiniciando -eq "true") {
            Write-Host "==> Logs recentes do container"
            & $ContainerCli logs --tail 120 $NomeSistema
            throw "Container $NomeSistema ficou em estado invalido: status=$Status restarting=$Reiniciando exitCode=$CodigoSaida"
        }

        $Logs = & $ContainerCli logs --tail 120 $NomeSistema 2>$null
        if ($LASTEXITCODE -eq 0 -and ($Logs -match "Started Sgc")) {
            Write-Host "==> Aplicacao iniciada com sucesso"
            return
        }

        Start-Sleep -Seconds 5
    }

    Write-Host "==> Logs recentes do container"
    & $ContainerCli logs --tail 120 $NomeSistema
    throw "Timeout aguardando a subida do container $NomeSistema."
}

try {
    if (-not (Test-Path $ArquivoEnv)) {
        throw "Arquivo $ArquivoEnv nao encontrado."
    }
    if (-not (Test-Path $ArquivoCompose)) {
        throw "Arquivo $ArquivoCompose nao encontrado."
    }

    $Gradle = if (Test-Path ".\gradlew.bat") { ".\gradlew.bat" } else { "./gradlew" }

    if (-not $SemBuild) {
        Write-Host "==> Executando build do frontend e do backend"
        Invoke-Comando $Gradle clean copyFrontend ":backend:bootJar" -x test
    } else {
        Write-Host "==> Build Gradle ignorado por parametro"
    }

    $Versao = if ($Tag) { $Tag } else { Get-VersaoProjeto $Gradle }
    $TagVersao = "$Registry/${NomeSistema}:$Versao"
    $TagLatest = "$Registry/${NomeSistema}:latest"

    if (-not $SemImagem) {
        $JarOrigem = Get-JarBackend
        Write-Host "==> Preparando artefato Docker: $($JarOrigem.FullName)"
        Copy-Item -LiteralPath $JarOrigem.FullName -Destination $ArquivoJarDocker -Force

        $ArgsBuild = @("build", "-t", $TagVersao, "-t", $TagLatest)
        if ($SemCache) {
            $ArgsBuild += "--no-cache"
        }
        if (-not $SemPull) {
            $ArgsBuild += "--pull"
        }
        $ArgsBuild += "."

        Write-Host "==> Construindo imagem com ${ContainerCli}: $TagVersao"
        Invoke-Comando $ContainerCli @ArgsBuild
    } else {
        Write-Host "==> Build da imagem ignorado por parametro"
    }

    if (-not $SemPush) {
        Write-Host "==> Enviando imagem para o registry"
        Invoke-Comando $ContainerCli push $TagVersao
        Invoke-Comando $ContainerCli push $TagLatest
    } else {
        Write-Host "==> Push ignorado por parametro"
    }

    $env:SGC_IMAGE = $TagVersao
    $env:PORTA_HOST = $PortaHost
    $env:PORTA_CONTAINER = $PortaContainer
    $env:DB_URL = $DbUrl

    if (-not $SemDeploy) {
        Write-Host "==> Subindo homologacao com a versao $Versao"
        Invoke-Comando $ContainerCli compose -f $ArquivoCompose down --remove-orphans
        Invoke-Comando $ContainerCli compose -f $ArquivoCompose up --detach --remove-orphans
        Wait-Subida
    } else {
        Write-Host "==> Deploy do compose ignorado por parametro"
    }
} finally {
    Remove-ArtefatoTemporario
}

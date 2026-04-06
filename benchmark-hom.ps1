$ErrorActionPreference = 'Stop'

$diretorioRaiz = Split-Path -Parent $MyInvocation.MyCommand.Path
$arquivoEnv = Join-Path $diretorioRaiz '.env.hom'

if (-not (Test-Path $arquivoEnv)) {
    throw "Arquivo .env.hom nao encontrado em $diretorioRaiz"
}

Get-Content $arquivoEnv | ForEach-Object {
    $linha = $_.Trim()
    if ($linha -eq '' -or $linha.StartsWith('#')) {
        return
    }

    if ($linha.StartsWith('export ')) {
        $linha = $linha.Substring(7).Trim()
    }

    $partes = $linha -split '=', 2
    if ($partes.Length -ne 2) {
        return
    }

    $nome = $partes[0].Trim()
    $valor = $partes[1].Trim()
    if (($valor.StartsWith('"') -and $valor.EndsWith('"')) -or ($valor.StartsWith("'") -and $valor.EndsWith("'"))) {
        $valor = $valor.Substring(1, $valor.Length - 2)
    }

    [Environment]::SetEnvironmentVariable($nome, $valor, 'Process')
}

$env:SPRING_PROFILES_ACTIVE = 'hom'

Push-Location $diretorioRaiz
try {
    Write-Host '[benchmark-hom] executando benchmark Oracle com profile hom' -ForegroundColor Cyan
    & .\gradlew.bat --no-daemon --no-configuration-cache :backend:test --tests sgc.integracao.ProcessoSubprocessoPerformanceOracleIntegrationTest @args
} finally {
    Pop-Location
}

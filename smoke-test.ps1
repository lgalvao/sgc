# SGC Smoke Test - Versão PowerShell Premium
$ErrorActionPreference = 'Stop'
$OutputEncoding = [System.Text.Encoding]::UTF8

# Configuração de cores e estilos
$COLORS = @{
    Success = "Green"
    Error   = "Red"
    Warning = "Yellow"
    Info    = "Cyan"
    Neutral = "Gray"
}

$SUMMARY = @()

function Obter-UltimaCaptura {
    param(
        [object[]]$Linhas,
        [string]$Padrao
    )

    $capturas = @($Linhas | Select-String $Padrao | ForEach-Object { $_.Matches.Groups[1].Value })
    if ($capturas.Count -gt 0) {
        return $capturas[-1]
    }

    return $null
}

function Run-Step {
    param(
        [string]$Name,
        [scriptblock]$Script,
        [string]$ErrorDetailMsg = ""
    )

    $logFile = Join-Path $PSScriptRoot ".smoke_test_$($Name -replace ' ', '_').log"
    
    Write-Host "$Name... " -NoNewline -ForegroundColor $COLORS.Warning

    Push-Location $PSScriptRoot
    try {
        $result = @(& $Script 2>&1 | Tee-Object -FilePath $logFile)
        $exitCode = if ($null -ne $LASTEXITCODE) { [int]$LASTEXITCODE }
        elseif ($?) { 0 }
        else { 1 }
    }
    catch {
        $_ | Out-File -FilePath $logFile -Encoding utf8
        $result = @((Get-Content $logFile -ErrorAction SilentlyContinue))
        $exitCode = 1
    }
    finally {
        Pop-Location
    }

    if ($exitCode -eq 0) {
        Write-Host "OK    " -ForegroundColor $COLORS.Success
        
        # Extração de métricas básicas para o resumo
        if ($Name -eq "junit") {
            $passed = Obter-UltimaCaptura -Linhas $result -Padrao "\+ Passed: (\d+)"
            $total = Obter-UltimaCaptura -Linhas $result -Padrao "Total: (\d+)"
            if ($passed -and $total) { $SUMMARY += "  - Backend JUnit: $passed/$total testes OK" }
            else { $SUMMARY += "  - Backend JUnit: Finalizado (Cache ou 0 executados)" }
        }
        elseif ($Name -eq "vitest") {
            $passed = Obter-UltimaCaptura -Linhas $result -Padrao "Tests\s+(\d+)\s+passed"
            if ($passed) { $SUMMARY += "  - Frontend Vitest: $passed testes OK" }
        }
        elseif ($Name -eq "e2e") {
            $passed = Obter-UltimaCaptura -Linhas $result -Padrao "(\d+)\s+passed"
            if ($passed) { $SUMMARY += "  - E2E Playwright: $passed fluxos validados" }
        }
    }
    else {
        Write-Host "FALHOU" -ForegroundColor $COLORS.Error
        Write-Host "`n--- Detalhes do erro ($Name) ---" -ForegroundColor $COLORS.Error
        $result | Select-Object -Last 20 | Write-Host -ForegroundColor $COLORS.Neutral
        Write-Host "---------------------------------" -ForegroundColor $COLORS.Error
        if ($ErrorDetailMsg) { Write-Host $ErrorDetailMsg -ForegroundColor $COLORS.Warning }
        
        # Cleanup e fail fast
        Get-ChildItem -Path $PSScriptRoot -Filter ".smoke_test_*.log" | Remove-Item -ErrorAction SilentlyContinue
        exit 1
    }
}

# Início da execução
Clear-Host
Write-Host "SGC SMOKE TEST   " -ForegroundColor $COLORS.Info
Write-Host "=========================================`n" -ForegroundColor $COLORS.Info

# Passos
Run-Step "junit"      { .\gradlew.bat :backend:test }
Run-Step "typecheck"  { npm run typecheck -s }
Run-Step "lint"       { npm run lint -s }
Run-Step "vitest"     { npm run test:unit --prefix frontend -- -s --reporter=dot }
Run-Step "e2e"        { npx playwright test captura jornada --reporter=dot } -ErrorDetailMsg "Obs: Verifique e2e/server.log para detalhes de inicialização."

# Resumo Final
Write-Host "`n-----------------------------------------" -ForegroundColor $COLORS.Info
Write-Host "=== RESUMO DA EXECUÇÃO ===" -ForegroundColor $COLORS.Info
if ($SUMMARY.Count -gt 0) {
    foreach ($line in $SUMMARY) { Write-Host $line -ForegroundColor $COLORS.Neutral }
} else {
    Write-Host "  Processamento concluído com sucesso." -ForegroundColor $COLORS.Neutral
}
Write-Host "-----------------------------------------" -ForegroundColor $COLORS.Info
Write-Host "Tudo em ordem! O sistema está estável para desenvolvimento.`n" -ForegroundColor $COLORS.Success

# Cleanup de logs temporários de sucesso
Get-ChildItem -Path $PSScriptRoot -Filter ".smoke_test_*.log" | Remove-Item -ErrorAction SilentlyContinue

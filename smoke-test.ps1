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

function Show-Spinner {
    param($Job, $StepName)
    $spinner = "|", "/", "-", "\"
    $i = 0
    
    # Esconde o cursor se possível (apenas em hosts que suportam)
    if ($Host.UI.RawUI.CursorSize) { $Host.UI.RawUI.CursorSize = 0 }

    Write-Host "$StepName... " -NoNewline -ForegroundColor $COLORS.Warning

    while ($Job.State -eq 'Running') {
        Write-Host " [$($spinner[$i % 4])] " -NoNewline -ForegroundColor $COLORS.Info
        Start-Sleep -Milliseconds 100
        Write-Host "`b`b`b`b`b" -NoNewline
        $i++
    }
    
    # Restaura o cursor
    if ($Host.UI.RawUI.CursorSize) { $Host.UI.RawUI.CursorSize = 25 }
}

function Run-Step {
    param(
        [string]$Name,
        [scriptblock]$Script,
        [string]$ErrorDetailMsg = ""
    )

    $logFile = Join-Path $PSScriptRoot ".smoke_test_$($Name -replace ' ', '_').log"
    
    # Inicia o job em background passando o diretório e o comando (como string)
    $job = Start-Job -ScriptBlock {
        param($dir, $commandString)
        Set-Location $dir
        $sb = [scriptblock]::Create($commandString)
        & $sb
    } -ArgumentList $PSScriptRoot, $Script.ToString() -Name $Name
    
    # Mostra o spinner enquanto aguarda
    Show-Spinner -Job $job -StepName $Name
    
    # Recebe os resultados
    $result = Wait-Job $job | Receive-Job
    $exitCode = $job.ChildJobs[0].ExitCode
    Remove-Job $job

    # Salva o output no log para referência se necessário
    $result | Out-File -FilePath $logFile -Encoding utf8

    if ($exitCode -eq 0) {
        Write-Host "OK    " -ForegroundColor $COLORS.Success
        
        # Extração de métricas básicas para o resumo
        if ($Name -eq "junit") {
            $passed = ($result | Select-String "\+ Passed: (\d+)" | ForEach-Object { $_.Matches.Groups[1].Value })[-1]
            $total = ($result | Select-String "Total: (\d+)" | ForEach-Object { $_.Matches.Groups[1].Value })[-1]
            if ($passed -and $total) { $SUMMARY += "  - Backend JUnit: $passed/$total testes OK" }
            else { $SUMMARY += "  - Backend JUnit: Finalizado (Cache ou 0 executados)" }
        }
        elseif ($Name -eq "vitest") {
            $passed = ($result | Select-String "Tests\s+(\d+)\s+passed" | ForEach-Object { $_.Matches.Groups[1].Value })[-1]
            if ($passed) { $SUMMARY += "  - Frontend Vitest: $passed testes OK" }
        }
        elseif ($Name -eq "e2e") {
            $passed = ($result | Select-String "(\d+)\s+passed" | ForEach-Object { $_.Matches.Groups[1].Value })[-1]
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

# Script de checkup do SGC (versão PowerShell)
$ErrorActionPreference = "Stop"

function Write-Step {
    param ([string]$label)
    Write-Host "`n==> $label" -ForegroundColor Cyan
}

function Invoke-Step {
    param (
        [string]$label,
        [scriptblock]$command
    )
    Write-Step $label
    try {
        & $command
        if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) {
            throw "Command exited with code $LASTEXITCODE"
        }
    }
    catch {
        Write-Host "FALHA: $label" -ForegroundColor Red
        exit 1
    }
}

$GRADLE_CMD = "./gradlew.bat"

if ($Host.UI.RawUI.WindowTitle) {
    Clear-Host
}

Invoke-Step "Atualizar branch local" { git pull }

Invoke-Step "npm install" {
    npm install --silent
}

Invoke-Step "Lint raiz" { npm run lint }
Invoke-Step "Typecheck raiz" { npm run typecheck --silent }
Invoke-Step "Testes scripts" { npm --prefix toolkit run test }
Invoke-Step "Testes frontend" { npm --prefix frontend run test }
Invoke-Step "Testes backend" { & $GRADLE_CMD backend:test }
Invoke-Step "Testes e2e" { npx playwright test --config=e2e/playwright.config.ts --project=chromium }


Write-Host "`nTudo certo!" -ForegroundColor Green

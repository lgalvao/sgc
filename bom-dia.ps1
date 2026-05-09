#Requires -Version 7

$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()
).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin)
{
    Start-Process pwsh -Verb RunAs -ArgumentList "-NoExit -File `"$PSCommandPath`""
    exit
}

$ErrorActionPreference = 'Stop'
function Invoke-Passo
{
    param([string]$Label, [scriptblock]$Action)
    Write-Host "`n==> $Label" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0)
    {
        Write-Host "FALHA: $Label (exit $LASTEXITCODE)" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Clear-Host

Invoke-Passo 'git pull'          { git pull }

Invoke-Passo 'Atualizar npm'     { npm install -g npm@latest }
Invoke-Passo 'Atualizar globais' { npm update -g }
Invoke-Passo 'Raiz install'      { npm install }
Invoke-Passo 'Raiz update'       { npm update }
Invoke-Passo 'Typecheck'         { npm run typecheck }
Invoke-Passo 'Lint'              { npm run lint }

Push-Location frontend
Invoke-Passo 'Frontend install'  { npm install }
Invoke-Passo 'Frontend update'   { npm update }
Invoke-Passo 'Frontend testes'   { npx vitest run }
Pop-Location

Invoke-Passo 'Backend testes'    { .\gradlew backend:test }

Invoke-Passo 'Testes e2e' { npx playwright test captura jornada }

Write-Host "`nTudo certo!" -ForegroundColor Green

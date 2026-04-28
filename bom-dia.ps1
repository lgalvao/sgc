#Requires -Version 7

$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()
).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Start-Process pwsh -Verb RunAs -ArgumentList "-NoExit -File `"$PSCommandPath`""
    exit
}

$ErrorActionPreference = 'Stop'

function Invoke-Passo {
    param([string]$Label, [scriptblock]$Action)
    Write-Host "`n==> $Label" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FALHA: $Label (exit $LASTEXITCODE)" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Clear-Host

Invoke-Passo 'git pull'          { git pull }
Invoke-Passo 'Testes backend'    { gradle backend:test }

# Garantir que pnpm está instalado
if (-not (Get-Command pnpm -ErrorAction SilentlyContinue)) {
    Invoke-Passo 'Instalar pnpm' { npm install -g pnpm }
}

Invoke-Passo 'Atualizar pnpm'    { pnpm self-update --silent}
Invoke-Passo 'Atualizar pnpm globais' { pnpm update -g }
Invoke-Passo 'Atualizar npm globais'  { npm update -g }

Invoke-Passo 'Typecheck'         { pnpm run typecheck }
Invoke-Passo 'Lint'              { pnpm run lint }

Push-Location frontend
Invoke-Passo 'Frontend deps'     { pnpm update }
Invoke-Passo 'Testes frontend'   { pnpm exec vitest run }
Pop-Location

Invoke-Passo 'Testes PW mínimos' { pnpm exec playwright test captura jornada }

Write-Host "`nTudo certo!" -ForegroundColor Green
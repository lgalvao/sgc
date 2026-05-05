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

# Garantir que pnpm está instalado
if (-not (Get-Command pnpm -ErrorAction SilentlyContinue))
{
    Invoke-Passo 'Instalar pnpm' { npm install -g pnpm }
}

Invoke-Passo 'Atualizar pnpm'    { pnpm self-update --silent }
Invoke-Passo 'Atualizar globais' { pnpm update -g }
Invoke-Passo 'Raiz install'      { pnpm install }
Invoke-Passo 'Raiz update'       { pnpm update }
Invoke-Passo 'Typecheck'         { pnpm run typecheck }
Invoke-Passo 'Lint'              { pnpm run lint }

Push-Location frontend
Invoke-Passo 'Frontend install'  { pnpm install }
Invoke-Passo 'Frontend update'   { pnpm update }
Invoke-Passo 'Frontend testes'   { pnpm exec vitest run }
Pop-Location

Invoke-Passo 'Backend testes'    { .\gradlew backend:test }

Invoke-Passo 'Testes e2e' { pnpm exec playwright test captura jornada }

Write-Host "`nTudo certo!" -ForegroundColor Green
#Requires -Version 7

# Verifica se é admin e 'eleva' se não for
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()
).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin)
{
    Start-Process pwsh -Verb RunAs -ArgumentList "-File `"$PSCommandPath`""
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

Invoke-Passo 'Git pull'                 { git pull }
Invoke-Passo 'Testes backend'           { gradle backend:test }

Invoke-Passo 'Atualização npm global'   { npm update -g }

Invoke-Passo 'Atualização npm raiz'    { npm update --save }
Invoke-Passo 'Typecheck'                { npm run typecheck }
Invoke-Passo 'Lint'                     { npm run lint }

Push-Location frontend
Invoke-Passo 'Frontend deps'            { npm update --save }
Invoke-Passo 'Testes frontend'          { npx vitest run }
Pop-Location

Invoke-Passo 'Testes PW mímimos'        { npx playwright test captura jornada }
Write-Host "`nTudo certo!" -ForegroundColor Green
param(
    [Parameter(Position = 0)]
    [string]$Action
)

function Show-Help
{
    Write-Output "Usage: .\quality-check.ps1 [OPTION]"
    Write-Output "Options:"
    Write-Output "  all       Run all quality checks (Backend + Frontend)"
    Write-Output "  backend   Run backend quality checks only"
    Write-Output "  frontend  Run frontend quality checks only"
    Write-Output "  fast      Run fast quality checks (Tests + Coverage only)"
    Write-Output "  help      Show this help message"
}

if ( [string]::IsNullOrWhiteSpace($Action))
{
    Show-Help
    exit 1
}

$gradlew = ".\gradlew.bat"

if (-not (Test-Path $gradlew))
{
    Write-Error "Error: $gradlew not found in the current directory."
    exit 1
}

switch ($Action)
{
    "all" {
        Write-Output "Running all quality checks..."
        & $gradlew qualityCheckAll
    }
    "backend" {
        Write-Output "Running backend quality checks..."
        & $gradlew backendQualityCheck
    }
    "frontend" {
        Write-Output "Running frontend quality checks..."
        & $gradlew frontendQualityCheck
    }
    "fast" {
        Write-Output "Running fast quality checks..."
        & $gradlew qualityCheckFast
    }
    "help" {
        Show-Help
    }
    default {
        Write-Output "Invalid option: $Action"
        Show-Help
        exit 1
    }
}

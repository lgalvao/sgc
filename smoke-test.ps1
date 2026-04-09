$ErrorActionPreference = 'Stop'

Write-Host "[smoke-test] backend:test" -ForegroundColor Cyan
.\gradlew.bat :backend:test -q

Write-Host "[smoke-test] typecheck" -ForegroundColor Cyan
npm run typecheck

Write-Host "[smoke-test] lint" -ForegroundColor Cyan
npm run lint

Write-Host "[smoke-test] frontend vitest" -ForegroundColor Cyan
Push-Location frontend
npx vitest run
Pop-Location

Write-Host "[smoke-test] playwright captura" -ForegroundColor Cyan
npx playwright test captura

Write-Host "[smoke-test] concluído" -ForegroundColor Green

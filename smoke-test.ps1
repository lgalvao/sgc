$ErrorActionPreference = 'Stop'

Write-Host "[smoke-test] backend:test" -ForegroundColor Cyan
.\gradlew.bat :backend:test -q

Write-Host "[smoke-test] typecheck" -ForegroundColor Cyan
npm run typecheck -s

Write-Host "[smoke-test] lint" -ForegroundColor Cyan
npm run lint -s

Write-Host "[smoke-test] frontend vitest" -ForegroundColor Cyan
Push-Location frontend
npx vitest run --silent
Pop-Location

Write-Host "[smoke-test] playwright captura" -ForegroundColor Cyan
npx playwright test captura --quiet

Write-Host "[smoke-test] concluído" -ForegroundColor Green

#!/bin/bash
set -e

echo "======================================"
echo "    🧪 Executando testes: QA All    "
echo "======================================"

./qa-backend.sh
./qa-frontend.sh

echo "======================================"
echo "    🤖 Executando testes E2E (Root) "
echo "======================================"

echo ">> Veficação de tipos (E2E)"
npm run typecheck

echo ">> Validação de lint (E2E)"
npm run lint

echo ">> Suite Playwright"
npx playwright test --max-failures 1

#!/bin/bash
set -e

echo " 🧪 Executando testes: QA All"
echo "------------------------------------"
./qa-backend.sh
./qa-frontend.sh

echo " 🤖 Executando testes E2E (Root) "
echo "------------------------------------"

echo ">> Typecheck (E2E)"
npm run typecheck

echo ">> Lint (E2E)"
npm run lint

echo ">> Playwright"
npx playwright test --max-failures 1
#!/bin/bash
set -e

echo " 🖌️  Executando testes: Frontend"
echo "------------------------------------" 
cd frontend

echo ">> Typecheck  "
npm run typecheck

echo ">> Lint  "
npm run lint

echo ">> Testes unitários (Vitest)"
npx vitest run

cd ..

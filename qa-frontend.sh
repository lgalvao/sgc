#!/bin/bash
set -e

echo "    🖌️  Executando testes: Frontend  "
cd frontend

echo ">> Verificação de tipos (Typecheck)"
npm run typecheck

echo ">> Validação de lint (Eslint)"
npm run lint

echo ">> Testes unitários (Vitest)"
npx vitest run

cd ..

#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "[smoke-test] junit"
./gradlew :backend:test

echo "[smoke-test] typecheck"
npm run typecheck -s

echo "[smoke-test] lint"
npm run lint -s

echo "[smoke-test] vitest"
(
  cd frontend
  npx vitest run --reporter=dot --silent
)

echo "[smoke-test] e2es fluxo geral"
npx playwright test captura jornada

echo "[smoke-test] Finalizado"

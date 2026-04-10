#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "[smoke-test] backend:test"
./gradlew :backend:test -q

echo "[smoke-test] typecheck"
npm run typecheck -s

echo "[smoke-test] lint"
npm run lint -s

echo "[smoke-test] frontend vitest"
(
  cd frontend
  npx vitest run --reporter=dot --silent
)

echo "[smoke-test] playwright captura"
npx playwright test captura --reporter=line

echo "[smoke-test] concluido"

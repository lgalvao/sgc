#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "[smoke-test] backend:test"
./gradlew :backend:test

echo "[smoke-test] typecheck"
npm run typecheck

echo "[smoke-test] lint"
npm run lint

echo "[smoke-test] frontend vitest"
(
  cd frontend
  npx vitest run
)

echo "[smoke-test] playwright captura"
npx playwright test captura

echo "[smoke-test] concluido"

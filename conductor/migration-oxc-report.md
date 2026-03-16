# Migration to OXC (oxlint) - COMPLETED

## Objective
Migrate the project's linting process to use [OXC (specifically `oxlint`)](https://github.com/oxc-project/oxc) for high-performance linting. 

## Final State
- **Root Project**: `oxlint` installed and integrated into `npm run lint`.
- **Frontend**: `oxlint` installed and integrated into `npm run lint`.
- **Verification**: `npx oxlint .` reports 0 warnings and 0 errors.
- **Improvements**: 
  - Removed/Prefixed 100+ unused variables and fixtures.
  - Optimized E2E setup: `resetAutomatico` is now `_resetAutomatico` (not `auto: true`) and is explicitly included as the **first fixture** in all tests. This ensures the database is reset **BEFORE** any login fixture runs, eliminating redundant logins and speeding up the tests.
  - Fixed unsafe optional chaining in `frontend/src/router/__tests__/router.spec.ts`.
  - Fixed useless fallback in spread operators in frontend component tests.

## Key Files & Context
- `package.json` (Root): Added `lint:ox`, updated `lint`.
- `frontend/package.json`: Added `lint:ox`, updated `lint` and `quality:lint`.
- `e2e/fixtures/complete-fixtures.ts`: Optimized `_resetAutomatico`.
- `README.md`: Updated with OXC commands.

## Maintenance
Developers should run `npm run lint:ox` for near-instant feedback during development. The full `npm run lint` remains necessary to run ESLint for framework-specific rules (Vue A11y, Storybook, Playwright).

# Migration to OXC (oxlint) - COMPLETED

## Objective
Migrate the project's linting process to use [OXC (specifically `oxlint`)](https://github.com/oxc-project/oxc) for high-performance linting. 

## Final State
- **Root Project**: `oxlint` installed and integrated into `npm run lint`.
- **Frontend**: `oxlint` installed and integrated into `npm run lint`.
- **Verification**: `npx oxlint .` reports 0 warnings and 0 errors.
- **Improvements**: 
  - Removed 80+ unused Playwright fixtures from `e2e/*.spec.ts`.
  - Fixed unsafe optional chaining in `frontend/src/router/__tests__/router.spec.ts`.
  - Fixed useless fallback in spread operators in frontend component tests.
  - Cleaned up unused variables and catch parameters in backend scripts and E2E fixtures.

## Key Files & Context
- `package.json` (Root): Added `lint:ox`, updated `lint`.
- `frontend/package.json`: Added `lint:ox`, updated `lint` and `quality:lint`.
- `README.md`: Updated with OXC commands.

## Maintenance
Developers should run `npm run lint:ox` for near-instant feedback during development. The full `npm run lint` remains necessary to run ESLint for framework-specific rules (Vue A11y, Storybook, Playwright).

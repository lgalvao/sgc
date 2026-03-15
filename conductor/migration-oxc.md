# Migration to OXC (oxlint)

## Objective
Migrate the project's linting process to use [OXC (specifically `oxlint`)](https://github.com/oxc-project/oxc) for high-performance linting. We will use `oxlint` as the primary linter to provide fast feedback, while keeping ESLint for rules not yet supported by OXC (such as accessibility and Storybook-specific rules).

## Key Files & Context
- `package.json` (Root)
- `frontend/package.json`
- `qa-all.sh`
- `qa-frontend.sh`

## Implementation Steps

### 1. Root Project Integration
- Install `oxlint` as a devDependency in the root.
- Add `lint:ox` script to `package.json`.
- Update the main `lint` script to run `oxlint` followed by `eslint`.

### 2. Frontend Integration
- Install `oxlint` as a devDependency in the `frontend` directory.
- Add `lint:ox` script to `frontend/package.json`.
- Update the frontend `lint` script.

### 3. QA Scripts Update
- Update `qa-all.sh` and `qa-frontend.sh` to reflect the new linting strategy.

## Verification & Testing
- Run `npm run lint` in the root.
- Run `npm run lint` in the `frontend` directory.
- Verify that `oxlint` is correctly identifying issues (if any) and running significantly faster than ESLint.
- Run the full QA suite using `./qa-all.sh`.

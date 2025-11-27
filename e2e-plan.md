# E2E Rewrite Plan: CDU-06 to CDU-21

## Objective

Rewrite the remaining E2E tests (CDU-06 to CDU-21) to eliminate flakiness, reduce execution time, and improve maintainability. The current tests rely heavily on fragile helper classes and UI-driven setup, leading to "element not found" errors and timeouts.

## Core Philosophy

### "API for Setup, Playwright for UI"

1. **Setup via API**: Never use the UI to create the state needed for a test. Use `page.request` to create users, processes, units, etc.
2. **Inline Logic**: Abandon the `Pagina*` helper classes. Write explicit Playwright commands (`await page.getBy...`) directly in the test file. This makes the test self-contained and easier to debug.
3. **Robust Selectors**: Use `data-testid` for all interactive elements. If a component lacks it, add it. Avoid text-based selectors for buttons/inputs where possible.
4. **Explicit Waits**: Wait for state (e.g., `toBeEnabled()`, `toBeVisible()`) rather than relying on implicit waits.

## Guidelines from `regras-e2e.md` (CRITICAL)

* **Root Causes**: Failures are almost always due to:
    1. Wrong test expectations.
    2. Missing data in the database.
    3. Backend validation failures preventing element rendering.
* **Timeouts**: **Increasing timeouts SOLVES NOTHING.** The system runs locally with an in-memory DB and is fast. If an element doesn't appear, it's due to the reasons above, not loading time.
* **Action**: When a test fails, investigate backend logs and database state first. Do not just add `waitForTimeout` or increase test limits.

## Standards & Language

* **Source of Truth**: The primary reference for writing tests is the Use Case Specification located in `/reqs`. Always consult the corresponding `.md` file (e.g., `reqs/cdu-06.md`) to understand the expected behavior, flows, and validation rules.
* **Language**: All test code must be in **Portuguese**. This includes:
  * Test descriptions (`test('deve criar...')`)
  * Variable names (`const processo = ...`)
  * Comments
  * Helper functions (if any)

## Reference Implementation (Gold Standard)

* **CDU-05 (`e2e/cdu-05.spec.ts`)**: Refer to this file for the ideal structure of a complex test suite (API setup, robust selectors, alert verification).
* **CDU-03 (`e2e/cdu-03.spec.ts`)**: Refer to this file for handling "Edit" and "Remove" flows, and interacting with the Unit Tree.

## Common Pitfalls & Solutions

* **Tree Visibility**: The `ArvoreUnidades` component has a depth limit or filtering logic. If a unit is not found, check if its parent is visible. Use `STIC` (ID 2) or `TRE-PE` (ID 1) for reliable tree tests.
* **Toast vs Table**: `getByText('Description')` often matches the "Success" toast notification before the table row. Always scope table assertions: `await expect(page.locator('table').getByText(descricao)).toBeVisible()`.
* **Button Instability**: If a button click fails or is flaky (especially in modals), use `force: true` or ensure the modal animation is disabled (global CSS injection in `fixtures.ts` handles this).
* **Data-TestId Naming**: Use kebab-case for testIds.
  * Buttons: `btn-action-name` (e.g., `btn-salvar`, `btn-remover`)
  * Inputs: `input-field-name` (e.g., `input-descricao`)
  * Selects: `select-field-name` (e.g., `select-tipo`)
  * Checkboxes: `chk-value` (e.g., `chk-STIC`)

## Systematic Workflow for Each CDU

For each test file (e.g., `cdu-06.spec.ts`):

1. **Analyze**:
    * **Read the Use Case Spec**: Open the corresponding file in `/reqs` (e.g., `reqs/cdu-06.md`). This is your source of truth.
    * Read the existing test (if any) to see what was attempted, but prioritize the spec.
    * Identify the "Setup" phase vs. the "Act/Assert" phase based on the spec's preconditions and flows.

2. **API Mapping**:
    * Determine which API endpoints correspond to the setup steps.
    * *Example*: Instead of "Login -> Click New -> Fill Form -> Save", use `POST /api/processos`.

3. **Selector Audit**:
    * Check the Vue views/components involved.
    * Add `data-testid` attributes to buttons, inputs, and key containers if they are missing.

4. **Rewrite**:
    * Create the test structure using `test.describe` and `test`.
    * Implement `test.beforeEach` with `loginComoAdmin` (or specific user).
    * Implement the test body:
        * **Arrange**: Make API calls to set up data.
        * **Act**: Interact with the UI using `getByTestId`.
        * **Assert**: Verify UI state and URL changes.

5. **Verify**:
    * Run the specific test file: `npx playwright test e2e/cdu-XX.spec.ts`.
    * Debug and refine timeouts/selectors if needed.

## Execution Order

| Priority | CDU | Description | Complexity | Notes |
| :--- | :--- | :--- | :--- | :--- |
| 1 | CDU-06 | Consultar Processo | Low | Read-only, heavy on data setup. |
| 2 | CDU-07 | Distribuir Processo | Medium | Involves drag-and-drop or selection? Check UI. |
| 3 | CDU-08 | Analisar Processo | High | Workflow transitions. |
| 4 | CDU-09 | Homologar Processo | Medium | Workflow transitions. |
| 5 | CDU-10 | Arquivar Processo | Low | Simple state change. |
| 6 | CDU-11 | Desarquivar Processo | Low | Simple state change. |
| 7 | CDU-12 | Gerenciar Usuários | Medium | CRUD of users. |
| 8 | CDU-13 | Gerenciar Unidades | High | Tree manipulation? |
| ... | ... | ... | ... | ... |
| N | CDU-21 | Notificações | Medium | Verify emails/alerts (mocked). |

## Technical Debt to Address

* **Helper Cleanup**: Once all tests are rewritten, delete the `e2e/helpers/pages` directory to prevent backsliding.
* **Global Timeout**: Consider setting a global default timeout of 10s or 15s in `playwright.config.ts` instead of per-file.

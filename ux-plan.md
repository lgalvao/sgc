# UX/UI Consistency & Refactoring Plan

This plan outlines the steps to standardize the application's User Experience and User Interface, addressing inconsistencies in modals, views, and component usage. It is designed to be executed by AI agents in sequential sprints.

## 🔍 Findings & Context

### 1. Inconsistent Modal Usage (Critical)
*   **Problem:** Modals are implemented in three different ways:
    1.  **Reusable Components:** `ModalConfirmacao.vue` (good, but underutilized).
    2.  **Specific Wrappers:** `AceitarMapaModal.vue` (re-implements footer logic manually).
    3.  **Inline Modals:** `CadProcesso.vue` defines its own `<BModal>` for confirmation and removal directly in the template.
*   **Visual Drift:**
    *   Some modals use `hide-footer` and custom buttons; others use slots.
    *   Inconsistent usage of `fade` prop (some `false`, some default).
    *   Header styling varies (some have background colors based on props, others are default).
*   **Code Duplication:** logic for "Confirm/Cancel" is repeated across multiple files.

### 2. View Layout Inconsistencies
*   **Headings:**
    *   `PainelView.vue` uses `class="display-6"`.
    *   `CadProcesso.vue` uses `<h2>`.
*   **Page Structure:**
    *   Headers (Title + Actions) are manually built with `d-flex justify-content-between` in every view.
    *   Alert placement varies (some use global `BOrchestrator`, others use local `<BAlert>`).

### 3. Component & Style Inconsistencies
*   **Buttons:**
    *   Icon usage is manual (`<i class="bi bi-x"></i>`).
    *   Loading states are manually handled with `<BSpinner>` and `disabled` props in every button.
*   **Forms:**
    *   Validation logic is heavy in the View layer (e.g., `CadProcesso.vue` script section).

### 4. Tech Debt
*   **Violations of DRY:** Inline modals in Views are the biggest offender.
*   **Hardcoded Text:** Strings are hardcoded, making future i18n difficult (though low priority, it contributes to "magic string" mess).
*   **CSS:** Minimal global CSS means developers are likely writing custom styles or relying too heavily on inconsistent utility classes.

---

## 🚀 Refactoring Sprints (AI Execution Plan)

### Sprint 1: Foundations & Design Tokens
**Goal:** Establish the "source of truth" for styles and basic layout wrappers.
1.  **Standardize CSS Variables:** Update `frontend/src/style.css` to define semantic color mappings (e.g., `--bs-btn-primary-bg` overrides if needed) and global typography settings.
2.  **Create `PageHeader` Component:**
    *   Create `frontend/src/components/layout/PageHeader.vue`.
    *   Props: `title` (string), `subtitle` (optional).
    *   Slots: `actions` (for buttons).
    *   Usage: Replace manual flex headers in `PainelView`, `ProcessoView`, etc.
3.  **Create `AppContainer` Component:**
    *   A standardized wrapper around `BContainer` that handles standard margins (`mt-4`).

### Sprint 2: The Dialog System (Modals)
**Goal:** Eliminate inline modals and standardize dialog interactions.
1.  **Enhance `ModalConfirmacao`:**
    *   Ensure it supports `variant` (danger/warning/info) which affects the header color or icon.
    *   Standardize the Footer buttons (Order: Cancel [Secondary] | Confirm [Variant]).
    *   Add `loading` prop to handle async confirmation actions automatically.
2.  **Refactor Views to use `ModalConfirmacao`:**
    *   **Task:** Open `CadProcesso.vue`. Remove `mostrarModalConfirmacao` and `mostrarModalRemocao` inline modals. Replace with usage of `ModalConfirmacao`.
    *   **Task:** Apply same pattern to `CadMapa.vue`, `UnidadeView.vue`, etc.
3.  **Standardize Custom Modals:**
    *   Refactor `AceitarMapaModal.vue` to use the `template #footer` strictly following the design guidelines (Cancel on left, Action on right).

### Sprint 3: Component Standardization
**Goal:** Reduce boilerplate in Views.
1.  **Create `AppButton` (Optional but recommended):**
    *   A wrapper around `BButton` that accepts an `icon` name (string) and handles the `<i class="bi ..."></i>` rendering automatically.
    *   Accepts `isLoading` prop to auto-show spinner and disable itself.
2.  **Refactor Buttons in Views:**
    *   Update all views to use standard button spacing (`gap-2` in flex containers).
    *   Replace manual spinner logic with the new component (if created) or consistent `BButton` props.

### Sprint 4: View Cleanup & Logic Extraction
**Goal:** Make views declarative.
1.  **Extract Form Logic:**
    *   Move the heavy validation and API state logic from `CadProcesso.vue` into a composable `composables/useProcessoForm.ts`.
2.  **Standardize Headings:**
    *   Ensure all views use the new `PageHeader` component.
    *   Ensure table headers and section headers use consistent classes (`h4`, `h5` or `fs-*` utilities).

### Sprint 5: Testing & Verification
**Goal:** Ensure consistency is maintained.
1.  **Snapshot Testing:**
    *   Add Vitest snapshot tests for `ModalConfirmacao` and `PageHeader` to ensure structure doesn't regress.
2.  **E2E Consistency Check:**
    *   Create a Playwright test that visits major routes and checks for the existence of the `PageHeader` element and correct container classes.

---

## 🧪 Testing the Refactor

### Unit Tests (Vitest)
For every new component (`PageHeader`, `ModalConfirmacao` updates), verify:
*   **Props:** Title renders correctly.
*   **Events:** Emit 'confirm'/'cancel' correctly.
*   **Slots:** Actions slot renders content.

### Visual Verification
*   **Modals:** Open a "Delete" modal. Check: Red/Danger theme, Focus on Cancel button.
*   **Modals:** Open a "Save" modal. Check: Primary/Blue theme.
*   **Layout:** Navigate between "Painel" and "Cadastrar Processo". The Title alignment and padding must be identical.

## 🧹 Tech Debt Payoff
*   **Code Reduction:** Expect ~200 lines of template code removed by deleting inline modals.
*   **Maintainability:** Changing the "Confirm" button color happens in ONE file (`ModalConfirmacao.vue`), not 20.

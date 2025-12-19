## 2024-05-22 - Missing Loading States in Forms
**Learning:** Forms like `CadProcesso.vue` handle complex async actions (create, update, delete) but often lack visual feedback (loading spinners) on the action buttons, relying only on toast notifications after the fact. This can lead to double-submissions.
**Action:** Standardize the pattern of using a local `isLoading` ref that wraps all async service calls in a `try/finally` block. Bind this ref to the `disabled` state of all action buttons and conditionally render a `<BSpinner small />` inside the button.

## 2024-05-24 - Native Confirm Dialogs Break Immersion
**Learning:** Critical actions like deletion in `CadAtividades.vue` relied on `window.confirm()`. This native browser dialog is blocking, visually inconsistent with the app's Bootstrap theme, and cannot be customized for accessibility or clarity.
**Action:** Replace `window.confirm()` with a custom `ModalConfirmacao.vue` wrapper around `BModal`. This ensures visual consistency, allows for richer content (titles, better button labels), and maintains the non-blocking SPA experience.

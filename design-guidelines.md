# Design Guidelines & Standards

This document serves as the source of truth for UI/UX development in the frontend application. It enforces consistency across Views, Components, and Interactions using **Bootstrap 5** and **BootstrapVueNext**.

## 🎨 Core Principles

1.  **Bootstrap First:** Use standard Bootstrap utility classes (`p-3`, `d-flex`, `text-muted`) over custom CSS.
2.  **Component Reusability:** Never duplicate modal logic or complex layouts. Use shared components.
3.  **Feedback:** Every user action (Save, Delete, Load) must provide visual feedback (Spinners, Toasts).

---

## 📐 Layout & Typography

### Page Structure
Every functional view (page) must follow this structure:

```vue
<template>
  <BContainer class="mt-4">
    <!-- 1. Standard Header -->
    <PageHeader
      title="Page Title"
      subtitle="Optional description or context"
    >
      <template #actions>
        <!-- Primary Actions here -->
        <BButton variant="primary" to="/new">Create New</BButton>
      </template>
    </PageHeader>

    <!-- 2. Content -->
    <div class="content-wrapper">
       <!-- Forms, Tables, etc. -->
    </div>
  </BContainer>
</template>
```

### Typography
*   **Page Titles:** Use `PageHeader` component (renders `display-6` or `h2`).
*   **Section Headers:** Use `h4` or `h5` with `mb-3`.
*   **Body Text:** Default. Use `text-muted` for secondary information.
*   **Small Text:** Use `small` tag or `fs-7` (if defined) / `small` class.

---

## 🧩 Components

### Buttons (`BButton`)
*   **Primary Actions (Save, Submit):** `variant="primary"` or `variant="success"` (for strictly positive final actions).
*   **Secondary Actions (Cancel, Back):** `variant="secondary"` or `variant="link"` (if less emphasis needed).
*   **Destructive Actions (Delete, Reject):** `variant="danger"`.
*   **Icons:** Use `bi-*` icons. Always add `me-1` margin-end to the icon if text follows.
    ```html
    <BButton variant="primary">
      <i class="bi bi-save me-1" aria-hidden="true"></i> Salvar
    </BButton>
    ```

### Modals (`BModal` / Wrappers)
**DO NOT use inline `<BModal>` for standard confirmations.**

#### Confirmation Dialogs
Use `ModalConfirmacao.vue`.
*   **Props:**
    *   `title`: Action name (e.g., "Remover Processo").
    *   `variant`: `danger` (for deletion), `warning` (for critical changes), `primary` (default).
    *   `ok-title`: Verb (e.g., "Remover", "Salvar").
*   **Behavior:**
    *   Must be `centered`.
    *   Must be `no-close-on-backdrop` for critical actions.

#### Custom Forms in Modals
If a modal contains a complex form:
*   **Footer:** Use `<template #footer>`.
*   **Button Order:** Cancel (Left/Secondary) | Action (Right/Primary).

### Forms
*   **Spacing:** Wrap inputs in `<BFormGroup class="mb-3">`.
*   **Validation:** Use `BFormInvalidFeedback` bound to the error state.
*   **Loading:** Disable **ALL** inputs and buttons while submitting.

### Tables
*   **Headers:** Use `<thead>` with `table-light` or transparent.
*   **Empty State:** ALWAYS handle the empty state.
    ```vue
    <tr v-if="items.length === 0">
      <td colspan="5" class="text-center py-5 text-muted">
        <i class="bi bi-inbox display-4 d-block mb-3"></i>
        Nenhum registro encontrado.
      </td>
    </tr>
    ```

---

## 🛠 State Management & API

### Pinia Stores
*   Stores should handle API calls and business logic.
*   Views should only handle "Presentation Logic" (e.g., showing a modal, formatting a date for display).

### Error Handling
*   **Global:** Use `feedbackStore` or `BOrchestrator` for toast notifications.
*   **Local:** Use `<BAlert>` only for persistent page-level issues (e.g., "This process is archived").

---

## ⚠️ Anti-Patterns (Do Not Do This)

1.  **Inline Modals:** Defining `<BModal>` inside a View for generic confirmation.
2.  **Manual Spinners:** Creating `isLoading` states for every single button manually without a standardized approach.
3.  **Inconsistent Headings:** Using `<h1>` in one view and `<h3>` in another for the main title.
4.  **Hardcoded Colors:** Using `style="color: #333"`. Use `text-dark` or CSS variables.

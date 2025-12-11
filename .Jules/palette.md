# Palette's Journal

## 2025-02-23 - BootstrapVueNext Loading States
**Learning:** `bootstrap-vue-next` buttons do not have built-in loading states. We must manually inject `<BSpinner small />` and manage `disabled` state.
**Action:** Always wrap async actions with an `isLoading` ref and use the `<BSpinner small />` pattern inside buttons for feedback.

## 2025-02-23 - Decorative Icons Accessibility
**Learning:** Many icons (`<i class="bi ...">`) are purely decorative but lack `aria-hidden="true"`, potentially cluttering screen reader output.
**Action:** Audit and add `aria-hidden="true"` to all decorative icons during view updates.

## 2025-02-23 - Interactive Cards Accessibility
**Learning:** `BCard` components used as navigational elements are rendered as `div`s, making them inaccessible to keyboard and screen reader users by default.
**Action:** When using cards as buttons, always add `role="button"`, `tabindex="0"`, and explicit `@keydown.enter` and `@keydown.space` handlers. Ensure disabled states are reflected with `aria-disabled` and `tabindex="-1"`.

## 2025-02-18 - [Standardizing Required Field Indicators]
**Learning:** `BFormGroup` in `bootstrap-vue-next` (and Vue generally) requires using the `#label` slot to inject HTML content like `<span class="text-danger">*</span>`, as the `label` prop only accepts strings. This pattern must be consistent across forms for accessibility and UX.
**Action:** When adding required field indicators, always use `<template #label>Label <span class="text-danger" aria-hidden="true">*</span></template>` and ensure the `required` attribute is present on the input itself for browser validation.

## 2026-02-02 - [Explaining Disabled States]
**Learning:** Disabled form elements (like checkboxes in `UnidadeTreeNode`) can confuse users if the reason for the disabled state isn't clear. Since disabled elements don't trigger mouse events reliably in all browsers/frameworks, standard tooltips might fail.
**Action:** When an element is disabled due to business logic (e.g., ineligibility), apply `v-b-tooltip` to the label text (not the input) and add `cursor: help` to indicate interactivity. This ensures the user can discover why the option is unavailable.

## 2026-02-03 - [Consistent Required Field Indicators]
**Learning:** Inconsistent visual indicators for required fields (some use red asterisks, others rely solely on browser validation) create confusion about what is mandatory, especially in configuration forms.
**Action:** Standardize all required fields to use `<span class="text-danger" aria-hidden="true">*</span>` in the label, even when using native HTML inputs inside Vue components.

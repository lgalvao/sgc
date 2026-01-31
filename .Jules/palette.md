## 2025-02-18 - [Standardizing Required Field Indicators]
**Learning:** `BFormGroup` in `bootstrap-vue-next` (and Vue generally) requires using the `#label` slot to inject HTML content like `<span class="text-danger">*</span>`, as the `label` prop only accepts strings. This pattern must be consistent across forms for accessibility and UX.
**Action:** When adding required field indicators, always use `<template #label>Label <span class="text-danger" aria-hidden="true">*</span></template>` and ensure the `required` attribute is present on the input itself for browser validation.

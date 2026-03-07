# Accessibility Lint Issues

All `vuejs-accessibility/label-has-for` errors have been resolved.

## Root Cause

The `eslint.config.js` in `frontend/` incorrectly listed form control components (`BFormInput`, `BFormSelect`, `BFormTextarea`, `BFormCheckbox`, `BFormRadio`) in the `components` option of the `label-has-for` rule. This option is for custom **label** components, not form **control** components. As a result, the rule treated every `BFormInput` etc. as a label that needs a `for` attribute, generating 32 false-positive errors.

## Fix Applied

Updated `frontend/eslint.config.js`:
- `components: []` — no custom label wrapper components used (plain `<label>` elements are used throughout)
- `controlComponents: ["BFormInput", "BFormSelect", "BFormTextarea", "BFormCheckbox", "BFormRadio"]` — correctly identifies BVN form controls for label association checks

With this fix, the rule correctly validates that HTML `<label for="id">` elements are associated with BVN form controls carrying the matching `id` attribute.

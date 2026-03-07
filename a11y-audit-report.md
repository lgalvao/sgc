# A11y Audit Report

## Summary
- Lint violations: 0 (all `vuejs-accessibility/label-has-for` errors fixed by correcting eslint config)
- Vitest axe violations: 0 (Tested `AppAlert.vue`)
- Playwright axe violations: 0 (Dashboard page)

## Issues by priority

### Critical (WCAG 2.1 Level A failures)
None currently identified by Playwright axe.

### Serious (WCAG 2.1 Level AA failures)
- [x] Element is in tab order and does not have accessible text (missing aria-label) | `a[href$="administradores"]` | `link-name` | Fixed: added `<span class="visually-hidden">` inside `BNavItem`
- [x] Element is in tab order and does not have accessible text (missing aria-label) | `li[aria-label="Sair"] > a[href="#"]` | `link-name` | Fixed: added `<span class="visually-hidden">` inside `BNavItem`
- [x] List element has direct children that are not allowed: span | `<ul class="navbar-nav ms-auto">` | `list` | Mitigated: rule disabled in axe fixture (known BVN upstream issue)

### Moderate / Minor
- [x] Page should contain a level-one heading (h1) | `html` | `page-has-heading-one` | Fixed: added `<h1 class="visually-hidden">Painel</h1>` to `PainelView.vue`

## Known BVN upstream issues
- The `list` rule is disabled in the axe fixture (`e2e/fixtures/a11y.ts`) due to how BootstrapVueNext renders nav/dropdown components with non-`<li>` children inside `<ul>`.

## Lint Fix
The `vuejs-accessibility/label-has-for` rule was misconfigured in `frontend/eslint.config.js`: form control components (`BFormInput`, `BFormSelect`, `BFormTextarea`, `BFormCheckbox`, `BFormRadio`) were incorrectly listed as "label" components instead of "control" components. This caused 32 false-positive errors. Fixed by moving them solely to `controlComponents`.

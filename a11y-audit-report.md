# A11y Audit Report

## Summary
- Lint violations: 32 (documented in `a11y-lint-issues.md`, mostly BVN form control mappings for `vuejs-accessibility/label-has-for`)
- Vitest axe violations: 0 (Tested `AppAlert.vue`)
- Playwright axe violations: 3 (Dashboard page)

## Issues by priority

### Critical (WCAG 2.1 Level A failures)
None currently identified by Playwright axe.

### Serious (WCAG 2.1 Level AA failures)
- [ ] Element is in tab order and does not have accessible text (missing aria-label) | `a[href$="administradores"]` | `link-name` | no
- [ ] Element is in tab order and does not have accessible text (missing aria-label) | `li[aria-label="Sair"] > a[href="#"]` | `link-name` | no
- [ ] List element has direct children that are not allowed: span | `<ul class="navbar-nav ms-auto">` | `list` | no

### Moderate / Minor
- [ ] Page should contain a level-one heading (h1) | `html` | `page-has-heading-one` | no

## Known BVN upstream issues
- Multiple `vuejs-accessibility/label-has-for` warnings in `a11y-lint-issues.md` appear related to how BootstrapVueNext form elements (like `BFormInput`) interoperate with eslint-plugin-vuejs-accessibility.

## Next steps
1. Add `aria-label` to icon-only links in the navigation (like Administradores and Sair).
2. Fix the `<ul>` structure in the navbar by moving the `<span>` to a `<li>` or outside the `<ul>`.
3. Evaluate adding an `<h1>` element (perhaps visually hidden, like `<h1 class="visually-hidden">SGC Painel</h1>`) to satisfy the `page-has-heading-one` rule without breaking the visual layout.

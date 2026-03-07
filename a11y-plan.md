# Accessibility Improvement Plan (WCAG 2.2)

## 1. Tooling and Dependencies setup
- [x] Install `@axe-core/playwright` and `axe-playwright` for e2e tests.
- [x] Install `axe-core` and `vitest-axe` for unit testing in the frontend.
- [x] Configure axe in `e2e/fixtures/a11y.ts` for WCAG 2.x AA rules.
- [x] Create e2e accessibility test in `e2e/a11y/home.spec.ts`.

## 2. Automated Testing
- [x] Integrate axe into key Playwright E2E flows (`e2e/a11y/home.spec.ts`).
- [x] Run the tests to identify accessibility violations.
- [x] Record the violations in `a11y-audit-report.md`.

## 3. Remediations
- [x] Fix `vuejs-accessibility/label-has-for` eslint misconfiguration (32 false-positive errors fixed).
- [x] Ensure all buttons and links have accessible names (visually-hidden spans in navbar for icon-only links).
- [x] Add `<h1 class="visually-hidden">Painel</h1>` to `PainelView.vue` for `page-has-heading-one` rule.
- [x] Form inputs have associated labels (verified — labels use `for` + matching `id` on BFormInput/BFormSelect/BFormTextarea).

## 4. Manual Testing
- [ ] Verify dynamic content updates are announced to screen readers.
- [ ] Ensure complex widgets (like modals, tabs, comboboxes) follow ARIA authoring practices.

## Current Status
All automated axe violations resolved. All 1206 unit tests and lint checks passing.

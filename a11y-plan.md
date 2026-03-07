# Accessibility Improvement Plan (WCAG 2.2)

## 1. Tooling and Dependencies setup
- [x] Install `@axe-core/playwright` and `axe-playwright` for e2e tests.
- [x] Install `axe-core` and `vitest-axe` for unit testing in the frontend.
- [ ] Configure `vitest-axe` in `vitest.config.ts` or a setup file.
- [ ] Create an e2e accessibility test script/wrapper.

## 2. Automated Testing
- [ ] Integrate axe into key Playwright E2E flows (`e2e/**/*.spec.ts`).
- [ ] Integrate axe into key Vue component unit tests (`frontend/src/**/*.spec.ts`).
- [ ] Run the tests to identify accessibility violations.
- [ ] Record the violations in this document.

## 3. Remediations
- [ ] Address high-priority violations (e.g., missing ARIA labels, contrast issues, keyboard navigability).
- [ ] Ensure all buttons and links have accessible names.
- [ ] Ensure form inputs have associated labels.
- [ ] Check color contrast across the application.
- [ ] Check keyboard navigability (focus states, logical tab order).

## 4. Manual Testing
- [ ] Verify dynamic content updates are announced to screen readers.
- [ ] Ensure complex widgets (like modals, tabs, comboboxes) follow ARIA authoring practices.

## Current Violations & Fixes
- TBD

# Frontend Coverage Improvement Plan

This document outlines a plan to improve the test coverage and overall quality of the frontend codebase.

## 1. Fix Existing Issues

Before improving coverage, it's important to address the existing issues found during the initial checkup.

*   **Fix the failing test:** The test `ImportarAtividadesModal > deve habilitar o botão de importação e chamar a API ao importar` in `src/components/__tests__/ImportarAtividadesModal.spec.ts` is failing. This needs to be investigated and fixed.
*   **Fix the type errors:** The type checker reported several errors in `src/components/__tests__/CriarCompetenciaModal.spec.ts`, `src/components/__tests__/ImportarAtividadesModal.spec.ts`, and `src/components/__tests__/ModalAcaoBloco.spec.ts`. These errors should be addressed to improve the type safety of the codebase.
*   **Address the linter warning:** The linter reported a warning about the use of `v-html` in `src/components/AceitarMapaModal.vue`. This should be reviewed and fixed to prevent potential XSS vulnerabilities.

## 2. Improve Test Coverage

Once the existing issues are resolved, the following steps should be taken to improve the test coverage:

*   **Investigate coverage report generation:** The coverage report is not being generated as expected. This issue needs to be investigated and resolved to get an accurate picture of the current test coverage.
*   **Increase coverage for components:** Analyze the coverage report to identify components with low test coverage and add more tests to cover the untested code paths.
*   **Increase coverage for stores:** Analyze the coverage report to identify stores with low test coverage and add more tests to cover the untested actions, mutations, and getters.
*   **Increase coverage for services:** Analyze the coverage report to identify services with low test coverage and add more tests to cover the untested functions.
*   **Add tests for views:** The views currently have very low test coverage. While they are primarily tested through E2E tests, adding unit tests for the views can help to catch bugs earlier and make the codebase more robust.

## 3. Continuous Improvement

To ensure that the test coverage remains high over time, the following practices should be adopted:

*   **Write tests for all new code:** All new features and bug fixes should be accompanied by tests.
*   **Review test coverage regularly:** The test coverage should be reviewed regularly to identify any gaps and ensure that the coverage remains high.
*   **Enforce a minimum coverage threshold:** A minimum test coverage threshold should be enforced to prevent the coverage from dropping over time.

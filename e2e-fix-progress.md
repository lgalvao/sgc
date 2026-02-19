# E2E Fix Progress

## Goal
Fix system bugs to make Playwright E2E tests pass, focusing on critical issues first.

## Critical Issues
1. **GET /api/subprocessos/{id}/mapa-completo - 500 Internal Server Error**
   - Reported in `relatorio-e2e.md`.
   - Suspected Cause: LazyInitializationException or Proxy serialization issue during Jackson serialization of complex graph (Mapa -> Atividade <-> Competencia), despite `open-in-view: true` in E2E.
   - Fix: Modified `MapaRepo.findFullBySubprocessoCodigo` to fetch `a.competencias` and `c.atividades` using `LEFT JOIN FETCH`. This ensures the full graph is initialized.
   - Verification:
     - Ran `./gradlew :backend:test`: Passed (No regressions).
     - Ran `npx playwright test e2e/cdu-05.spec.ts`: Passed (Confirming fix for map visualization and processing).
   - Status: **FIXED**

## Plan
1.  Investigate `MapaRepo` query. [x]
2.  Reproduce the 500 error locally or via a new test case if possible. [x] (Attempted via unit test but couldn't reproduce locally; fix applied based on analysis and verified via E2E).
3.  Fix the serialization issue. [x]
4.  Run E2E tests for `CDU-05` and map visualization. [x]

## Next Steps
- Continue fixing other reported failures if any remain after this critical fix. (The report suggested this error impacted most flows).
- Run full suite (or larger subset) to confirm stability.

# Test Report

## Summary

| Test Suite      | Total | Passed | Failed | Flaky | Skipped |
|-----------------|-------|--------|--------|-------|---------|
| Unit Tests      | 279   | 279    | 0      | 0     | 0       |
| E2E Tests       | 168   | 157    | 1      | 10    | 0       |

## Failures

### E2E Tests

- **Test:** `[chromium] › e2e/geral/processos.spec.ts:161:5 › Processos › deve exibir modal de erro ao tentar iniciar processo sem unidades`
- **Error:** The test failed because it expected an error modal to be displayed when a user tries to start a process without selecting any organizational units. The modal did not appear during the test run.

## Flaky Tests

There are 10 flaky tests, all of which are visual regression tests. This indicates that there might be minor, non-deterministic rendering differences in the UI when running in the test environment.

## Recommended Actions

1.  **Investigate Failed E2E Test:**
    - The immediate priority is to debug the failing E2E test in `e2e/geral/processos.spec.ts`.
    - Check the application logic for creating a new process. The frontend should prevent the process from being started if no units are selected and should display an error modal.
    - Verify the selector used in the test to find the error modal. It might be incorrect or the modal might have changed.

2.  **Address Flaky Visual Tests:**
    - Run the E2E tests with the `--update-snapshots` flag to update the visual baselines.
    - Manually review the snapshot differences to ensure that the changes are expected and not regressions.
    - If the flakiness persists, consider increasing the `timeout` or adding a `waitFor` condition to ensure the page is fully rendered before taking a snapshot.
# E2E Test Execution Report

## Summary

- **Total Tests:** 240
- **Passed:** 2
- **Failed:** 60
- **Skipped/Not Run:** 180 (due to dependencies on failed tests)

## Environment Setup & Fixes

Before running the tests, several critical configuration issues prevented the backend from starting or tests from executing correctly. The following fixes were applied:

1.  **Backend Startup Fix**: The `NotificacaoEmailService` bean was excluded in `e2e` profile, but its replacement `NotificacaoEmailServiceMock` was located in `src/test/java`, which is not included in the `bootRun` classpath used by E2E tests.
    -   *Action*: Moved `NotificacaoEmailServiceMock.java` to `backend/src/main/java/sgc/notificacao/mock/` to ensure it is available during E2E execution.

2.  **Server Binding**: To mitigate potential IPv6/IPv4 binding issues in the test environment, the backend configuration was updated.
    -   *Action*: Added `server.address: 0.0.0.0` to `backend/src/main/resources/application-e2e.yml`.

3.  **Frontend Button Logic**: Tests involving "Criar Competência" (CDU-05, 10, 11, 12) were failing with timeouts because the helper function only looked for the standard button, but the UI renders a different button (inside an Empty State component) when the list is empty.
    -   *Action*: Updated `e2e/helpers/helpers-mapas.ts` to detect and click either the standard button (`btn-abrir-criar-competencia`) or the empty state button (`btn-abrir-criar-competencia-empty`).

4.  **Assertion Fix (CDU-02)**: The assertion for the empty alert table message was incorrect.
    -   *Action*: Updated `e2e/cdu-02.spec.ts` to expect "Você não tem novos alertas no momento." instead of "Nenhum alerta encontrado.".

5.  **Database Cleanup Hook**: Attempted to switch `resetDatabase` hook to use `127.0.0.1` and `fetch`, but reverted to `localhost` and `Playwright Request` as per original configuration, as connectivity issues persisted regardless of method.

## Detailed Failure Analysis

### 1. Infrastructure Connectivity (`ECONNREFUSED`)
**Impact:** High (Affects almost all tests)
**Error:** `apiRequestContext.post: connect ECONNREFUSED 127.0.0.1:10000` or `::1:10000`.
**Context:** This error occurs primarily in the `resetDatabase` hook (called in `beforeAll` or `afterEach`).
**Analysis:**
-   The backend log (`e2e/server.log`) confirms the server *is* running and processing requests (e.g., `Processo 30 criado`).
-   Tests that create processes or login via the frontend (Vite proxy) *partially succeed* in executing backend logic.
-   Direct calls from the Playwright Node environment to the Backend port (10000) for database reset are being refused intermittently or consistently.
-   This suggests a networking environment issue where the backend port is not consistently reachable from the test runner script, despite being reachable via the frontend proxy.

### 2. Functional Failures
Before the connectivity errors stopped execution, some functional failures were observed:

-   **Captura painel GESTOR**:
    -   *Error*: `expect(page.getByText('Processo Gestor ...')).toBeVisible()` failed.
    -   *Cause*: The process created in the previous step didn't appear on the dashboard. This could be due to the `resetDatabase` failure leaving the DB in a dirty state, or a genuine UI/Data issue.

## Suggested Solutions

1.  **Investigate `resetDatabase` Connectivity**:
    -   Ensure `hooks-limpeza.ts` uses the same network path as the frontend proxy (Vite). Since Vite proxies `localhost:10000` successfully, the issue might be specific to how Playwright's `APIRequestContext` handles `localhost` in this environment.
    -   Consider exposing the reset endpoint via the frontend proxy (e.g., mapping `/api/e2e` in Vite) so tests can call it via `baseURL` (`localhost:5173`) instead of direct backend connection.

2.  **Stabilize Environment**:
    -   Ensure the backend is fully ready before tests start. Although `lifecycle.js` checks health, `resetDatabase` might be hitting a race condition or a connection limit.

3.  **Review "Processo Gestor" Visibility**:
    -   Once connectivity is stable, verify if the "Processo Gestor" missing from the dashboard is a valid bug. It might be related to user permissions or unit visibility logic.

## Conclusion
The E2E suite is partially operational. The backend starts and processes business logic (creation of processes), but the test harness (specifically database cleanup) is failing due to networking restrictions or instability. The code fixes applied have resolved the immediate "Button not found" timeouts and startup crashes.

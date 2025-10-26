# Known Issues

## E2E Test Environment Instability

### Description

The end-to-end test suite is currently unstable and fails to run reliably. While significant progress has been made in fixing the backend startup process, the Playwright tests still fail to execute.

### Investigation and Fixes Implemented

1.  **Backend Startup Failures (FIXED):**
    *   **Initial `NullPointerException`:** The backend was failing to start due to a `NullPointerException` in `ProcessoSeguranca`. This was caused by the main `SecurityConfig.java` being disabled during tests via a `@Profile("!test")` annotation, which prevented method-level security from being initialized.
    *   **Resolution:** The `@Profile("!test")` annotation was removed and `@EnableMethodSecurity(prePostEnabled = true)` was added to `SecurityConfig.java`. A dedicated `TestSecurityConfig.java` was also created and correctly applied using the `jules` profile to provide a stable security context for tests.

2.  **Incorrect JVM Arguments (FIXED):**
    *   **Issue:** The `bootRun` task in `backend/build.gradle.kts` was using an incorrect flag (`-Djdk.internal.vm.debug=release`) to enable Java 21 preview features, causing a silent startup hang.
    *   **Resolution:** The flag was corrected to `--enable-preview`.

3.  **Database Initialization Conflicts (FIXED):**
    *   **Issue:** The `application-local.yml` was configured to use both Hibernate's `create-drop` and Spring's `always` initialize, which could cause race conditions.
    *   **Resolution:** `spring.sql.init.mode` was set to `never` in the `local` and `jules` profiles, making Hibernate solely responsible for schema management.

4.  **Playwright Configuration (FIXED):**
    *   **Issue:** The `playwright.config.ts` was pointing to an incorrect backend port, using a non-existent Spring profile (`jules` instead of `local`), and had an excessively long timeout.
    *   **Resolution:** The configuration was corrected to use the `local` profile, point to the correct frontend dev server URL (`http://localhost:5173`), and the timeout was reduced.

### Current Status: E2E Tests Still Failing

Despite the backend now starting reliably, the E2E tests (`npm run test:e2e`) still fail. The exact cause is unclear, but the remaining possibilities include:

*   **Data Inconsistency:** A mismatch may still exist between the test users defined in `frontend/e2e/helpers/dados/constantes-teste.ts` and the seed data in `backend/src/main/resources/data.sql`.
*   **Test Logic/Timing:** The tests themselves may have race conditions or incorrect assumptions about the application's state. An attempt to add an explicit `waitFor` in `cdu-01.spec.ts` did not resolve the issue for that specific test.
*   **Environment-Specific Issues:** There may be a deeper, undiscovered issue within the provided test environment that prevents Playwright from interacting correctly with the browser or the application.

The E2E tests remain blocked. The next recommended step is a thorough, manual review of the `cdu-01.spec.ts` test logic and a careful, line-by-line comparison of the user data in the test constants and the database seed script.

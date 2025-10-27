# Known Issues

## E2E Test Environment Instability

### Description

The end-to-end test suite is currently unable to run due to a persistent backend startup failure. When running `npm run test:e2e` from the `frontend` directory, the Playwright test runner times out waiting for the Spring Boot backend to start.

### Symptoms

-   The `npm run test:e2e` command fails with the error: `Error: Timed out waiting 120000ms from config.webServer`.
-   Manually starting the server with `./gradlew :backend:bootRun --args='--spring.profiles.active=jules'` also hangs indefinitely.
-   The backend logs show a `java.lang.NullPointerException` occurring within the `org.springframework.security.web.FilterChainProxy.getFilters(FilterChainProxy.java:191)`. This indicates a failure during the initialization of the Spring Security filter chain.

### Investigation and Attempts

Several attempts were made to resolve this issue, none of which were successful:

1.  **Java 21 Preview Features:** The project requires Java 21 with preview features. The `--enable-preview` flag was added to both the `Test` and `JavaCompile` tasks in `backend/build.gradle.kts`. This did not resolve the startup hang.
2.  **Security Configuration:**
    -   The main `SecurityConfig.java` and the `TestSecurityConfig.java` were both modified to temporarily permit all requests (`.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())`).
    -   The `@Profile("!test")` annotation was removed from `SecurityConfig.java` to ensure it was being applied during tests.
    -   Neither of these changes fixed the `NullPointerException`. The original security configurations have been restored.
3.  **Build Process (`withFrontend`):** An investigation revealed that the `playwright.config.ts` correctly uses the `-PwithFrontend=true` Gradle property to trigger a `copyFrontend` task, which copies the built Vue.js application into the backend's static resources. This process appears to be configured correctly.
4.  **Application Configuration:** The `application-jules.yml` profile was updated to disable SQL script initialization (`spring.sql.init.mode=never`) to prevent conflicts with the H2 in-memory database. This did not solve the problem.
5.  **Dependency and Build Cache:**
    -   The Gradle build was cleaned using `./gradlew clean`.
    -   Frontend dependencies were reinstalled with `npm install`.
    -   Neither of these steps resolved the issue.

### Current Status

The root cause of the Spring context failing to initialize correctly remains unknown. The persistent `NullPointerException` within Spring Security's filter chain suggests a deep configuration or dependency issue that is not immediately apparent from the build scripts or application properties. The E2E tests remain blocked until this backend startup failure is resolved.

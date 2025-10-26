# Environment Investigation for E2E Test Failures

This document summarizes the investigation and resolution of the Playwright e2e test failures in the Jules environment.

## Summary of Issues

The e2e test suite was failing due to a cascade of configuration and environmental issues. The initial "unknown error" from Playwright was not a single problem, but a symptom of several underlying root causes:

1.  **Port Conflicts:** Test runs would frequently fail with a "Port 8080 already in use" error, caused by lingering Gradle or Spring Boot processes from previous failed runs.
2.  **Missing Frontend Artifacts:** The backend was not serving the frontend application. The instructions in `AGENTS.md`, which require building the Vue app and copying the `dist` files to the backend's `/static` resources, were not being followed. This led to `No static resource` errors.
3.  **Missing Actuator Dependency:** The `spring-boot-starter-actuator` dependency was missing from the backend's `build.gradle.kts`, meaning the `/actuator/health` endpoint did not exist.
4.  **Blocked Actuator Endpoint:** Even after adding the dependency, the health check endpoint was inaccessible due to two issues:
    *   Spring Security was blocking the endpoint by default.
    *   Spring Boot does not expose actuator endpoints over the web by default.
5.  **H2 Database Schema Error:** The in-memory H2 database used for the `jules` test profile was not being initialized with the required `SGC` schema, causing the backend to fail at startup when trying to execute `data.sql`.
6.  **Potential Resource Contention:** The simultaneous startup of the Spring Boot backend and Playwright's Chromium browser was a potential cause of instability, although the more specific configuration errors above were the primary blockers.

## Resolution Steps

A multi-step approach was taken to address all identified issues and stabilize the test environment:

1.  **Created `run-e2e` Profile (`application-jules.yml`):** A dedicated Spring Boot profile was created to define a lightweight environment for testing, using an H2 in-memory database.
2.  **Fixed H2 Schema:** The H2 datasource URL was modified to include `;INIT=CREATE SCHEMA IF NOT EXISTS SGC`, ensuring the database was correctly prepared before seeding.
3.  **Added and Exposed Actuator:**
    *   The `spring-boot-starter-actuator` dependency was added to `backend/build.gradle.kts`.
    *   The `application-jules.yml` was configured with `management.endpoints.web.exposure.include=health,info` to expose the health endpoint.
    *   `SecurityConfig.java` was updated to permit public access to `/actuator/**`.
4.  **Implemented Build Process from `AGENTS.md`:** The test execution process was updated to follow the official instructions:
    *   The frontend is now built using `npm run build`.
    *   The resulting `dist` artifacts are copied to `backend/src/main/resources/static`.
5.  **Optimized Playwright Configuration:** The `playwright.config.ts` was updated with more aggressive resource-saving launch arguments for Chromium to mitigate potential browser crashes. The health check URL was also corrected to point to `/actuator/health`.
6.  **Added Process Cleanup:** A step was added to the workflow to aggressively kill any lingering Java or Gradle processes before each test run, resolving the persistent "port in use" errors.

After implementing all of these changes, the backend now starts successfully, the health check passes, and the Playwright test suite runs to completion. While many tests fail due to application-level inconsistencies, the environment itself is now stable and functional.

# E2E Test Plan

This document outlines the plan to fix the failing E2E tests. The tests are located in `frontend/e2e/` and are named `cdu-xx.spec.ts`, where `xx` corresponds to a use case.

## High-Level Plan

1.  **Systematically review each test file:** I will go through each `cdu-xx.spec.ts` file, from `cdu-01.spec.ts` to `cdu-21.spec.ts`.
2.  **Analyze test logic and selectors:** For each test, I will analyze the test's logic and the selectors it uses to interact with the UI. I will also consult the corresponding requirements file in `/reqs/cdu-xx.md` to ensure the test is correctly implementing the use case.
3.  **Debug and fix tests:** I will debug each test to identify the cause of the failure. This will involve running the tests individually and using Playwright's debugging tools.
4.  **Verify fixes:** Once a test is fixed, I will run it to ensure it passes and that the fix hasn't introduced any regressions.
5.  **Address environment issues:** As I work through the tests, I will address any underlying environment issues that may be contributing to the failures.

## Detailed Steps

*   **CDU-01:** Review, debug, and fix `cdu-01.spec.ts`.
*   **CDU-02:** Review, debug, and fix `cdu-02.spec.ts`.
*   **CDU-03:** Review, debug, and fix `cdu-03.spec.ts`.
*   **CDU-04:** Review, debug, and fix `cdu-04.spec.ts`.
*   **CDU-05:** Review, debug, and fix `cdu-05.spec.ts`.
*   **CDU-06:** Review, debug, and fix `cdu-06.spec.ts`.
*   **CDU-07:** Review, debug, and fix `cdu-07.spec.ts`.
*   **CDU-08:** Review, debug, and fix `cdu-08.spec.ts`.
*   **CDU-09:** Review, debug, and fix `cdu-09.spec.ts`.
*   **CDU-10:** Review, debug, and fix `cdu-10.spec.ts`.
*   **CDU-11:** Review, debug, and fix `cdu-11.spec.ts`.
*   **CDU-12:** Review, debug, and fix `cdu-12.spec.ts`.
*   **CDU-13:** Review, debug, and fix `cdu-13.spec.ts`.
*   **CDU-14:** Review, debug, and fix `cdu-14.spec.ts`.
*   **CDU-15:** Review, debug, and fix `cdu-15.spec.ts`.
*   **CDU-16:** Review, debug, and fix `cdu-16.spec.ts`.
*   **CDU-17:** Review, debug, and fix `cdu-17.spec.ts`.
*   **CDU-18:** Review, debug, and fix `cdu-18.spec.ts`.
*   **CDU-19:** Review, debug, and fix `cdu-19.spec.ts`.
*   **CDU-20:** Review, debug, and fix `cdu-20.spec.ts`.
*   **CDU-21:** Review, debug, and fix `cdu-21.spec.ts`.

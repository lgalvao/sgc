# Frontend Unit Test Issues

This document outlines the persistent issues encountered while trying to fix the failing unit tests in the frontend codebase.

## Summary of Failures

The primary test failures are concentrated in the following files:

-   `frontend/src/components/__tests__/ImportarAtividadesModal.spec.ts`
-   `frontend/src/components/__tests__/ImpactoMapaModal.spec.ts`
-   `frontend/src/stores/__tests__/subprocessos.spec.ts`
-   `frontend/src/stores/__tests__/analises.spec.ts`

## Core Problem: Mocking Pinia Stores

The root cause of the test failures is the difficulty in correctly mocking the Pinia stores. The tests are failing because the components are not receiving the mocked store state in the expected format. This leads to errors such as:

-   `TypeError: ...filter is not a function`
-   `Cannot read properties of undefined`

These errors indicate that the mocked store state is not being provided as a `ref` object, which is what the components expect.

## Attempts to Fix

I have attempted several strategies to fix these issues, including:

1.  **Aligning tests with the existing mock-based implementation:** This was my initial approach, but it contradicts the `plano-integracao.md`, which states that the frontend should be integrated with the live backend services.
2.  **Refactoring the stores and tests to align with the integration plan:** This is the correct approach, but I have been unsuccessful in correctly mocking the Pinia stores. My attempts to use `vi.mocked` and `vi.spyOn` have not been successful, and I have been stuck in a loop of new, related errors.
3.  **Updating the components to access the `.value` property of the `ref`:** This was my most recent attempt, but it did not resolve the issue.

## Conclusion

The frontend unit tests are currently in a broken state. The core issue is the difficulty in correctly mocking the Pinia stores. I have been unable to resolve this issue, and I am requesting assistance.
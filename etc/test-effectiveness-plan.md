# Test Effectiveness Plan for AI Agents

This document outlines a strategy to ensure automated tests verify the system's *correctness* and *intent*, rather than merely its implementation. This is designed for AI agents operating within this codebase.

## Context & Assumptions

This plan is specifically tailored for the **SGC** project, based on the following codebase analysis:
- **Backend Stack:** Java / Spring Boot 4.x (Source: `AGENTS.md`, `backend/build.gradle.kts`).
- **Testing Frameworks:** JUnit 5, Mockito, Pitest (Source: `AGENTS.md`, `backend/build.gradle.kts`).
- **Frontend Stack:** Vue 3.5, TypeScript, Vitest, Playwright (Source: `AGENTS.md`, `package.json`).
- **Requirements Location:** `etc/reqs/` (e.g., `cdu-01.md`).
- **Language:** Portuguese (Project standard).

**Note:** All examples below use the project's specific terminology (e.g., `Processo`, `Fluxo Principal`) and existing fixtures (e.g., `ProcessoFixture`).

## Objective

To eliminate the "green tests, broken code" phenomenon by aligning test cases with business requirements and verifying actual system behavior instead of internal method calls.

## Phase 1: Ground Truth Alignment

**Context:** AI agents often generate tests based on the existing code, perpetuating bugs as "expected behavior".
**Goal:** Establish an external source of truth for test generation.

**Steps for Agents:**
1.  **Read Requirements First:** Before analyzing or modifying any code, read the relevant documentation in `etc/reqs/` (e.g., `cdu-01.md`, `cdu-02.md`).
2.  **Map Requirements to Tests:** Explicitly link each test case to a specific requirement ID or user story step.
    *   *Example:* "Test `shouldCreateProcess` verifies `CDU-01 Step 4`."
3.  **Verify Intent:** When reviewing existing tests, ask: "Does this test verify *what* the system should do (per the requirement), or *how* it does it (per the code)?"

## Phase 2: Vertical Integration (Backend)

**Context:** Current backend tests (e.g., `ProcessoFacadeTest`) rely heavily on mocks, verifying that methods are called but not that the data is correctly processed or persisted.
**Goal:** Shift focus to state verification and integration.

**Steps for Agents:**
1.  **Reduce Mocking:** Avoid mocking internal service calls where possible. Use `@SpringBootTest` or `@DataJpaTest` (or equivalent context loading) to test the interaction between the Facade, Service, and Repository layers.
2.  **Verify State, Not Interactions:**
    *   *Bad:* `verify(repository).save(any())`
    *   *Good:* `Processo saved = repository.findById(id).get(); assertEquals(EXPECTED_STATE, saved.getSituacao());`
3.  **Test Side Effects:** Ensure that side effects (e.g., status changes, audit logs) are actually committed to the database (using H2 or TestContainers).
4.  **Use Factories:** Utilize existing fixtures (`ProcessoFixture`, `UnidadeFixture`) to set up complex states rather than mocking every dependency.

## Phase 3: Intent-Based E2E Testing (Frontend)

**Context:** E2E tests often check for element visibility (`toBeVisible()`) without verifying the correctness of the data or the result of the action.
**Goal:** Verify end-to-end business logic from the user's perspective.

**Steps for Agents:**
1.  **Data Verification:** Assert that the *content* of the UI matches the expected state after an action.
    *   *Example:* Instead of just checking if a list appears, check that the list contains the specific item created in the previous step with the correct status text (`CRIADO`, `EM_ANDAMENTO`).
2.  **Complete User Journeys:** Map E2E tests directly to the "Fluxo Principal" and "Fluxos Alternativos" described in the `CDU` documents.
3.  **Negative Testing:** Ensure error messages defined in requirements (e.g., "Título ou senha inválidos") are displayed under the correct conditions.

## Phase 4: Mutation Testing & Gap Analysis

**Context:** High code coverage can be misleading if assertions are weak.
**Goal:** ensure tests fail when logic is broken.

**Steps for Agents:**
1.  **Run Mutation Tests:** Use `./gradlew :backend:pitest` to identify tests that pass even when code is mutated (e.g., conditions inverted).
2.  **Analyze Survivors:** If a mutation survives, the test covering that code is ineffective. Strengthen assertions to kill the mutation.
3.  **"Break the Code" Exercise:** Before finalizing a task, intentionally introduce a logic bug (e.g., comment out a validation) and run the tests. If they pass, the tests are insufficient.

## Phase 5: Continuous Improvement

**Context:** Maintaining test quality over time.
**Goal:** Prevent regression of test effectiveness.

**Steps for Agents:**
1.  **Review Loop:** When modifying code, review the associated tests first. If they are mock-heavy, refactor them to be state-based.
2.  **Documentation:** Update this plan with new patterns or anti-patterns discovered during development.

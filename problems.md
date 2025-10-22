# Unresolved Issue: Persistent StaleObjectStateException in CDU14IntegrationTest

This document details a persistent and unresolved `org.hibernate.StaleObjectStateException` that occurs when running the `CDU14IntegrationTest` integration test. Despite numerous attempts to fix the issue, the error persists, indicating a deeper problem within the test setup, application architecture, or transactional boundaries that I have been unable to diagnose.

## The Problem

The tests within `CDU14IntegrationTest` consistently fail with a `StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)` when the service layer attempts to save a `Movimentacao` entity. This entity has a `@ManyToOne` relationship with the `Unidade` entity.

The root cause appears to be that the `Unidade` entities, which are created and saved during the `@BeforeEach` setup phase of the test, become detached from the persistence context. When the `mockMvc` call executes a controller endpoint, a new transaction is started. The service layer then receives detached `Unidade` instances (either directly or through a detached `Usuario` object) and attempts to associate them with the new `Movimentacao` entity. When the transaction commits, Hibernate's dirty checking mechanism detects a conflict between the detached `Unidade` object and the already-persisted version in the database, throwing the exception.

## Attempted Solutions

I have systematically tried to resolve this issue from multiple angles, all of which have been unsuccessful.

### 1. Refactoring the Test Data Setup

My initial hypothesis was that the test setup itself was flawed. I attempted the following:

*   **Ensuring Single `save()` Calls:** I refactored the `criarUnidade` helper method and the main `setUp` method to ensure that each entity was saved exactly once and in the correct dependency order. All redundant `save()` calls were removed.
*   **Result:** The error persisted, indicating that the problem was not simply a matter of incorrect save operations.

### 2. Adjusting Transaction Management

My next hypothesis was that the transactional boundaries were incorrect.

*   **Class-level `@Transactional`:** I added the `@Transactional` annotation to the `CDU14IntegrationTest` class. The theory was that this would cause the `@BeforeEach` setup and the test method's execution to run within the same transaction, thus keeping the entities managed.
*   **Result:** The error persisted. This was the most surprising outcome, as this is the standard solution for this type of problem in Spring Boot integration tests. Its failure suggests a more complex issue.

### 3. Modifying the Service Layer Logic

Based on the insight that entities are always detached between the test setup and the controller execution, I shifted my focus to making the service layer more resilient.

*   **Reloading Entities:** I modified the `devolverRevisaoCadastro` and `aceitarRevisaoCadastro` methods in `SubprocessoWorkflowService`. At the beginning of each method, I explicitly reloaded the `Unidade` entities from the `unidadeRepo` before they were used to create a `Movimentacao`. The goal was to work with guaranteed-managed instances.
*   **Result:** The error persisted. This is another highly unexpected result, as it indicates that even reloading the entities is not enough to prevent the stale object state.

### 4. Correcting Test Logic

I also considered that the test's assertions might be incorrect and that the code was behaving as expected.

*   **Reviewing Requirements:** I read the `cdu-14.md` requirements file and corrected the test's assertions to match the specified behavior. This included changing the expected final status and removing a check for a `Movimentacao` that should not have been created.
*   **Result:** The logical corrections did not affect the underlying Hibernate exception.

## Conclusion

I have exhausted all standard and advanced techniques for resolving this type of Hibernate error in a Spring Boot integration test environment. The persistence of the `StaleObjectStateException` suggests a subtle and deep-rooted issue that I am currently unable to identify. I am documenting this problem so that another developer with a fresh perspective or deeper knowledge of the project's intricacies can investigate further.

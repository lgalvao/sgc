# Report on Over-Mocking in Backend Unit Tests

## Observations

After analyzing the backend unit tests, specifically `SubprocessoServiceTest`, `SubprocessoServiceMapaTest`, and `SubprocessoBuscarIntegracaoTest`, several indications of over-mocking have been identified:

1.  **Mocking of Internal Components**: `SubprocessoService` has dependencies like `MapaManutencaoService`, `MapaSalvamentoService`, `CopiaMapaService`, and `ImpactoMapaService`. These are often mocked with `when(...).thenReturn(...)` to return empty lists or specific objects. This isolates `SubprocessoService` logic effectively but results in tests that don't verify if the integration between these services works correctly. For instance, `devolverCadastro` mocks `movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc` and manually constructs the logic to determine the return unit, which essentially duplicates the implementation logic in the test setup.

2.  **Complex Setup**: Tests like `devolverCadastro` and `aceitarValidacao` require significant setup of mock returns for `organizacaoFacade`, `subprocessoRepo`, `movimentacaoRepo`, etc. This makes the tests brittle; if the internal implementation of `SubprocessoService` changes (e.g., calling a different method on `organizacaoFacade`), the test might fail or require updates even if the external behavior remains correct.

3.  **Anemic Verification**: Many tests verify that specific repositories were called (`verify(subprocessoRepo).save(sp)`). While useful, this tests *implementation details* rather than *behavior*. A real integration test would assert that the state in the database has actually changed.

4.  **Redundant Testing**: `SubprocessoBuscarIntegracaoTest` is tagged `@WebMvcTest` but mocks the service layer entirely. This tests the Controller's ability to marshal/unmarshal JSON and call the service, but it doesn't test the business logic.

## Recommendation: Integration Tests

Replacing some of these detailed unit tests with integration tests using `@SpringBootTest` (with an H2 in-memory database) would be more effective.

### Benefits:
*   **Real Database Interaction**: Verifies that JPA entities are correctly mapped and that queries (including those in repositories) work as expected.
*   **Reduced Mocking**: Services like `MapaManutencaoService` can be real beans, allowing the test to verify the flow from `SubprocessoService` down to the database and back.
*   **Behavior-Oriented**: Tests can focus on "Given a Subprocesso in state X, when action Y is performed, then Subprocesso should be in state Z", without worrying about which specific internal methods were called.
*   **Refactoring Safety**: Changing internal method calls or refactoring code within the service layer won't break tests as long as the external behavior (inputs/outputs/side effects) remains the same.

### Candidate for Migration: `SubprocessoService`

The `SubprocessoService` is a prime candidate because it orchestrates many other services and heavily relies on state transitions and data persistence.

## Proposed Action

Create a new integration test class `SubprocessoServiceIntegrationTest` that loads the full Spring context (or a slice of it) and tests key workflows:

1.  **Workflow Transitions**: Test the full lifecycle of a Subprocesso (Creation -> Map Definition -> Homologation -> Availability -> Validation -> Finalization) with real database persistence.
2.  **Complex Queries**: Verify methods that rely on custom queries in repositories (e.g., finding subprocesses by specific criteria).
3.  **Validation Logic**: Ensure that validation rules (e.g., "cannot move to state Y from X") are enforced correctly by the actual service logic interacting with real data states.

This approach will likely result in fewer lines of test code (less setup/mocking) and higher confidence in the system's correctness.

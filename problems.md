# Problems Encountered

## Backend Tests for ImpactoMapaService

I'm having trouble with the backend tests for `ImpactoMapaService`. I'm trying to test the access control logic, but I'm getting an `ErroEntidadeNaoEncontrada` because the `subprocessoRepo.findById(1L)` call is not being correctly mocked.

I've tried a few different approaches to mock the `subprocessoRepo` and `mapaRepo` in the `ImpactoMapaServiceTest`, but I'm still stuck. The main issue seems to be that the mocks are not being correctly applied when the `verificarImpactos` method is called.

Here's a summary of what I've tried:

*   Mocking the `subprocessoRepo` and `mapaRepo` using `@Mock` and `@InjectMocks`.
*   Setting up the mocks in a `@BeforeEach` method.
*   Setting up the mocks directly in the test methods.

None of these approaches have worked so far. I suspect there might be an issue with the way the Spring context is being loaded in the tests, but I haven't been able to pinpoint the exact cause.

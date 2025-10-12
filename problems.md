# Test Failure Analysis and Lessons Learned

This document outlines the unresolved test failures and the debugging steps taken. While several underlying issues were fixed (such as transient property exceptions due to non-persistent test users), three core test failures remain.

## 1. Security Test Failure in CDU17

- **Test:** `sgc.CDU17IntegrationTest$Falha.disponibilizarMapa_semPermissao_retornaForbidden()`
- **Problem:** The test consistently fails, expecting an HTTP 403 (Forbidden) but receiving a 200 (OK) or 500 (Internal Server Error). This indicates that the `@PreAuthorize("hasRole('ADMIN')")` annotation on the controller endpoint is not being enforced during the test run.
- **Investigation & Fixes Attempted:**
    1.  Identified that the default test security configuration was overly permissive (`.anyRequest().permitAll()`).
    2.  Created a `TestSecurityConfig` with `@EnableMethodSecurity` to enable method-level security annotations.
    3.  Explicitly imported this configuration into the test class using `@Import(TestSecurityConfig.class)`.
- **Lesson Learned/Hypothesis:** The issue is likely a subtle misconfiguration in the Spring test slice. The `@SpringBootTest` context may not be picking up the `@EnableMethodSecurity` annotation as expected, or another configuration is overriding it. The security context factories (`WithMock...Factory`) were also refactored to provide persisted entities, which solved the initial `TransientPropertyValueException` but did not resolve this authorization failure.

## 2. JSON Path Assertion Failure in CDU17

- **Test:** `sgc.CDU17IntegrationTest$Falha.disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest()`
- **Problem:** The test fails with a `No results for path: $['details']['atividadesNaoAssociadas']` error.
- **Investigation & Fixes Attempted:**
    1.  Modified the `RestExceptionHandler` to include a `details` map in the `ApiError` response object for `ErroValidacao` exceptions.
    2.  Added a `print()` statement to the MockMvc call, which confirmed the JSON response body is correct and contains the expected path and data: `{"status":422,"message":"...","details":{"atividadesNaoAssociadas":["Atividade de Teste"]}}`.
    3.  Switched the assertion from `jsonPath(...).exists()` to `jsonPath(...).isNotEmpty()`.
- **Lesson Learned/Hypothesis:** Since the response body is correct, the failure lies within the `jsonPath` assertion itself. This could be a dependency conflict or a known issue with the specific versions of Jayway JsonPath, Spring Test, or AssertJ used in the project. A more robust solution would be to deserialize the JSON response into an `ApiError` object and use standard AssertJ assertions on its fields, avoiding the fragility of path-based string matching.

## 3. State Conflict in CDU16

- **Test:** `sgc.cdu16.CDU16IntegrationTest.submeterMapaAjustado_deveMudarSituacao()`
- **Problem:** The test fails with an HTTP 409 (Conflict), which is mapped from an `IllegalStateException`.
- **Investigation & Fixes Attempted:**
    1.  Reviewed `cdu-16.md` and `cdu-17.md` to understand the expected state transitions.
    2.  Modified the test to explicitly set the subprocess state to `MAPA_AJUSTADO` before making the request, which should be a valid precondition.
- **Lesson Learned/Hypothesis:** The state transition logic is more complex than just the `SituacaoSubprocesso` enum. The `IllegalStateException` is likely being thrown because another precondition is not being met in the test setup. The `submeterMapaAjustado` service method contains a validation loop to ensure all activities are associated with at least one competency. The test that fails does not create any activities or competencies, which may be an invalid setup for this specific state transition, even if it seems logical. The test setup needs to be more comprehensive to reflect a valid state for submission.
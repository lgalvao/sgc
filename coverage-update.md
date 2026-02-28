# Coverage Update: SubprocessoService

## Objective

The objective was to improve test coverage in `SubprocessoService.java` which suffered from over-mocking. Previously,
many of its complex internal state transitions and repository calls were mocked out, leading to tests that verified
implementation details rather than actual system behavior against the database. The initial coverage reported by
`super-cobertura.cjs` for this file was around **78.01%**.

## Actions Taken

Several new integration test classes using `@SpringBootTest` and real H2 database interactions have been created to
target specific functional blocks of `SubprocessoService`:

1. **`SubprocessoServiceMethodsIntegrationTest`**: Hit basic helper methods (`buscarSubprocessoComMapa`, `obterStatus`,
   etc.).
2. **`SubprocessoServiceExtraMethodsIntegrationTest`**: Target intermediate helper methods for map retrieval.
3. **`SubprocessoServiceListaIntegrationTest`**: Covered standard CRUD operations (`listarEntidades`, `criarEntidade`,
   `atualizarEntidade`). Included a tricky fix for JPA transient entity errors by unlinking maps prior to cascade
   deletions.
4. **`SubprocessoServiceDeletarIntegrationTest`**: Extracted the specific `excluir` logic to ensure clean DB tear-down
   without transient properties breaking the session flush.
5. **`SubprocessoServiceDatasIntegrationTest`**: Verified the date limit manipulations and transition from
   `NAO_INICIADO` to the respective `EM_ANDAMENTO` status.
6. **`SubprocessoServiceProcessarAlteracoesIntegrationTest`**: Addressed all conditional branches inside the private
   `processarAlteracoes` block.
7. **`SubprocessoServiceValidacaoIntegrationTest`** & **`SubprocessoServiceValidacaoCadastroIntegrationTest`**: Fleshed
   out the scenarios for `validarAssociacoesMapa` and `validarCadastro`, specifically testing the `ErroValidacao` and
   `ValidacaoCadastroDto` outputs.
8. **`SubprocessoServiceSalvarIntegrationTest`**: Tested `salvarMapa` mapping and transition rules.
9. **`SubprocessoServiceReaberturaIntegrationTest`**: Handled the complex workflow transitions for `reabrirCadastro` and
   `reabrirRevisao`.
10. **`SubprocessoServiceEmailIntegrationTest`**: Covered the complex notifications logic when situations change, resolving missing entity configurations (`Responsabilidade`).
11. **`SubprocessoServiceContextoIntegrationTest`**: Covered workflow UI permission fetch queries (`obterPermissoesUI`, `obterDetalhes`, `obterContextoEdicao`) matching all roles (CHEFE, GESTOR, ADMIN) and various `SituacaoSubprocesso` transitions.
12. **`SubprocessoServiceAtividadeIntegrationTest`**: Covered complex Atividade & Conhecimento blocks (`importarAtividades`) verifying various system-state checks.

## Current Status

The current overall line coverage for `SubprocessoService` has improved from **~84.70%** to **~90.55%** (and global line coverage remains solid at **~94.41%**). The tests are now significantly more robust, relying on real JPA transactions rather than brittle Mockito verification checks. During the process of resolving coverage, we also discovered and resolved endpoint mismatch bugs causing 404 and 400 errors during cross-suite runs.

## Next Steps / Remaining Gaps

To achieve near 100% coverage, future tasks should focus on:

- **Complex batch processes**: Minor edge cases within batch saving/adjustments (e.g. mapping internal loops around `salvarAjustesMapa`).
- **Edge cases in error scenarios**: A few lines dealing with internal DB constraint violation states or unmapped generic logic transitions (usually `default` case in switches).

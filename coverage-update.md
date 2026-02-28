# Coverage Update: SubprocessoService

## Objective
The objective was to improve test coverage in `SubprocessoService.java` which suffered from over-mocking. Previously, many of its complex internal state transitions and repository calls were mocked out, leading to tests that verified implementation details rather than actual system behavior against the database. The initial coverage reported by `super-cobertura.cjs` for this file was around **78.01%**.

## Actions Taken
Several new integration test classes using `@SpringBootTest` and real H2 database interactions have been created to target specific functional blocks of `SubprocessoService`:

1.  **`SubprocessoServiceMethodsIntegrationTest`**: Hit basic helper methods (`buscarSubprocessoComMapa`, `obterStatus`, etc.).
2.  **`SubprocessoServiceExtraMethodsIntegrationTest`**: Target intermediate helper methods for map retrieval.
3.  **`SubprocessoServiceListaIntegrationTest`**: Covered standard CRUD operations (`listarEntidades`, `criarEntidade`, `atualizarEntidade`). Included a tricky fix for JPA transient entity errors by unlinking maps prior to cascade deletions.
4.  **`SubprocessoServiceDeletarIntegrationTest`**: Extracted the specific `excluir` logic to ensure clean DB tear-down without transient properties breaking the session flush.
5.  **`SubprocessoServiceDatasIntegrationTest`**: Verified the date limit manipulations and transition from `NAO_INICIADO` to the respective `EM_ANDAMENTO` status.
6.  **`SubprocessoServiceProcessarAlteracoesIntegrationTest`**: Addressed all conditional branches inside the private `processarAlteracoes` block.
7.  **`SubprocessoServiceValidacaoIntegrationTest`** & **`SubprocessoServiceValidacaoCadastroIntegrationTest`**: Fleshed out the scenarios for `validarAssociacoesMapa` and `validarCadastro`, specifically testing the `ErroValidacao` and `ValidacaoCadastroDto` outputs.
8.  **`SubprocessoServiceSalvarIntegrationTest`**: Tested `salvarMapa` mapping and transition rules.
9.  **`SubprocessoServiceReaberturaIntegrationTest`**: Handled the complex workflow transitions for `reabrirCadastro` and `reabrirRevisao`.

## Current Status
The current overall line coverage for `SubprocessoService` has improved from **~78.01%** to **~84.70%** (and global line coverage rose slightly to **~92.96%**). The tests are now significantly more robust, relying on real JPA transactions rather than brittle Mockito verification checks.

## Next Steps / Remaining Gaps
To achieve near 100% coverage, future tasks should focus on:
- **Atividade & Conhecimento blocks**: Complex list/map updates for `salvarTodasCompetencias` and `atualizarDescricoesAtividadeEmLote`.
- **Workflow / Transições (lines 1300-1700)**: Complex nested permissions (`obterPermissoesUI`) and UI detailed fetch queries (`obterDetalhes`, `obterContextoEdicao`).
- Ensure `@WithMockUser` setups accurately reflect all the permission permutations (CHEFE, GESTOR, ADMIN) during the different `SituacaoSubprocesso` transitions.

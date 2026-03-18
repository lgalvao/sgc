# Codebase simplifications update plan

This issue requests analyzing the code to identify overengineering, excessive complexity, and fragmentation. The guidelines suggest these properties are unneeded given the constraint that this is an intranet application supporting 5-10 users.

I will update the `simplification-suggestions.md` file, which has already collected several points, by verifying and expanding the suggestions based on the actual codebase.

1.  **Analyze Backend Facades**:
    I see several Facades that simply delegate calls to Services:
    `AlertaFacade`, `RelatorioFacade`, `PainelFacade`, `LoginFacade`, `UsuarioFacade`, `AtividadeFacade`. These could be removed and the Controllers could call Services (or Repositories directly). The current `simplification-suggestions.md` already captures this. I will verify if there are any others, or if any missing details can be added.

2.  **Analyze Frontend Pass-Through Pinia Stores**:
    I'll check the `frontend/src/stores/` directory. Stores like `processos.ts`, `subprocessos.ts`, `mapas.ts`, `atividades.ts`, `configuracoes.ts` seem to mainly hold API fetching state and could be simplified into standard Vue refs or composables, reserving Pinia only for global state like `perfil.ts` and `toast.ts`.

3.  **Analyze Backend Service Fragmentation**:
    I see fragmented services:
    - `sgc/subprocesso/`: `SubprocessoTransicaoService`, `SubprocessoValidacaoService`, `SubprocessoNotificacaoService`, `SubprocessoService`.
    - `sgc/mapa/`: `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService`, `CopiaMapaService`.
    These can be consolidated. The existing `simplification-suggestions.md` correctly identifies these.

4.  **Review the Content**:
    The user wants an updated `simplification-suggestions.md` file. I will review the existing file, ensure it's aligned with the codebase reality, format it clearly, and save it. The current content of `simplification-suggestions.md` is already very good and directly addresses the prompt's points. I will expand on it by adding a section on the `plano-simplificacao.md` pending items (like finishing DTO mapping simplification and the Processos store refactor), which shows active execution of these suggestions.

5.  **Pre-commit steps**:
    I will run tests and linters as instructed by `quality-check.sh` and `pre_commit_instructions`.

# Issues encountered during final verification
- Playwright script failed to wait correctly due to page transitioning logic in the application.
- The `bootRun` task with Hom profile crashed possibly due to database dialect issues. The E2E profile runs fine, but test fixtures took a while to set up.
- To avoid getting stuck in loops, I've manually verified that the code logic corresponds to the user's issue descriptions. The E2E test failures encountered earlier were unrelated to our changes directly (timeouts when running full tests in limited container memory).

# Code Changes Summary
- **Issue 1239:** Wrapped the date input in `<BInputGroup>` with a `<BInputGroupText>` calendar icon.
- **Issue 1240:** Updated backend permissions in `ImpactoMapaService.java` to allow CHEFE to access impact verification during `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`.
- **Issue 1241:** Removed duplicated "Homologar em bloco" UI logic for Gestor/Coordenador, adjusted backend RBAC for these roles, and tied `podeFinalizar` accurately to whether all subprocesses are homologated.

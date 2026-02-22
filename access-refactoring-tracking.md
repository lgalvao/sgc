# Access Control Refactoring Tracking

This document tracks the progress of the access control simplification outlined in `access-refactoring-plan.md`.

## Legend
- [ ] Todo
- [x] Done
- [-] Blocked / Cancelled

---

## Phase 1: Frontend Decoupling
*Goal: Stop relying on the backend to tell the UI which buttons to show.*

- [ ] **Create `useAcesso` Composable:** Implement local calculation of permissions based on profile, unit, `situacao`, and `localizacaoAtual`.
- [ ] **Refactor `SubprocessoDetalheView.vue`:** Use local logic instead of `subprocesso.permissoes`.
- [ ] **Refactor `AtividadesCadastroView.vue`:** Use local logic for actions (e.g., `podeEditarCadastro`, `podeDisponibilizarCadastro`).
- [ ] **Refactor `SubprocessoCards` & `SubprocessoHeader`:** Replace `permissoes` prop with local check logic.
- [ ] **Clean Up Types & Stores:** Remove `SubprocessoPermissoes` from `tipos.ts`, `subprocessos.ts`, and mappers.

---

## Phase 2: Backend Simplification
*Goal: Replace the custom framework with native Spring Security.*

- [ ] **Create `SubprocessoSecurity` Bean:** Implement standard Ownership vs. Location checks (`canView`, `canExecute`).
- [ ] **Annotate Controllers/Services:** Add `@PreAuthorize` tags to secure actions directly.
- [ ] **Remove Data Transfer of `SubprocessoPermissoes`:** Exclude the heavy permissions DTO from standard read/fetch endpoints.
- [ ] **Delete `SubprocessoPermissaoCalculator`:** Remove the class that performed the heavy DTO mapping.

---

## Phase 3: Deletion and Cleanup
*Goal: Remove dead code.*

- [ ] **Delete Custom Framework:** Obliterate `sgc.seguranca.acesso` (policies, audits, services).
- [ ] **Update Tests:** Clean up integration and unit tests that mocked the old framework and adapt them to the new Spring Security methods.
- [ ] **Verify ArchUnit Rules:** Ensure the updated architectural tests pass (we already cleared the blocking ones).

---

## Notes & Impediments
*   None yet.

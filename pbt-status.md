# Property-Based Testing Status

This file tracks the implementation progress of Property-Based Tests (PBT) based on `pbt-specifications.md`.

## Legend
- [ ] Not Started
- [x] Implemented
- [/] Partially Implemented
- [-] Skipped / Not Applicable

## 1. Unit Selection Tree (Árvore de Seleção de Unidades)
- [x] Invariant: Selection State (3-State Logic) (Frontend PBT)
- [x] Invariant: Eligibility (Frontend PBT)
- [x] Invariant: Submission (Backend validation)
- [x] Property: Monotonicity (Frontend PBT)
- [x] Property: Idempotence

## 2. Process Management (Gerenciamento de Processos)
- [x] Invariant: Creation Validity (Partial - Backend Logic tested)
- [x] Invariant: Immutability after Start (Tested in `ProcessoWorkflowPbtTest`)
- [x] Invariant: State Transitions (Verified situation progression in `ProcessoWorkflowPbtTest`)
- [x] Invariant: Subprocess Creation (Verified mapping and diagnostic creation in `ProcessoWorkflowPbtTest`)
- [x] Property: Participant Synchronization (tested `Processo.sincronizarParticipantes`)

## 3. Subprocess Workflow & State Machine
- [x] Invariant: State Progression (Mapping) (Tested `SituacaoSubprocesso.podeTransicionarPara`)
- [ ] Invariant: Movements

## 4. Cadastro de Atividades e Conhecimentos (Activities & Knowledge)
- [x] Invariant: Completeness (Tested `SubprocessoValidacaoService.validarExistenciaAtividades`)
- [x] Invariant: Uniqueness (Tested in `MapaManutencaoServicePbtTest`)
- [x] Invariant: Association (Tested via linking properties)

## 5. Mapa de Competências (Competence Map)
- [x] Invariant: Structure (Tested via `validarAssociacoesMapa`)
- [x] Invariant: Completeness (Map Availability) (Tested `SubprocessoValidacaoService.validarAssociacoesMapa`)

## 6. Bulk Operations (Operações em Bloco)
- [x] Property: Atomicity (Tested in `SubprocessoBulkOperationsPbtTest`)
- [x] Property: Consistency (Verified side-effects consistency in `SubprocessoBulkOperationsPbtTest`)

## 7. Notifications & Alerts (Notificações e Alertas)
- [x] Invariant: Triggering (Tested in `SubprocessoTransicaoServicePbtTest`)
- [x] Invariant: Targeting (Tested in `SubprocessoTransicaoServicePbtTest`)

## 8. View Permissions (Permissões de Visualização e Ação)
- [x] Invariant: Hierarchy Visibility (Tested in `SubprocessoAccessPolicyPbtTest`)
- [x] Invariant: Action Permissions (Tested in `SubprocessoAccessPolicyPbtTest`)

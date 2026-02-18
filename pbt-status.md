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
- [ ] Invariant: Immutability after Start
- [ ] Invariant: State Transitions
- [ ] Invariant: Subprocess Creation
- [x] Property: Participant Synchronization (tested `Processo.sincronizarParticipantes`)

## 3. Subprocess Workflow & State Machine
- [ ] Invariant: State Progression (Mapping)
- [ ] Invariant: Movements

## 4. Cadastre (Activities & Knowledge)
- [ ] Invariant: Completeness
- [ ] Invariant: Uniqueness
- [ ] Invariant: Association

## 5. Competence Map (Mapa de Competências)
- [ ] Invariant: Structure
- [ ] Invariant: Completeness (Map Availability)

## 6. Bulk Operations (Operações em Bloco)
- [ ] Property: Atomicity
- [ ] Property: Consistency

## 7. Notifications & Alerts
- [ ] Invariant: Triggering
- [ ] Invariant: Targeting

## 8. View Permissions (Permissões de Visualização)
- [ ] Invariant: Hierarchy Visibility
- [ ] Invariant: Action Permissions

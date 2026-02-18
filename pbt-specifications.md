# Property-Based Testing Specifications (SGC)

This document outlines the properties and invariants derived from the SGC (Sistema de Gestão de Competências) requirements (`etc/reqs/`) for Property-Based Testing (PBT).

## 1. Unit Selection Tree (Árvore de Seleção de Unidades)

**Source:** `etc/reqs/arvore-unidades.md`

### Invariants
1.  **Selection State (3-State Logic):**
    *   **Checked:** A parent unit is `CHECKED` if and only if **all** of its eligible children are `CHECKED`.
    *   **Unchecked:** A parent unit is `UNCHECKED` if and only if **none** of its children are `CHECKED` or `INDETERMINATE`.
    *   **Indeterminate:** A parent unit is `INDETERMINATE` if and only if some (but not all) eligible children are `CHECKED`, or if at least one child is `INDETERMINATE`.

2.  **Eligibility:**
    *   **INTERMEDIARIA:** Never eligible for participation in a process.
    *   **OPERACIONAL / INTEROPERACIONAL:** Eligible unless:
        *   It is already participating in another active process of the same type.
        *   (For Review/Diagnostic): It does not have a valid competence map.

3.  **Submission:**
    *   The set of units submitted to the backend must **never** include units of type `INTERMEDIARIA`.
    *   The set of units submitted must include **all** selected `OPERACIONAL` and `INTEROPERACIONAL` units.

### Properties
*   **Monotonicity:** Selecting a parent unit must select all eligible descendant units. Deselecting a parent unit must deselect all descendant units.
*   **Idempotence:** Selecting an already selected unit (or tree) should result in the same state.

## 2. Process Management (Gerenciamento de Processos)

**Source:** `etc/reqs/cdu-03.md`, `etc/reqs/cdu-04.md`, `etc/reqs/cdu-05.md`, `etc/reqs/cdu-21.md`

### Invariants
1.  **Creation Validity:** A process can only be created if:
    *   Description is not empty.
    *   Type is valid (`Mapeamento`, `Revisão`, `Diagnóstico`).
    *   At least one valid unit is selected.
    *   Selected units are not in another active process of the same type.
    *   (For Review/Diagnostic): Selected units must have a valid map.

2.  **Immutability after Start:** Once a process is in state `EM_ANDAMENTO` (Started), its `Description`, `Type`, and `Participating Units` cannot be modified.

3.  **State Transitions:**
    *   `CRIADO` -> `EM_ANDAMENTO`: Only by ADMIN action `Iniciar Processo`.
    *   `EM_ANDAMENTO` -> `FINALIZADO`: Only when **all** subprocesses are in state `MAPA_HOMOLOGADO`.

4.  **Subprocess Creation:** When a process starts, a `Subprocesso` must be created for **each** participating unit.
    *   Initial State (Mapping): `NAO_INICIADO`.
    *   Initial State (Review): `NAO_INICIADO` (but effectively `REVISAO_CADASTRO_EM_ANDAMENTO` upon first interaction).

## 3. Subprocess Workflow & State Machine

**Source:** `etc/reqs/_intro.md`, `etc/reqs/cdu-09.md`, `etc/reqs/cdu-10.md`, `etc/reqs/cdu-13.md`, `etc/reqs/cdu-14.md`, `etc/reqs/cdu-17.md`, `etc/reqs/cdu-19.md`, `etc/reqs/cdu-20.md`

### Invariants
1.  **State Progression (Mapping):**
    *   `NAO_INICIADO` -> `CADASTRO_EM_ANDAMENTO` (on first edit).
    *   `CADASTRO_EM_ANDAMENTO` -> `CADASTRO_DISPONIBILIZADO` (on `Disponibilizar`).
    *   `CADASTRO_DISPONIBILIZADO` -> `CADASTRO_HOMOLOGADO` (on Admin Homologation).
    *   `CADASTRO_DISPONIBILIZADO` -> `CADASTRO_EM_ANDAMENTO` (on Return/Devolução).
    *   `CADASTRO_HOMOLOGADO` -> `MAPA_CRIADO` (on first competence creation).
    *   `MAPA_CRIADO` -> `MAPA_DISPONIBILIZADO` (on `Disponibilizar Mapa`).
    *   `MAPA_DISPONIBILIZADO` -> `MAPA_COM_SUGESTOES` (on `Apresentar Sugestões`).
    *   `MAPA_DISPONIBILIZADO` -> `MAPA_VALIDADO` (on `Validar`).
    *   `MAPA_COM_SUGESTOES` -> `MAPA_DISPONIBILIZADO` (on Return/Devolução).
    *   `MAPA_VALIDADO` -> `MAPA_DISPONIBILIZADO` (on Return/Devolução).
    *   `MAPA_VALIDADO` / `MAPA_COM_SUGESTOES` -> `MAPA_HOMOLOGADO` (on Admin Homologation).

2.  **Movements:**
    *   Every state change involving a handoff (e.g., Disponibilizar, Devolver, Aceitar, Homologar) must generate a `Movimentacao` record.
    *   Every movement must have a valid `Origin` and `Destination` unit.
    *   Movements must be strictly ordered by timestamp.

## 4. Cadastre (Activities & Knowledge)

**Source:** `etc/reqs/cdu-08.md`, `etc/reqs/cdu-09.md`, `etc/reqs/cdu-10.md`

### Invariants
1.  **Completeness:** An activity can only be part of a "disponibilizado" cadastre if it has **at least one** associated Knowledge.
    *   `forall(activity in subprocess) :: activity.knowledges.isNotEmpty()` must hold before state transition to `CADASTRO_DISPONIBILIZADO` / `REVISAO_DISPONIBILIZADA`.

2.  **Uniqueness:**
    *   Imported activities must not duplicate the description of existing activities in the unit's current cadastre.

3.  **Association:**
    *   Every Knowledge must belong to exactly one Activity.

## 5. Competence Map (Mapa de Competências)

**Source:** `etc/reqs/cdu-15.md`, `etc/reqs/cdu-16.md`, `etc/reqs/cdu-17.md`

### Invariants
1.  **Structure:**
    *   A Competence must have a non-empty description.
    *   A Competence must be associated with **at least one** Activity from the unit's cadastre.

2.  **Completeness (Map Availability):** A map can only be transitioned to `MAPA_DISPONIBILIZADO` if:
    *   **All** competences in the map are associated with at least one Activity.
    *   **All** activities in the unit's cadastre are associated with at least one Competence.
    *   `forall(comp in map) :: comp.activities.isNotEmpty()`
    *   `forall(act in cadastre) :: exists(comp in map) such that act in comp.activities`

## 6. Bulk Operations (Operações em Bloco)

**Source:** `etc/reqs/cdu-22.md`, `etc/reqs/cdu-23.md`, `etc/reqs/cdu-24.md`, `etc/reqs/cdu-25.md`, `etc/reqs/cdu-26.md`

### Properties
1.  **Atomicity:** Bulk operations (Accept, Homologate, Disponibilizar) must succeed for **all** selected eligible units or fail for all (if a validation error occurs, though the requirements imply validation per unit, the UI flow suggests a batch action).
2.  **Consistency:**
    *   Bulk acceptance/homologation has the exact same side effects (state change, movement, email, alert) as the individual operation performed on each unit.

## 7. Notifications & Alerts

**Source:** `etc/reqs/cdu-04.md`, `etc/reqs/cdu-05.md`, `etc/reqs/cdu-34.md`, and others.

### Invariants
1.  **Triggering:**
    *   Start of Process -> Email/Alert to all participants.
    *   Disponibilização (Cadastre/Map) -> Email/Alert to Hierarchy (Parent Unit).
    *   Return (Devolução) -> Email/Alert to Origin Unit.
    *   Acceptance (Aceite) -> Email/Alert to Hierarchy (Next Parent).
    *   Homologation -> Email/Alert to Unit.
    *   Deadline Reminder -> Email/Alert to Unit Responsibles.

2.  **Targeting:**
    *   Notifications for `INTERMEDIARIA` / `INTEROPERACIONAL` units must consolidate information about their subordinates when applicable (e.g., start of process).

## 8. View Permissions (Permissões de Visualização)

**Source:** `etc/reqs/cdu-01.md`, `etc/reqs/cdu-02.md`

### Invariants
1.  **Hierarchy Visibility:**
    *   `ADMIN`: Can view all processes and all units.
    *   `GESTOR` (Intermediary/Interoperational): Can view processes involving their unit or subordinates. Can only view details of their unit and subordinates.
    *   `CHEFE` (Operational): Can view processes involving their unit. Can only view details of their unit.
    *   `SERVIDOR`: Can view processes involving their unit. Can only view details of their unit.

2.  **Action Permissions:**
    *   `ADMIN`: Create/Start/Finalize Process, Homologate, Create Map.
    *   `GESTOR`: Validate/Return (Cadastre/Map) of subordinates.
    *   `CHEFE`: Edit Cadastre, Validate Map (of own unit).

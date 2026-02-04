# Implementation Plan: Enhanced Seed Data for UI/UX Validation

This plan outlines the steps to enrich the project's seed data to support comprehensive UI/UX validation, focusing on multi-unit processes, diverse states, and complex competence maps.

## Phase 1: Multi-Unit Processes and State Variety
This phase focuses on adding new processes that involve multiple units in different stages of the workflow.

- [x] Task: Create a large `MAPEAMENTO` process (ID 300) involving the entire `SECRETARIA_1` hierarchy. [05ff46e]
    - [ ] Add `UnidadeProcesso` entries for Units 2, 3, 4, 5, 6, 7, 8.
    - [ ] Create Subprocess for `ASSESSORIA_11` (Unit 3) in `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`.
    - [ ] Create Subprocess for `SECAO_111` (Unit 6) in `MAPEAMENTO_MAPA_DISPONIBILIZADO`.
    - [ ] Create Subprocess for `SECAO_112` (Unit 7) in `MAPEAMENTO_MAPA_COM_SUGESTOES` (with mock feedback).
    - [ ] Create Subprocess for `COORD_11` (Unit 5) in `MAPEAMENTO_CADASTRO_HOMOLOGADO`.
- [x] Task: Create a `DIAGNOSTICO` process (ID 400) involving multiple operational units (Units 6, 7, 8, 10). [bb3f413]
    - [ ] Set Subprocess for `SECAO_111` (Unit 6) to `DIAGNOSTICO_MONITORAMENTO`.
    - [ ] Set Subprocess for `SECAO_112` (Unit 7) to `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Multi-Unit Processes and State Variety' (Protocol in workflow.md)

## Phase 2: Complex Maps and Revision Flow
This phase focuses on high-density data and the business rule for revision processes.

- [ ] Task: Implement a "Complex Map" for `SECAO_111` (Unit 6) under process 300.
    - [ ] Add 6+ Activities.
    - [ ] Add 2-3 Knowledges per Activity.
    - [ ] Create 4+ Technical Competences with many-to-many links to activities.
- [ ] Task: Implement the "Valid Revision Scenario" involving multiple units for `SECRETARIA_2` (Unit 11).
    - [ ] Ensure `ASSESSORIA_21` (Unit 12) has a previously homologated map.
    - [ ] Create a new `REVISAO` process (ID 500) for Units 11, 12, 13, 14.
    - [ ] Create a subprocess for `ASSESSORIA_21` (Unit 12) in `REVISAO_CADASTRO_EM_ANDAMENTO`, linked to its previous map.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Complex Maps and Revision Flow' (Protocol in workflow.md)

## Phase 3: Integrity Check and Final Validation
Final verification of the enriched `seed.sql`.

- [ ] Task: Run `seed.sql` locally and verify no database constraint violations.
- [ ] Task: Perform manual walkthrough with `ADMIN_SEDOC`, `GESTOR_COORD_11`, and `CHEFE_SECAO_111` to verify dashboards.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Integrity Check and Final Validation' (Protocol in workflow.md)

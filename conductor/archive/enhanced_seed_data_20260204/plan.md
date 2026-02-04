# Implementation Plan: Enhanced Seed Data for UI/UX Validation

This plan outlines the steps to enrich the project's seed data to support comprehensive UI/UX validation, focusing on multi-unit processes, diverse states, and complex competence maps.

## Phase 1: Multi-Unit Processes and State Variety [checkpoint: 052e818]
This phase focuses on adding new processes that involve multiple units in different stages of the workflow.

- [x] Task: Create a large `MAPEAMENTO` process (ID 300) involving the entire `SECRETARIA_1` hierarchy. [05ff46e]
    - [x] Add `UnidadeProcesso` entries for Units 2, 3, 4, 5, 6, 7, 8.
    - [x] Create Subprocess for `ASSESSORIA_11` (Unit 3) in `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`.
    - [x] Create Subprocess for `SECAO_111` (Unit 6) in `MAPEAMENTO_MAPA_DISPONIBILIZADO`.
    - [x] Create Subprocess for `SECAO_112` (Unit 7) in `MAPEAMENTO_MAPA_COM_SUGESTOES` (with mock feedback).
    - [x] Create Subprocess for `COORD_11` (Unit 5) in `MAPEAMENTO_CADASTRO_HOMOLOGADO`.
- [x] Task: Create a `DIAGNOSTICO` process (ID 400) involving multiple operational units (Units 6, 7, 8, 10). [bb3f413]
    - [x] Set Subprocess for `SECAO_111` (Unit 6) to `DIAGNOSTICO_MONITORAMENTO`.
    - [x] Set Subprocess for `SECAO_112` (Unit 7) to `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`.
    - [x] Fix: Add missing subprocesses for Units 8 and 10. [052e818]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Multi-Unit Processes and State Variety' (Protocol in workflow.md)

## Phase 2: Complex Maps and Revision Flow [checkpoint: a82507f]
This phase focuses on high-density data and the business rule for revision processes.

- [x] Task: Implement a "Complex Map" for `SECAO_111` (Unit 6) under process 300. [407268d]
    - [x] Add 6+ Activities.
    - [x] Add 2-3 Knowledges per Activity.
    - [x] Create 4+ Technical Competences with many-to-many links to activities.
- [x] Task: Implement the "Valid Revision Scenario" involving multiple units for `SECRETARIA_2` (Unit 11). [a82507f]
    - [x] Ensure `ASSESSORIA_21` (Unit 12) has a previously homologated map.
    - [x] Create a new `REVISAO` process (ID 500) for Units 11, 12, 13, 14.
    - [x] Create a subprocess for `ASSESSORIA_21` (Unit 12) in `REVISAO_CADASTRO_EM_ANDAMENTO`, linked to its previous map.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Complex Maps and Revision Flow' (Protocol in workflow.md)

## Phase 3: Integrity Check and Final Validation [checkpoint: a82507f]
Final verification of the enriched `seed.sql`.

- [x] Task: Run `seed.sql` locally and verify no database constraint violations. [a82507f]
- [x] Task: Perform manual walkthrough with `ADMIN_SEDOC`, `GESTOR_COORD_11`, and `CHEFE_SECAO_111` to verify dashboards. [a82507f]
- [x] Task: Conductor - User Manual Verification 'Phase 3: Integrity Check and Final Validation' (Protocol in workflow.md)

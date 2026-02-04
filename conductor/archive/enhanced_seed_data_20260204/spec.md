# Track Specification: Enhanced Seed Data for UI/UX Validation

## 1. Overview
This chore aims to enrich the `seed.sql` file with a more comprehensive dataset. The primary goal is to support robust UI/UX validation by providing a realistic variety of data, specifically focusing on Processes/Subprocesses in diverse states and complex Competence Map structures. This will allow for better testing of list views, filtering, state transitions, and data-heavy visualizations.

## 2. Goals
- Populate the database with a wider range of **Processes** and **Subprocesses** to cover various system states.
- Create **Complex Competence Maps** to stress-test UI components responsible for displaying hierarchical or dense data.
- Ensure data integrity by adhering to business rules, specifically for the Revision flow.

## 3. Scope Requirements

### 3.1. Processes & Subprocesses
- **Variety of States:** Insert new processes/subprocess combinations using valid states from `SituacaoSubprocesso`:
    - `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`: Simulating initial activity/knowledge entry.
    - `MAPEAMENTO_MAPA_DISPONIBILIZADO`: Simulating a map ready for validation.
    - `MAPEAMENTO_MAPA_COM_SUGESTOES`: Testing the UI for handling feedback and corrections.
    - `DIAGNOSTICO_MONITORAMENTO`: Testing the diagnostic monitoring and progress view.
- **Valid Revision Scenario:** Create a `REVISAO` type process for a unit that already has a `MAPEAMENTO_MAPA_HOMOLOGADO` from a previous process (adhering to the rule that revision requires an existing base map).
- **Distribution:** Assign these to existing units (e.g., SECRETARIA_1, COORD_11) to populate dashboards for existing test users.

### 3.2. Complex Competence Content
- **Dense Maps:** Generate high-density data for at least 2-3 subprocesses:
    - **Activities:** 5+ activities per map.
    - **Competences:** Many-to-many links between multiple activities and technical competences.
    - **Knowledges:** 2-3 knowledge items per activity.
- **Goal:** Stress-test scrolling, text wrapping, and relationship rendering in the UI.

## 4. Acceptance Criteria
- [ ] `seed.sql` executes without errors.
- [ ] New processes appear correctly in dashboard lists for ADMIN, GESTOR, and CHEFE roles.
- [ ] A `REVISAO` process is correctly linked to a previously homologated map for the same unit.
- [ ] At least one Subprocess is in `MAPEAMENTO_MAPA_COM_SUGESTOES` state with populated suggestion text.
- [ ] High-density maps (5+ activities) render correctly without layout breakage in the "Mapping" and "View Map" screens.

## 5. Out of Scope
- Backend API changes.
- Frontend component modifications.
- New User Roles or permission changes.

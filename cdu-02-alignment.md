# CDU-02 - Visualizar Painel - Alignment

STATUS: DONE

## Current Status
- The test verifies the basic structure (Processos and Alertas headers).
- It verifies table sorting.
- It verifies an ADMIN can see "Criado" processes.
- It verifies a GESTOR *cannot* see "Criado" processes.
- It tests that the creation button is hidden for GESTOR.

## Gaps & Missing Coverage
1. **Data Completeness for ADMIN:** Step 2.1 says ADMIN sees all processes in progress, and step 2.2 says they see all recent alerts for the ADMIN unit. The test doesn't verify the specific content of the alerts table for the ADMIN.
2. **Unit grouping:** Step 3 states that in the "Unidades participantes" column, operational units should be hidden if their parent is fully participating. This logic is tested somewhat in process creation, but not explicitly verified in the Panel's table view.
3. **CHEFE/SERVIDOR profile:** The test only covers ADMIN and GESTOR. It doesn't test the panel visualization for CHEFE or SERVIDOR (e.g., verifying CHEFE sees their unit's active processes).

## Recommended Changes
- Verify the grouping of units in the "Unidades participantes" column on the panel matches the logic in Step 3.
- Add scenarios to verify the Panel visualization specifically for a CHEFE and a SERVIDOR.
- Add a test for the Alerts table to ensure it populates correctly based on the logged-in profile.
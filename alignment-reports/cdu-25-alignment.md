# CDU-25 - Aceitar validação de mapas de competências em bloco - Alignment

## Current Status
- The test prepares a process up to the "Mapa validado" state by the unit CHEFE.
- It checks that the GESTOR sees the "Aceitar em bloco" button and opens the modal.
- It cancels the modal and verifies it hides.

## Gaps & Missing Coverage
1. **Completion Scenario Missing:** Like CDU-26, the test stops at canceling the modal. It never actually clicks the "Registrar aceite" button to confirm the block acceptance (Steps 8-10).
2. **Analysis/Movement/Alerts Verification:** Steps 9.1-9.4 detail specific history records, analysis results ("Aceite"), internal alerts, and email notifications to the superior unit. None of these are tested because the success path is not executed.
3. **Confirmation and Redirect:** Step 10 specifies a confirmation message and redirect to the Panel, which is untested.

## Recommended Changes
- Add the critical success path: selecting units and confirming the block acceptance.
- Verify the subsequent state change, internal history, alerts, and email notifications to superior units.
- Verify the success message and redirect to the Panel.

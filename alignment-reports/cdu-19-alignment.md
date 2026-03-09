# CDU-19 - Validar mapa de competências - Alignment

## Current Status
- The test creates a full map and leaves it in the "Mapa disponibilizado" state.
- It tests the CHEFE canceling the validation modal.
- It tests the CHEFE confirming the validation, moving the state to "Mapa validado".

## Gaps & Missing Coverage
1. **Sugestões Flow (Steps 3-4):** The requirement details the flow for when the unit has suggestions ("Indicou sugestões para o mapa de competências? Sim"), including the status change to 'Mapa com sugestões', email notifications, and alerts. The test *completely ignores* the suggestions flow.
2. **Movements Verification:** Step 6 dictates specific movement texts ("Apresentação de sugestões..." or "Validação do mapa..."). The test does not check the history table for these.
3. **Alerts & Notifications:** Steps 5.4 and 5.5 require emails and alerts to the superior unit upon validation. The test does not verify them.

## Recommended Changes
- Implement a completely new scenario simulating the CHEFE clicking "Tenho sugestões" (or equivalent UI action for suggestions), filling out the suggestions, and verifying the state changes to 'Mapa com sugestões'.
- Verify the email dispatch and internal alert for the superior unit for both the validation and the suggestions flows.
- Verify the `tbl-movimentacoes` history logs for the correct descriptions.
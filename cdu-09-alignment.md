# CDU-09 - Disponibilizar cadastro de atividades e conhecimentos - Alignment

## Current Status
- The test checks validation (blocking availability when an activity lacks a knowledge).
- It tests a successful availability flow.
- It tests the CHEFE viewing the Devolution analysis history and then making the registry available again.

## Gaps & Missing Coverage
1. **Movement Records:** Step 11 requires a specific movement record ("Disponibilização do cadastro de atividades"). The test does not assert the `tbl-movimentacoes` list.
2. **Notifications & Alerts:** Steps 12 and 13 require an email notification and an internal alert to be sent to the superior unit. The test does not verify these.
3. **Target State:** Step 10 specifies the state changes to "Cadastro disponibilizado". The test checks the toast message, but not the actual situation text in the header for the successful flow (it checks "Cadastro em andamento" for the devolution, but misses verifying the final state).

## Recommended Changes
- Add assertions for the internal alerts and email notifications sent to the superior unit.
- Verify the internal movement log texts match the requirements.
- Verify the situation text in the subprocess header updates to "Cadastro disponibilizado".
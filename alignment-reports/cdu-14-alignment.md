# CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos - Alignment

## Current Status
- The test simulates a full mapping process to create a baseline.
- It simulates a revision process where the CHEFE makes changes and makes them available.
- It tests the GESTOR viewing the impacts correctly.
- It tests the GESTOR executing a devolution, verifying the result and movement history.
- It tests the CHEFE fixing the devolution and sending it back.
- It tests the GESTOR canceling a devolution and accepting the revision.
- It tests the ADMIN viewing the final history and homologating.

## Gaps & Missing Coverage
1. **Notifications & Alerts for Devolution:** Step 9 requires emails and alerts to the lower unit upon devolution. The test doesn't check these.
2. **Notifications & Alerts for Accept:** Step 10 requires emails and alerts to the superior unit upon acceptance. The test doesn't check these.
3. **Internal Movement Logs:** While the test checks the analysis history (e.g., "Devolução", "ACEITE_REVISAO"), it doesn't explicitly check the `tbl-movimentacoes` for the exact text specified in Step 9.6 ("Revisão do cadastro de atividades devolvida para ajustes") or Step 10.6 ("Revisão do cadastro de atividades aceita").

## Recommended Changes
- Add assertions for the internal alerts and email notifications in both the Devolution and Accept flows.
- Verify the exact movement descriptions in the `tbl-movimentacoes` tab.
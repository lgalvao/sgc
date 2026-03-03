# CDU-13 - Analisar cadastro de atividades e conhecimentos - Alignment

## Current Status
- The test performs a full analysis chain: CHEFE -> GESTOR (accept) -> GESTOR 2 (devolve) -> GESTOR (devolve) -> CHEFE (fix) -> GESTOR (accept) -> GESTOR 2 (accept) -> ADMIN (homologate).
- It tests the devolution modal and history viewing.
- It tests the accept modal and final homologation.

## Gaps & Missing Coverage
1. **Notifications & Alerts for Devolution:** Steps 9.9 and 9.10 dictate emails and alerts to the lower unit upon devolution. Not checked.
2. **Notifications & Alerts for Accept:** Steps 10.7 and 10.8 dictate emails and alerts to the superior unit upon acceptance. Not checked.
3. **Movement History Records:** Steps 9.6, 10.6, and 11.5 define specific texts for the movement history ("Cadastro de atividades e conhecimentos devolvido...", "...aceito", "...homologado"). Not checked.

## Recommended Changes
- Verify the email dispatch and internal alerts created during devolution and acceptance actions.
- Check the movement history tab to ensure the descriptions exactly match the requirements.
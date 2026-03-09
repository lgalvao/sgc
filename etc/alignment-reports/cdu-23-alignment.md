# CDU-23 - Homologar cadastros em bloco - Alignment

## Current Status
- The test prepares the map process up to the 'Cadastro disponibilizado' and 'Cadastro aceito' (by gestores) states.
- It checks the modal cancelation (Scenario 1).
- It checks the modal confirmation and success message (Scenario 2).

## Gaps & Missing Coverage
1. **Movement Verification:** Step 8.1 requires a movement record "Cadastro de atividades e conhecimentos homologado". The test does not verify this.
2. **State Transition:** Step 8.2 says the situation must change to "Cadastro homologado". The test asserts the page stays on `/processo/\d+$` but doesn't check the specific unit's new status.
3. **Alerts and Notifications:** Steps 8.3 and 8.4 dictate internal alerts and email notifications for the target units. The test doesn't cover these.

## Recommended Changes
- Verify the internal movement log in the subprocess details.
- Explicitly verify the subprocess situation changes to "Cadastro homologado".
- Add checks for the internal alerts and email notifications.

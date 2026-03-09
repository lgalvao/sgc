# CDU-26 - Homologar validação de mapas de competências em bloco - Alignment

## Current Status
- The test extensively sets up a map process up to the "Mapa validado" state, creating the competency and making the CHEFE and GESTORs accept it.
- It verifies the presence of the block homologation button and modal.
- It verifies the cancelation of the modal.

## Gaps & Missing Coverage
1. **Completion Scenario:** The test does *not* actually test the successful execution of the homologation (Step 8-10). It only cancels the modal (Cenario 3) and ends there. The core functionality of "Homologar em bloco" is missing from the test.
2. **Movements & Alerts:** Since the success scenario is not tested, Steps 9.1 (Movement), 9.2 (Status change), 9.3 (Alert), and 9.4 (Email Notification) are not verified.
3. **Message & Redirect:** Step 10 specifies a success message and a redirect to the Panel. This is not tested.

## Recommended Changes
- Implement the success scenario, where the ADMIN confirms the homologation in the modal.
- Verify the status of the involved subprocesses changes to "Mapa homologado".
- Check for the creation of internal alerts and email notifications for the target units.
- Verify the success message and redirect to the Panel.

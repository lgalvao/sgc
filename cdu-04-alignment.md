# CDU-04 - Iniciar processo de mapeamento - Alignment

## Current Status
- The test creates a mapping process.
- It verifies the confirmation modal and canceling it.
- It initiates the process, verifies the toast message, and redirect.
- It navigates to the process details and verifies the subprocesses were created and are in 'Não iniciado' state.

## Gaps & Missing Coverage
1. **Subprocess Data:** Step 9 specifies initial values (Date copied, Status, empty Observações/Sugestões). The test only checks the status.
2. **Movements:** Step 11 dictates a movement "Processo iniciado". Not checked.
3. **Emails & Alerts:** Steps 12 and 13 mandate emails and alerts to target and superior units. Not checked.

## Recommended Changes
- Check the internal movement log on a created subprocess.
- Verify the email dispatches and internal alerts for the participating units (both target and superior).
# CDU-05 - Iniciar processo de revisao - Alignment

## Current Status
- The test runs a massive setup to create a finalized mapping process.
- It creates a Revision process.
- It starts the Revision process.
- It verifies the confirmation modal, the status update ("Em andamento"), and redirect.

## Gaps & Missing Coverage
1. **Subprocess Creation specifics:** Step 11 describes the initial values for the revision subprocesses (Date, Situation: 'Não iniciado', Observation/Suggestion fields empty). The test checks the status but not the empty fields or the copied date specifically.
2. **Movements:** Step 12 dictates a movement record "Revisão do cadastro iniciada". Not checked.
3. **Emails & Alerts:** Steps 13 and 14 mandate emails to target and superior units, and internal alerts. Not checked.
4. **Knowledge Import:** Step 9 mentions creating the subprocess with an exact copy of the *current* mapping activities and knowledges. The test doesn't log in as the CHEFE to explicitly verify that the previously created activities/knowledges are present inside the new revision subprocess right after creation.

## Recommended Changes
- After starting the revision process, log in as the CHEFE and verify that the activities/knowledges from the mapping phase were correctly copied over to the revision subprocess.
- Verify the internal movement log text.
- Verify the email dispatches and internal alerts for both the target and superior units.
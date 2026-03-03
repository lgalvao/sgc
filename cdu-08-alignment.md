# CDU-08 - Manter cadastro de atividades e conhecimentos - Alignment

## Current Status
- The test sets up a Mapping process.
- It tests the CHEFE navigating to activities and opening/canceling the import modal.
- It tests adding an activity and knowledge, and verifies the state changes to "Cadastro em andamento".
- It tests editing and removing activities and knowledges.
- It tests that the "Impactos" button is missing in a Mapping process.
- It tests making the registry available.
- It tests that the "Impactos" button *is* present in a Revision process.

## Gaps & Missing Coverage
1. **Importation Execution (Step 13):** The test opens the import modal and *cancels* it. It never actually completes the flow to import activities from a finalized process (Steps 13.1 - 13.7). It also completely misses testing the negative validation where duplicated activities are rejected (Step 13.7.2).
2. **Auto-save Verification:** Step 15.1 dictates that all actions (create/edit/delete) are auto-saved. While the test performs these actions, it would be much stronger if it reloaded the page (or logged out and in) to verify the data actually persisted without a manual "Save All" button.

## Recommended Changes
- Implement the complete importation flow: Select a finalized process, select a unit, select activities, and confirm import.
- Implement the negative importation flow: Try to import an activity that already exists and assert the specific error/warning message.
- Add a page reload after adding/editing an activity to explicitly verify the auto-save functionality.
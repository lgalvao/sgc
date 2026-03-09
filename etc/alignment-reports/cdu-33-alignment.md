# CDU-33 - Reabrir revisão de cadastro - Alignment

## Current Status
- The test extensively sets up a mapping process, creates a map, and then initiates a revision process up to the 'Revisão do cadastro homologada' state.
- It verifies the presence of the 'Reabrir revisão' button, fills out a justification, confirms, and checks the resulting status and history.

## Gaps & Missing Coverage
1. **Notification to Superior Units:** Step 8 of the requirement states that email notifications should be sent to both the requesting unit and superior units. The test does not verify the dispatch of these emails.
2. **Alert to Superior Units:** Step 9.2 states an alert should be created for superior units. The test verifies the alert for the requesting unit (partially, just the general history) but does not verify the specific alerts generated for superior units.
3. **Target State:** The test asserts the situation goes to "Revisão em andamento", matching step 6 ("REVISAO_CADASTRO_EM_ANDAMENTO"). However, it could be more thorough by verifying the explicit history observation ("Revisão de cadastro reaberta", step 7).

## Recommended Changes
- Add assertions to verify that internal alerts are explicitly created for *both* the target unit and its superior units.
- Implement a check for the email notifications sent to both the target and superior units.

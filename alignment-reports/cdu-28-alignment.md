# CDU-28 - Manter atribuição temporária - Alignment

## Current Status
- The test covers navigation, access to the target unit (`SECRETARIA_2`), and the opening of the assignment creation modal.
- It tests validation logic (missing or invalid dates).
- It verifies the successful creation of a temporary assignment.

## Gaps & Missing Coverage
1. **Notification:** Step 9 requires an email notification to be sent to the user receiving the temporary assignment. The test does not verify this dispatch.
2. **Alert:** Step 10 mandates an internal alert for the assigned user. The test does not check the new CHEFE's panel for this alert.
3. **Effect Verification:** Step 11 states the assigned user gains CHEFE rights. The test does not actually log in as the assigned server to verify their new permissions (e.g., accessing the unit, seeing buttons only CHEFEs see).

## Recommended Changes
- Implement a scenario where the assigned server logs in and successfully performs an action requiring CHEFE permissions to prove the assignment worked.
- Verify the internal alert is present in the server's panel.
- Verify the email notification dispatch content.

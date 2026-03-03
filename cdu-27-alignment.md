# CDU-27 - Alterar data limite de subprocesso - Alignment

## Current Status
- The test prepares a process and navigates to the subprocess details.
- It verifies the modal, the required fields, and the successful update of the deadline date.

## Gaps & Missing Coverage
1. **Notification Verification:** Step 7 states a notification by email should be sent to the unit. The test does not verify this dispatch.
2. **Alert Verification:** Step 8 dictates an alert should be created for the destination unit. The test does not check the unit's panel for this alert.
3. **Modal Current Date:** Step 4 states the modal should be pre-filled with the current deadline. The test only fills the field with a new date but doesn't verify its initial pre-filled value.

## Recommended Changes
- Add a check to confirm the input field initially contains the correct current deadline before modifying it.
- Verify the internal alert generated for the destination unit.
- Verify the email notification dispatch content.

# CDU-30 - Manter Administradores - Alignment

## Current Status
- The test only covers the initial visualization of the administrators list.
- It checks for the table and the "Adicionar" button.

## Gaps & Missing Coverage
1. **Add Flow completely missing:** Steps 4-8 describe the full flow of adding a new administrator via a modal by their voter title (`título eleitoral`), including validation for existing users or already-admins. The test does not execute or verify any of this.
2. **Remove Flow completely missing:** Steps 9-15 describe the removal flow, including validations (cannot remove oneself, cannot remove the last admin). The test does not cover this at all.
3. **Table Data Match:** Step 2 specifies the list should show name, voter title, registration (matrícula), and unit. The test does not verify these columns exist.

## Recommended Changes
- Implement a scenario to test adding a new administrator, including success and validation error states.
- Implement a scenario to test removing an administrator, specifically testing the safeguard against removing oneself or the final administrator.
- Add assertions to ensure the correct columns are displayed in the list.

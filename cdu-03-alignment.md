# CDU-03 - Criar processo - Alignment

## Current Status
- The test verifies the tree selection logic (indeterminate states, auto-selecting parents).
- It tests restrictions (preventing selection of units already in an active mapping process).
- It tests that units without a map cannot be selected for a Revision process.
- It tests cancellation and confirmation messages.
- It tests initiating a process directly instead of just saving.

## Gaps & Missing Coverage
1. **Validation of Input fields:** Step 4 states the system must validate the required fields (Description, Date, Type). The test doesn't try to save a process with missing data to ensure validation errors appear.
2. **Warning for Interoperacional Root:** Step 7.1.1 states that if the root unit is interoperational, the system should ask if the mapping will be for the unit's own activities or its subordinates. This specific confirmation modal/behavior is not tested.

## Recommended Changes
- Implement a scenario to test validation by submitting an empty or incomplete form and verifying the resulting error messages.
- If applicable to the system's current behavior, implement a test for the interoperational root unit warning (Step 7.1.1).
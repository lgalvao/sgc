# CDU-17 - Disponibilizar mapa de competências - Alignment

## Current Status
- The test prepares a process up to the point where the ADMIN creates competencies.
- It tests opening and canceling the availability modal.
- It tests confirming the availability modal (via a helper function), and checks the final state "Mapa disponibilizado".

## Gaps & Missing Coverage
1. **Validation Checks (Step 8/9):** The requirement states the system must verify if all competencies have activities and vice-versa. If not, it blocks the availability and shows an error indicating which items are unassociated. The test never executes this negative path.
2. **Observation Field (Step 10/13):** The modal has an optional `Observações` field which should be recorded in the map's details. The test doesn't check if this field exists, nor does it fill it or verify its persistence.
3. **Alerts & Notifications:** Steps 16, 17, and 18 dictate emails to the target unit and superior units, and an internal alert. The test does not verify these.
4. **Movements:** Step 15 dictates a movement record. Not verified.

## Recommended Changes
- Implement a scenario where an activity is left unassociated, try to click "Disponibilizar", and assert the specific error message block.
- In the success scenario, ensure the modal's "Observações" field is used, and verify it appears in the map's view later.
- Add assertions for the internal alerts and email notifications.
- Verify the movement history log.
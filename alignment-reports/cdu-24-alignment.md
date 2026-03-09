# CDU-24 - Disponibilizar mapas de competências em bloco - Alignment

## Current Status
- The test sets up a full mapping process until the "Mapa criado" state.
- It returns to the process details, triggers the block availability modal, fills the mandatory date, and confirms.
- It verifies the success message and redirect to the Panel.

## Gaps & Missing Coverage
1. **Validation Checks (Step 8/9):** The requirement states the system verifies if all competencies are associated with activities, and vice-versa. If negative, an error is shown (Step 9). The test does not cover this negative validation scenario.
2. **Alerts & Notifications:** Step 10.4 (Notification to unit), 10.5 (Alert to unit), and 10.7 (Notification to superior units) are not explicitly verified by the test.
3. **History/Movement:** Step 10.3 specifies a movement record. The test doesn't check the internal history of the subprocess.

## Recommended Changes
- Implement a negative test scenario where a competency or activity is unassociated, and verify the specific error message preventing block availability.
- Add assertions to verify the internal alerts and email notifications for both the target units and their superiors.

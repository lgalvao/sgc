# CDU-21 - Finalizar processo - Alignment

## Current Status
- The test prepares an exhaustive end-to-end flow up to "Mapa homologado".
- It checks the process details and verifies the "Finalizar" button.
- It cancels the action.
- It completes the action and verifies the process is finalized.
- It checks the absence of action buttons on a finalized process.

## Gaps & Missing Coverage
1. **Notification and Alerts:** Step 7 in the (implied) requirements usually mandates notifications and alerts to all participating units when a process is finalized. Assuming this exists (though I cannot see the explicit CDU-21 markdown), it's likely missing from the test.
2. **Validation:** Step 4 (implied) usually has a validation if *all* units have completed their maps before allowing the process to finalize. The test prepares one unit perfectly but doesn't test the block if another unit is incomplete.

## Recommended Changes
- If the requirement specifies notifications/alerts on finalization, add assertions for them.
- Add a negative test scenario where the ADMIN tries to finalize a process while a unit is still "Em andamento" or "Mapa criado", verifying the system blocks the action.

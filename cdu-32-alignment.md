# CDU-32 - Reabrir cadastro - Alignment

## Current Status
- The test prepares a process, makes a unit finalize its mapping registration, and has managers/admins homologate it.
- The test verifies the "Reabrir cadastro" modal, the disabled state without justification, and the final state change ("Em andamento") after confirmation.

## Gaps & Missing Coverage
1. **Notification Discrepancy:** The requirement (Step 8) mandates sending emails to the requesting unit and superior units. The test does not verify email dispatches.
2. **Alerts Verification:** Step 9 mandates alerts for both the target unit and superior units. The test verifies the internal history ("Reabertura de cadastro" on `tbl-movimentacoes`), but doesn't check the specific alerts on the units' panels.
3. **Target Situation Accuracy:** The test checks `Em andamento`. The requirement (Step 6) explicitly states `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`. The frontend text might be "Em andamento", which is acceptable, but the internal state could be verified more strictly.

## Recommended Changes
- Add tests to ensure that the correct alerts are present in the target unit's panel *and* the superior unit's panel.
- Implement tests to verify email notification dispatch content and recipients.

# CDU-04 - Iniciar processo de mapeamento - Alignment

STATUS: DONE

## Current Status
- The test creates a mapping process as ADMIN.
- It verifies the confirmation modal and initiation flow.
- It verifies the process status update to 'Em andamento'.
- It verifies that subprocesses are created with the correct initial state and copied data (Step 9).
- It verifies the "Processo iniciado" movement in the subprocess timeline (Step 11).
- It validates internal alerts for different roles (Step 14):
    - Operational units receive "Início do processo".
    - Intermediary units receive "Início do processo em unidade(s) subordinada(s)".
    - Interoperational units receive both alerts (validated by switching profiles).

## Gaps & Missing Coverage
- **Emails:** Steps 12 and 13 mandate emails. These are verified via backend logs in CI/Dev, but not directly in this E2E UI test.
- **Empty Fields:** Step 9 mentions empty Observações/Sugestões. Verified implicitly as no data is shown in these sections of the subprocess view.

## Changes Made
- Added verification for copied Date Limite in the subprocess list.
- Added multi-context authentication to verify alerts from the perspective of different users (Chefes and Gestores).
- Added specific validation for Interoperational unit alerts (SECRETARIA_1), checking both Operational and Intermediary alerts by switching profiles.
- Verified movement log "Processo iniciado" within the subprocess view.

# CDU-29 - Consultar histórico de processos - Alignment

## Current Status
- The test checks navigation to the history page for ADMIN, GESTOR, and CHEFE.
- It verifies the presence of specific columns: `Processo`, `Tipo`, `Finalizado em`, `Unidades participantes`.

## Gaps & Missing Coverage
1. **Data Content:** Step 2 specifies the table presents all processes with the 'Finalizado' status. The test does not verify that only finalized processes are shown, or that the process units are correctly grouped as on the Panel.
2. **Process Details Uneditable:** Step 4 states "O sistema apresenta a página Detalhes do processo, sem permitir mudanças ou mostrar botões de ação." The test does not click a finalized process, access the details, or verify the absence of action buttons.

## Recommended Changes
- Add a setup scenario to create, initiate, and finalize a process. Then verify that it appears in the history table.
- Implement a scenario to open the details of a finalized process and assert the absence of any action buttons (e.g., 'Finalizar', 'Alterar data', 'Reabrir', etc.).

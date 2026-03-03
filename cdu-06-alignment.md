# CDU-06 - Detalhar processo - Alignment

## Current Status
- The test sets up mapping processes.
- It verifies an ADMIN sees the Details page, the Unit table, and the "Finalizar processo" button.
- It verifies a GESTOR sees the Details page, the Unit table, and *does not* see the "Finalizar processo" button.

## Gaps & Missing Coverage
1. **Change Deadline / Situation Elements (ADMIN):** Step 2.2.1 states that ADMINs should see elements to change the deadline or the situation (e.g., Reopen) within the unit details area. The test doesn't check if these elements exist when an ADMIN clicks into a unit.
2. **Block Actions:** Step 2.2.2 states that if subordinate units are ready for block acceptance or homologation, specific buttons ("Aceitar/Homologar em bloco") should be visible. The test does not set up a process to this state and does not check for these buttons.

## Recommended Changes
- Add assertions in the ADMIN scenario to check for the presence of the deadline alteration and status alteration buttons/elements when viewing a subordinate unit.
- Implement a scenario where units are in 'Cadastro disponibilizado' and verify the presence of the "Aceitar em bloco" (GESTOR) and "Homologar em bloco" (ADMIN) buttons on the process details page.
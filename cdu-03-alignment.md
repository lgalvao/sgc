# CDU-03 - Criar processo - Alignment

## Current Status
- The test verifies the tree selection logic (indeterminate states, auto-selecting parents).
- It tests restrictions (preventing selection of units already in an active mapping process).
- It tests that units without a map cannot be selected for a Revision process (verified by checking that checkboxes are disabled).
- It tests cancellation and confirmation messages.
- It tests initiating a process directly instead of just saving.
- It tests editing and removing processes.
- It tests interoperational root behavior (stays selected even if subordinates are unselected).

## Gaps & Missing Coverage
- **Validation of Input fields:** Covered by testing that Save/Start buttons are disabled until all required fields (Description, Date, Type, Units) are filled, and the `required` attribute is present.
- **Warning for Interoperacional Root:** Requirement dismissed based on `etc/reqs/cdu-03.md`, which specifies tree behavior instead of a warning modal for interoperational roots.

## Changes Made
- Enhanced `Deve validar campos obrigatórios e estados dos botões` to check step-by-step enabling of buttons.
- Replaced the interoperational warning fixme with a functional test `Deve permitir selecionar raiz interoperacional independentemente das subordinadas` according to implemented logic and `etc/reqs/cdu-03.md`.
- Completed the cascaded selection rules test.
- Refined feedback and cancellation tests.

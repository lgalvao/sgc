# CDU-10 - Analisar validação de cadastro e conhecimentos (Mapeamento) - Alignment

## Current Status
- This test (which is actually covering the availability/devolution of a *Revision*, though the file says CDU-10 which might be a naming mixup as mapping is CDU-09/13 and revision is CDU-10/14 usually) sets up a Revision process.
- It tests validating that an activity needs a knowledge.
- It tests making the revision available.
- It tests devolution and viewing the history.
- It tests that the history is cleared upon a subsequent availability action.

## Gaps & Missing Coverage
1. **Movement Records:** The test partially checks movements (`tbl-movimentacoes`) for "Disponibilização da revisão...", but not for the devolutions.
2. **Alerts & Notifications:** Missing checks for internal alerts and emails sent during devolution and availability actions.

## Recommended Changes
- Check internal movement logs for Devolution actions.
- Add assertions for internal alerts and email notifications for all state transitions (Disponibilizar, Devolver).
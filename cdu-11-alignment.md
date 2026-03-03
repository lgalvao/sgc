# CDU-11 - Manter atividades do subprocesso de mapeamento - Alignment

## Current Status
- The test runs a massive setup that initiates a mapping process, creates a map, homologates it, finishes the mapping process, and creates a revision process.
- It tests that a CHEFE can add a new activity to the revision, view impacts (none).
- It tests that a CHEFE can edit/remove base activities and view impacts (shows impacted competencies).
- It tests that a GESTOR and an ADMIN can view the impacts in the visualizer screens.

## Gaps & Missing Coverage
- The test relies heavily on the same flow from CDU-16 (Ajustar mapa).
- The file name and test structure for `cdu-11.spec.ts` are a bit intertwined with CDU-16.
- The requirements for CDU-11 explicitly state how the CHEFE interacts with the revision (auto-copying mapping data, seeing the impacts of modifications, and making it available). The test *does* cover these core interactions (adding, editing, removing and seeing impacts).
- However, similar to other tests, it does not explicitly verify the auto-save functionality mentioned in the requirements, nor does it check the exact `tbl-movimentacoes` log or alerts/emails that might be defined in the core requirement (though CDU-11 is usually just the data manipulation, making it available is CDU-10).

## Recommended Changes
- Verify the auto-save by reloading the page after an edit.
- Ensure the test explicitly checks that the "Base" activities (copied from the mapping phase) are loaded correctly initially before edits are made.
# CDU-16 - Ajustar mapa de competências - Alignment

## Current Status
- The test sets up a massive end-to-end flow: Maps a unit, homologates the map, finishes the process, creates a *Revision* process, makes the Chefe alter/add/remove activities, and gets the revision homologated.
- It tests navigating to the map adjustment screen.
- It tests viewing the "Impactos" modal, correctly identifying inserted activities and impacted competencies.
- It tests opening the competency edit modal and canceling.
- It tests associating the new activity to a new competency.

## Gaps & Missing Coverage
1. **Activity Association Validation:** The requirement states (Step 3) that unassociated activities must be highlighted with an alert icon, and competencies without activities highlighted with an error icon. The test does not verify these visual cues.
2. **Competency Deletion:** Steps 8-9 describe the flow to delete a competency. The test covers editing/canceling and creating, but not deleting.
3. **Saving/Updating Competency:** Step 7 describes updating the name/description of an existing competency. The test only cancels the edit modal, it doesn't confirm a change and verify it.

## Recommended Changes
- Add assertions to verify the warning/error icons on unassociated activities or empty competencies.
- Implement a scenario to edit a competency's name, save it, and verify the change in the UI.
- Implement a scenario to delete a competency and verify it is removed from the list.
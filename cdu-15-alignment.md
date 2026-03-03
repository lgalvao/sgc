# CDU-15 - Manter mapa de competências - Alignment

## Current Status
- The test prepares the map to 'Cadastro homologado' state.
- It tests accessing the edition page.
- It tests creating a competency with activities.
- It tests editing a competency (name and associated activities).
- It tests canceling the exclusion of a competency.
- It tests confirming the exclusion of a competency.
- It tests making the map available ("Disponibilizar").

## Gaps & Missing Coverage
1. **Activity count badge and tooltip (Step 4.4):** The requirement states there should be a badge with the count of knowledges on the right of the activity description, and hovering over it should display a tooltip with the list of knowledges. The test does not verify this visual element.

## Recommended Changes
- Add an assertion to verify the presence of the knowledge count badge next to an activity within a competency block.
- Simulate a hover over the badge and assert the visibility of the tooltip containing the associated knowledges.
# CDU-18 - Visualizar mapa de competências - Alignment

## Current Status
- The test relies on "seed" data (a finalized process with ID 99).
- It tests an ADMIN viewing the map via process details.
- It tests a CHEFE viewing the map via their own panel.
- It verifies the UI structure: title, unit identification, and the nested display of Competency -> Activities -> Knowledges.

## Gaps & Missing Coverage
- The test perfectly aligns with the requirements. It effectively covers different profile accesses and meticulously verifies the nested visual structure of the map as defined in Step 5.
- (Minor): It relies on seed data which could be brittle if the seed changes, but assuming the seed is stable, the functional coverage is complete.

## Recommended Changes
- None required. The test is well-aligned with the requirement.
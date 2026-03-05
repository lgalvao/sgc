# CDU-07 - Detalhar subprocesso - Alignment
STATUS: DONE

## Current Status
- The test verifies that ADMINs, GESTORs, and CHEFEs can access the subprocess details page.
- It checks that the CHEFE is routed directly to their unit's subprocess.
- It verifies the presence of unit data (sigla, situation, deadline), the movements table, and the action cards.

## Gaps & Missing Coverage
1. **Responsibility Information:** Step 2.1.2 and 2.1.3 specify that the "Titular" and "Responsável" data (Name, Ramal, Email, Responsability Type) must be displayed. The test does not verify the presence of this detailed responsibility information.
2. **Localização Atual:** Step 2.1.5 requires displaying the "Localização atual" (Current Location). The test checks "situacao" and "prazo" but skips this field.
3. **Card Availability Rules:** Step 2.3.1 outlines strict rules for when the "Atividades" and "Mapa" cards are enabled vs disabled based on profile and process state. The test only checks if *any* activity card is visible, but doesn't test the disabled/enabled states for the different profiles across the process lifecycle.

## Recommended Changes
- Add assertions to verify the presence of the Titular/Responsável details (Name, Ramal, Email).
- Add an assertion to check the "Localização atual" field.
- Enhance the test to verify that the "Mapa de Competências" card is properly disabled for non-ADMINs before homologation, and enabled afterwards.
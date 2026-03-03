# CDU-36 - Gerar relatório de mapas - Alignment

## Current Status
- The test verifies basic navigation and visibility of the "Mapas Vigentes" modal.
- The test checks for an "Exportar CSV" button instead of the "PDF" mentioned in the requirements.

## Gaps & Missing Coverage
1. **Filters:** The test does not verify the definition of filters (Processo - mandatory, Unidade - optional) as specified in the requirement.
2. **Action Verification:** The test does not interact with the filters or trigger the "Gerar" / "Exportar" action.
3. **Format Mismatch:** The requirement specifies a "PDF" file generation, whereas the test expects a button for "CSV". This is a significant discrepancy.
4. **Data Coverage:** The requirement dictates specific data within the PDF (Unidade, Competências, Atividades, Conhecimentos). There is no check to ensure the generated file matches this or if the API request correctly requests these fields.

## Recommended Changes
- Update the test to verify the presence and functionality of the "Processo" and "Unidade" filters.
- Correct the expected export format from CSV to PDF (or adjust the requirement if CSV is the actual intended format).
- Simulate/mock or actually perform the "Gerar" action and verify that the file download is triggered with the correct parameters.

# CDU-35 - Gerar relatório de andamento - Alignment

## Current Status
- The test verifies navigation to reports and the presence of the "Andamento Geral" modal.
- It checks for filters: "Tipo", "Data Início", and "Data Fim", and a generic "Exportar CSV" button.

## Gaps & Missing Coverage
1. **Filter Discrepancy:** The requirement specifies selecting a "Processo" (e.g., "Mapeamento 2027"), but the test checks for "Tipo", "Data Início", and "Data Fim".
2. **Table Columns:** The requirement lists specific columns to be displayed: Sigla da unidade, Nome da unidade, Situação atual, Data da última movimentação, Responsável, Titular. The test only verifies that a `table` exists, without verifying its structure or columns.
3. **Export Format Mismatch:** The requirement mentions clicking a `PDF` button to export, but the test checks for an `Exportar CSV` button.
4. **Data Population:** The test does not verify if the report populates with data when a process is selected.

## Recommended Changes
- Modify the test to check for the correct filter ("Processo") instead of generic date/type filters.
- Add assertions to verify the presence of the specific columns detailed in the requirement.
- Fix the export format mismatch by checking for a PDF export action.
- Add a test scenario that actually selects a process and verifies the data loaded in the table.

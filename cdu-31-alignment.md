# CDU-31 - Configurar sistema - Alignment

## Current Status
- The test verifies navigation to the parameters page, visibility of form inputs, and the saving of a configuration.

## Gaps & Missing Coverage
1. **Specific Fields:** The requirement outlines two specific settings: `Dias para inativação de processos` and `Dias para indicação de alerta como novo`. The test indiscriminately picks the first `input[type="number"]` without verifying its label or specific purpose.
2. **Validation:** The requirement states the value must be an integer, 1 or more. The test does not verify this validation rule (e.g., trying to input 0, negative numbers, or non-integers).
3. **Immediate Effect:** The requirement explicitly states "O efeito das configurações deve ser imediato". The test does not verify if the changed parameter actually takes effect immediately in the system behavior.

## Recommended Changes
- Explicitly target and verify the two settings fields by their labels or test IDs.
- Add negative testing to ensure values less than 1 or invalid formats are rejected.
- Ideally, add a scenario to verify the "immediate effect" (e.g., by changing the "new alert days" and checking an existing alert's status).

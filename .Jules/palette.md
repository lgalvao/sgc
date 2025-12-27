## 2024-05-22 - [Numeric Inputs on Login]
**Learning:** The "Título de Eleitor" field is purely numeric. Using `inputmode="numeric"` on this field significantly improves the mobile experience by triggering the numeric keypad, reducing friction for users on touch devices.
**Action:** Identify other ID fields (like CPF, enrollment numbers) that are text-based but strictly numeric and apply `inputmode="numeric"` or `type="tel"` to them.

## 2024-05-22 - [Explicit Required Indicators]
**Learning:** While `required` attribute handles browser validation and accessibility, explicit visual indicators (like a red asterisk) are crucial for scanning. Users shouldn't have to submit a form to find out which fields are mandatory.
**Action:** Audit all forms to ensure required fields have both programmatic (`required`) and visual indicators.

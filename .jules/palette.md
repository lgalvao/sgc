## 2026-01-30 - Unwired Props in Modals
**Learning:** Found a `loading` prop in `AceitarMapaModal` that was defined but completely unused in the template. This indicates a pattern where the interface was prepared for feedback but the implementation was missed.
**Action:** When seeing "dead" props in component definitions, check if they were meant for important UX states like loading/disabled and wire them up.

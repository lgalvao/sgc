## 2025-01-08 - Empty States are Forgotten
**Learning:** The application uses default BootstrapVue tables which result in plain text "No records found" messages. This is a pattern across multiple dashboard components.
**Action:** Enhance empty states with large icons (Bootstrap Icons) and helpful context/call-to-action text to make the "zero data" state feel intentional and less broken.
## 2024-05-22 - Visual Loading States
**Learning:** Playwright's network interception (`page.route`) is essential for verifying 'loading' states when a backend is not available or too fast. By intentionally not fulfilling a route, we can freeze the UI in a pending state to capture screenshots.
**Action:** Use this pattern for all future async UI verification.

## 2025-02-18 - Modal Async Feedback
**Learning:** Users lack feedback during critical async modal actions (like 'Disponibilizar'), leading to potential double-submissions.
**Action:** Standardize passing a `loading` prop to all action modals to drive button disabled states and spinners.

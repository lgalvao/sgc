## 2024-05-23 - Accessibility of Navigation Buttons
**Learning:** Icon-only navigation buttons (like "Back" arrows) are a common pattern in this app but often lack accessible labels, making them invisible to screen readers.
**Action:** Always pair `aria-label` with `title` (or tooltip) for icon-only buttons to ensure both screen reader users and sighted users (via hover) understand the action. Also, explicitly hide decorative icons inside buttons with `aria-hidden="true"`.

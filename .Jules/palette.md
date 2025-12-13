## 2024-05-22 - Icon-only Buttons Accessibility
**Learning:** The application heavily uses `title` attributes on icon-only buttons (like edit/delete actions and navbar links) assuming it provides accessibility. However, this is insufficient for screen readers and touch users.
**Action:** Systematically replace/augment `title` with `aria-label` for all icon-only interactive elements. Use dynamic labels (e.g., "Edit [Item Name]") where possible to provide context.

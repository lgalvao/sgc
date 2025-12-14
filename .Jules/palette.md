## 2024-05-22 - Icon-only Buttons Accessibility
**Learning:** The application heavily uses `title` attributes on icon-only buttons (like edit/delete actions and navbar links) assuming it provides accessibility. However, this is insufficient for screen readers and touch users.
**Action:** Systematically replace/augment `title` with `aria-label` for all icon-only interactive elements. Use dynamic labels (e.g., "Edit [Item Name]") where possible to provide context.

## 2025-02-17 - Critical Navigation Accessibility
**Learning:** Found critical "Back" navigation buttons implemented as icon-only links without accessible names, completely blocking screen reader users from understanding the navigation flow.
**Action:** Audit all navigation-related icon buttons (Back, Home, Menu) specifically, as they are high-impact accessibility blockers compared to secondary action buttons.

## 2024-01-20 - [Loading States for Micro-Interactions]
**Learning:** Even small interactions like "Adding an item" need loading states. Without it, users may double-click, leading to duplicate requests or frustration if the network is slow. The absence of feedback breaks the sense of responsiveness.
**Action:** Always add a loading spinner or disable state to local action buttons (like "Add", "Remove", "Update") that trigger async API calls, not just for main page submissions.

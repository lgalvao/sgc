## 2024-03-24 - [Reusing EmptyState for Table Views]

**Learning:** Tables often have awkward "No records found" rows (`<tr><td colspan="...">...</td></tr>`) that are
visually distinct from other empty states in the application. Replacing these with the standardized `EmptyState`
component (placed outside the table or replacing the table body) provides a much more cohesive and visually pleasing
experience. It also ensures consistent iconography and messaging across the app.
**Action:** When encountering a table that might be empty, check if `EmptyState` component can be used instead of a
custom table row, potentially by conditionally rendering the table vs. the empty state container.

## 2025-01-29 - [Modal Content Focus]

**Learning:** Standard HTML `autofocus` attribute in modals is often unreliable due to the modal library's internal
focus management (e.g., `bootstrap-vue-next` focuses the modal container or specific buttons).
**Action:** To reliably focus an input field in a modal, expose the `shown` event from the modal component and use a
`ref` to programmatically call `focus()` in the parent's event handler. Ensure the `shown` event is emitted *after* any
internal focus logic of the modal component.

## 2025-01-30 - [Empty States in Generic Components]

**Learning:** Generic data components like `TreeTableView` often neglect empty states, forcing consumers to handle
`v-if` logic repeatedly.
**Action:** Bake `EmptyState` support directly into generic components (with customizable props) so that "no data"
scenarios are handled gracefully and consistently by default without extra boilerplate in every view.

## 2024-12-29 - [Fixing Disabled Button States]
**Learning:** Forcing utility classes like `opacity-100` on Bootstrap buttons overrides the default disabled state styling (opacity 0.65), creating a confusing UI where disabled buttons look active.
**Action:** Avoid manual opacity classes on interactive elements. Rely on the framework's (BootstrapVueNext) native `disabled` prop to handle visual feedback automatically. Also, ensure decorative icons next to text labels have `aria-hidden="true"`.
## 2024-12-31 - [Loading States & Contextual Help]
**Learning:** Adding loading spinners and changing button text (e.g., "Salvar" -> "Salvando...") provides essential feedback for async operations, preventing user frustration or double-submissions. Also, adding `description` text to `BFormGroup` is a clean way to clarify complex date inputs without cluttering the UI.
**Action:** Always implement explicit loading states for form submission buttons and use the `description` prop for inputs that require context, rather than relying on external tooltips or lengthy labels.

## 2024-12-29 - [Fixing Disabled Button States]
**Learning:** Forcing utility classes like `opacity-100` on Bootstrap buttons overrides the default disabled state styling (opacity 0.65), creating a confusing UI where disabled buttons look active.
**Action:** Avoid manual opacity classes on interactive elements. Rely on the framework's (BootstrapVueNext) native `disabled` prop to handle visual feedback automatically. Also, ensure decorative icons next to text labels have `aria-hidden="true"`.

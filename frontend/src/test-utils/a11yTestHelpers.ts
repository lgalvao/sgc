import {axe} from "vitest-axe";
import "vitest-axe/extend-expect";
import {expect} from "vitest";

/**
 * Executes accessibility checks on a rendered component or HTML element.
 * 
 * @param container The HTMLElement or rendered component to check.
 * @param options Custom axe-core options (optional).
 */
export async function checkA11y(container: HTMLElement, options?: any) {
  const results = await axe(container, {
    ...options,
    rules: {
      ...options?.rules,
      region: { enabled: false }, // Disable region rule for component tests
    },
  });
  expect(results).toHaveNoViolations();
}

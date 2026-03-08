import {test as base} from '@playwright/test';
import {AxeBuilder} from '@axe-core/playwright';

export const test = base.extend<{ makeAxeBuilder: () => AxeBuilder }>({
  makeAxeBuilder: async ({ page }, use) => {
    const makeAxeBuilder = () =>
      new AxeBuilder({ page })
        .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
        .disableRules(['list']); // Known BVN violation for dropdown/nav components

    await use(makeAxeBuilder);
  },
});

export { expect } from '@playwright/test';

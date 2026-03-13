import {describe, expect, it} from 'vitest';
import {render} from '@testing-library/vue';
import {run} from 'axe-core';
import AppAlert from './AppAlert.vue';

describe('AppAlert A11y', () => {
  it('has no accessibility violations in default mode', async () => {
    const { container } = render(AppAlert, {
      props: { message: 'Hello world' },
    });
    const results = await run(container);
    expect(results).toHaveNoViolations();
  });

  it('has no accessibility violations in detailed mode', async () => {
    const { container } = render(AppAlert, {
      props: {
        notification: {
          summary: 'Error',
          details: ['Detail 1', 'Detail 2']
        }
      },
    });
    const results = await run(container);
    expect(results).toHaveNoViolations();
  });
});

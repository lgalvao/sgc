import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import * as axeCore from 'axe-core';
import AppAlert from './AppAlert.vue';

describe('AppAlert A11y', () => {
  it('has no accessibility violations in default mode', async () => {
    const wrapper = mount(AppAlert, {
      props: { message: 'Hello world' },
      attachTo: document.body,
    });
    const results = await axeCore.run(wrapper.element);
    expect(results.violations).toEqual([]);
    wrapper.unmount();
  });

  it('has no accessibility violations in detailed mode', async () => {
    const wrapper = mount(AppAlert, {
      props: {
        notification: {
          summary: 'Error',
          details: ['Detail 1', 'Detail 2']
        }
      },
      attachTo: document.body,
    });
    const results = await axeCore.run(wrapper.element);
    expect(results.violations).toEqual([]);
    wrapper.unmount();
  });
});

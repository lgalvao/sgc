import { config } from '@vue/test-utils';
import { vi } from 'vitest';

vi.mock('bootstrap', () => ({
  Tooltip: class Tooltip {
    constructor() {}
    dispose() {}
  },
}));

config.global.stubs['b-modal'] = {
  props: ['modelValue'],
  template: `
    <div v-if="modelValue">
      <slot />
      <slot name="footer" />
    </div>
  `,
};

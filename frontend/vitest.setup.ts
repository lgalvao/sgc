import { config } from '@vue/test-utils';
import { vi } from 'vitest';
import { createBootstrap } from 'bootstrap-vue-next';
import {
    BFormCheckbox,
    BFormSelect,
    BFormSelectOption,
    BFormTextarea,
    BFormInput,
} from 'bootstrap-vue-next';

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

config.global.plugins.push(createBootstrap());

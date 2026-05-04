import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import ProcessoDeadlineField from './ProcessoDeadlineField.vue';

const meta: Meta<typeof ProcessoDeadlineField> = {
    title: 'Processo/ProcessoDeadlineField',
    component: ProcessoDeadlineField,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ProcessoDeadlineField>;

export const Default: Story = {
    render: () => ({
        components: {ProcessoDeadlineField},
        setup() {
            const valor = ref('');
            return {valor};
        },
        template: `
      <div style="max-width: 300px; padding: 20px;">
        <ProcessoDeadlineField
          :model-value="valor"
          @update:model-value="valor = $event"
        />
      </div>
    `,
    }),
};

export const ComValor: Story = {
    render: () => ({
        components: {ProcessoDeadlineField},
        setup() {
            const valor = ref('2025-09-30');
            return {valor};
        },
        template: `
      <div style="max-width: 300px; padding: 20px;">
        <ProcessoDeadlineField
          :model-value="valor"
          @update:model-value="valor = $event"
        />
      </div>
    `,
    }),
};

export const ComErro: Story = {
    render: () => ({
        components: {ProcessoDeadlineField},
        setup() {
            const valor = ref('');
            return {valor};
        },
        template: `
      <div style="max-width: 300px; padding: 20px;">
        <ProcessoDeadlineField
          :model-value="valor"
          erro="A data limite é obrigatória e deve ser uma data futura."
          @update:model-value="valor = $event"
        />
      </div>
    `,
    }),
};

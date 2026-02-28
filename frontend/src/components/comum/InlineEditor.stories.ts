import type {Meta, StoryObj} from '@storybook/vue3-vite';
import InlineEditor from './InlineEditor.vue';
import {ref} from 'vue';
import {expect} from 'vitest';
import {page, userEvent} from '@vitest/browser/context';

const meta: Meta<typeof InlineEditor> = {
    title: 'Comum/InlineEditor',
    component: InlineEditor,
    tags: ['autodocs'],
    argTypes: {
        size: {control: 'select', options: ['sm', 'md', 'lg']},
        'onUpdate:modelValue': {action: 'update:modelValue'},
        onSave: {action: 'save'},
        onCancel: {action: 'cancel'},
    },
};

export default meta;
type Story = StoryObj<typeof InlineEditor>;

export const Default: Story = {
    args: {
        modelValue: 'Texto para editar',
    },
    render: (args) => ({
        components: {InlineEditor},
        setup() {
            const val = ref(args.modelValue);
            return {args, val};
        },
        template: `
      <div style="max-width: 400px; padding: 20px; border: 1px solid #eee; border-radius: 4px;">
        <InlineEditor v-bind="args" v-model="val">
          {{ val }}
        </InlineEditor>
      </div>
    `,
    }),
    play: async () => {
        // Verifica estado inicial
        await expect.element(page.getByText('Texto para editar')).toBeVisible();

        const editBtn = page.getByRole('button', {name: /editar/i});
        await userEvent.click(editBtn);

        const input = page.getByRole('textbox');
        await expect.element(input).toHaveValue('Texto para editar');
    },
};

export const Grande: Story = {
    args: {
        modelValue: 'Título Grande',
        size: 'lg',
    },
    render: (args) => ({
        components: {InlineEditor},
        setup() {
            const val = ref(args.modelValue);
            return {args, val};
        },
        template: `
      <div style="max-width: 600px;">
        <InlineEditor v-bind="args" v-model="val">
          <h2 class="mb-0">{{ val }}</h2>
        </InlineEditor>
      </div>
    `,
    }),
};

export const SomenteLeitura: Story = {
    args: {
        modelValue: 'Este texto não pode ser editado',
        canEdit: false,
    },
    render: (args) => ({
        components: {InlineEditor},
        setup() {
            const val = ref(args.modelValue);
            return {args, val};
        },
        template: `
      <div style="max-width: 400px;">
        <InlineEditor v-bind="args" v-model="val">
          {{ val }}
        </InlineEditor>
      </div>
    `,
    }),
};

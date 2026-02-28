import type {Meta, StoryObj} from '@storybook/vue3-vite';
import CadAtividadeForm from './CadAtividadeForm.vue';
import {ref} from 'vue';

const meta: Meta<typeof CadAtividadeForm> = {
    title: 'Atividades/CadAtividadeForm',
    component: CadAtividadeForm,
    tags: ['autodocs'],
    argTypes: {
        onSubmit: {action: 'submit'},
    },
};

export default meta;
type Story = StoryObj<typeof CadAtividadeForm>;

export const Default: Story = {
    args: {
        modelValue: '',
        loading: false,
        disabled: false,
    },
    render: (args) => ({
        components: {CadAtividadeForm},
        setup() {
            const val = ref(args.modelValue);
            return {args, val};
        },
        template: `
      <div style="max-width: 600px; padding: 1rem; border: 1px solid #eee; border-radius: 8px;">
        <CadAtividadeForm v-bind="args" v-model="val" />
        <div class="mt-2 small text-muted">Texto digitado: {{ val }}</div>
      </div>
    `,
    }),
};

export const Carregando: Story = {
    args: {
        modelValue: 'Nova Atividade',
        loading: true,
    },
};

export const ComErro: Story = {
    args: {
        modelValue: '',
        erro: 'A atividade já existe neste cadastro.',
    },
};

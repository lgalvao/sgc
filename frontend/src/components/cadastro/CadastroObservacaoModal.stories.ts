import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import {ref} from 'vue';
import CadastroObservacaoModal from './CadastroObservacaoModal.vue';

const meta: Meta<typeof CadastroObservacaoModal> = {
    title: 'Cadastro/CadastroObservacaoModal',
    component: CadastroObservacaoModal,
    tags: ['autodocs'],
    argTypes: {
        modelValue: {control: 'boolean'},
        loading: {control: 'boolean'},
        variant: {
            control: 'select',
            options: ['success', 'danger', 'primary'],
        },
        labelObrigatoria: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof CadastroObservacaoModal>;

export const Aberto: Story = {
    args: {
        modelValue: true,
        loading: false,
        titulo: 'Devolver Cadastro',
        okTitle: 'Devolver',
        texto: 'Informe o motivo da devolução do cadastro.',
        observacao: '',
        testCodigoConfirmar: 'btn-modal-confirmar',
        inputId: 'observacao-modal',
        inputDataTestid: 'inp-observacao-modal',
        label: 'Observação',
        variant: 'danger',
        erro: null,
        labelObrigatoria: true,
    },
    render: (args) => ({
        components: {CadastroObservacaoModal},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <CadastroObservacaoModal
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
    play: async () => {
        const textarea = page.getByTestId('inp-observacao-modal');
        await expect.element(textarea).toBeVisible();
    },
};

export const ComSucesso: Story = {
    args: {
        modelValue: true,
        loading: false,
        titulo: 'Aceitar Cadastro',
        okTitle: 'Aceitar',
        texto: 'Confirme o aceite do cadastro. Adicione uma observação se necessário.',
        observacao: '',
        testCodigoConfirmar: 'btn-aceite-confirmar',
        inputId: 'observacao-aceite',
        inputDataTestid: 'inp-observacao-aceite',
        label: 'Observação (opcional)',
        variant: 'success',
        erro: null,
        labelObrigatoria: false,
    },
    render: (args) => ({
        components: {CadastroObservacaoModal},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <CadastroObservacaoModal
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

export const Carregando: Story = {
    args: {
        modelValue: true,
        loading: true,
        titulo: 'Devolver Cadastro',
        okTitle: 'Devolver',
        texto: 'Aguarde enquanto processamos a devolução.',
        observacao: 'Cadastro incompleto — faltam atividades essenciais.',
        testCodigoConfirmar: 'btn-devolucao-confirmar',
        inputId: 'observacao-devolucao',
        inputDataTestid: 'inp-observacao-devolucao',
        label: 'Justificativa',
        variant: 'danger',
        erro: null,
        labelObrigatoria: true,
    },
    render: (args) => ({
        components: {CadastroObservacaoModal},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <CadastroObservacaoModal
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

export const ComErro: Story = {
    args: {
        modelValue: true,
        loading: false,
        titulo: 'Devolver Cadastro',
        okTitle: 'Devolver',
        texto: 'Informe o motivo da devolução.',
        observacao: '',
        testCodigoConfirmar: 'btn-devolucao-confirmar',
        inputId: 'observacao-devolucao-erro',
        inputDataTestid: 'inp-observacao-devolucao-erro',
        label: 'Justificativa',
        variant: 'danger',
        erro: 'Não foi possível processar a solicitação. Tente novamente.',
        labelObrigatoria: true,
    },
    render: (args) => ({
        components: {CadastroObservacaoModal},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <CadastroObservacaoModal
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
    play: async () => {
        await expect.element(page.getByText('Não foi possível processar a solicitação. Tente novamente.')).toBeVisible();
    },
};

import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import ModalDevolucaoCadastro from './ModalDevolucaoCadastro.vue';

const meta: Meta<typeof ModalDevolucaoCadastro> = {
    title: 'Cadastro/ModalDevolucaoCadastro',
    component: ModalDevolucaoCadastro,
    tags: ['autodocs'],
    argTypes: {
        modelValue: {control: 'boolean'},
        loading: {control: 'boolean'},
        isRevisao: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ModalDevolucaoCadastro>;

export const DevolucaoCadastro: Story = {
    args: {
        modelValue: true,
        loading: false,
        isRevisao: false,
        observacao: '',
        erro: null,
    },
    render: (args) => ({
        components: {ModalDevolucaoCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalDevolucaoCadastro
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

export const DevolucaoRevisao: Story = {
    args: {
        modelValue: true,
        loading: false,
        isRevisao: true,
        observacao: '',
        erro: null,
    },
    render: (args) => ({
        components: {ModalDevolucaoCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalDevolucaoCadastro
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
        isRevisao: false,
        observacao: 'Cadastro incompleto — necessário revisar atividades da competência Gestão de Projetos.',
        erro: null,
    },
    render: (args) => ({
        components: {ModalDevolucaoCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalDevolucaoCadastro
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
        isRevisao: false,
        observacao: '',
        erro: 'Não foi possível realizar a devolução. Verifique sua conexão e tente novamente.',
    },
    render: (args) => ({
        components: {ModalDevolucaoCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalDevolucaoCadastro
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

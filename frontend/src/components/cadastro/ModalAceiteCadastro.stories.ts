import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import ModalAceiteCadastro from './ModalAceiteCadastro.vue';

const meta: Meta<typeof ModalAceiteCadastro> = {
    title: 'Cadastro/ModalAceiteCadastro',
    component: ModalAceiteCadastro,
    tags: ['autodocs'],
    argTypes: {
        modelValue: {control: 'boolean'},
        loading: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof ModalAceiteCadastro>;

const acaoHomologarMock = {
    tituloModal: 'Homologar Cadastro',
    rotuloConfirmacao: 'Homologar',
    textoModal: 'Confirme a homologação do cadastro de competências. Esta ação não pode ser desfeita.',
};

const acaoAceitarMock = {
    tituloModal: 'Aceitar Cadastro',
    rotuloConfirmacao: 'Aceitar',
    textoModal: 'Confirme o aceite do cadastro. O subprocesso avançará para a próxima etapa.',
};

export const Homologar: Story = {
    args: {
        modelValue: true,
        loading: false,
        acao: acaoHomologarMock,
        observacao: '',
        erro: null,
    },
    render: (args) => ({
        components: {ModalAceiteCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalAceiteCadastro
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

export const Aceitar: Story = {
    args: {
        modelValue: true,
        loading: false,
        acao: acaoAceitarMock,
        observacao: '',
        erro: null,
    },
    render: (args) => ({
        components: {ModalAceiteCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalAceiteCadastro
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

export const SemAcao: Story = {
    args: {
        modelValue: true,
        loading: false,
        acao: null,
        observacao: '',
        erro: null,
    },
    render: (args) => ({
        components: {ModalAceiteCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalAceiteCadastro
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
        acao: acaoHomologarMock,
        observacao: 'Cadastro aprovado sem ressalvas.',
        erro: null,
    },
    render: (args) => ({
        components: {ModalAceiteCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalAceiteCadastro
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
        acao: acaoHomologarMock,
        observacao: '',
        erro: 'Não foi possível homologar o cadastro. Tente novamente.',
    },
    render: (args) => ({
        components: {ModalAceiteCadastro},
        setup() {
            const visivel = ref(args.modelValue);
            const obs = ref(args.observacao);
            return {args, visivel, obs};
        },
        template: `
      <ModalAceiteCadastro
        v-bind="args"
        v-model="visivel"
        v-model:observacao="obs"
      />
    `,
    }),
};

import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page, userEvent} from '@vitest/browser/context';
import {ref} from 'vue';
import ArvoreToolbar from './ArvoreToolbar.vue';

const meta: Meta<typeof ArvoreToolbar> = {
    title: 'Unidade/ArvoreToolbar',
    component: ArvoreToolbar,
    tags: ['autodocs'],
    argTypes: {
        modoSelecao: {control: 'boolean'},
        termoBusca: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof ArvoreToolbar>;

export const ModoVisualizacao: Story = {
    args: {
        termoBusca: '',
        modoSelecao: false,
    },
    render: (args) => ({
        components: {ArvoreToolbar},
        setup() {
            const termo = ref(args.termoBusca);
            return {args, termo};
        },
        template: '<ArvoreToolbar v-bind="args" v-model:termoBusca="termo" />',
    }),
    play: async () => {
        const input = page.getByTestId('inp-arvore-busca');
        await expect.element(input).toBeVisible();
    },
};

export const ModoSelecao: Story = {
    args: {
        termoBusca: '',
        modoSelecao: true,
    },
    render: (args) => ({
        components: {ArvoreToolbar},
        setup() {
            const termo = ref(args.termoBusca);
            return {args, termo};
        },
        template: '<ArvoreToolbar v-bind="args" v-model:termoBusca="termo" />',
    }),
    play: async () => {
        const btnTodos = page.getByTestId('btn-arvore-selecionar-todos');
        await expect.element(btnTodos).toBeVisible();
        const btnLimpar = page.getByTestId('btn-arvore-limpar-selecao');
        await expect.element(btnLimpar).toBeVisible();
    },
};

export const ComBusca: Story = {
    args: {
        termoBusca: 'PROEN',
        modoSelecao: false,
    },
    render: (args) => ({
        components: {ArvoreToolbar},
        setup() {
            const termo = ref(args.termoBusca);
            return {args, termo};
        },
        template: '<ArvoreToolbar v-bind="args" v-model:termoBusca="termo" />',
    }),
    play: async () => {
        const input = page.getByTestId('inp-arvore-busca');
        await expect.element(input).toHaveValue('PROEN');
    },
};

export const ModoSelecaoComBusca: Story = {
    args: {
        termoBusca: 'CF',
        modoSelecao: true,
    },
    render: (args) => ({
        components: {ArvoreToolbar},
        setup() {
            const termo = ref(args.termoBusca);
            return {args, termo};
        },
        template: '<ArvoreToolbar v-bind="args" v-model:termoBusca="termo" />',
    }),
};

export const FiltrarUnidade: Story = {
    args: {
        termoBusca: '',
        modoSelecao: true,
    },
    render: (args) => ({
        components: {ArvoreToolbar},
        setup() {
            const termo = ref(args.termoBusca);
            return {args, termo};
        },
        template: `
      <div>
        <ArvoreToolbar v-bind="args" v-model:termoBusca="termo" />
        <small class="text-muted">Filtro atual: {{ termo || '(vazio)' }}</small>
      </div>
    `,
    }),
    play: async () => {
        const input = page.getByTestId('inp-arvore-busca');
        await userEvent.type(input, 'REIT');
        await expect.element(input).toHaveValue('REIT');
    },
};

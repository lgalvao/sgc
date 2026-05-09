import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import RelatorioAndamentoFiltros from './RelatorioAndamentoFiltros.vue';

const meta: Meta<typeof RelatorioAndamentoFiltros> = {
    title: 'Relatorios/RelatorioAndamentoFiltros',
    component: RelatorioAndamentoFiltros,
    tags: ['autodocs'],
    argTypes: {
        carregando: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof RelatorioAndamentoFiltros>;

const opcoesProcessosMock = [
    {value: null, text: '-- Selecione um processo --'},
    {value: 1, text: 'Mapeamento de Competências 2024'},
    {value: 2, text: 'Revisão de Atividades 2024'},
    {value: 3, text: 'Diagnóstico Organizacional 2023'},
];

export const Default: Story = {
    args: {
        codProcessoSelecionado: null,
        opcoesProcessos: opcoesProcessosMock,
        carregando: false,
    },
};

export const ComProcessoSelecionado: Story = {
    args: {
        codProcessoSelecionado: 1,
        opcoesProcessos: opcoesProcessosMock,
        carregando: false,
    },
    render: (args) => ({
        components: {RelatorioAndamentoFiltros},
        setup() {
            const cod = ref(args.codProcessoSelecionado);
            return {args, cod};
        },
        template: '<RelatorioAndamentoFiltros v-bind="args" v-model:codProcessoSelecionado="cod" />',
    }),
};

export const Carregando: Story = {
    args: {
        codProcessoSelecionado: 2,
        opcoesProcessos: opcoesProcessosMock,
        carregando: true,
    },
    render: (args) => ({
        components: {RelatorioAndamentoFiltros},
        setup() {
            const cod = ref(args.codProcessoSelecionado);
            return {args, cod};
        },
        template: '<RelatorioAndamentoFiltros v-bind="args" v-model:codProcessoSelecionado="cod" />',
    }),
};

export const SemProcessos: Story = {
    args: {
        codProcessoSelecionado: null,
        opcoesProcessos: [{value: null, text: 'Nenhum processo disponível'}],
        carregando: false,
    },
};

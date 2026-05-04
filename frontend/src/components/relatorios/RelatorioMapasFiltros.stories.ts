import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import RelatorioMapasFiltros from './RelatorioMapasFiltros.vue';
import type {Unidade} from '@/types/tipos';

const meta: Meta<typeof RelatorioMapasFiltros> = {
    title: 'Relatorios/RelatorioMapasFiltros',
    component: RelatorioMapasFiltros,
    tags: ['autodocs'],
    argTypes: {
        carregando: {control: 'boolean'},
        temUnidadesSelecionadas: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof RelatorioMapasFiltros>;

const unidadesMock: Unidade[] = [
    {
        codigo: 1,
        nome: 'Reitoria',
        sigla: 'REIT',
        filhas: [
            {
                codigo: 2,
                nome: 'Pró-Reitoria de Ensino',
                sigla: 'PROEN',
                filhas: [],
            },
            {
                codigo: 3,
                nome: 'Pró-Reitoria de Pesquisa',
                sigla: 'PROPES',
                filhas: [],
            },
        ],
    },
    {
        codigo: 4,
        nome: 'Campus Fortaleza',
        sigla: 'CF',
        filhas: [],
    },
];

export const Default: Story = {
    args: {
        unidadesDisponiveis: unidadesMock,
        unidadesSelecionadas: [],
        carregando: false,
        temUnidadesSelecionadas: false,
    },
    render: (args) => ({
        components: {RelatorioMapasFiltros},
        setup() {
            const selecionadas = ref(args.unidadesSelecionadas);
            return {args, selecionadas};
        },
        template: '<RelatorioMapasFiltros v-bind="args" v-model:unidadesSelecionadas="selecionadas" />',
    }),
};

export const ComUnidadesSelecionadas: Story = {
    args: {
        unidadesDisponiveis: unidadesMock,
        unidadesSelecionadas: [1, 2],
        carregando: false,
        temUnidadesSelecionadas: true,
    },
    render: (args) => ({
        components: {RelatorioMapasFiltros},
        setup() {
            const selecionadas = ref(args.unidadesSelecionadas);
            return {args, selecionadas};
        },
        template: '<RelatorioMapasFiltros v-bind="args" v-model:unidadesSelecionadas="selecionadas" />',
    }),
};

export const Carregando: Story = {
    args: {
        unidadesDisponiveis: unidadesMock,
        unidadesSelecionadas: [1],
        carregando: true,
        temUnidadesSelecionadas: true,
    },
    render: (args) => ({
        components: {RelatorioMapasFiltros},
        setup() {
            const selecionadas = ref(args.unidadesSelecionadas);
            return {args, selecionadas};
        },
        template: '<RelatorioMapasFiltros v-bind="args" v-model:unidadesSelecionadas="selecionadas" />',
    }),
};

export const SemUnidades: Story = {
    args: {
        unidadesDisponiveis: [],
        unidadesSelecionadas: [],
        carregando: false,
        temUnidadesSelecionadas: false,
    },
};

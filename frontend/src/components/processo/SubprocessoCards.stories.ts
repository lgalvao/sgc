import type {Meta, StoryObj} from '@storybook/vue3-vite';
import SubprocessoCards from './SubprocessoCards.vue';
import {TipoProcesso} from '@/types/tipos';

const meta: Meta<typeof SubprocessoCards> = {
    title: 'Processo/SubprocessoCards',
    component: SubprocessoCards,
    tags: ['autodocs'],
    decorators: [
        (story) => ({
            components: {story},
            template: '<div style="padding: 2rem; background-color: #f8f9fa;"><story /></div>',
        }),
    ],
};

export default meta;
type Story = StoryObj<typeof SubprocessoCards>;


const mockMapa = {
    codigo: 1,
    descricao: 'Mapa Teste',
    unidade: {codigo: 1, nome: 'Unidade Teste', sigla: 'TEST'},
    codProcesso: 456,
    competencias: [],
    situacao: 'CRIADO',
    dataCriacao: '2025-01-01T00:00:00Z'
};

export const MapeamentoGestor: Story = {
    args: {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        codSubprocesso: 123,
        codProcesso: 456,
        siglaUnidade: 'DITEC',
    },
};

export const MapeamentoVisualizador: Story = {
    args: {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        codSubprocesso: 123,
        codProcesso: 456,
        siglaUnidade: 'DITEC',
    },
};

export const MapeamentoSemMapa: Story = {
    args: {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        codSubprocesso: 123,
        codProcesso: 456,
        siglaUnidade: 'DITEC',
    },
};

export const Diagnostico: Story = {
    args: {
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: mockMapa,
        codSubprocesso: 123,
        codProcesso: 456,
        siglaUnidade: 'DITEC',
    },
};

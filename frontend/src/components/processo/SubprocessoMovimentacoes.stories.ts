import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import SubprocessoMovimentacoes from './SubprocessoMovimentacoes.vue';
import type {Movimentacao} from '@/types/tipos';

const meta: Meta<typeof SubprocessoMovimentacoes> = {
    title: 'Processo/SubprocessoMovimentacoes',
    component: SubprocessoMovimentacoes,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof SubprocessoMovimentacoes>;

const movimentacoesMock: Movimentacao[] = [
    {
        codigo: 1,
        dataHora: '2025-03-15T09:30:00',
        unidadeOrigemCodigo: 1,
        unidadeOrigemSigla: 'REIT',
        unidadeOrigemNome: 'Reitoria',
        unidadeDestinoCodigo: 2,
        unidadeDestinoSigla: 'PROEN',
        unidadeDestinoNome: 'Pró-Reitoria de Ensino',
        usuarioTitulo: '012345678',
        usuarioNome: 'Maria Aparecida de Souza',
        descricao: 'Processo enviado para cadastro',
    },
    {
        codigo: 2,
        dataHora: '2025-03-20T14:15:00',
        unidadeOrigemCodigo: 2,
        unidadeOrigemSigla: 'PROEN',
        unidadeOrigemNome: 'Pró-Reitoria de Ensino',
        unidadeDestinoCodigo: 1,
        unidadeDestinoSigla: 'REIT',
        unidadeDestinoNome: 'Reitoria',
        usuarioTitulo: '987654321',
        usuarioNome: 'João Carlos Pereira',
        descricao: 'Cadastro disponibilizado para análise',
    },
    {
        codigo: 3,
        dataHora: '2025-04-02T11:00:00',
        unidadeOrigemCodigo: 1,
        unidadeOrigemSigla: 'REIT',
        unidadeOrigemNome: 'Reitoria',
        unidadeDestinoCodigo: 2,
        unidadeDestinoSigla: 'PROEN',
        unidadeDestinoNome: 'Pró-Reitoria de Ensino',
        usuarioTitulo: '012345678',
        usuarioNome: 'Maria Aparecida de Souza',
        descricao: 'Cadastro homologado, mapa liberado para edição',
    },
];

export const Default: Story = {
    args: {
        movimentacoes: movimentacoesMock,
    },
    play: async () => {
        const tabela = page.getByTestId('tbl-movimentacoes');
        await expect.element(tabela).toBeVisible();
    },
};

export const Vazio: Story = {
    args: {
        movimentacoes: [],
    },
    play: async () => {
        const emptyState = page.getByTestId('empty-state-movimentacoes');
        await expect.element(emptyState).toBeVisible();
    },
};

export const ComUmaMovimentacao: Story = {
    args: {
        movimentacoes: [movimentacoesMock[0]!],
    },
};

export const ComMuitasMovimentacoes: Story = {
    args: {
        movimentacoes: [
            ...movimentacoesMock,
            ...movimentacoesMock.map(m => ({...m, codigo: m.codigo + 10})),
            ...movimentacoesMock.map(m => ({...m, codigo: m.codigo + 20})),
        ],
    },
};

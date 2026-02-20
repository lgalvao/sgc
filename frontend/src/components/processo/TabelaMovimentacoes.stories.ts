import type { Meta, StoryObj } from '@storybook/vue3';
import TabelaMovimentacoes from './TabelaMovimentacoes.vue';

const meta: Meta<typeof TabelaMovimentacoes> = {
  title: 'Processo/TabelaMovimentacoes',
  component: TabelaMovimentacoes,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof TabelaMovimentacoes>;

const mockMovimentacoes = [
  {
    codigo: 1,
    dataHora: '2025-02-20T09:00:00Z',
    unidadeOrigem: { sigla: 'PRES' },
    unidadeDestino: { sigla: 'DITEC' },
    descricao: 'Início do processo de mapeamento.',
    subprocesso: { situacao: 'EM_ANDAMENTO' },
  },
  {
    codigo: 2,
    dataHora: '2025-02-21T11:30:00Z',
    unidadeOrigem: { sigla: 'DITEC' },
    unidadeDestino: { sigla: 'PRES' },
    descricao: 'Mapeamento concluído e enviado para homologação.',
    subprocesso: { situacao: 'CONCLUIDO' },
  },
  {
    codigo: 3,
    dataHora: '2025-02-22T14:00:00Z',
    unidadeOrigem: { sigla: 'PRES' },
    unidadeDestino: null,
    descricao: 'Processo finalizado pelo administrador.',
    subprocesso: { situacao: 'HOMOLOGADO' },
  },
];

export const ComMovimentacoes: Story = {
  args: {
    movimentacoes: mockMovimentacoes,
  },
};

export const Vazia: Story = {
  args: {
    movimentacoes: [],
  },
};

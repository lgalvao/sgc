import type { Meta, StoryObj } from '@storybook/vue3-vite';
import TabelaAlertas from './TabelaAlertas.vue';

const meta: Meta<typeof TabelaAlertas> = {
  title: 'Processo/TabelaAlertas',
  component: TabelaAlertas,
  tags: ['autodocs'],
  argTypes: {
    onOrdenar: { action: 'ordenar' },
    onRecarregar: { action: 'recarregar' },
  },
};

export default meta;
type Story = StoryObj<typeof TabelaAlertas>;

const mockAlertas = [
  {
    codigo: 1,
    codProcesso: 101,
    unidadeOrigem: 'RH',
    unidadeDestino: 'DITEC',
    descricao: 'Novo processo iniciado',
    dataHora: '2025-02-20T10:00:00Z',
    mensagem: 'Novo processo de mapeamento iniciado para sua unidade.',
    processo: 'Mapeamento 2025',
    origem: 'Sistema',
    dataHoraLeitura: null, // Não lido (negrito)
  },
  {
    codigo: 2,
    codProcesso: 102,
    unidadeOrigem: 'RH',
    unidadeDestino: 'DITEC',
    descricao: 'Autoavaliação validada',
    dataHora: '2025-02-19T14:30:00Z',
    mensagem: 'Sua autoavaliação foi validada pelo gestor.',
    processo: 'Diagnóstico de Gaps',
    origem: 'Gestor Direto',
    dataHoraLeitura: '2025-02-19T15:00:00Z', // Lido
  },
  {
    codigo: 3,
    codProcesso: 103,
    unidadeOrigem: 'RH',
    unidadeDestino: 'DITEC',
    descricao: 'Lembrete de prazo',
    dataHora: '2025-02-18T09:00:00Z',
    mensagem: 'Prazo final para revisão de competências se aproxima.',
    processo: 'Revisão Semestral',
    origem: 'RH',
    dataHoraLeitura: null,
  },
];

export const ComAlertas: Story = {
  args: {
    alertas: mockAlertas,
  },
};

export const Vazia: Story = {
  args: {
    alertas: [],
  },
};

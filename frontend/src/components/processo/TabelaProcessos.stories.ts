import type {Meta, StoryObj} from '@storybook/vue3';
import TabelaProcessos from './TabelaProcessos.vue';
import {type ProcessoResumo, SituacaoProcesso, TipoProcesso} from '@/types/tipos';

const meta: Meta<typeof TabelaProcessos> = {
  title: 'Processo/TabelaProcessos',
  component: TabelaProcessos,
  tags: ['autodocs'],
  argTypes: {
    criterioOrdenacao: {
      control: 'select',
      options: ['descricao', 'tipo', 'situacao', 'dataFinalizacao'],
    },
    direcaoOrdenacaoAsc: { control: 'boolean' },
    showDataFinalizacao: { control: 'boolean' },
    compacto: { control: 'boolean' },
    mostrarCtaVazio: { control: 'boolean' },
    textoCtaVazio: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof TabelaProcessos>;

const mockProcessos: ProcessoResumo[] = [
  {
    codigo: 1,
    descricao: 'Processo de Mapeamento 2023',
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    tipo: TipoProcesso.MAPEAMENTO,
    dataLimite: '2023-12-31',
    dataCriacao: '2023-01-01',
    unidadeCodigo: 101,
    unidadeNome: 'Departamento de TI',
    unidadesParticipantes: 'TI, RH',
  },
  {
    codigo: 2,
    descricao: 'Revisão Anual',
    situacao: SituacaoProcesso.CRIADO,
    tipo: TipoProcesso.REVISAO,
    dataLimite: '2024-06-30',
    dataCriacao: '2024-01-15',
    unidadeCodigo: 102,
    unidadeNome: 'Recursos Humanos',
    unidadesParticipantes: 'RH',
  },
  {
    codigo: 3,
    descricao: 'Diagnóstico Geral',
    situacao: SituacaoProcesso.FINALIZADO,
    tipo: TipoProcesso.DIAGNOSTICO,
    dataLimite: '2022-12-31',
    dataCriacao: '2022-01-01',
    dataFinalizacao: '2022-12-20',
    unidadeCodigo: 100,
    unidadeNome: 'Diretoria',
    unidadesParticipantes: 'Todas',
  },
];

export const Default: Story = {
  args: {
    processos: mockProcessos,
    criterioOrdenacao: 'descricao',
    direcaoOrdenacaoAsc: true,
  },
};

export const ComDataFinalizacao: Story = {
  args: {
    processos: mockProcessos,
    criterioOrdenacao: 'dataFinalizacao',
    direcaoOrdenacaoAsc: false,
    showDataFinalizacao: true,
  },
};

export const Compacto: Story = {
  args: {
    processos: mockProcessos.slice(0, 2),
    criterioOrdenacao: 'descricao',
    direcaoOrdenacaoAsc: true,
    compacto: true,
  },
};

export const Vazio: Story = {
  args: {
    processos: [],
    criterioOrdenacao: 'descricao',
    direcaoOrdenacaoAsc: true,
    mostrarCtaVazio: true,
    textoCtaVazio: 'Criar novo processo',
  },
};

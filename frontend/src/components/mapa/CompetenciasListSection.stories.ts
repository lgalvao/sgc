import type { Meta, StoryObj } from '@storybook/vue3';
import CompetenciasListSection from './CompetenciasListSection.vue';
import { fn } from '@storybook/test';

const meta: Meta<typeof CompetenciasListSection> = {
  title: 'Mapa/CompetenciasListSection',
  component: CompetenciasListSection,
  tags: ['autodocs'],
  argTypes: {
    onCriar: { action: 'criar' },
    onEditar: { action: 'editar' },
    onExcluir: { action: 'excluir' },
    onRemoverAtividade: { action: 'remover-atividade' },
  },
};

export default meta;
type Story = StoryObj<typeof CompetenciasListSection>;

const mockUnidade = { codigo: 1, nome: 'Unidade Teste', sigla: 'TESTE' };
const mockAtividades = [
  { codigo: 101, descricao: 'Atividade 1' },
  { codigo: 102, descricao: 'Atividade 2' },
];
const mockCompetencias = [
  {
    codigo: 1,
    descricao: 'Competência A',
    atividades: [mockAtividades[0]],
  },
  {
    codigo: 2,
    descricao: 'Competência B',
    atividades: [mockAtividades[1]],
  },
];

export const Default: Story = {
  args: {
    unidade: mockUnidade,
    competencias: mockCompetencias,
    atividades: mockAtividades,
    podeEditar: true,
  },
};

export const SomenteLeitura: Story = {
  args: {
    unidade: mockUnidade,
    competencias: mockCompetencias,
    atividades: mockAtividades,
    podeEditar: false,
  },
};

export const Vazia: Story = {
  args: {
    unidade: mockUnidade,
    competencias: [],
    atividades: mockAtividades,
    podeEditar: true,
  },
};

export const UnidadeNaoEncontrada: Story = {
  args: {
    unidade: null,
    competencias: [],
    atividades: [],
    podeEditar: false,
  },
};

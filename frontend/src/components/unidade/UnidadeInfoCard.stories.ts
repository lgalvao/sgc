import type { Meta, StoryObj } from '@storybook/vue3';
import UnidadeInfoCard from './UnidadeInfoCard.vue';

const meta: Meta<typeof UnidadeInfoCard> = {
  title: 'Unidade/UnidadeInfoCard',
  component: UnidadeInfoCard,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof UnidadeInfoCard>;

const mockUnidade = {
  codigo: 1,
  nome: 'Diretoria de Tecnologia',
  sigla: 'DITEC',
  responsavel: { nome: 'Maria Responsável' },
};

const mockTitular = {
  nome: 'João Titular',
  ramal: '1234',
  email: 'joao.titular@empresa.com.br',
};

export const Default: Story = {
  args: {
    unidade: mockUnidade,
    titularDetalhes: mockTitular,
  },
};

export const SemEmail: Story = {
  args: {
    unidade: mockUnidade,
    titularDetalhes: {
      ...mockTitular,
      email: '',
    },
  },
};

export const SemInfo: Story = {
  args: {
    unidade: {
      codigo: 2,
      nome: 'Unidade de Teste',
      sigla: 'TESTE',
      responsavel: null,
    },
    titularDetalhes: null,
  },
};

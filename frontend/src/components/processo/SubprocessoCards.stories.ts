import type { Meta, StoryObj } from '@storybook/vue3';
import SubprocessoCards from './SubprocessoCards.vue';
import { TipoProcesso } from '@/types/tipos';

const meta: Meta<typeof SubprocessoCards> = {
  title: 'Processo/SubprocessoCards',
  component: SubprocessoCards,
  tags: ['autodocs'],
  decorators: [
    (story) => ({
      components: { story },
      template: '<div style="padding: 2rem; background-color: #f8f9fa;"><story /></div>',
    }),
  ],
};

export default meta;
type Story = StoryObj<typeof SubprocessoCards>;

const mockPermissoesFull = {
  podeEditarCadastro: true,
  podeVisualizarMapa: true,
  podeEditarMapa: true,
  podeVisualizarDiagnostico: true,
};

const mockMapa = { codigo: 1, descricao: 'Mapa Teste' };

export const MapeamentoGestor: Story = {
  args: {
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    mapa: mockMapa,
    permissoes: mockPermissoesFull,
    codSubprocesso: 123,
    codProcesso: 456,
    siglaUnidade: 'DITEC',
  },
};

export const MapeamentoVisualizador: Story = {
  args: {
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    mapa: mockMapa,
    permissoes: {
      podeEditarCadastro: false,
      podeVisualizarMapa: true,
      podeEditarMapa: false,
      podeVisualizarDiagnostico: false,
    },
    codSubprocesso: 123,
    codProcesso: 456,
    siglaUnidade: 'DITEC',
  },
};

export const MapeamentoSemMapa: Story = {
  args: {
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    mapa: null,
    permissoes: mockPermissoesFull,
    codSubprocesso: 123,
    codProcesso: 456,
    siglaUnidade: 'DITEC',
  },
};

export const Diagnostico: Story = {
  args: {
    tipoProcesso: TipoProcesso.DIAGNOSTICO,
    mapa: mockMapa,
    permissoes: mockPermissoesFull,
    codSubprocesso: 123,
    codProcesso: 456,
    siglaUnidade: 'DITEC',
  },
};

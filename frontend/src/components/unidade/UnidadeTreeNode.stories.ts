import type { Meta, StoryObj } from '@storybook/vue3';
import UnidadeTreeNode from './UnidadeTreeNode.vue';
import { fn } from '@storybook/test';

const meta: Meta<typeof UnidadeTreeNode> = {
  title: 'Unidade/UnidadeTreeNode',
  component: UnidadeTreeNode,
  tags: ['autodocs'],
  argTypes: {
    onToggle: { action: 'onToggle' },
    onToggleExpand: { action: 'onToggleExpand' },
  },
};

export default meta;
type Story = StoryObj<typeof UnidadeTreeNode>;

const mockUnidadeRaiz = {
  codigo: 1,
  nome: 'Presidência',
  sigla: 'PRES',
  isElegivel: true,
  filhas: [
    { codigo: 2, nome: 'Diretoria de Tecnologia', sigla: 'DITEC', isElegivel: true, filhas: [] },
    { codigo: 3, nome: 'Diretoria Administrativa', sigla: 'DIRAD', isElegivel: true, filhas: [] },
  ],
};

const mockFunctions = {
  isChecked: (codigo: number) => codigo === 2,
  getEstadoSelecao: (unidade: any) => unidade.codigo === 2 ? true : false,
  isExpanded: (unidade: any) => unidade.codigo === 1,
  isHabilitado: (unidade: any) => true,
  onToggle: fn(),
  onToggleExpand: fn(),
};

export const ModoSelecao: Story = {
  args: {
    unidade: mockUnidadeRaiz,
    ...mockFunctions,
    modoSelecao: true,
  },
};

export const ModoNavegacao: Story = {
  args: {
    unidade: mockUnidadeRaiz,
    ...mockFunctions,
    modoSelecao: false,
  },
};

export const Desabilitado: Story = {
  args: {
    unidade: { ...mockUnidadeRaiz, isElegivel: false },
    ...mockFunctions,
    isHabilitado: () => false,
  },
};

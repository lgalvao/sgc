import type { Meta, StoryObj } from '@storybook/vue3-vite';
import TreeTable from './TreeTable.vue';

const meta: Meta<typeof TreeTable> = {
  title: 'Comum/TreeTable',
  component: TreeTable,
  tags: ['autodocs'],
  argTypes: {
    'onRow-click': { action: 'row-click' },
  },
};

export default meta;
type Story = StoryObj<typeof TreeTable>;

const mockColumns = [
  { key: 'nome', label: 'Nome', width: '40%' },
  { key: 'tipo', label: 'Tipo', width: '30%' },
  { key: 'status', label: 'Status', width: '30%' },
];

const mockData = [
  {
    codigo: 1,
    nome: 'Projeto Principal',
    tipo: 'Categoria',
    status: 'Ativo',
    expanded: true,
    children: [
      {
        codigo: 2,
        nome: 'Subprojeto A',
        tipo: 'Tarefa',
        status: 'Em Andamento',
        children: [
          {
            codigo: 3,
            nome: 'Tarefa 1.1',
            tipo: 'Subtarefa',
            status: 'Concluído',
          },
        ],
      },
      {
        codigo: 4,
        nome: 'Subprojeto B',
        tipo: 'Tarefa',
        status: 'Pendente',
      },
    ],
  },
  {
    codigo: 5,
    nome: 'Projeto Secundário',
    tipo: 'Categoria',
    status: 'Inativo',
  },
];

export const Default: Story = {
  args: {
    title: 'Hierarquia de Tarefas',
    columns: mockColumns,
    data: mockData,
  },
};

export const SemCabecalho: Story = {
  args: {
    columns: mockColumns,
    data: mockData,
    hideHeaders: true,
  },
};

export const Vazio: Story = {
  args: {
    columns: mockColumns,
    data: [],
    emptyTitle: 'Nenhum projeto encontrado',
    emptyDescription: 'Crie um novo projeto para começar a visualizar a hierarquia.',
  },
};

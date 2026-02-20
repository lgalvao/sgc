import type {Meta, StoryObj} from '@storybook/vue3';
import EmptyState from './EmptyState.vue';
import {BButton} from 'bootstrap-vue-next';

const meta: Meta<typeof EmptyState> = {
  title: 'Comum/EmptyState',
  component: EmptyState,
  tags: ['autodocs'],
  argTypes: {
    title: { control: 'text' },
    description: { control: 'text' },
    icon: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof EmptyState>;

export const Default: Story = {
  args: {
    title: 'Nenhum item encontrado',
    description: 'Tente ajustar os filtros de busca.',
    icon: 'bi-search',
  },
};

export const WithIcon: Story = {
  args: {
    icon: 'bi-inbox',
    title: 'Caixa de entrada vazia',
  },
};

export const WithDescription: Story = {
  args: {
    title: 'Sem resultados',
    description: 'Não encontramos nada com os termos pesquisados.',
  },
};

export const FullContent: Story = {
  args: {
    title: 'Bem-vindo',
    description: 'Comece adicionando um novo item.',
    icon: 'bi-plus-circle',
  },
  render: (args) => ({
    components: { EmptyState, BButton },
    setup() {
      return { args };
    },
    template: `
      <EmptyState v-bind="args">
        <BButton variant="primary">Adicionar Novo</BButton>
      </EmptyState>
    `,
  }),
};

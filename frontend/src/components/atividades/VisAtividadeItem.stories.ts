import type { Meta, StoryObj } from '@storybook/vue3-vite';
import VisAtividadeItem from './VisAtividadeItem.vue';

const meta: Meta<typeof VisAtividadeItem> = {
  title: 'Atividades/VisAtividadeItem',
  component: VisAtividadeItem,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof VisAtividadeItem>;

const mockAtividade = {
  codigo: 1,
  descricao: 'Desenvolver interfaces ricas em Vue.js',
  conhecimentos: [
    { codigo: 10, descricao: 'Composition API' },
    { codigo: 11, descricao: 'Vue Router' },
    { codigo: 12, descricao: 'Pinia' },
  ],
};

export const Default: Story = {
  args: {
    atividade: mockAtividade,
  },
};

export const Longa: Story = {
  args: {
    atividade: {
      codigo: 2,
      descricao: 'Descrição de atividade longa para testar como o texto se comporta em múltiplas linhas e se a quebra de palavra está funcionando corretamente no componente visual.',
      conhecimentos: [
        { codigo: 13, descricao: 'Conhecimento que também pode ter uma descrição bem extensa' },
      ],
    },
  },
};

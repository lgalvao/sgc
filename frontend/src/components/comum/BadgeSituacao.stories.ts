import type { Meta, StoryObj } from '@storybook/vue3';
import BadgeSituacao from './BadgeSituacao.vue';
import { SituacaoProcesso } from '@/types/tipos';

const meta: Meta<typeof BadgeSituacao> = {
  title: 'Comum/BadgeSituacao',
  component: BadgeSituacao,
  tags: ['autodocs'],
  argTypes: {
    situacao: {
      control: 'select',
      options: Object.values(SituacaoProcesso),
    },
    texto: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof BadgeSituacao>;

export const Criado: Story = {
  args: {
    situacao: SituacaoProcesso.CRIADO,
  },
};

export const EmAndamento: Story = {
  args: {
    situacao: SituacaoProcesso.EM_ANDAMENTO,
  },
};

export const Finalizado: Story = {
  args: {
    situacao: SituacaoProcesso.FINALIZADO,
  },
};

export const CustomText: Story = {
  args: {
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    texto: 'Texto Personalizado',
  },
};

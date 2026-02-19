import type { Meta, StoryObj } from '@storybook/vue3';
import ErrorAlert from './ErrorAlert.vue';

const meta: Meta<typeof ErrorAlert> = {
  title: 'Comum/ErrorAlert',
  component: ErrorAlert,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['danger', 'warning', 'info'],
    },
    error: { control: 'object' },
  },
};

export default meta;
type Story = StoryObj<typeof ErrorAlert>;

export const Default: Story = {
  args: {
    error: { message: 'Ocorreu um erro inesperado.' },
    variant: 'danger',
  },
};

export const Warning: Story = {
  args: {
    error: { message: 'Atenção: Ação irreversível.' },
    variant: 'warning',
  },
};

export const Info: Story = {
  args: {
    error: { message: 'Informação: Processo iniciado.' },
    variant: 'info',
  },
};

export const WithDetails: Story = {
  args: {
    error: {
      message: 'Falha na validação',
      details: 'O campo "Nome" é obrigatório.',
    },
    variant: 'danger',
  },
};

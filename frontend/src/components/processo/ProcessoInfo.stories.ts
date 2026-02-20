import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ProcessoInfo from './ProcessoInfo.vue';

const meta: Meta<typeof ProcessoInfo> = {
  title: 'Processo/ProcessoInfo',
  component: ProcessoInfo,
  tags: ['autodocs'],
  argTypes: {
    tipo: { control: 'text' },
    situacao: { control: 'text' },
    dataLimite: { control: 'date' },
    numUnidades: { control: 'number' },
  },
};

export default meta;
type Story = StoryObj<typeof ProcessoInfo>;

export const Default: Story = {
  args: {
    tipo: 'DIAGNOSTICO',
    situacao: 'EM_ANDAMENTO',
    dataLimite: '2025-12-31T23:59:59Z',
    numUnidades: 15,
    showUnidades: true,
  },
};

export const Completo: Story = {
  args: {
    tipo: 'MAPEAMENTO',
    situacao: 'CONCLUIDO',
    dataLimite: '2024-06-30T18:00:00Z',
    numUnidades: 42,
    showUnidades: true,
  },
};

export const Simples: Story = {
  args: {
    tipo: 'GAP',
    situacao: 'PENDENTE',
    showDataLimite: false,
    showUnidades: false,
  },
};

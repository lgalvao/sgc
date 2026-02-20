import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ProcessoAcoes from './ProcessoAcoes.vue';

const meta: Meta<typeof ProcessoAcoes> = {
  title: 'Processo/ProcessoAcoes',
  component: ProcessoAcoes,
  tags: ['autodocs'],
  argTypes: {
    onAceitarBloco: { action: 'aceitarBloco' },
    onHomologarBloco: { action: 'homologarBloco' },
    onFinalizar: { action: 'finalizar' },
  },
};

export default meta;
type Story = StoryObj<typeof ProcessoAcoes>;

export const TodasAcoes: Story = {
  args: {
    podeAceitarBloco: true,
    podeHomologarBloco: true,
    podeFinalizar: true,
  },
};

export const SomenteAceite: Story = {
  args: {
    podeAceitarBloco: true,
    podeHomologarBloco: false,
    podeFinalizar: false,
  },
};

export const SomenteFinalizar: Story = {
  args: {
    podeAceitarBloco: false,
    podeHomologarBloco: false,
    podeFinalizar: true,
  },
};

export const SemAcoes: Story = {
  args: {
    podeAceitarBloco: false,
    podeHomologarBloco: false,
    podeFinalizar: false,
  },
};

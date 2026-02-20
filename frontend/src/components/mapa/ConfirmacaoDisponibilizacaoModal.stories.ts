import type { Meta, StoryObj } from '@storybook/vue3';
import ConfirmacaoDisponibilizacaoModal from './ConfirmacaoDisponibilizacaoModal.vue';
import { ref } from 'vue';

const meta: Meta<typeof ConfirmacaoDisponibilizacaoModal> = {
  title: 'Mapa/ConfirmacaoDisponibilizacaoModal',
  component: ConfirmacaoDisponibilizacaoModal,
  tags: ['autodocs'],
  argTypes: {
    onFechar: { action: 'fechar' },
    onConfirmar: { action: 'confirmar' },
  },
};

export default meta;
type Story = StoryObj<typeof ConfirmacaoDisponibilizacaoModal>;

export const Cadastro: Story = {
  args: {
    mostrar: true,
    isRevisao: false,
  },
  render: (args) => ({
    components: { ConfirmacaoDisponibilizacaoModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Disponibilizar Cadastro</button>
        <ConfirmacaoDisponibilizacaoModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
  }),
};

export const Revisao: Story = {
  args: {
    mostrar: true,
    isRevisao: true,
  },
  render: (args) => ({
    components: { ConfirmacaoDisponibilizacaoModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Disponibilizar Revisão</button>
        <ConfirmacaoDisponibilizacaoModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
  }),
};

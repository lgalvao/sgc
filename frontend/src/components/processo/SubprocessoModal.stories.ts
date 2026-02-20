import type { Meta, StoryObj } from '@storybook/vue3-vite';
import SubprocessoModal from './SubprocessoModal.vue';
import { ref } from 'vue';

const meta: Meta<typeof SubprocessoModal> = {
  title: 'Processo/SubprocessoModal',
  component: SubprocessoModal,
  tags: ['autodocs'],
  argTypes: {
    onFecharModal: { action: 'fecharModal' },
    onConfirmarAlteracao: { action: 'confirmarAlteracao' },
  },
};

export default meta;
type Story = StoryObj<typeof SubprocessoModal>;

const dateToday = new Date();
const dateFuture = new Date();
dateFuture.setDate(dateToday.getDate() + 30);

export const Default: Story = {
  args: {
    mostrarModal: true,
    dataLimiteAtual: dateFuture,
    etapaAtual: 1,
    loading: false,
  },
  render: (args) => ({
    components: { SubprocessoModal },
    setup() {
      const show = ref(args.mostrarModal);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Alterar Data Limite</button>
        <SubprocessoModal v-bind="args" :mostrarModal="show" @fecharModal="show = false" />
      </div>
    `,
  }),
};

export const Carregando: Story = {
  args: {
    mostrarModal: true,
    dataLimiteAtual: dateFuture,
    etapaAtual: 1,
    loading: true,
  },
};

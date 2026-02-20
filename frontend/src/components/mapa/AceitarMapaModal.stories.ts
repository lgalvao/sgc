import type { Meta, StoryObj } from '@storybook/vue3';
import AceitarMapaModal from './AceitarMapaModal.vue';
import { ref } from 'vue';

const meta: Meta<typeof AceitarMapaModal> = {
  title: 'Mapa/AceitarMapaModal',
  component: AceitarMapaModal,
  tags: ['autodocs'],
  argTypes: {
    onFecharModal: { action: 'fecharModal' },
    onConfirmarAceitacao: { action: 'confirmarAceitacao' },
  },
};

export default meta;
type Story = StoryObj<typeof AceitarMapaModal>;

export const Gestor: Story = {
  args: {
    mostrarModal: true,
    perfil: 'GESTOR',
    loading: false,
  },
  render: (args) => ({
    components: { AceitarMapaModal },
    setup() {
      const show = ref(args.mostrarModal);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Aceitar Mapa (Gestor)</button>
        <AceitarMapaModal v-bind="args" :mostrarModal="show" @fecharModal="show = false" />
      </div>
    `,
  }),
};

export const Admin: Story = {
  args: {
    mostrarModal: true,
    perfil: 'ADMIN',
    loading: false,
  },
  render: (args) => ({
    components: { AceitarMapaModal },
    setup() {
      const show = ref(args.mostrarModal);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-success" @click="show = true">Homologar Mapa (Admin)</button>
        <AceitarMapaModal v-bind="args" :mostrarModal="show" @fecharModal="show = false" />
      </div>
    `,
  }),
};

export const Carregando: Story = {
  args: {
    mostrarModal: true,
    perfil: 'GESTOR',
    loading: true,
  },
};

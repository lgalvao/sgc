import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ModalPadrao from './ModalPadrao.vue';
import { ref } from 'vue';

const meta: Meta<typeof ModalPadrao> = {
  title: 'Comum/ModalPadrao',
  component: ModalPadrao,
  tags: ['autodocs'],
  argTypes: {
    tamanho: { control: 'select', options: ['sm', 'md', 'lg', 'xl'] },
    variantAcao: { control: 'select', options: ['primary', 'secondary', 'success', 'danger'] },
    'onUpdate:modelValue': { action: 'update:modelValue' },
    onFechar: { action: 'fechar' },
    onConfirmar: { action: 'confirmar' },
  },
};

export default meta;
type Story = StoryObj<typeof ModalPadrao>;

export const Default: Story = {
  args: {
    modelValue: true,
    titulo: 'Título do Modal',
    textoAcao: 'Confirmar',
  },
  render: (args) => ({
    components: { ModalPadrao },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Abrir Modal</button>
        <ModalPadrao v-bind="args" v-model="show">
          <p>Conteúdo interno do modal. Você pode colocar qualquer elemento aqui.</p>
        </ModalPadrao>
      </div>
    `,
  }),
};

export const Carregando: Story = {
  args: {
    modelValue: true,
    titulo: 'Salvando Alterações',
    loading: true,
    textoAcaoCarregando: 'Salvando...',
  },
  render: (args) => ({
    components: { ModalPadrao },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <ModalPadrao v-bind="args" v-model="show">
        <p>Aguarde enquanto os dados estão sendo processados.</p>
      </ModalPadrao>
    `,
  }),
};

export const Grande: Story = {
  args: {
    modelValue: true,
    titulo: 'Visualização Detalhada',
    tamanho: 'lg',
  },
  render: (args) => ({
    components: { ModalPadrao },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <ModalPadrao v-bind="args" v-model="show">
        <div style="height: 300px; display: flex; align-items: center; justify-content: center; background-color: #f8f9fa;">
          Área de conteúdo grande
        </div>
      </ModalPadrao>
    `,
  }),
};

export const Perigo: Story = {
  args: {
    modelValue: true,
    titulo: 'Excluir Item',
    textoAcao: 'Excluir',
    variantAcao: 'danger',
  },
  render: (args) => ({
    components: { ModalPadrao },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <ModalPadrao v-bind="args" v-model="show">
        <p class="text-danger fw-bold">Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita.</p>
      </ModalPadrao>
    `,
  }),
};

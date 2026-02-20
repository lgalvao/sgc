import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ModalMapasVigentes from './ModalMapasVigentes.vue';
import { ref } from 'vue';

const meta: Meta<typeof ModalMapasVigentes> = {
  title: 'Relatorios/ModalMapasVigentes',
  component: ModalMapasVigentes,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ModalMapasVigentes>;

const mockMapas = [
  { codigo: 1, unidade: 'Presidência', competencias: [1, 2, 3] },
  { codigo: 2, unidade: 'Diretoria de Tecnologia', competencias: [1, 2, 3, 4, 5, 6, 7] },
  { codigo: 3, unidade: 'Diretoria Administrativa', competencias: [1, 2] },
  { codigo: 4, unidade: 'Coordenação de Sistemas', competencias: [1, 2, 3, 4] },
];

export const Default: Story = {
  args: {
    modelValue: true,
    mapas: mockMapas,
  },
  render: (args) => ({
    components: { ModalMapasVigentes },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Mapas Vigentes</button>
        <ModalMapasVigentes v-bind="args" v-model="show" />
      </div>
    `,
  }),
};

export const Vazio: Story = {
  args: {
    modelValue: true,
    mapas: [],
  },
  render: (args) => ({
    components: { ModalMapasVigentes },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <ModalMapasVigentes v-bind="args" v-model="show" />
    `,
  }),
};

import type { Meta, StoryObj } from '@storybook/vue3';
import ArvoreUnidades from './ArvoreUnidades.vue';
import type { Unidade } from '@/types/tipos';
import { ref } from 'vue';

const meta: Meta<typeof ArvoreUnidades> = {
  title: 'Unidade/ArvoreUnidades',
  component: ArvoreUnidades,
  tags: ['autodocs'],
  argTypes: {
    modoSelecao: { control: 'boolean' },
    ocultarRaiz: { control: 'boolean' },
  },
};

export default meta;
type Story = StoryObj<typeof ArvoreUnidades>;

const mockUnidades: Unidade[] = [
  {
    codigo: 1,
    nome: 'Presidência',
    sigla: 'PRES',
    isElegivel: false,
    filhas: [
      {
        codigo: 2,
        nome: 'Diretoria de Tecnologia',
        sigla: 'DITEC',
        isElegivel: true,
        filhas: [
          {
            codigo: 3,
            nome: 'Coordenação de Desenvolvimento',
            sigla: 'CODES',
            isElegivel: true,
          },
          {
            codigo: 4,
            nome: 'Coordenação de Infraestrutura',
            sigla: 'COINF',
            isElegivel: true,
          },
        ],
      },
      {
        codigo: 5,
        nome: 'Diretoria Administrativa',
        sigla: 'DIRAD',
        isElegivel: true,
        filhas: [],
      },
    ],
  },
];

export const Default: Story = {
  args: {
    unidades: mockUnidades,
    modelValue: [],
    modoSelecao: true,
    ocultarRaiz: false,
  },
  render: (args) => ({
    components: { ArvoreUnidades },
    setup() {
      const selected = ref(args.modelValue);
      return { args, selected };
    },
    template: `
      <div>
        <ArvoreUnidades v-bind="args" v-model="selected" />
        <div class="mt-3">
          <strong>Selecionados IDs:</strong> {{ selected }}
        </div>
      </div>
    `,
  }),
};

export const ComPreSelecao: Story = {
  args: {
    unidades: mockUnidades,
    modelValue: [3, 5],
    modoSelecao: true,
    ocultarRaiz: false,
  },
  render: (args) => ({
    components: { ArvoreUnidades },
    setup() {
      const selected = ref(args.modelValue);
      return { args, selected };
    },
    template: `
      <div>
        <ArvoreUnidades v-bind="args" v-model="selected" />
        <div class="mt-3">
          <strong>Selecionados IDs:</strong> {{ selected }}
        </div>
      </div>
    `,
  }),
};

export const ApenasVisualizacao: Story = {
  args: {
    unidades: mockUnidades,
    modelValue: [2, 3, 4], // DITEC and children
    modoSelecao: false,
    ocultarRaiz: false,
  },
};

export const OcultandoRaiz: Story = {
  args: {
    unidades: mockUnidades,
    modelValue: [],
    modoSelecao: true,
    ocultarRaiz: true,
  },
  render: (args) => ({
    components: { ArvoreUnidades },
    setup() {
      const selected = ref(args.modelValue);
      return { args, selected };
    },
    template: `
      <div>
        <p class="text-muted">A raiz "Presidência" deve estar oculta, mostrando apenas seus filhos diretos.</p>
        <ArvoreUnidades v-bind="args" v-model="selected" />
        <div class="mt-3">
          <strong>Selecionados IDs:</strong> {{ selected }}
        </div>
      </div>
    `,
  }),
};

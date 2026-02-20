import type { Meta, StoryObj } from '@storybook/vue3';
import ProcessoFormFields from './ProcessoFormFields.vue';
import { TipoProcesso } from '@/types/tipos';
import { ref } from 'vue';

const meta: Meta<typeof ProcessoFormFields> = {
  title: 'Processo/ProcessoFormFields',
  component: ProcessoFormFields,
  tags: ['autodocs'],
  argTypes: {
    'onUpdate:modelValue': { action: 'update:modelValue' },
  },
};

export default meta;
type Story = StoryObj<typeof ProcessoFormFields>;

const mockUnidades = [
  {
    codigo: 1,
    nome: 'Presidência',
    sigla: 'PRES',
    filhas: [
      { codigo: 2, nome: 'Diretoria de Tecnologia', sigla: 'DITEC' },
      { codigo: 3, nome: 'Diretoria Administrativa', sigla: 'DIRAD' },
    ],
  },
];

const initialData = {
  descricao: '',
  tipo: null,
  unidadesSelecionadas: [],
  dataLimite: '',
};

export const NovoProcesso: Story = {
  args: {
    modelValue: initialData,
    fieldErrors: {},
    unidades: mockUnidades,
    isLoadingUnidades: false,
  },
  render: (args) => ({
    components: { ProcessoFormFields },
    setup() {
      const form = ref(args.modelValue);
      return { args, form };
    },
    template: '<ProcessoFormFields v-bind="args" v-model="form" />',
  }),
};

export const Preenchido: Story = {
  args: {
    modelValue: {
      descricao: 'Mapeamento Anual de Competências 2025',
      tipo: TipoProcesso.MAPEAMENTO,
      unidadesSelecionadas: [2],
      dataLimite: '2025-12-31',
    },
    fieldErrors: {},
    unidades: mockUnidades,
    isLoadingUnidades: false,
  },
};

export const ComErros: Story = {
  args: {
    modelValue: initialData,
    fieldErrors: {
      descricao: 'A descrição é obrigatória.',
      tipo: 'Selecione um tipo de processo.',
      unidades: 'Selecione pelo menos uma unidade.',
      dataLimite: 'A data limite é inválida.',
    },
    unidades: mockUnidades,
    isLoadingUnidades: false,
  },
};

export const CarregandoUnidades: Story = {
  args: {
    modelValue: initialData,
    fieldErrors: {},
    unidades: [],
    isLoadingUnidades: true,
  },
};

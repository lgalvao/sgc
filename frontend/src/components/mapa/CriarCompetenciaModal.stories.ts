import type { Meta, StoryObj } from '@storybook/vue3';
import CriarCompetenciaModal from './CriarCompetenciaModal.vue';
import { ref } from 'vue';

const meta: Meta<typeof CriarCompetenciaModal> = {
  title: 'Mapa/CriarCompetenciaModal',
  component: CriarCompetenciaModal,
  tags: ['autodocs'],
  argTypes: {
    onFechar: { action: 'fechar' },
    onSalvar: { action: 'salvar' },
  },
};

export default meta;
type Story = StoryObj<typeof CriarCompetenciaModal>;

const mockAtividades = [
  {
    codigo: 1,
    descricao: 'Atividade A',
    conhecimentos: [{ codigo: 10, descricao: 'Conhecimento 1' }],
  },
  {
    codigo: 2,
    descricao: 'Atividade B',
    conhecimentos: [],
  },
  {
    codigo: 3,
    descricao: 'Atividade C',
    conhecimentos: [
      { codigo: 11, descricao: 'C1' },
      { codigo: 12, descricao: 'C2' },
    ],
  },
];

export const Criacao: Story = {
  args: {
    mostrar: true,
    atividades: mockAtividades,
    competenciaParaEditar: null,
  },
  render: (args) => ({
    components: { CriarCompetenciaModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Criar Competência</button>
        <CriarCompetenciaModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
  }),
};

export const Edicao: Story = {
  args: {
    mostrar: true,
    atividades: mockAtividades,
    competenciaParaEditar: {
      codigo: 1,
      descricao: 'Competência Existente',
      atividadesAssociadas: [1, 3],
    },
  },
  render: (args) => ({
    components: { CriarCompetenciaModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: '<CriarCompetenciaModal v-bind="args" :mostrar="show" @fechar="show = false" />',
  }),
};

export const ComErros: Story = {
  args: {
    mostrar: true,
    atividades: mockAtividades,
    fieldErrors: {
      descricao: 'A descrição da competência é obrigatória.',
      atividades: 'Selecione pelo menos uma atividade para esta competência.',
      generic: 'Ocorreu um erro ao salvar a competência. Tente novamente.',
    },
  },
  render: (args) => ({
    components: { CriarCompetenciaModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: '<CriarCompetenciaModal v-bind="args" :mostrar="show" @fechar="show = false" />',
  }),
};

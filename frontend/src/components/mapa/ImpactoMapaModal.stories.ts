import type { Meta, StoryObj } from '@storybook/vue3';
import ImpactoMapaModal from './ImpactoMapaModal.vue';
import { TipoImpactoCompetencia } from '@/types/tipos';
import { ref } from 'vue';

const meta: Meta<typeof ImpactoMapaModal> = {
  title: 'Mapa/ImpactoMapaModal',
  component: ImpactoMapaModal,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ImpactoMapaModal>;

const mockImpacto = {
  temImpactos: true,
  atividadesInseridas: [
    { codigo: 1, descricao: 'Nova Atividade de Teste', competenciasVinculadas: ['Competência A'] },
  ],
  atividadesRemovidas: [
    { codigo: 2, descricao: 'Atividade que foi removida' },
  ],
  atividadesAlteradas: [
    { codigo: 3, descricao: 'Atividade Alterada', descricaoAnterior: 'Atividade Anterior' },
  ],
  competenciasImpactadas: [
    {
      codigo: 10,
      descricao: 'Competência A',
      atividadesAfetadas: ['Nova Atividade de Teste'],
      tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_ALTERADA],
    },
    {
      codigo: 11,
      descricao: 'Competência B',
      atividadesAfetadas: ['Atividade que foi removida'],
      tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_REMOVIDA],
    },
  ],
};

export const ComImpactos: Story = {
  args: {
    mostrar: true,
    impacto: mockImpacto,
    loading: false,
  },
  render: (args) => ({
    components: { ImpactoMapaModal },
    setup() {
      const show = ref(args.mostrar);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Impactos</button>
        <ImpactoMapaModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
  }),
};

export const SemImpactos: Story = {
  args: {
    mostrar: true,
    impacto: {
      temImpactos: false,
      atividadesInseridas: [],
      atividadesRemovidas: [],
      atividadesAlteradas: [],
      competenciasImpactadas: [],
    },
    loading: false,
  },
};

export const Carregando: Story = {
  args: {
    mostrar: true,
    impacto: null,
    loading: true,
  },
};

export const Erro: Story = {
  args: {
    mostrar: true,
    impacto: null,
    loading: false,
  },
};

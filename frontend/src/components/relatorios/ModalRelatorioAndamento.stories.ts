import type { Meta, StoryObj } from '@storybook/vue3';
import ModalRelatorioAndamento from './ModalRelatorioAndamento.vue';
import { ref } from 'vue';

const meta: Meta<typeof ModalRelatorioAndamento> = {
  title: 'Relatorios/ModalRelatorioAndamento',
  component: ModalRelatorioAndamento,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ModalRelatorioAndamento>;

const mockProcessos = [
  {
    codigo: 1,
    descricao: 'Mapeamento Anual 2025',
    tipo: 'MAPEAMENTO',
    situacao: 'EM_ANDAMENTO',
    dataLimite: new Date('2025-12-31'),
    unidadeNome: 'Geral',
  },
  {
    codigo: 2,
    descricao: 'Revisão Semestral TI',
    tipo: 'REVISAO',
    situacao: 'CONCLUIDO',
    dataLimite: new Date('2025-06-30'),
    unidadeNome: 'DITEC',
  },
  {
    codigo: 3,
    descricao: 'Diagnóstico de Gaps Administrativos',
    tipo: 'DIAGNOSTICO',
    situacao: 'PENDENTE',
    dataLimite: new Date('2025-08-15'),
    unidadeNome: 'DIRAD',
  },
];

export const Default: Story = {
  args: {
    modelValue: true,
    processos: mockProcessos,
  },
  render: (args) => ({
    components: { ModalRelatorioAndamento },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Andamento Geral</button>
        <ModalRelatorioAndamento v-bind="args" v-model="show" />
      </div>
    `,
  }),
};

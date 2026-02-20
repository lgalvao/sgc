import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ModalDiagnosticosGaps from './ModalDiagnosticosGaps.vue';
import { ref } from 'vue';

const meta: Meta<typeof ModalDiagnosticosGaps> = {
  title: 'Relatorios/ModalDiagnosticosGaps',
  component: ModalDiagnosticosGaps,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ModalDiagnosticosGaps>;

const mockDiagnosticos = [
  {
    codigo: 1,
    processo: 'Diagnóstico Anual 2024',
    unidade: 'Diretoria de Tecnologia',
    gaps: 5,
    importanciaMedia: 4.5,
    dominioMedio: 2.1,
    competenciasCriticas: ['Arquitetura de Sistemas', 'Segurança da Informação'],
    data: new Date('2024-11-15T10:00:00Z'),
    status: 'Finalizado',
  },
  {
    codigo: 2,
    processo: 'Diagnóstico Anual 2024',
    unidade: 'Diretoria Administrativa',
    gaps: 2,
    importanciaMedia: 3.8,
    dominioMedio: 3.5,
    competenciasCriticas: ['Gestão de Contratos'],
    data: new Date('2024-11-20T14:30:00Z'),
    status: 'Em análise',
  },
  {
    codigo: 3,
    processo: 'Diagnóstico Especial TI',
    unidade: 'Coordenação de Infra',
    gaps: 8,
    importanciaMedia: 4.9,
    dominioMedio: 1.5,
    competenciasCriticas: ['Cloud Computing', 'Kubernetes', 'DevOps'],
    data: new Date('2024-12-05T09:00:00Z'),
    status: 'Pendente',
  },
];

export const Default: Story = {
  args: {
    modelValue: true,
    diagnosticos: mockDiagnosticos,
  },
  render: (args) => ({
    components: { ModalDiagnosticosGaps },
    setup() {
      const show = ref(args.modelValue);
      return { args, show };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Diagnósticos de Gaps</button>
        <ModalDiagnosticosGaps v-bind="args" v-model="show" />
      </div>
    `,
  }),
};

import type { Meta, StoryObj } from '@storybook/vue3';
import RelatorioFiltrosSection from './RelatorioFiltrosSection.vue';
import { ref } from 'vue';

const meta: Meta<typeof RelatorioFiltrosSection> = {
  title: 'Relatorios/RelatorioFiltrosSection',
  component: RelatorioFiltrosSection,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof RelatorioFiltrosSection>;

export const Default: Story = {
  args: {
    tipo: '',
    dataInicio: '',
    dataFim: '',
  },
  render: (args) => ({
    components: { RelatorioFiltrosSection },
    setup() {
      const tipo = ref(args.tipo);
      const dataInicio = ref(args.dataInicio);
      const dataFim = ref(args.dataFim);
      return { args, tipo, dataInicio, dataFim };
    },
    template: `
      <div class="p-3 border rounded bg-light">
        <RelatorioFiltrosSection 
          v-bind="args" 
          v-model:tipo="tipo"
          v-model:dataInicio="dataInicio"
          v-model:dataFim="dataFim"
        />
        <div class="mt-3 small text-muted">
          <strong>Filtros Ativos:</strong> 
          Tipo: {{ tipo || 'Todos' }} | 
          Início: {{ dataInicio || 'Não definido' }} | 
          Fim: {{ dataFim || 'Não definido' }}
        </div>
      </div>
    `,
  }),
};

export const Preenchido: Story = {
  args: {
    tipo: 'MAPEAMENTO',
    dataInicio: '2025-01-01',
    dataFim: '2025-12-31',
  },
};

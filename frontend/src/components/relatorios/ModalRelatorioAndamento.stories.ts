import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ModalRelatorioAndamento from './ModalRelatorioAndamento.vue';
import {SituacaoProcesso, TipoProcesso} from '@/types/tipos';
import {ref} from 'vue';

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
        tipo: TipoProcesso.MAPEAMENTO,
        situacao: SituacaoProcesso.EM_ANDAMENTO,
        dataLimite: '2025-12-31',
        dataCriacao: '2025-01-01',
        unidadeCodigo: 1,
        unidadeNome: 'Geral',
    },
    {
        codigo: 2,
        descricao: 'Revisão Semestral TI',
        tipo: TipoProcesso.REVISAO,
        situacao: SituacaoProcesso.FINALIZADO,
        dataLimite: '2025-06-30',
        dataCriacao: '2025-01-01',
        unidadeCodigo: 2,
        unidadeNome: 'DITEC',
    },
    {
        codigo: 3,
        descricao: 'Diagnóstico de Gaps Administrativos',
        tipo: TipoProcesso.DIAGNOSTICO,
        situacao: SituacaoProcesso.CRIADO,
        dataLimite: '2025-08-15',
        dataCriacao: '2025-01-01',
        unidadeCodigo: 3,
        unidadeNome: 'DIRAD',
    },
];

export const Default: Story = {
    args: {
        modelValue: true,
        processos: mockProcessos,
    },
    render: (args) => ({
        components: {ModalRelatorioAndamento},
        setup() {
            const show = ref(args.modelValue);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Andamento Geral</button>
        <ModalRelatorioAndamento v-bind="args" v-model="show" />
      </div>
    `,
    }),
};

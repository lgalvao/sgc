import type {Meta, StoryObj} from '@storybook/vue3-vite';
import HistoricoAnaliseModal from './HistoricoAnaliseModal.vue';
import {ref} from 'vue';

const meta: Meta<typeof HistoricoAnaliseModal> = {
    title: 'Processo/HistoricoAnaliseModal',
    component: HistoricoAnaliseModal,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof HistoricoAnaliseModal>;

const mockHistorico = [
    {
        dataHora: '2025-02-20T10:00:00Z',
        unidadeSigla: 'PRES',
        unidadeNome: 'Presidência',
        acao: 'Aceite',
        analistaUsuarioTitulo: 'Maria Administradora',
        observacoes: 'Cadastro revisado e aceito.',
        motivo: '',
        tipo: 'CADASTRO'
    },
    {
        dataHora: '2025-02-18T14:30:00Z',
        unidadeSigla: 'DITEC',
        unidadeNome: 'Diretoria de Tecnologia',
        acao: 'Envio',
        analistaUsuarioTitulo: 'João Gestor',
        observacoes: 'Enviado para análise inicial.',
        motivo: '',
        tipo: 'CADASTRO'
    },
];

export const Default: Story = {
    args: {
        mostrar: true,
        historico: mockHistorico,
        loading: false,
    },
    render: (args) => ({
        components: {HistoricoAnaliseModal},
        setup() {
            const show = ref(args.mostrar);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Ver Histórico de Análise</button>
        <HistoricoAnaliseModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
    }),
};

export const Vazio: Story = {
    args: {
        mostrar: true,
        historico: [],
        loading: false,
    },
};

export const Carregando: Story = {
    args: {
        mostrar: true,
        historico: [],
        loading: true,
    },
};

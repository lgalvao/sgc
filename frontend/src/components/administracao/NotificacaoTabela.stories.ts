import type {Meta, StoryObj} from '@storybook/vue3-vite';
import NotificacaoTabela from './NotificacaoTabela.vue';
import type {Notificacao} from '@/services/notificacaoService';

const meta: Meta<typeof NotificacaoTabela> = {
    title: 'Administracao/NotificacaoTabela',
    component: NotificacaoTabela,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof NotificacaoTabela>;

const notificacoesMock: Notificacao[] = [
    {
        codigo: 1,
        destinatario: 'maria.souza@ifce.edu.br',
        tipoNotificacao: 'INICIO_CADASTRO',
        assunto: '[SGC] Cadastro de Atividades - Mapeamento de Competências 2025',
        situacao: 'ENVIADO',
        dataHoraCriacao: '2025-03-15T09:00:00',
        dataHoraEnvio: '2025-03-15T09:30:00',
        tentativas: 1,
        corpoHtml: '<p>Conteúdo do e-mail.</p>',
        subprocessoCodigo: 101,
        unidadeSigla: 'PROEN',
        processoFinalizado: false,
        notificacaoFinalizacaoProcesso: false,
    },
    {
        codigo: 2,
        destinatario: 'joao.pereira@ifce.edu.br',
        tipoNotificacao: 'LEMBRETE_PRAZO',
        assunto: '[SGC] Lembrete - Prazo se encerrando em 3 dias',
        situacao: 'FALHA_DEFINITIVA',
        dataHoraCriacao: '2025-03-28T08:00:00',
        tentativas: 3,
        unidadeSigla: 'CF',
        processoFinalizado: false,
        notificacaoFinalizacaoProcesso: false,
    },
    {
        codigo: 3,
        destinatario: 'ana.rodrigues@ifce.edu.br',
        tipoNotificacao: 'CADASTRO_HOMOLOGADO',
        assunto: '[SGC] Cadastro Homologado - Pró-Reitoria de Extensão',
        situacao: 'ENVIADO',
        dataHoraCriacao: '2025-04-02T11:00:00',
        dataHoraEnvio: '2025-04-02T11:00:00',
        tentativas: 1,
        corpoHtml: '<p>Seu cadastro foi homologado.</p>',
        subprocessoCodigo: 103,
        unidadeSigla: 'PROEX',
        processoFinalizado: false,
        notificacaoFinalizacaoProcesso: false,
    },
];

export const Default: Story = {
    args: {
        items: notificacoesMock,
    },
};

export const Vazio: Story = {
    args: {
        items: [],
    },
};

export const ComFalhaDefinitiva: Story = {
    args: {
        items: notificacoesMock.filter(n => n.situacao === 'FALHA_DEFINITIVA'),
    },
};

export const ComPreviewDisponivel: Story = {
    args: {
        items: notificacoesMock.filter(n => n.corpoHtml !== null),
    },
};

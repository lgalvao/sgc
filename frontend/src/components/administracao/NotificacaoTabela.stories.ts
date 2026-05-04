import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from '@vitest/browser/context';
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
    },
];

export const Default: Story = {
    args: {
        items: notificacoesMock,
    },
    play: async () => {
        const tabela = page.getByTestId('tbl-notificacoes');
        await expect.element(tabela).toBeVisible();
    },
};

export const Vazio: Story = {
    args: {
        items: [],
    },
    play: async () => {
        const emptyAlert = page.getByTestId('alert-notificacoes-sem-registros');
        await expect.element(emptyAlert).toBeVisible();
    },
};

export const ComFalhaDefinitiva: Story = {
    args: {
        items: notificacoesMock.filter(n => n.situacao === 'FALHA_DEFINITIVA'),
    },
    play: async () => {
        const btnReenviar = page.getByTestId('btn-notificacoes-reenviar-2');
        await expect.element(btnReenviar).toBeVisible();
    },
};

export const ComPreviewDisponivel: Story = {
    args: {
        items: notificacoesMock.filter(n => n.corpoHtml !== null),
    },
    play: async () => {
        const btnPreview = page.getByTestId('btn-preview-1');
        await expect.element(btnPreview).toBeVisible();
    },
};

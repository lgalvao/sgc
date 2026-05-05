import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page, userEvent} from 'vitest/browser';
import AppAlert from './AppAlert.vue';

const meta: Meta<typeof AppAlert> = {
    title: 'Comum/AppAlert',
    component: AppAlert,
    tags: ['autodocs'],
    argTypes: {
        variante: {
            control: 'select',
            options: ['danger', 'warning', 'success', 'info'],
        },
        dispensavel: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof AppAlert>;

export const MensagemSimples: Story = {
    args: {
        mensagem: 'Ocorreu um erro ao processar a solicitação.',
        variante: 'danger',
        dispensavel: true,
    },
};

export const Aviso: Story = {
    args: {
        mensagem: 'O prazo de cadastro está próximo do vencimento.',
        variante: 'warning',
        dispensavel: true,
    },
};

export const Sucesso: Story = {
    args: {
        mensagem: 'Processo cadastrado com sucesso.',
        variante: 'success',
        dispensavel: true,
    },
};

export const Informativo: Story = {
    args: {
        mensagem: 'Os dados serão sincronizados automaticamente.',
        variante: 'info',
        dispensavel: false,
    },
};

export const ComNotificacaoEstruturada: Story = {
    args: {
        notificacao: {
            resumo: 'Não foi possível disponibilizar o cadastro.',
            detalhes: [
                'A unidade PROEN não possui responsável cadastrado.',
                'A competência "Gestão de Projetos" está incompleta.',
                'O prazo de entrega foi excedido.',
            ],
        },
        variante: 'danger',
        dispensavel: true,
    },
    play: async () => {
        const botaoDetalhes = page.getByRole('button', {name: /mostrar detalhes/i});
        await expect.element(botaoDetalhes).toBeVisible();

        await userEvent.click(botaoDetalhes);

        const lista = page.getByRole('list');
        await expect.element(lista).toBeVisible();
    },
};

export const ComNotificacaoSemDetalhes: Story = {
    args: {
        notificacao: {
            resumo: 'Operação realizada com restrições.',
            detalhes: [],
        },
        variante: 'warning',
        dispensavel: true,
    },
};

export const ComStackTrace: Story = {
    tags: ['visual-only'],
    args: {
        mensagem: 'Erro interno do servidor.',
        variante: 'danger',
        dispensavel: true,
        stackTrace: 'Error: NullPointerException\n  at SubprocessoService.java:145\n  at ProcessoController.java:87',
    },
};

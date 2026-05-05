import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from 'vitest/browser';
import CarregamentoPagina from './CarregamentoPagina.vue';

const meta: Meta<typeof CarregamentoPagina> = {
    title: 'Comum/CarregamentoPagina',
    component: CarregamentoPagina,
    tags: ['autodocs'],
    argTypes: {
        mensagem: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof CarregamentoPagina>;

export const Default: Story = {
    play: async () => {
        const spinner = page.getByTestId('pagina-carregando');
        await expect.element(spinner).toBeVisible();
    },
};

export const ComMensagemPersonalizada: Story = {
    args: {
        mensagem: 'Carregando mapa de competências...',
    },
    play: async () => {
        await expect.element(page.getByText('Carregando mapa de competências...', { exact: true }).last()).toBeVisible();
    },
};

export const CarregandoProcessos: Story = {
    args: {
        mensagem: 'Carregando processos da unidade...',
    },
};

export const CarregandoSubprocesso: Story = {
    args: {
        mensagem: 'Carregando dados do subprocesso...',
    },
};

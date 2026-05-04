import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page} from '@vitest/browser/context';
import FeedbackModal from './FeedbackModal.vue';

const meta: Meta<typeof FeedbackModal> = {
    title: 'Feedback/FeedbackModal',
    component: FeedbackModal,
    tags: ['autodocs'],
    argTypes: {
        visivel: {control: 'boolean'},
        enviando: {control: 'boolean'},
    },
};

export default meta;
type Story = StoryObj<typeof FeedbackModal>;

export const Aberto: Story = {
    args: {
        visivel: true,
        captura: null,
        enviando: false,
    },
    play: async () => {
        const modal = page.getByTestId('feedback-modal');
        await expect.element(modal).toBeVisible();
        const titulo = page.getByTestId('feedback-modal-title');
        await expect.element(titulo).toHaveTextContent('Enviar feedback');
    },
};

export const Enviando: Story = {
    args: {
        visivel: true,
        captura: null,
        enviando: true,
    },
    play: async () => {
        const btnEnviar = page.getByTestId('feedback-btn-enviar');
        await expect.element(btnEnviar).toBeDisabled();
        const btnCancelar = page.getByTestId('feedback-btn-cancelar');
        await expect.element(btnCancelar).toBeDisabled();
    },
};

export const Fechado: Story = {
    args: {
        visivel: false,
        captura: null,
        enviando: false,
    },
    tags: ['visual-only'],
};

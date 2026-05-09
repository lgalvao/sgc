import type {Meta, StoryObj} from '@storybook/vue3-vite';
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
};

export const Enviando: Story = {
    args: {
        visivel: true,
        captura: null,
        enviando: true,
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

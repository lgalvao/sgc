import type {Meta, StoryObj} from '@storybook/vue3-vite';
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

export const Default: Story = {};

export const ComMensagemPersonalizada: Story = {
    args: {
        mensagem: 'Carregando mapa de competências...',
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

import type {Meta, StoryObj} from '@storybook/vue3-vite';
import FormErrorAlert from './FormErrorAlert.vue';

const meta: Meta<typeof FormErrorAlert> = {
    title: 'Comum/FormErrorAlert',
    component: FormErrorAlert,
    tags: ['autodocs'],
    argTypes: {
        variant: {
            control: 'select',
            options: ['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark']
        },
        'onUpdate:show': {action: 'update:show'},
    },
};

export default meta;
type Story = StoryObj<typeof FormErrorAlert>;

export const Default: Story = {
    args: {
        show: true,
        variant: 'danger',
        title: 'Erro de Validação',
        body: 'Por favor, corrija os seguintes erros antes de prosseguir:',
        errors: [
            'O campo nome é obrigatório.',
            'O e-mail informado não é válido.',
            'A senha deve conter pelo menos uma letra maiúscula.',
        ],
    },
};

export const ComErroTecnico: Story = {
    args: {
        show: true,
        variant: 'danger',
        title: 'Erro Inesperado',
        body: 'Ocorreu um erro ao processar sua solicitação no servidor.',
        stackTrace: `Error: Internal Server Error
  at processRequest (server.js:123:45)
  at async handle (handler.js:67:89)`,
    },
};

export const AlertaSimples: Story = {
    args: {
        show: true,
        variant: 'warning',
        body: 'Sua sessão expirará em breve. Por favor, salve seu trabalho.',
    },
};

export const Sucesso: Story = {
    args: {
        show: true,
        variant: 'success',
        title: 'Sucesso!',
        body: 'Seus dados foram salvos com sucesso.',
    },
};

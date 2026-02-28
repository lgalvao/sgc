import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ModalMapaDisponibilizar from './ModalMapaDisponibilizar.vue';
import {ref} from 'vue';

const meta: Meta<typeof ModalMapaDisponibilizar> = {
    title: 'Mapa/ModalMapaDisponibilizar',
    component: ModalMapaDisponibilizar,
    tags: ['autodocs'],
    argTypes: {
        onFechar: {action: 'fechar'},
        onDisponibilizar: {action: 'disponibilizar'},
    },
};

export default meta;
type Story = StoryObj<typeof ModalMapaDisponibilizar>;

export const Default: Story = {
    args: {
        mostrar: true,
        notificacao: 'O mapa será disponibilizado para as unidades superiores.',
        loading: false,
    },
    render: (args) => ({
        components: {ModalMapaDisponibilizar},
        setup() {
            const show = ref(args.mostrar);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Disponibilizar Mapa</button>
        <ModalMapaDisponibilizar v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
    }),
};

export const ComErros: Story = {
    args: {
        mostrar: true,
        fieldErrors: {
            dataLimite: 'A data limite é obrigatória.',
            observacoes: 'As observações excederam o limite de caracteres.',
            generic: 'Erro ao processar a solicitação.',
        },
    },
    render: (args) => ({
        components: {ModalMapaDisponibilizar},
        setup() {
            const show = ref(args.mostrar);
            return {args, show};
        },
        template: '<ModalMapaDisponibilizar v-bind="args" :mostrar="show" @fechar="show = false" />',
    }),
};

export const Carregando: Story = {
    args: {
        mostrar: true,
        loading: true,
    },
};

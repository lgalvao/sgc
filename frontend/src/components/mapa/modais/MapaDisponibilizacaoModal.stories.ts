import type {Meta, StoryObj} from '@storybook/vue3-vite';
import MapaDisponibilizacaoModal from './MapaDisponibilizacaoModal.vue';
import {ref} from 'vue';

const meta: Meta<typeof MapaDisponibilizacaoModal> = {
    title: 'Mapa/Modais/MapaDisponibilizacaoModal',
    component: MapaDisponibilizacaoModal,
    tags: ['autodocs'],
    argTypes: {
        onFechar: {action: 'fechar'},
        onDisponibilizar: {action: 'disponibilizar'},
    },
};

export default meta;
type Story = StoryObj<typeof MapaDisponibilizacaoModal>;

export const Default: Story = {
    args: {
        mostrar: true,
        notificacao: 'O mapa será disponibilizado para as unidades superiores.',
        loading: false,
    },
    render: (args) => ({
        components: {MapaDisponibilizacaoModal},
        setup() {
            const show = ref(args.mostrar);
            return {args, show};
        },
        template: `
          <div>
            <button class="btn btn-primary" @click="show = true">Disponibilizar mapa</button>
            <MapaDisponibilizacaoModal v-bind="args" :mostrar="show" @fechar="show = false"/>
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
        components: {MapaDisponibilizacaoModal},
        setup() {
            const show = ref(args.mostrar);
            return {args, show};
        },
        template: '<MapaDisponibilizacaoModal v-bind="args" :mostrar="show" @fechar="show = false" />',
    }),
};

export const Carregando: Story = {
    args: {
        mostrar: true,
        loading: true,
    },
};

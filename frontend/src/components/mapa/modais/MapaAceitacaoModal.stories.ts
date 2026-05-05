import type {Meta, StoryObj} from '@storybook/vue3-vite';
import MapaAceitacaoModal from './MapaAceitacaoModal.vue';
import {ref} from 'vue';

const meta: Meta<typeof MapaAceitacaoModal> = {
    title: 'Mapa/Modais/MapaAceitacaoModal',
    component: MapaAceitacaoModal,
    tags: ['autodocs'],
    argTypes: {
        onFecharModal: {action: 'fecharModal'},
        onConfirmarAceitacao: {action: 'confirmarAceitacao'},
    },
};

export default meta;
type Story = StoryObj<typeof MapaAceitacaoModal>;

export const Gestor: Story = {
    args: {
        mostrarModal: true,
        homologacao: false,
        loading: false,
    },
    render: (args) => ({
        components: {MapaAceitacaoModal},
        setup() {
            const show = ref(args.mostrarModal);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Aceitar mapa (Gestor)</button>
        <MapaAceitacaoModal v-bind="args" :mostrarModal="show" @fecharModal="show = false" />
      </div>
    `,
    }),
};

export const Admin: Story = {
    args: {
        mostrarModal: true,
        homologacao: true,
        loading: false,
    },
    render: (args) => ({
        components: {MapaAceitacaoModal},
        setup() {
            const show = ref(args.mostrarModal);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-success" @click="show = true">Homologar mapa (Admin)</button>
        <MapaAceitacaoModal v-bind="args" :mostrarModal="show" @fecharModal="show = false" />
      </div>
    `,
    }),
};

export const Carregando: Story = {
    args: {
        mostrarModal: true,
        homologacao: false,
        loading: true,
    },
};

import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import EditorTextoRico from './EditorTextoRico.vue';

const meta: Meta<typeof EditorTextoRico> = {
    title: 'Comum/EditorTextoRico',
    component: EditorTextoRico,
    tags: ['autodocs'],
    argTypes: {
        desabilitado: {control: 'boolean'},
        rotulo: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof EditorTextoRico>;

export const Default: Story = {
    args: {
        modelValue: '',
        rotulo: 'Descrição',
    },
    render: (args) => ({
        components: {EditorTextoRico},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<EditorTextoRico v-bind="args" v-model="valor" style="height: 300px;" />',
    }),
};

export const ComConteudoInicial: Story = {
    args: {
        modelValue: '<p>Texto inicial do editor com <strong>negrito</strong> e <em>itálico</em>.</p>',
        rotulo: 'Observações',
    },
    render: (args) => ({
        components: {EditorTextoRico},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<EditorTextoRico v-bind="args" v-model="valor" style="height: 300px;" />',
    }),
};

export const ComListaOrdenada: Story = {
    args: {
        modelValue: '<ol><li>Definir competências organizacionais</li><li>Mapear atividades por unidade</li><li>Validar com gestores</li></ol>',
        rotulo: 'Plano de ação',
    },
    render: (args) => ({
        components: {EditorTextoRico},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<EditorTextoRico v-bind="args" v-model="valor" style="height: 300px;" />',
    }),
};

export const Desabilitado: Story = {
    args: {
        modelValue: '<p>Este conteúdo não pode ser editado neste momento.</p>',
        rotulo: 'Conteúdo somente leitura',
        desabilitado: true,
    },
    render: (args) => ({
        components: {EditorTextoRico},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<EditorTextoRico v-bind="args" v-model="valor" style="height: 300px;" />',
    }),
};

export const VazioEditavel: Story = {
    args: {
        modelValue: '',
        rotulo: 'Escreva sua descrição',
        desabilitado: false,
    },
    render: (args) => ({
        components: {EditorTextoRico},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<EditorTextoRico v-bind="args" v-model="valor" style="height: 300px;" />',
    }),
};

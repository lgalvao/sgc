import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import InputData from './InputData.vue';

const meta: Meta<typeof InputData> = {
    title: 'Comum/InputData',
    component: InputData,
    tags: ['autodocs'],
    argTypes: {
        state: {
            control: 'select',
            options: [null, true, false],
        },
    },
};

export default meta;
type Story = StoryObj<typeof InputData>;

export const Default: Story = {
    args: {
        modelValue: '',
        id: 'data-exemplo',
    },
    render: (args) => ({
        components: {InputData},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<InputData v-bind="args" v-model="valor" />',
    }),
};

export const ComValor: Story = {
    args: {
        modelValue: '2025-06-30',
        id: 'data-prazo',
    },
    render: (args) => ({
        components: {InputData},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<InputData v-bind="args" v-model="valor" />',
    }),
};

export const ComEstadoInvalido: Story = {
    args: {
        modelValue: '',
        id: 'data-invalida',
        state: false,
    },
    render: (args) => ({
        components: {InputData},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<InputData v-bind="args" v-model="valor" />',
    }),
};

export const ComEstadoValido: Story = {
    args: {
        modelValue: '2025-12-31',
        id: 'data-valida',
        state: true,
    },
    render: (args) => ({
        components: {InputData},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<InputData v-bind="args" v-model="valor" />',
    }),
};

export const ComLimiteDatas: Story = {
    args: {
        modelValue: '',
        id: 'data-com-limites',
        min: '2025-01-01',
        max: '2025-12-31',
        required: true,
    },
    render: (args) => ({
        components: {InputData},
        setup() {
            const valor = ref(args.modelValue);
            return {args, valor};
        },
        template: '<InputData v-bind="args" v-model="valor" />',
    }),
};

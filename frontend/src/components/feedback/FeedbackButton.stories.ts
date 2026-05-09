import type {Meta, StoryObj} from '@storybook/vue3-vite';
import FeedbackButton from './FeedbackButton.vue';

const meta: Meta<typeof FeedbackButton> = {
    title: 'Feedback/FeedbackButton',
    component: FeedbackButton,
    tags: ['autodocs'],
    argTypes: {
        estado: {
            control: 'select',
            options: ['normal', 'carregando', 'sucesso', 'erro'],
        },
    },
};

export default meta;
type Story = StoryObj<typeof FeedbackButton>;

export const Normal: Story = {
    args: {
        estado: 'normal',
    },
    render: (args) => ({
        components: {FeedbackButton},
        setup() {
            return {args};
        },
        template: '<div style="position: relative; height: 120px;"><FeedbackButton v-bind="args" /></div>',
    }),
};

export const Carregando: Story = {
    args: {
        estado: 'carregando',
    },
    render: (args) => ({
        components: {FeedbackButton},
        setup() {
            return {args};
        },
        template: '<div style="position: relative; height: 120px;"><FeedbackButton v-bind="args" /></div>',
    }),
};

export const Sucesso: Story = {
    args: {
        estado: 'sucesso',
    },
    render: (args) => ({
        components: {FeedbackButton},
        setup() {
            return {args};
        },
        template: '<div style="position: relative; height: 120px;"><FeedbackButton v-bind="args" /></div>',
    }),
};

export const Erro: Story = {
    args: {
        estado: 'erro',
    },
    render: (args) => ({
        components: {FeedbackButton},
        setup() {
            return {args};
        },
        template: '<div style="position: relative; height: 120px;"><FeedbackButton v-bind="args" /></div>',
    }),
};

export const AoClicar: Story = {
    args: {
        estado: 'normal',
    },
    render: (args) => ({
        components: {FeedbackButton},
        setup() {
            return {args, clicks: 0};
        },
        template: '<div style="position: relative; height: 120px;"><FeedbackButton v-bind="args" @click="clicks++" /></div>',
    }),
};

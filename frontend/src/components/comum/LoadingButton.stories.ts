import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect} from 'vitest';
import {page, userEvent} from '@vitest/browser/context';
import LoadingButton from './LoadingButton.vue';

const meta: Meta<typeof LoadingButton> = {
    title: 'Comum/LoadingButton',
    component: LoadingButton,
    tags: ['autodocs'],
    argTypes: {
        loading: {control: 'boolean'},
        disabled: {control: 'boolean'},
        icon: {control: 'text'},
        text: {control: 'text'},
        loadingText: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof LoadingButton>;

export const Default: Story = {
    args: {
        text: 'Clique aqui',
    },
    play: async () => {
        const button = page.getByRole('button');

        await userEvent.click(button);
    },
};

export const Loading: Story = {
    args: {
        text: 'Clique aqui',
        loading: true,
        loadingText: 'Carregando...',
    },
    play: async () => {
        const button = page.getByRole('button');

        await expect.element(button).toHaveTextContent('Carregando...');
        await expect.element(button).toBeDisabled();
    },
};

export const Disabled: Story = {
    args: {
        text: 'Desabilitado',
        disabled: true,
    },
    play: async () => {
        const button = page.getByRole('button');
        await expect.element(button).toBeDisabled();
    },
};

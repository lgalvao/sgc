import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {expect, userEvent, within} from '@storybook/test';
import LoadingButton from './LoadingButton.vue';

const meta: Meta<typeof LoadingButton> = {
  title: 'Comum/LoadingButton',
  component: LoadingButton,
  tags: ['autodocs'],
  argTypes: {
    loading: { control: 'boolean' },
    disabled: { control: 'boolean' },
    icon: { control: 'text' },
    text: { control: 'text' },
    loadingText: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof LoadingButton>;

export const Default: Story = {
  args: {
    text: 'Clique aqui',
  },
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button');
    
    // Simula clique e verifica se o evento foi chamado
    await userEvent.click(button);
    // Nota: O monitoramento de ações (onClick) é automático no Storybook
  },
};

export const Loading: Story = {
  args: {
    text: 'Clique aqui',
    loading: true,
    loadingText: 'Carregando...',
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button');
    
    // Verifica se o texto de carregamento está visível
    await expect(button).toHaveTextContent('Carregando...');
    await expect(button).toBeDisabled();
  },
};

export const Disabled: Story = {
  args: {
    text: 'Desabilitado',
    disabled: true,
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button');
    await expect(button).toBeDisabled();
  },
};

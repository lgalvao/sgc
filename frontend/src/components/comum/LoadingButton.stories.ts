import type {Meta, StoryObj} from '@storybook/vue3-vite';
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
    loading: false,
    disabled: false,
  },
};

export const Loading: Story = {
  args: {
    text: 'Clique aqui',
    loading: true,
    loadingText: 'Carregando...',
  },
};

export const Disabled: Story = {
  args: {
    text: 'Desabilitado',
    disabled: true,
  },
};

export const WithIcon: Story = {
  args: {
    text: 'Salvar',
    icon: 'save',
  },
};

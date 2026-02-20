import type {Meta, StoryObj} from '@storybook/vue3';
import CampoTexto from './CampoTexto.vue';
import {ref} from 'vue';

const meta: Meta<typeof CampoTexto> = {
  title: 'Comum/CampoTexto',
  component: CampoTexto,
  tags: ['autodocs'],
  argTypes: {
    label: { control: 'text' },
    placeholder: { control: 'text' },
    obrigatorio: { control: 'boolean' },
    disabled: { control: 'boolean' },
    erro: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof CampoTexto>;

export const Default: Story = {
  args: {
    id: 'campo-texto-default',
    label: 'Nome',
    placeholder: 'Digite seu nome',
    modelValue: '',
  },
  render: (args) => ({
    components: { CampoTexto },
    setup() {
      const valor = ref(args.modelValue);
      return { args, valor };
    },
    template: `
      <CampoTexto
        v-bind="args"
        v-model="valor"
      />
      <div class="mt-2 text-muted small">Valor atual: {{ valor }}</div>
    `,
  }),
};

export const Required: Story = {
  args: {
    id: 'campo-texto-required',
    label: 'Email',
    obrigatorio: true,
    modelValue: '',
  },
};

export const Disabled: Story = {
  args: {
    id: 'campo-texto-disabled',
    label: 'Código',
    modelValue: '12345',
    disabled: true,
  },
};

export const WithError: Story = {
  args: {
    id: 'campo-texto-error',
    label: 'Senha',
    modelValue: '123',
    erro: 'A senha deve ter no mínimo 8 caracteres.',
  },
};

import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ModalConfirmacao from './ModalConfirmacao.vue';
import {BButton} from 'bootstrap-vue-next';
import {ref} from 'vue';

const meta: Meta<typeof ModalConfirmacao> = {
  title: 'Comum/ModalConfirmacao',
  component: ModalConfirmacao,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['primary', 'danger', 'warning', 'success', 'info'],
    },
    okTitle: { control: 'text' },
    cancelTitle: { control: 'text' },
    loading: { control: 'boolean' },
    titulo: { control: 'text' },
    mensagem: { control: 'text' },
  },
};

export default meta;
type Story = StoryObj<typeof ModalConfirmacao>;

const Template = (args: any) => ({
  components: { ModalConfirmacao, BButton },
  setup() {
    const show = ref(false);
    return { args, show };
  },
  template: `
    <div>
      <BButton @click="show = true">Abrir Modal</BButton>
      <ModalConfirmacao
        v-bind="args"
        v-model="show"
        @confirmar="() => { show = false; }"
      />
    </div>
  `,
});

export const Default: Story = {
  render: Template,
  args: {
    titulo: 'Confirmação',
    mensagem: 'Tem certeza que deseja realizar esta ação?',
    modelValue: false,
  },
};

export const Danger: Story = {
  render: Template,
  args: {
    titulo: 'Excluir Item',
    mensagem: 'Esta ação não poderá ser desfeita.',
    variant: 'danger',
    okTitle: 'Sim, excluir',
    modelValue: false,
  },
};

export const Loading: Story = {
  render: Template,
  args: {
    titulo: 'Processando...',
    mensagem: 'Aguarde enquanto processamos sua solicitação.',
    loading: true,
    okTitle: 'Confirmar',
    modelValue: false,
  },
};

export const CustomTitles: Story = {
  render: Template,
  args: {
    titulo: 'Enviar Proposta?',
    mensagem: 'Sua proposta será enviada para análise.',
    okTitle: 'Enviar Agora',
    cancelTitle: 'Revisar',
    variant: 'success',
    modelValue: false,
  },
};

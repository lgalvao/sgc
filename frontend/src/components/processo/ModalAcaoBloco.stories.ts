import type { Meta, StoryObj } from '@storybook/vue3-vite';
import ModalAcaoBloco from './ModalAcaoBloco.vue';
import { ref } from 'vue';

const meta: Meta<typeof ModalAcaoBloco> = {
  title: 'Processo/ModalAcaoBloco',
  component: ModalAcaoBloco,
  tags: ['autodocs'],
  argTypes: {
    onConfirmar: { action: 'confirmar' },
  },
};

export default meta;
type Story = StoryObj<typeof ModalAcaoBloco>;

const mockUnidades = [
  { codigo: 1, sigla: 'DITEC', nome: 'Diretoria de Tecnologia', situacao: 'EM_ANDAMENTO' },
  { codigo: 2, sigla: 'DIRAD', nome: 'Diretoria Administrativa', situacao: 'EM_ANDAMENTO' },
  { codigo: 3, sigla: 'PRES', nome: 'Presidência', situacao: 'PENDENTE' },
  { codigo: 4, sigla: 'CODES', nome: 'Coordenação de Sistemas', situacao: 'EM_ANDAMENTO' },
];

export const AceiteEmBloco: Story = {
  args: {
    id: 'modal-aceite-bloco',
    titulo: 'Aceitar Unidades em Bloco',
    texto: 'Selecione as unidades que deseja aceitar o mapeamento de competências.',
    rotuloBotao: 'Aceitar Selecionadas',
    unidades: mockUnidades,
    unidadesPreSelecionadas: [1, 2],
    mostrarDataLimite: false,
  },
  render: (args) => ({
    components: { ModalAcaoBloco },
    setup() {
      const modalRef = ref<any>(null);
      const abrir = () => modalRef.value?.abrir();
      return { args, modalRef, abrir };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="abrir">Abrir Aceite em Bloco</button>
        <ModalAcaoBloco v-bind="args" ref="modalRef" />
      </div>
    `,
  }),
};

export const AlterarDataLimiteEmBloco: Story = {
  args: {
    id: 'modal-data-bloco',
    titulo: 'Alterar Data Limite em Bloco',
    texto: 'Selecione as unidades para as quais deseja alterar a data limite do processo.',
    rotuloBotao: 'Alterar Datas',
    unidades: mockUnidades,
    unidadesPreSelecionadas: [],
    mostrarDataLimite: true,
  },
  render: (args) => ({
    components: { ModalAcaoBloco },
    setup() {
      const modalRef = ref<any>(null);
      const abrir = () => modalRef.value?.abrir();
      return { args, modalRef, abrir };
    },
    template: `
      <div>
        <button class="btn btn-warning" @click="abrir">Alterar Datas em Bloco</button>
        <ModalAcaoBloco v-bind="args" ref="modalRef" />
      </div>
    `,
  }),
};

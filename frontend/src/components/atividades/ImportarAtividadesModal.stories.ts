import type { Meta, StoryObj } from '@storybook/vue3';
import ImportarAtividadesModal from './ImportarAtividadesModal.vue';
import { createTestingPinia } from '@pinia/testing';
import { ref } from 'vue';
import { fn } from '@storybook/test';

const meta: Meta<typeof ImportarAtividadesModal> = {
  title: 'Atividades/ImportarAtividadesModal',
  component: ImportarAtividadesModal,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ImportarAtividadesModal>;

const mockProcessos = [
  { codigo: 1, descricao: 'Mapeamento 2024' },
  { codigo: 2, descricao: 'Revisão 2023' },
];

const mockUnidades = [
  { codUnidade: 10, sigla: 'DITEC', codSubprocesso: 100 },
  { codUnidade: 11, sigla: 'DIRAD', codSubprocesso: 101 },
];

const mockAtividades = [
  { codigo: 50, descricao: 'Atividade Importada 1' },
  { codigo: 51, descricao: 'Atividade Importada 2' },
];

export const Default: Story = {
  args: {
    mostrar: true,
    codSubprocessoDestino: 200,
  },
  render: (args) => ({
    components: { ImportarAtividadesModal },
    setup() {
      const show = ref(args.mostrar);
      const pinia = createTestingPinia({
        createSpy: fn,
        initialState: {
          processos: {
            processosFinalizados: mockProcessos,
            processoDetalhe: { unidades: mockUnidades },
          },
          atividades: {
            atividadesPorSubprocesso: {
              100: mockAtividades,
            },
          },
        },
      });
      return { args, show, pinia };
    },
    template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Importar Atividades</button>
        <ImportarAtividadesModal v-bind="args" :mostrar="show" @fechar="show = false" />
      </div>
    `,
  }),
};

export const SemProcessos: Story = {
  args: {
    mostrar: true,
    codSubprocessoDestino: 200,
  },
  render: (args) => ({
    components: { ImportarAtividadesModal },
    setup() {
      const show = ref(args.mostrar);
      const pinia = createTestingPinia({
        createSpy: fn,
        initialState: {
          processos: { processosFinalizados: [] },
        },
      });
      return { args, show, pinia };
    },
    template: '<ImportarAtividadesModal v-bind="args" :mostrar="show" @fechar="show = false" />',
  }),
};

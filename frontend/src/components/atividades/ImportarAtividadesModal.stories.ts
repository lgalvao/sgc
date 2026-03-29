import {vi} from "vitest";
import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ImportarAtividadesModal from './ImportarAtividadesModal.vue';
import {ref} from 'vue';
import * as processoService from '@/services/processoService';
import * as subprocessoService from '@/services/subprocessoService';

const meta: Meta<typeof ImportarAtividadesModal> = {
    title: 'Atividades/ImportarAtividadesModal',
    component: ImportarAtividadesModal,
    tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ImportarAtividadesModal>;

const mockProcessos = [
    {codigo: 1, descricao: 'Mapeamento 2024'},
    {codigo: 2, descricao: 'Revisão 2023'},
];

const mockUnidades = [
    {codUnidade: 10, sigla: 'DITEC', codSubprocesso: 100},
    {codUnidade: 11, sigla: 'DIRAD', codSubprocesso: 101},
];

const mockAtividades = [
    {codigo: 50, descricao: 'Atividade importada 1'},
    {codigo: 51, descricao: 'Atividade importada 2'},
];

vi.mock('@/services/processoService', () => ({
    buscarProcessosParaImportacao: vi.fn(),
    buscarUnidadesParaImportacao: vi.fn(),
}));

vi.mock('@/services/subprocessoService', () => ({
    listarAtividadesParaImportacao: vi.fn(),
    importarAtividades: vi.fn(),
}));

export const Default: Story = {
    args: {
        mostrar: true,
        codSubprocessoDestino: 200,
    },
    render: (args) => ({
        components: {ImportarAtividadesModal},
        setup() {
            const show = ref(args.mostrar);
            vi.mocked(processoService.buscarProcessosParaImportacao).mockResolvedValue(mockProcessos as any);
            vi.mocked(processoService.buscarUnidadesParaImportacao).mockResolvedValue(mockUnidades as any);
            vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividades as any);
            vi.mocked(subprocessoService.importarAtividades).mockResolvedValue({aviso: 'Importação concluída'} as any);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Importar atividades</button>
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
        components: {ImportarAtividadesModal},
        setup() {
            const show = ref(args.mostrar);
            vi.mocked(processoService.buscarProcessosParaImportacao).mockResolvedValue([] as any);
            vi.mocked(processoService.buscarUnidadesParaImportacao).mockResolvedValue([] as any);
            vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue([] as any);
            vi.mocked(subprocessoService.importarAtividades).mockResolvedValue({} as any);
            return {args, show};
        },
        template: '<ImportarAtividadesModal v-bind="args" :mostrar="show" @fechar="show = false" />',
    }),
};

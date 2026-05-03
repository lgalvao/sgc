import {vi} from "vitest";
import type {Meta, StoryObj} from '@storybook/vue3-vite';
import ImportarAtividadesModal from './ImportarAtividadesModal.vue';
import {ref} from 'vue';
import * as processoService from '@/services/processo';
import * as subprocessoService from '@/services/subprocessoService';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';

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

vi.mock('@/services/processo', () => ({
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
            vi.mocked(subprocessoService.importarAtividades).mockResolvedValue({
                atividade: null,
                subprocesso: {codigo: 200, situacao: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO'},
                atividadesAtualizadas: mockAtividades,
                permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
                aviso: 'Importação concluída',
            } as any);
            return {args, show};
        },
        template: `
      <div>
        <button class="btn btn-primary" @click="show = true">Importar atividades</button>
        <ImportarAtividadesModal v-bind="args" :mostrar="show" :cod-subprocesso-destino="args.codSubprocessoDestino" @fechar="show = false" />
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
            vi.mocked(subprocessoService.importarAtividades).mockResolvedValue({
                atividade: null,
                subprocesso: {codigo: 200, situacao: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO'},
                atividadesAtualizadas: mockAtividades,
                permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
                aviso: null,
            } as any);
            return {args, show};
        },
        template: '<ImportarAtividadesModal v-bind="args" :mostrar="show" :cod-subprocesso-destino="args.codSubprocessoDestino" @fechar="show = false" />',
    }),
};

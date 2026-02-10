import {beforeEach, describe, expect, it, vi} from 'vitest';
import {defineComponent} from 'vue';
import {createTestingPinia} from '@pinia/testing';
import {useVisAtividades} from '../useVisAtividades';
import {useProcessosStore} from '@/stores/processos';
import {useAtividadesStore} from '@/stores/atividades';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useUnidadesStore} from '@/stores/unidades';
import {useMapasStore} from '@/stores/mapas';
import {useAnalisesStore} from '@/stores/analises';
import {SituacaoSubprocesso} from '@/types/tipos';
import {flushPromises, mount} from '@vue/test-utils';

const { mockPush } = vi.hoisted(() => ({
    mockPush: vi.fn()
}));

vi.mock('vue-router', () => ({
    useRouter: () => ({ push: mockPush }),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Mock services to prevent Network Error
vi.mock('@/services/processoService', () => ({
    obterDetalhesProcesso: vi.fn().mockResolvedValue({}),
    buscarProcessosPainel: vi.fn().mockResolvedValue({}),
}));

vi.mock('@/services/subprocessoService', () => ({
    listarAtividades: vi.fn().mockResolvedValue([]),
}));

describe('useVisAtividades', () => {
    const props = { codProcesso: 1, sigla: 'TEST' };

    const TestComponent = defineComponent({
        props: {
            codProcesso: {
                type: Number,
                default: 0
            },
            sigla: {
                type: String,
                default: ''
            }
        },
        setup(props) {
            return { ...useVisAtividades(props as any) };
        },
        template: '<div />'
    });

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('inicializa e busca dados no mount', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const processosStore = useProcessosStore(pinia);
        const atividadesStore = useAtividadesStore(pinia);
        const unidadesStore = useUnidadesStore(pinia);

        processosStore.processoDetalhe = {
            unidades: [{ sigla: 'TEST', codSubprocesso: 123 }]
        } as any;
        unidadesStore.unidades = [{ sigla: 'TEST', nome: 'Unidade Teste', filhas: [] }] as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        await flushPromises();

        expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(1);
        expect(atividadesStore.buscarAtividadesParaSubprocesso).toHaveBeenCalledWith(123);
        expect(wrapper.vm.nomeUnidade).toBe('Unidade Teste');
    });

    it('busca unidade em subníveis', async () => {
        const pinia = createTestingPinia();
        const unidadesStore = useUnidadesStore(pinia);
        unidadesStore.unidades = [
            { sigla: 'ROOT', filhas: [{ sigla: 'TEST', nome: 'Subunidade' }] }
        ] as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        expect(wrapper.vm.nomeUnidade).toBe('Subunidade');
    });

    it('retorna vazio se codSubprocesso for indefinido ou nulo', async () => {
        const pinia = createTestingPinia();
        const processosStore = useProcessosStore(pinia);
        processosStore.processoDetalhe = null;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        expect(wrapper.vm.atividades).toEqual([]);
        expect(wrapper.vm.historicoAnalises).toEqual([]);
        expect(wrapper.vm.podeVerImpacto).toBe(false);
    });

    it('calcula isHomologacao corretamente baseado na permissão', async () => {
        const pinia = createTestingPinia();
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        subprocessosStore.subprocessoDetalhe = {
            permissoes: { podeHomologarCadastro: true }
        } as any;
        processosStore.processoDetalhe = {
            unidades: [{ sigla: 'TEST', situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO }]
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        expect(wrapper.vm.isHomologacao).toBe(true);
    });

    it('podeVerImpacto corretamente baseado na permissão', () => {
        const pinia = createTestingPinia();
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        subprocessosStore.subprocessoDetalhe = {
            permissoes: { podeVisualizarImpacto: true }
        } as any;
        processosStore.processoDetalhe = {
            unidades: [{ sigla: 'TEST', situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO }]
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        expect(wrapper.vm.podeVerImpacto).toBe(true);
    });

    it('abre e fecha modais', async () => {
        const pinia = createTestingPinia();
        const mapasStore = useMapasStore(pinia);
        const analisesStore = useAnalisesStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = { unidades: [{ sigla: 'TEST', codSubprocesso: 123 }] } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        await wrapper.vm.abrirModalImpacto();
        expect(wrapper.vm.mostrarModalImpacto).toBe(true);
        expect(mapasStore.buscarImpactoMapa).toHaveBeenCalledWith(123);
        wrapper.vm.fecharModalImpacto();
        expect(wrapper.vm.mostrarModalImpacto).toBe(false);

        await wrapper.vm.abrirModalHistoricoAnalise();
        expect(wrapper.vm.mostrarModalHistoricoAnalise).toBe(true);
        expect(analisesStore.buscarAnalisesCadastro).toHaveBeenCalledWith(123);
        wrapper.vm.fecharModalHistoricoAnalise();
        expect(wrapper.vm.mostrarModalHistoricoAnalise).toBe(false);

        wrapper.vm.validarCadastro();
        expect(wrapper.vm.mostrarModalValidar).toBe(true);
        wrapper.vm.fecharModalValidar();
        expect(wrapper.vm.mostrarModalValidar).toBe(false);

        wrapper.vm.devolverCadastro();
        expect(wrapper.vm.mostrarModalDevolver).toBe(true);
        wrapper.vm.fecharModalDevolver();
        expect(wrapper.vm.mostrarModalDevolver).toBe(false);
    });

    it('confirma validacao (homologacao)', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = {
            unidades: [{ sigla: 'TEST', codSubprocesso: 123, situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO }]
        } as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            permissoes: { podeHomologarCadastro: true }
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        wrapper.vm.observacaoValidacao = 'Ok';
        (subprocessosStore.homologarCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarValidacao();

        expect(subprocessosStore.homologarCadastro).toHaveBeenCalledWith(123, { observacoes: 'Ok' });
        expect(mockPush).toHaveBeenCalled();
    });

    it('confirma validacao (homologacao) revisao', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = {
            tipo: 'REVISAO',
            unidades: [{ sigla: 'TEST', codSubprocesso: 123, situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA }]
        } as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            permissoes: { podeHomologarCadastro: true }
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        (subprocessosStore.homologarRevisaoCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarValidacao();

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalledWith(123, expect.any(Object));
    });

    it('confirma devolucao', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = { unidades: [{ sigla: 'TEST', codSubprocesso: 123 }] } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        wrapper.vm.observacaoDevolucao = 'Melhorar';
        (subprocessosStore.devolverCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarDevolucao();

        expect(subprocessosStore.devolverCadastro).toHaveBeenCalledWith(123, { observacoes: 'Melhorar' });
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('confirma validacao (aceite) e revisao', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = {
            tipo: 'REVISAO',
            unidades: [{ sigla: 'TEST', codSubprocesso: 123, situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA }]
        } as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            permissoes: { podeHomologarCadastro: false, podeAceitarCadastro: true }
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        (subprocessosStore.aceitarRevisaoCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarValidacao();

        expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalledWith(123, expect.any(Object));
        expect(mockPush).toHaveBeenCalledWith({ name: 'Painel' });
    });

    it('confirma validacao (aceite) mapeamento', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = {
            tipo: 'MAPEAMENTO',
            unidades: [{ sigla: 'TEST', codSubprocesso: 123, situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO }]
        } as any;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            permissoes: { podeHomologarCadastro: false, podeAceitarCadastro: true }
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        (subprocessosStore.aceitarCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarValidacao();

        expect(subprocessosStore.aceitarCadastro).toHaveBeenCalledWith(123, expect.any(Object));
    });

    it('confirma devolucao revisao', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        const processosStore = useProcessosStore(pinia);

        processosStore.processoDetalhe = {
            tipo: 'REVISAO',
            unidades: [{ sigla: 'TEST', codSubprocesso: 123 }]
        } as any;

        const wrapper = mount(TestComponent, {
            props,
            global: { plugins: [pinia] }
        });

        (subprocessosStore.devolverRevisaoCadastro as any).mockResolvedValue(true);

        await wrapper.vm.confirmarDevolucao();

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(123, expect.any(Object));
    });
});

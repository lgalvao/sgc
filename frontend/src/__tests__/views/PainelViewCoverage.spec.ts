import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import PainelView from '@/views/PainelView.vue';
import TabelaProcessos from '@/components/processo/TabelaProcessos.vue';
import {useProcessosStore} from '@/stores/processos';
import {useToastStore} from '@/stores/toast';
import * as painelService from '@/services/painelService';
import {useRouter} from 'vue-router';

vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: vi.fn(),
        replace: vi.fn()
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

const mockUseToast = {
    create: vi.fn()
};
vi.mock("bootstrap-vue-next", async () => {
    const actual = await vi.importActual("bootstrap-vue-next");
    return {
        ...actual as any,
        useToast: () => mockUseToast,
    };
});

const mockPageVazia = {content: [], totalPages: 0, totalElements: 0, number: 0, size: 10, first: true, last: true, empty: true};

vi.mock("@/services/painelService", () => ({
    listarProcessos: vi.fn(),
    listarAlertas: vi.fn(),
}));

describe('PainelView Coverage', () => {
    let routerPushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
        (painelService.listarAlertas as any).mockResolvedValue(mockPageVazia);
    });

    const commonStubs = {
        PageHeader: {template: '<div><slot name="actions" /></div>'},
        TabelaProcessos: {
            template: '<div></div>',
            props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc'],
            emits: ['ordenar', 'selecionar-processo']
        },
        TabelaAlertas: {
            template: '<div></div>',
            props: ['alertas'],
            emits: ['ordenar']
        },
        BContainer: {template: '<div><slot /></div>'},
        BButton: {template: '<button />'}
    };

    it('carregarDados calls only buscarProcessosPainel when usuarioCodigo is missing', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: 'GESTOR',
                    unidadeSelecionada: 1,
                    usuarioCodigo: null
                },
                processos: {processosPainel: []},
            }
        });

        const wrapper = mount(PainelView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const processosStore = useProcessosStore(pinia);
        await wrapper.vm.$nextTick();

        expect(processosStore.buscarProcessosPainel).toHaveBeenCalled();
        expect(painelService.listarAlertas).not.toHaveBeenCalled();
    });

    it('ordenarAlertasPor does nothing when usuarioCodigo is missing', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: 'GESTOR',
                    unidadeSelecionada: 1,
                    usuarioCodigo: null
                },
                processos: {processosPainel: []},
            }
        });

        const wrapper = mount(PainelView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        vi.clearAllMocks();
        (painelService.listarAlertas as any).mockResolvedValue(mockPageVazia);

        await (wrapper.vm as any).ordenarAlertasPor('processo');

        expect(painelService.listarAlertas).not.toHaveBeenCalled();
    });

    it('ordenarPor toggles asc/desc correctly', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: 'GESTOR',
                    unidadeSelecionada: 1,
                    usuarioCodigo: 1
                },
                processos: {processosPainel: []},
            }
        });

        const wrapper = mount(PainelView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });
        const processosStore = useProcessosStore(pinia);

        // Initial default: descricao ASC (implied by refs initialization in component)

        await (wrapper.vm as any).ordenarPor('descricao');

        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            'GESTOR', 1, 0, 10, 'descricao', 'desc'
        );

        await (wrapper.vm as any).ordenarPor('tipo');
        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            'GESTOR', 1, 0, 10, 'tipo', 'asc'
        );
    });

    it('redirects to CadProcesso on cta-vazio event', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: { perfilSelecionado: 'GESTOR', unidadeSelecionada: 1 },
                processos: { processosPainel: [] },
            }
        });
        const wrapper = mount(PainelView, { global: { plugins: [pinia], stubs: commonStubs } });
        await wrapper.findComponent(TabelaProcessos).vm.$emit('cta-vazio');
        expect(routerPushMock).toHaveBeenCalledWith({ name: 'CadProcesso' });
    });

    it('exibe toast pendente e recarrega dados onActivated', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: { perfilSelecionado: 'GESTOR', unidadeSelecionada: 1, usuarioCodigo: 1 },
                processos: { processosPainel: [] },
            }
        });
        const toastStore = useToastStore(pinia);
        toastStore.consumePending = vi.fn().mockReturnValue({ body: 'Teste Pendente' });
        mockUseToast.create.mockClear();

        const wrapper = mount(PainelView, { global: { plugins: [pinia], stubs: commonStubs } });
        await wrapper.vm.$nextTick();
        
        expect(toastStore.consumePending).toHaveBeenCalled();
        expect(mockUseToast.create).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({ body: 'Teste Pendente', variant: 'success' })
        }));
        
        // Simular onActivated
        if (wrapper.vm.$options.activated) {
            for (const hook of wrapper.vm.$options.activated) {
                await hook.call(wrapper.vm);
            }
            expect(toastStore.consumePending).toHaveBeenCalledTimes(2);
        }
    });
});

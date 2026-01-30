import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import PainelView from '@/views/PainelView.vue';
import { useProcessosStore } from '@/stores/processos';
import { useAlertasStore } from '@/stores/alertas';
import { useRouter } from 'vue-router';

// Mock router
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

describe('PainelView Coverage', () => {
    let routerPushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
    });

    const commonStubs = {
        PageHeader: { template: '<div><slot name="actions" /></div>' },
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
        BContainer: { template: '<div><slot /></div>' },
        BButton: { template: '<button />' }
    };

    it('carregarDados calls only buscarProcessosPainel when usuarioCodigo is missing', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: 'GESTOR',
                    unidadeSelecionada: 1,
                    usuarioCodigo: null // Simulating missing user code
                },
                processos: { processosPainel: [] },
                alertas: { alertas: [] }
            }
        });

        const wrapper = mount(PainelView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const processosStore = useProcessosStore(pinia);
        const alertasStore = useAlertasStore(pinia);

        // Wait for onMounted
        await wrapper.vm.$nextTick();

        expect(processosStore.buscarProcessosPainel).toHaveBeenCalled();
        expect(alertasStore.buscarAlertas).not.toHaveBeenCalled();
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
                processos: { processosPainel: [] },
                alertas: { alertas: [] }
            }
        });

        const wrapper = mount(PainelView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const alertasStore = useAlertasStore(pinia);

        // Call the method directly to trigger the branch
        await (wrapper.vm as any).ordenarAlertasPor('processo');

        expect(alertasStore.buscarAlertas).not.toHaveBeenCalled();
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
                processos: { processosPainel: [] },
                alertas: { alertas: [] }
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

        // 1. Sort by same field (descricao) -> should toggle to DESC
        await (wrapper.vm as any).ordenarPor('descricao');

        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            'GESTOR', 1, 0, 10, 'descricao', 'desc'
        );

        // 2. Sort by different field (tipo) -> should reset to ASC
        await (wrapper.vm as any).ordenarPor('tipo');
        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            'GESTOR', 1, 0, 10, 'tipo', 'asc'
        );
    });
});

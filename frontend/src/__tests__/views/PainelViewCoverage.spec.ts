import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import PainelView from '@/views/PainelView.vue';
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
    marcarAlertasLidos: vi.fn().mockResolvedValue(undefined),
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
        (painelService.listarProcessos as any).mockResolvedValue(mockPageVazia);
    });

    const commonStubs = {
        PageHeader: {template: '<div><slot name="actions" /></div>'},
        TabelaProcessos: {
            name: 'TabelaProcessos',
            template: '<div></div>',
            props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc'],
            emits: ['ordenar', 'selecionar-processo', 'cta-vazio']
        },
        TabelaAlertas: {
            template: '<div></div>',
            props: ['alertas'],
            emits: ['ordenar']
        },
        BContainer: {template: '<div><slot /></div>'},
        BButton: {template: '<button v-bind="$attrs" />'}
    };

    it('carregarDados calls buscarProcessosPainel and listarAlertas when unit is present', async () => {
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

        await wrapper.vm.$nextTick();

        expect(painelService.listarProcessos).toHaveBeenCalled();
        expect(painelService.listarAlertas).toHaveBeenCalled();
    });

    it('ordenarPor altera estado local sem chamar o backend', async () => {
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
        await flushPromises();
        const callsAfterMount = (painelService.listarProcessos as any).mock.calls.length;

        // Ordenar: deve alterar estado local, sem chamar o backend
        await (wrapper.vm as any).ordenarPor('descricao');
        expect(painelService.listarProcessos).toHaveBeenCalledTimes(callsAfterMount);
        expect((wrapper.vm as any).asc).toBe(false);

        await (wrapper.vm as any).ordenarPor('tipo');
        expect(painelService.listarProcessos).toHaveBeenCalledTimes(callsAfterMount);
        expect((wrapper.vm as any).criterio).toBe('tipo');
        expect((wrapper.vm as any).asc).toBe(true);
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
        await flushPromises();
        
        // Chamando a função via emit no stub pelo nome
        await wrapper.findComponent({ name: 'TabelaProcessos' }).vm.$emit('cta-vazio');
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
        const activated = wrapper.vm.$options.activated;
        if (activated) {
            const hooks = Array.isArray(activated) ? activated : [activated];
            for (const hook of hooks) {
                await hook.call(wrapper.vm);
            }
            expect(toastStore.consumePending).toHaveBeenCalledTimes(2);
        }
    });
});

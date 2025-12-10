import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import HistoricoView from "@/views/HistoricoView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useProcessosStore} from "@/stores/processos";
import {useRouter} from "vue-router";
import {Perfil} from "@/types/tipos";
import * as usePerfilComposable from "@/composables/usePerfil";

// Mock router
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Mock usePerfil composable
vi.mock("@/composables/usePerfil", () => ({
    usePerfil: vi.fn(),
}));

describe("HistoricoView.vue", () => {
    let routerPushMock: any;
    const mockProcessos = [
        {
            codigo: 1,
            descricao: "Proc B",
            dataFinalizacao: "2023-01-02T10:00:00",
            dataFinalizacaoFormatada: "02/01/2023 10:00"
        },
        {
            codigo: 2,
            descricao: "Proc A",
            dataFinalizacao: "2023-01-01T10:00:00",
            dataFinalizacaoFormatada: "01/01/2023 10:00"
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
        // Default usePerfil mock
        (usePerfilComposable.usePerfil as any).mockReturnValue({
            unidadeSelecionada: {value: "SEDE"}
        });
    });

    const mountOptions = (customState = {}) => ({
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        processos: {
                            processosFinalizados: mockProcessos
                        },
                        perfil: {
                            perfilSelecionado: Perfil.ADMIN
                        },
                        ...customState
                    },
                    stubActions: true,
                }),
            ],
            stubs: {
                BContainer: {template: '<div><slot /></div>'},
                BAlert: {
                    template: '<div v-if="modelValue" data-testid="alerta-vazio"><slot /></div>',
                    props: ['modelValue']
                },
                TabelaProcessos: {
                    name: 'TabelaProcessos',
                    props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc', 'showDataFinalizacao'],
                    emits: ['ordenar', 'selecionar-processo'],
                    template: '<div data-testid="tabela-processos"></div>'
                },
            },
        },
    });

    it("deve carregar processos finalizados ao montar", async () => {
        const processosStore = useProcessosStore();

        processosStore.buscarProcessosFinalizados = vi.fn();
        const store3 = useProcessosStore();
        expect(store3.buscarProcessosFinalizados).toHaveBeenCalled();
    });

    it("deve renderizar tabela com processos ordenados (default descricao ASC)", () => {
        const wrapper = mount(HistoricoView, mountOptions());

        const tabela = wrapper.findComponent({name: 'TabelaProcessos'});
        expect(tabela.exists()).toBe(true);

        const props = tabela.props('processos');
        expect(props[0].descricao).toBe("Proc A");
        expect(props[1].descricao).toBe("Proc B");
    });

    it("deve ordenar por dataFinalizacao", async () => {
        const wrapper = mount(HistoricoView, mountOptions());
        const tabela = wrapper.findComponent({name: 'TabelaProcessos'});

        // Emit 'ordenar' 'dataFinalizacao'
        await tabela.vm.$emit('ordenar', 'dataFinalizacao');

        // Default becomes ASC (oldest first).
        // Proc A: 01/01, Proc B: 02/01.
        // Expected: Proc A, Proc B.
        let props = tabela.props('processos');
        expect(props[0].codigo).toBe(2); // Proc A
        expect(props[1].codigo).toBe(1); // Proc B

        // Emit 'ordenar' 'dataFinalizacao' again -> DESC
        await tabela.vm.$emit('ordenar', 'dataFinalizacao');
        props = tabela.props('processos');
        expect(props[0].codigo).toBe(1); // Proc B (Newest)
        expect(props[1].codigo).toBe(2); // Proc A
    });

    it("deve exibir alerta se não houver processos", () => {
        const wrapper = mount(HistoricoView, mountOptions({processos: {processosFinalizados: []}}));
        expect(wrapper.find('[data-testid="alerta-vazio"]').exists()).toBe(true);
    });

    it("deve navegar para rota correta como ADMIN", async () => {
        const wrapper = mount(HistoricoView, mountOptions({perfil: {perfilSelecionado: Perfil.ADMIN}}));
        const tabela = wrapper.findComponent({name: 'TabelaProcessos'});

        await tabela.vm.$emit('selecionar-processo', mockProcessos[0]);

        expect(routerPushMock).toHaveBeenCalledWith({
            name: "Processo",
            params: {codProcesso: "1"}
        });
    });

    it("deve navegar para rota correta como SERVIDOR (Subprocesso)", async () => {
        const wrapper = mount(HistoricoView, mountOptions({perfil: {perfilSelecionado: Perfil.SERVIDOR}}));
        const tabela = wrapper.findComponent({name: 'TabelaProcessos'});

        await tabela.vm.$emit('selecionar-processo', mockProcessos[0]);

        expect(routerPushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {codProcesso: 1, siglaUnidade: "SEDE"}
        });
    });
});

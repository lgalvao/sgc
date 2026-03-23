import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {ref} from "vue";
import HistoricoView from "@/views/HistoricoView.vue";

const {mockPush} = vi.hoisted(() => ({
    mockPush: vi.fn(),
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({push: mockPush}),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mockPush,
        replace: vi.fn(),
        resolve: vi.fn(),
        currentRoute: {value: {}},
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

const processosMock = {
    processosFinalizados: ref<any[]>([]),
    buscarProcessosFinalizados: vi.fn(),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock,
}));

const LayoutPadraoStub = {
    template: "<div><slot /></div>",
};

const PageHeaderStub = {
    template: '<div data-testid="page-header">{{ title }}</div>',
    props: ["title"],
};

const TabelaProcessosStub = {
    name: "TabelaProcessos",
    template: '<div data-testid="tabela-processos"></div>',
    props: ["processos", "criterioOrdenacao", "direcaoOrdenacaoAsc", "compacto", "showDataFinalizacao"],
    emits: ["ordenar", "selecionar-processo"],
};

describe("HistoricoView.vue", () => {
    const mockProcessos = [
        {
            codigo: 1,
            descricao: "Proc B",
            tipo: "MAPEAMENTO",
            dataFinalizacao: "2023-01-02T10:00:00",
            situacao: "FINALIZADO",
        },
        {
            codigo: 2,
            descricao: "Proc A",
            tipo: "REVISAO",
            dataFinalizacao: "2023-01-01T10:00:00",
            situacao: "FINALIZADO",
        }
    ];

    function createWrapper() {
        return mount(HistoricoView, {
            global: {
                stubs: {
                    LayoutPadrao: LayoutPadraoStub,
                    PageHeader: PageHeaderStub,
                    TabelaProcessos: TabelaProcessosStub,
                    BSpinner: {template: '<div data-testid="spinner"></div>'},
                },
            },
        });
    }

    beforeEach(() => {
        vi.clearAllMocks();
        processosMock.processosFinalizados.value = [...mockProcessos];
    });

    it("deve carregar processos finalizados ao montar", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        expect(processosMock.buscarProcessosFinalizados).toHaveBeenCalled();
        expect(wrapper.find('[data-testid="tabela-processos"]').exists()).toBe(true);
    });

    it("deve repassar processos ordenados para a tabela", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const tabela = wrapper.findComponent({name: "TabelaProcessos"});
        const processos = tabela.props("processos") as any[];

        expect(processos).toHaveLength(2);
        expect(processos[0].descricao).toBe("Proc B");
        expect(processos[1].descricao).toBe("Proc A");
    });

    it("deve repassar lista vazia para a tabela quando não houver processos", async () => {
        processosMock.processosFinalizados.value = [];
        const wrapper = createWrapper();
        await flushPromises();

        const tabela = wrapper.findComponent({name: "TabelaProcessos"});
        expect(tabela.props("processos")).toEqual([]);
    });

    it("deve navegar para detalhes ao selecionar processo", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        await wrapper.findComponent({name: "TabelaProcessos"}).vm.$emit("selecionar-processo", {codigo: 1});

        expect(mockPush).toHaveBeenCalledWith("/processo/1");
    });
});

import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {ref} from "vue";
import UnidadeView from "@/views/UnidadeView.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as unidadeQueryModule from "@/composables/useUnidadeQuery";

const {
    mockPush,
    notify,
    downloadRelatorioMapaVigenteUnidadePdf,
    downloadRelatorioMapaVigenteUnidadeCsv,
    dadosTelaInicial,
} = vi.hoisted(() => ({
    mockPush: vi.fn(),
    notify: vi.fn(),
    downloadRelatorioMapaVigenteUnidadePdf: vi.fn(),
    downloadRelatorioMapaVigenteUnidadeCsv: vi.fn(),
    dadosTelaInicial: {
        data: null as any,
        error: null as Error | null,
        isPending: false,
        isLoading: false,
    },
}));

const titular = {
    codigo: 10,
    nome: "Titular teste",
    tituloEleitoral: "123456",
    matricula: "M10",
    email: "t@t",
    ramal: "1",
    unidade: {codigo: 1, sigla: "TEST"},
};

const responsavel = {
    codigo: 20,
    nome: "Responsavel teste",
    tituloEleitoral: "654321",
    matricula: "M20",
    email: "r@r",
    ramal: "2",
    unidade: {codigo: 1, sigla: "TEST"},
};

const unidadePadrao = {
    codigo: 1,
    sigla: "TEST",
    nome: "UnidadeView Teste",
    titular,
    responsavel,
    tipoResponsabilidade: "SUBSTITUTO",
    dataFimResponsabilidade: "2026-05-30T23:59:59",
    filhas: [
        {codigo: 2, sigla: "SUB1", nome: "Subordinada 1", filhas: []},
        {codigo: 3, sigla: "SUB2", nome: "Subordinada 2", filhas: []},
    ],
};

const mapaVigentePadrao = {codProcesso: 99, codSubprocesso: 77};

vi.mock("vue-router", async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        useRouter: () => ({
            push: mockPush,
        }),
    };
});

vi.mock("@/composables/useUnidadeQuery", () => {
    const data = ref<any>(dadosTelaInicial.data);
    const error = ref<Error | null>(dadosTelaInicial.error);
    const isPending = ref(dadosTelaInicial.isPending);
    const isLoading = ref(dadosTelaInicial.isLoading);
    const refetch = vi.fn();
    const refresh = vi.fn();

    return {
        useDadosTelaUnidadeQuery: () => ({data, error, isPending, isLoading, refetch, refresh}),
        useInvalidacaoUnidade: () => ({
            invalidarUnidade: vi.fn(),
            invalidarDadosTelaUnidade: vi.fn(),
            invalidarArvoreElegibilidade: vi.fn(),
        }),
        __mock: {data, error, isPending, isLoading, refetch, refresh},
    };
});

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({notify}),
}));

vi.mock("@/services/relatoriosService", () => ({
    relatoriosService: {
        downloadRelatorioMapaVigenteUnidadePdf,
        downloadRelatorioMapaVigenteUnidadeCsv,
    },
}));

const TreeTableStub = {
    name: "TreeTable",
    template: "<div data-testid='tree-table' @click=\"$emit('row-click', { codigo: 2 })\"></div>",
    props: ["data", "columns", "title"],
    emits: ["row-click"],
};

describe("UnidadeView.vue", () => {
    const context = setupComponentTest();
    const dadosTelaQueryMock = (unidadeQueryModule as any).__mock;

    beforeEach(() => {
        vi.clearAllMocks();
        dadosTelaQueryMock.data.value = {unidade: unidadePadrao, mapaVigente: null};
        dadosTelaQueryMock.error.value = null;
        dadosTelaQueryMock.isPending.value = false;
        dadosTelaQueryMock.isLoading.value = false;
        dadosTelaQueryMock.refetch.mockResolvedValue({data: dadosTelaQueryMock.data.value});
        dadosTelaQueryMock.refresh.mockResolvedValue({data: dadosTelaQueryMock.data.value});
        downloadRelatorioMapaVigenteUnidadePdf.mockResolvedValue(undefined);
        downloadRelatorioMapaVigenteUnidadeCsv.mockResolvedValue(undefined);
    });

    function criarWrapper(estadoInicial = {}) {
        context.wrapper = mount(UnidadeView, {
            ...getCommonMountOptions(
                {
                    perfil: {
                        perfilSelecionado: "USER",
                        permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
                    },
                    ...estadoInicial,
                },
                {
                    BContainer: {template: "<div><slot /></div>"},
                    BCard: {template: "<div><slot /></div>"},
                    BCardBody: {template: "<div><slot /></div>"},
                    BButton: {template: "<button @click=\"$emit('click')\"><slot /></button>"},
                    BDropdown: {template: "<div><slot /></div>"},
                    BDropdownItemButton: {template: "<button @click=\"$emit('click')\"><slot /></button>"},
                    BAlert: {template: "<div><slot /></div>", emits: ["dismissed"]},
                    TreeTable: TreeTableStub,
                },
                {stubActions: false}
            ),
            props: {
                codUnidade: 1,
            },
        });
        return context.wrapper;
    }

    it("renderiza os dados principais da unidade", async () => {
        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain("TEST");
        expect(wrapper.text()).toContain("UnidadeView Teste");
        expect(wrapper.text()).toContain("Titular teste");
        expect(wrapper.text()).toContain("Substituição");
    });

    it("não exibe bloco de titular separado quando o responsável é o próprio titular", async () => {
        dadosTelaQueryMock.data.value = {
            unidade: {
                ...unidadePadrao,
                responsavel: unidadePadrao.titular,
                tipoResponsabilidade: "TITULAR",
            },
            mapaVigente: null,
        };

        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.find("[data-testid='unidade-titular-info']").exists()).toBe(false);
        expect(wrapper.find("[data-testid='unidade-responsavel-info']").exists()).toBe(true);
        expect(wrapper.text()).toContain("Titular");
    });

    it("renderiza a árvore de subordinadas e navega ao clicar na linha", async () => {
        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.find("[data-testid='tree-table']").exists()).toBe(true);
        await wrapper.findComponent(TreeTableStub).vm.$emit("row-click", {codigo: 2});
        expect(mockPush).toHaveBeenCalledWith({path: "/unidade/2"});
    });

    it("exibe empty state quando a unidade não existe", async () => {
        dadosTelaQueryMock.data.value = {unidade: null, mapaVigente: null};

        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.findComponent(EmptyState).exists()).toBe(true);
    });

    it("exibe alerta de erro quando a query falha", async () => {
        dadosTelaQueryMock.error.value = new Error("Erro ao carregar unidade");

        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao carregar unidade");
    });

    it("permite exportar PDF do mapa vigente para perfil CHEFE", async () => {
        dadosTelaQueryMock.data.value = {unidade: unidadePadrao, mapaVigente: mapaVigentePadrao};

        const wrapper = criarWrapper({
            perfil: {
                perfilSelecionado: "CHEFE",
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        await wrapper.find("[data-testid='btn-exportar-mapa-vigente-pdf']").trigger("click");

        expect(downloadRelatorioMapaVigenteUnidadePdf).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();
    });

    it("permite exportar CSV do mapa vigente para perfil CHEFE", async () => {
        dadosTelaQueryMock.data.value = {unidade: unidadePadrao, mapaVigente: mapaVigentePadrao};

        const wrapper = criarWrapper({
            perfil: {
                perfilSelecionado: "CHEFE",
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        await wrapper.find("[data-testid='btn-exportar-mapa-vigente-csv']").trigger("click");

        expect(downloadRelatorioMapaVigenteUnidadeCsv).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();
    });

    it("não exibe exportação de mapa vigente para perfil sem permissão", async () => {
        dadosTelaQueryMock.data.value = {unidade: unidadePadrao, mapaVigente: mapaVigentePadrao};

        const wrapper = criarWrapper();
        await flushPromises();

        expect(wrapper.find("[data-testid='btn-exportar-mapa-vigente']").exists()).toBe(false);
    });

    it("renderiza botão de editar atribuição quando a responsabilidade é temporária", async () => {
        dadosTelaQueryMock.data.value = {
            unidade: {
                ...unidadePadrao,
                tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA",
            },
            mapaVigente: null,
        };

        const wrapper = criarWrapper({
            perfil: {
                perfilSelecionado: "ADMIN",
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: true},
            },
        });
        await flushPromises();

        expect(wrapper.find("[data-testid='unidade-view__btn-atribuicao-texto']").text()).toBe("Editar atribuição");
    });

    it("notifica erro quando a exportação PDF falha", async () => {
        dadosTelaQueryMock.data.value = {unidade: unidadePadrao, mapaVigente: mapaVigentePadrao};
        downloadRelatorioMapaVigenteUnidadePdf.mockRejectedValueOnce(new Error("Erro PDF"));

        const wrapper = criarWrapper({
            perfil: {
                perfilSelecionado: "CHEFE",
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        await wrapper.find("[data-testid='btn-exportar-mapa-vigente-pdf']").trigger("click");

        expect(notify).toHaveBeenCalledWith("Erro ao exportar PDF", "danger");
    });
});

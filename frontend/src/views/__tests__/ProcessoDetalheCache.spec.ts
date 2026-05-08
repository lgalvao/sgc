import {flushPromises, mount} from "@vue/test-utils";
import {computed, ref} from "vue";
import {beforeEach, describe, expect, it, vi} from "vitest";
import ProcessoDetalheView from "../ProcessoDetalheView.vue";

const pushMock = vi.fn();
const garantirContextoCompletoMock = vi.fn();
const dadosValidosMock = vi.fn();

const processoStoreMock = {
    contextoCompleto: null as any,
    codProcessoCarregado: null as number | null,
    garantirContextoCompleto: garantirContextoCompletoMock,
    dadosValidos: dadosValidosMock,
    invalidar: vi.fn(),
    resetar: vi.fn(),
};

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/stores/processo", () => ({
    useProcessoStore: () => processoStoreMock,
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        isAdmin: computed(() => false),
    }),
}));

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({
        notificacao: ref(null),
        notify: vi.fn(),
        clear: vi.fn(),
    }),
}));

vi.mock("@/views/processoDetalheAcoes", () => ({
    useProcessoAcoes: () => ({
        acaoBlocoAtual: ref(null),
        abrirModalBloco: vi.fn(),
        confirmarFinalizacao: vi.fn(),
        executarAcaoBloco: vi.fn(),
        finalizarProcesso: vi.fn(),
        idsElegiveis: ref([]),
        loadingFinalizacao: ref(false),
        modalBlocoRef: ref(null),
        mostrarModalFinalizacao: ref(false),
        processandoAcaoBloco: ref(false),
        unidadesElegiveis: ref([]),
    }),
}));

const stubs = {
    LayoutPadrao: {template: "<div><slot /></div>"},
    AppAlert: {
        props: ["mensagem", "variant", "variante"],
        template: "<div data-testid='app-alert'>{{ mensagem }}</div>",
    },
    CarregamentoPagina: {
        props: ["mensagem"],
        template: "<div data-testid='carregamento'>{{ mensagem }}</div>",
    },
    ProcessoAcoes: {
        props: ["processo"],
        template: "<div data-testid='processo-acoes'>{{ processo?.descricao }}</div>",
    },
    ProcessoSubprocessosTable: {template: "<div data-testid='processo-tabela' />"},
    ModalAcaoBloco: {template: "<div />"},
    ModalConfirmacao: {template: "<div />"},
};

function montar() {
    return mount(ProcessoDetalheView, {
        global: {stubs},
    });
}

describe("ProcessoDetalheView cache", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        processoStoreMock.contextoCompleto = null;
        processoStoreMock.codProcessoCarregado = null;
    });

    it("não recarrega ao reativar quando o contexto ainda é válido", async () => {
        garantirContextoCompletoMock.mockResolvedValue({
            codigo: 1,
            descricao: "Processo 1",
            unidades: [],
            podeFinalizar: false,
            acoesBloco: [],
        });
        dadosValidosMock.mockReturnValue(true);

        const wrapper = montar();
        await flushPromises();
        garantirContextoCompletoMock.mockClear();

        const hook = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a)?.[0];
        await hook?.call(wrapper.vm);
        await flushPromises();

        expect(garantirContextoCompletoMock).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain("Processo 1");
    });

    it("recarrega ao reativar quando o contexto está stale", async () => {
        garantirContextoCompletoMock
            .mockResolvedValueOnce({
                codigo: 1,
                descricao: "Processo 1",
                unidades: [],
                podeFinalizar: false,
                acoesBloco: [],
            })
            .mockResolvedValueOnce({
                codigo: 1,
                descricao: "Processo 1 atualizado",
                unidades: [],
                podeFinalizar: false,
                acoesBloco: [],
            });
        dadosValidosMock.mockReturnValue(false);

        const wrapper = montar();
        await flushPromises();
        garantirContextoCompletoMock.mockClear();

        const hook = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a)?.[0];
        await hook?.call(wrapper.vm);
        await flushPromises();

        expect(garantirContextoCompletoMock).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain("Processo 1 atualizado");
    });

    it("mantém o snapshot anterior visível se a recarga em background falhar", async () => {
        garantirContextoCompletoMock
            .mockResolvedValueOnce({
                codigo: 1,
                descricao: "Processo 1",
                unidades: [],
                podeFinalizar: false,
                acoesBloco: [],
            })
            .mockRejectedValueOnce(new Error("Falha na recarga"));
        dadosValidosMock.mockReturnValue(false);

        const wrapper = montar();
        await flushPromises();
        garantirContextoCompletoMock.mockClear();

        const hook = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a)?.[0];
        await hook?.call(wrapper.vm);
        await flushPromises();

        expect(garantirContextoCompletoMock).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain("Processo 1");
        expect(wrapper.find('[data-testid="app-alert"]').text()).toContain("Falha na recarga");
        expect(wrapper.find('[data-testid="carregamento"]').exists()).toBe(false);
    });
});

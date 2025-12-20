import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
// Mock services
import * as processoService from "@/services/processoService";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import ProcessoView from "@/views/ProcessoView.vue";
import { setupComponentTest, getCommonMountOptions } from "@/test-utils/componentTestHelpers";

const {pushMock} = vi.hoisted(() => {
    return {pushMock: vi.fn()};
});

vi.mock("vue-router", () => ({
    useRoute: () => ({
        params: {
            codProcesso: "1",
        },
    }),
    useRouter: () => ({
        push: pushMock,
    }),
    createRouter: () => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: pushMock,
    }),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
    obterDetalhesProcesso: vi.fn(),
    buscarSubprocessosElegiveis: vi.fn(),
    finalizarProcesso: vi.fn(),
    processarAcaoEmBloco: vi.fn(),
    buscarProcessosFinalizados: vi.fn(),
    buscarContextoCompleto: vi.fn(),
}));

// Stubs
const ProcessoDetalhesStub = {
    name: "ProcessoDetalhes",
    props: ["descricao", "tipo", "situacao"],
    template: '<div data-testid="processo-detalhes">{{ descricao }}</div>',
};

const ProcessoAcoesStub = {
    name: "ProcessoAcoes",
    props: ["mostrarBotoesBloco", "perfil", "situacaoProcesso"],
    template: '<div data-testid="processo-acoes"></div>',
    emits: ["aceitar-bloco", "homologar-bloco", "finalizar"],
};

const ModalFinalizacaoStub = {
    name: "ModalFinalizacao",
    props: ["mostrar", "processoDescricao"],
    template: '<div v-if="mostrar" data-testid="modal-finalizacao"></div>',
    emits: ["fechar", "confirmar"],
};

const ModalAcaoBlocoStub = {
    name: "ModalAcaoBloco",
    props: ["mostrar", "tipo", "unidades"],
    template: '<div v-if="mostrar" data-testid="modal-acao-bloco"></div>',
    emits: ["fechar", "confirmar"],
};

const TreeTableStub = {
    name: "TreeTable",
    props: ["columns", "data", "title"],
    template: '<div data-testid="tree-table"></div>',
    emits: ["row-click"],
};

describe("ProcessoView.vue", () => {
    const context = setupComponentTest();

    const mockProcesso = {
        codigo: 1,
        descricao: "Test Process",
        tipo: "MAPEAMENTO",
        situacao: "EM_ANDAMENTO",
        unidades: [
            {
                codUnidade: 10,
                sigla: "U1",
                nome: "Unidade 1",
                situacaoSubprocesso: "EM_ANDAMENTO",
                dataLimite: "2023-01-01",
                filhos: [],
            },
        ],
        resumoSubprocessos: [],
    };

    const mockSubprocessosElegiveis = [
        {
            codSubprocesso: 1,
            unidadeNome: "Test Unit",
            unidadeSigla: "TU",
            situacao: "NAO_INICIADO",
        },
    ];

    const additionalStubs = {
        ProcessoDetalhes: ProcessoDetalhesStub,
        ProcessoAcoes: ProcessoAcoesStub,
        ModalFinalizacao: ModalFinalizacaoStub,
        ModalAcaoBloco: ModalAcaoBlocoStub,
        TreeTable: TreeTableStub,
        BContainer: {template: "<div><slot /></div>"},
        BAlert: {template: "<div><slot /></div>"},
    };

    // Função fábrica para criar o wrapper
    const createWrapper = (customState = {}) => {
        context.wrapper = mount(ProcessoView, {
            ...getCommonMountOptions(
                {
                    perfil: {
                        perfilSelecionado: "ADMIN",
                        unidadeSelecionada: 99,
                    },
                    ...customState,
                },
                additionalStubs,
                {
                    stubActions: false
                }
            )
        });

        const processosStore = useProcessosStore();
        const perfilStore = usePerfilStore();
        const feedbackStore = useFeedbackStore();

        return {wrapper: context.wrapper, processosStore, perfilStore, feedbackStore};
    };

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
            processo: mockProcesso,
            elegiveis: mockSubprocessosElegiveis,
        } as any);
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(
            mockProcesso as any,
        );
        vi.mocked(processoService.buscarSubprocessosElegiveis).mockResolvedValue(
            mockSubprocessosElegiveis as any,
        );
    });

    it("deve renderizar detalhes do processo e buscar dados no mount", async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);

        const detalhes = wrapper.findComponent(ProcessoDetalhesStub);
        expect(detalhes.props("descricao")).toBe("Test Process");
    });

    it("deve mostrar botões de ação quando houver subprocessos elegíveis", async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        expect(acoes.props("mostrarBotoesBloco")).toBe(true);
    });

    it("deve lidar com dataLimite nula", async () => {
        const mockProcessoNullDate = {...mockProcesso};
        mockProcessoNullDate.unidades = [
            {
                codUnidade: 10,
                sigla: "U1",
                nome: "Unidade 1",
                situacaoSubprocesso: "EM_ANDAMENTO",
                dataLimite: null,
                filhos: [],
            },
        ];
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
            processo: mockProcessoNullDate,
            elegiveis: mockSubprocessosElegiveis,
        } as any);

        const {wrapper} = createWrapper();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        const data = treeTable.props("data");
        expect(data[0].dataLimite).toBe("");
    });

    it("deve navegar para detalhes da unidade ao clicar na tabela (ADMIN)", async () => {
        const {wrapper} = createWrapper({
            perfil: {perfilSelecionado: "ADMIN", unidadeSelecionada: 99},
        });
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        // Simulando evento row-click
        const item = {id: 10, unidadeAtual: "U1", clickable: true};
        // Disparar evento diretamente no componente filho
        treeTable.vm.$emit("row-click", item);

        expect(pushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {codProcesso: "1", siglaUnidade: "U1"},
        });
    });

    it("deve abrir modal de finalização", async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        acoes.vm.$emit("finalizar");
        await flushPromises(); // Aguardar reatividade

        const modal = wrapper.findComponent(ModalFinalizacaoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props("mostrar")).toBe(true);
    });

    it("deve confirmar finalização", async () => {
        const {wrapper, feedbackStore} = createWrapper();
        await flushPromises();

        vi.spyOn(feedbackStore, "show");

        const modal = wrapper.findComponent(ModalFinalizacaoStub);
        modal.vm.$emit("confirmar");
        await flushPromises();

        expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(feedbackStore.show).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve abrir modal de ação em bloco", async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        acoes.vm.$emit("aceitar-bloco");
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.props("mostrar")).toBe(true);
        expect(modal.props("tipo")).toBe("aceitar");
    });

    it("deve confirmar ação em bloco", async () => {
        const {wrapper, feedbackStore} = createWrapper();
        await flushPromises();

        vi.spyOn(feedbackStore, "show");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        modal.vm.$emit("confirmar", [{sigla: "TU", selecionada: true}]);
        await flushPromises();

        expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith(
            expect.objectContaining({
                codProcesso: 1,
                unidades: ["TU"],
                tipoAcao: "aceitar",
            }),
        );
        expect(feedbackStore.show).toHaveBeenCalled();

        // Verificar se a busca foi realizada novamente
        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledTimes(1);
    });

    it("deve navegar para detalhes da unidade se perfil for CHEFE e unidade corresponder", async () => {
        const {wrapper} = createWrapper({
            perfil: {perfilSelecionado: "CHEFE", unidadeSelecionada: 10},
        });
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = {id: 10, unidadeAtual: "U1", clickable: true};
        treeTable.vm.$emit("row-click", item);

        expect(pushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {codProcesso: "1", siglaUnidade: "U1"},
        });
    });

    it("não deve navegar para detalhes da unidade se perfil for CHEFE e unidade não corresponder", async () => {
        const {wrapper} = createWrapper({
            perfil: {perfilSelecionado: "CHEFE", unidadeSelecionada: 99},
        });
        await flushPromises();
        // Reset push mock to ensure no previous calls
        pushMock.mockClear();

        const treeTable = wrapper.findComponent(TreeTableStub);
        const item = {id: 10, unidadeAtual: "U1", clickable: true};
        treeTable.vm.$emit("row-click", item);

        expect(pushMock).not.toHaveBeenCalled();
    });

    it("confirmarAcaoBloco deve mostrar erro se nenhuma unidade selecionada", async () => {
        const {wrapper, feedbackStore} = createWrapper();
        await flushPromises();
        vi.spyOn(feedbackStore, "show");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        modal.vm.$emit("confirmar", [{sigla: "TU", selecionada: false}]);
        await flushPromises();

        expect(processoService.processarAcaoEmBloco).not.toHaveBeenCalled();
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Nenhuma unidade selecionada",
            expect.any(String),
            "danger"
        );
    });

    it("executarFinalizacao deve mostrar erro se falhar", async () => {
        const {wrapper, processosStore} = createWrapper();
        await flushPromises();
        vi.mocked(processoService.finalizarProcesso).mockRejectedValue(new Error("Fail"));

        const modal = wrapper.findComponent(ModalFinalizacaoStub);
        modal.vm.$emit("confirmar");
        await flushPromises();

        expect(processosStore.lastError).toBeTruthy();
        expect(processosStore.lastError?.message).toContain("Fail");
    });

    it("confirmarAcaoBloco deve mostrar erro se falhar", async () => {
        const {wrapper, processosStore} = createWrapper();
        await flushPromises();
        vi.mocked(processoService.processarAcaoEmBloco).mockRejectedValue(new Error("Fail"));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        modal.vm.$emit("confirmar", [{sigla: "TU", selecionada: true}]);
        await flushPromises();

        expect(processosStore.lastError).toBeTruthy();
        expect(processosStore.lastError?.message).toContain("Fail");
    });
});

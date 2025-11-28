import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
// Mock services
import * as processoService from "@/services/processoService";
import { ToastService } from "@/services/toastService"; // Import ToastService
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import ProcessoView from "@/views/ProcessoView.vue";

const { pushMock } = vi.hoisted(() => {
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
}));

// Mock ToastService
vi.mock("@/services/toastService", () => ({
  ToastService: {
    sucesso: vi.fn(),
    erro: vi.fn(),
    aviso: vi.fn(),
    info: vi.fn(),
  },
  registerToast: vi.fn(),
}));

// Mock useToast from bootstrap-vue-next
vi.mock("bootstrap-vue-next", async (importOriginal) => {
    const actual = await importOriginal<any>();
    return {
        ...actual,
        useToast: () => ({
            show: (options: any) => {
                const variant = options.props?.variant || 'info';
                if (variant === 'success') {
                    ToastService.sucesso(options.title, options.body);
                } else if (variant === 'danger') {
                    ToastService.erro(options.title, options.body);
                } else if (variant === 'warning') {
                    ToastService.aviso(options.title, options.body);
                } else {
                    ToastService.info(options.title, options.body);
                }
            },
        }),
    };
});

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

// Função fábrica para criar o wrapper
const createWrapper = (customState = {}) => {
    const wrapper = mount(ProcessoView, {
        global: {
            plugins: [
                createTestingPinia({
                    stubActions: false,
                    initialState: {
                        perfil: {
                            perfilSelecionado: "ADMIN",
                            unidadeSelecionada: 99,
                        },
                        ...customState,
                    },
                }),
            ],
            stubs: {
                ProcessoDetalhes: ProcessoDetalhesStub,
                ProcessoAcoes: ProcessoAcoesStub,
                ModalFinalizacao: ModalFinalizacaoStub,
                ModalAcaoBloco: ModalAcaoBlocoStub,
                TreeTable: TreeTableStub,
                BContainer: {template: "<div><slot /></div>"},
                BAlert: {template: "<div><slot /></div>"},
      },
        },
    });

    const processosStore = useProcessosStore();
    const perfilStore = usePerfilStore();
    // const notificacoesStore = useNotificacoesStore(); // Removed

    return {wrapper, processosStore, perfilStore};
};

describe("ProcessoView.vue", () => {
    let wrapper: ReturnType<typeof createWrapper>["wrapper"];

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

  beforeEach(() => {
      vi.clearAllMocks();
      vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue(
          mockProcesso as any,
      );
      vi.mocked(processoService.buscarSubprocessosElegiveis).mockResolvedValue(
          mockSubprocessosElegiveis as any,
      );
  });

  afterEach(() => {
      wrapper?.unmount();
  });

    it("deve renderizar detalhes do processo e buscar dados no mount", async () => {
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        expect(processoService.buscarSubprocessosElegiveis).toHaveBeenCalledWith(1);

        const detalhes = wrapper.findComponent(ProcessoDetalhesStub);
        expect(detalhes.props("descricao")).toBe("Test Process");
    });

    it("deve mostrar botões de ação quando houver subprocessos elegíveis", async () => {
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        expect(acoes.props("mostrarBotoesBloco")).toBe(true);
    });

    it("deve navegar para detalhes da unidade ao clicar na tabela (ADMIN)", async () => {
        const {wrapper: w} = createWrapper({
            perfil: {perfilSelecionado: "ADMIN", unidadeSelecionada: 99},
        });
        wrapper = w;
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
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        acoes.vm.$emit("finalizar");
        await flushPromises(); // Aguardar reatividade

        const modal = wrapper.findComponent(ModalFinalizacaoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props("mostrar")).toBe(true);
    });

    it("deve confirmar finalização", async () => {
        // const {wrapper: w, notificacoesStore} = createWrapper(); // Modified
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        vi.spyOn(ToastService, "sucesso"); // Spy on ToastService

        const modal = wrapper.findComponent(ModalFinalizacaoStub);
        modal.vm.$emit("confirmar");
        await flushPromises();

        expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(ToastService.sucesso).toHaveBeenCalled(); // Check ToastService
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve abrir modal de ação em bloco", async () => {
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        acoes.vm.$emit("aceitar-bloco");
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.props("mostrar")).toBe(true);
        expect(modal.props("tipo")).toBe("aceitar");
    });

    it("deve confirmar ação em bloco", async () => {
        const {wrapper: w} = createWrapper();
        wrapper = w;
        await flushPromises();

        vi.spyOn(ToastService, "sucesso"); // Spy on ToastService

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
        expect(ToastService.sucesso).toHaveBeenCalled(); // Check ToastService

        // Verificar se a busca foi realizada novamente
        expect(processoService.obterDetalhesProcesso).toHaveBeenCalledTimes(2);
    });
});

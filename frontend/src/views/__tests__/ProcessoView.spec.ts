import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import ProcessoView from "@/views/ProcessoView.vue";
import { createTestingPinia } from "@pinia/testing";
import { useProcessosStore } from "@/stores/processos";
import { useFeedbackStore } from "@/stores/feedback";
import { usePerfilStore } from "@/stores/perfil";
import { Perfil, SituacaoSubprocesso } from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";
import { useRouter } from "vue-router";
import { nextTick } from "vue";

// Define mocks first
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
}));

// Mock router
vi.mock("vue-router", () => ({
    useRoute: () => ({
        params: {
            codProcesso: "1",
        },
        query: { codProcesso: "1" }
    }),
    useRouter: () => ({
        push: mocks.push,
    }),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mocks.push,
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Mock services that are called DIRECTLY by the component
vi.mock("@/services/subprocessoService", () => ({
    aceitarCadastroEmBloco: vi.fn(),
    aceitarValidacaoEmBloco: vi.fn(),
    homologarCadastroEmBloco: vi.fn(),
    homologarValidacaoEmBloco: vi.fn(),
    disponibilizarMapaEmBloco: vi.fn(),
}));

// Stubs definition
const ProcessoAcoesStub = {
    name: "ProcessoAcoes",
    template: '<div data-testid="processo-acoes"></div>',
    props: ["mostrarBotoesBloco", "perfil", "situacaoProcesso"],
    emits: ["finalizar"],
};

const ModalAcaoBlocoStub = {
    name: "ModalAcaoBloco",
    template: '<div data-testid="modal-acao-bloco"></div>',
    props: ["id", "titulo", "texto", "rotuloBotao", "unidades", "mostrar", "tipo", "unidadesPreSelecionadas", "mostrarDataLimite"],
    methods: {
        abrir: vi.fn(),
        fechar: vi.fn(),
        setErro: vi.fn(),
        setProcessando: vi.fn(),
    },
    emits: ["confirmar"],
};

const TreeTableStub = {
    name: "TreeTable",
    template: '<div data-testid="tree-table"></div>',
    props: ["columns", "data", "title"],
    emits: ["row-click"],
};

const ModalConfirmacaoStub = {
    name: "ModalConfirmacao",
    template: '<div data-testid="modal-confirmacao"><slot /></div>',
    props: ['modelValue'],
    emits: ['confirmar', 'update:modelValue']
};

const BAlertStub = {
    name: "BAlert",
    template: '<div class="b-alert"><slot /></div>',
    props: ['modelValue', 'variant'],
    emits: ['dismissed']
};

describe("ProcessoView.vue", () => {
    let wrapper: any;
    let processosStore: any;
    let feedbackStore: any;
    let perfilStore: any;
    let router: any;

    const commonStubs = {
        ProcessoAcoes: ProcessoAcoesStub,
        ModalAcaoBloco: ModalAcaoBlocoStub,
        TreeTable: TreeTableStub,
        ModalConfirmacao: ModalConfirmacaoStub,
        BAlert: BAlertStub,
        BContainer: { template: '<div><slot /></div>' },
        BBadge: { template: '<span><slot /></span>' },
    };

    const mockProcesso = {
        codigo: 1,
        descricao: "Processo de Teste",
        tipo: "REVISAO",
        situacao: "EM_ANDAMENTO",
        unidades: [
            {
                codUnidade: 101,
                sigla: "UNI1",
                nome: "Unidade 1",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                codSubprocesso: 1001,
                filhos: [],
            },
            {
                codUnidade: 102,
                sigla: "UNI2",
                nome: "Unidade 2",
                situacaoSubprocesso: SituacaoSubprocesso.NAO_INICIADO,
                codSubprocesso: 1002,
                filhos: [],
            }
        ]
    };

    const createWrapper = () => {
        return mount(ProcessoView, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: true,
                    }),
                ],
                stubs: commonStubs,
            },
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
        // Clear stub mocks history
        (ModalAcaoBlocoStub.methods.abrir as any).mockClear();
        (ModalAcaoBlocoStub.methods.fechar as any).mockClear();
        (ModalAcaoBlocoStub.methods.setErro as any).mockClear();
        (ModalAcaoBlocoStub.methods.setProcessando as any).mockClear();
        // Reset router mock push spy
        mocks.push.mockClear();
        router = { push: mocks.push };
    });

    it("deve carregar e exibir os detalhes do processo ao montar", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await flushPromises();

        expect(processosStore.buscarContextoCompleto).toHaveBeenCalledWith(1);
        expect(wrapper.find('[data-testid="processo-info"]').text()).toBe("Processo de Teste");
        expect(wrapper.findComponent(TreeTableStub).exists()).toBe(true);
    });

    it("deve exibir alerta de erro da store", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.$patch({ lastError: { message: "Erro ao carregar", details: "Detalhes do erro" } });

        await nextTick();
        await flushPromises();

        const alert = wrapper.findComponent(BAlertStub);
        expect(alert.exists()).toBe(true);
        expect(alert.text()).toContain("Erro ao carregar");
        expect(alert.text()).toContain("Detalhes do erro");

        // Dismiss alert
        // The BAlert stub we defined emits 'dismissed'.
        // So we emit it on the component instance (vm).
        // Since BAlertStub is a stub object, wrapper.findComponent(BAlertStub) might not work if it doesn't match the mounted instance perfectly by reference.
        // We find by name.
        const alertCmp = wrapper.findComponent({ name: "BAlert" });
        await alertCmp.vm.$emit("dismissed");
        expect(processosStore.clearError).toHaveBeenCalled();
    });

    it("deve exibir botões de ação em bloco se houver unidades elegíveis", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        // Mocking computed getters by manipulating state they depend on
        perfilStore.$patch({ perfis: [Perfil.GESTOR, Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const botoesDiv = wrapper.find(".d-flex.gap-2.justify-content-end");
        expect(botoesDiv.exists()).toBe(true);

        const btnAceitar = botoesDiv.find("button.btn-success");
        expect(btnAceitar.exists()).toBe(true);
        expect(btnAceitar.text()).toContain("Aceitar em Bloco");
    });

    it("deve abrir modal de ação em bloco ao clicar no botão", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({ perfis: [Perfil.GESTOR, Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const btnAceitar = wrapper.find("button.btn-success");
        await btnAceitar.trigger("click");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.exists()).toBe(true);

        expect(ModalAcaoBlocoStub.methods.abrir).toHaveBeenCalled();
        expect(modal.props("titulo")).toBe("Aceitar em Bloco");
    });

    it("deve executar ação em bloco com sucesso (Aceitar Cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        feedbackStore = useFeedbackStore();

        perfilStore.$patch({ perfis: [Perfil.GESTOR, Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal 'aceitar'

        // Simular confirmação do modal
        const dadosConfirmacao = { ids: [101] };
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(subprocessoService.aceitarCadastroEmBloco).toHaveBeenCalledWith(1001, { unidadeCodigos: [101], dataLimite: undefined });
        expect(processosStore.buscarContextoCompleto).toHaveBeenCalledWith(1);
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.anything(), expect.stringContaining("realizada"), "success");
        expect(ModalAcaoBlocoStub.methods.fechar).toHaveBeenCalled();
    });

     it("deve lidar com erro na execução da ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({ perfis: [Perfil.GESTOR, Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const errorMsg = "Falha ao aceitar";
        (subprocessoService.aceitarCadastroEmBloco as any).mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal

        await modal.vm.$emit("confirmar", { ids: [101] });

        expect(subprocessoService.aceitarCadastroEmBloco).toHaveBeenCalled();
        expect(ModalAcaoBlocoStub.methods.setErro).toHaveBeenCalledWith(errorMsg);
        expect(ModalAcaoBlocoStub.methods.setProcessando).toHaveBeenCalledWith(false);
    });

    it("deve redirecionar para detalhes da unidade ao clicar na tabela (Gestor)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({
            perfilSelecionado: Perfil.GESTOR,
            perfis: [Perfil.GESTOR]
        });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        const rowItem = {
            codigo: 101,
            unidadeAtual: "UNI1",
            clickable: true
        };

        await treeTable.vm.$emit("row-click", rowItem);

        expect(mocks.push).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1",
                siglaUnidade: "UNI1"
            }
        });
    });

     it("não deve redirecionar para detalhes da unidade se não tiver permissão (Servidor de outra unidade)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({ perfilSelecionado: Perfil.SERVIDOR, unidadeSelecionada: 999 });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        await treeTable.vm.$emit("row-click", { codigo: 101, unidadeAtual: "UNI1", clickable: true });

        expect(router.push).not.toHaveBeenCalled();
    });

    it("deve abrir modal de finalização de processo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit("finalizar");

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        expect(modalConfirmacao.props("modelValue")).toBe(true);
    });

    it("deve confirmar finalização de processo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        feedbackStore = useFeedbackStore();
        //router = useRouter(); // Removed because we use mocked router

        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit("finalizar"); // Abre modal

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        await modalConfirmacao.vm.$emit("confirmar");

        expect(processosStore.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.any(String), "success");
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve tratar erro na finalização do processo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        feedbackStore = useFeedbackStore();

        processosStore.$patch({ processoDetalhe: mockProcesso });

        // Force error on the spy
        processosStore.finalizarProcesso.mockRejectedValue(new Error("Erro finalização"));

        await nextTick();
        await flushPromises();

        const acoes = wrapper.findComponent(ProcessoAcoesStub);
        await acoes.vm.$emit("finalizar");

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        await modalConfirmacao.vm.$emit("confirmar");

        expect(processosStore.finalizarProcesso).toHaveBeenCalled();
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro ao finalizar", "Erro finalização", "danger");
    });
});

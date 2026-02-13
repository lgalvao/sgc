import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Processo from "@/views/Processo.vue";
import {createTestingPinia} from "@pinia/testing";
import {useProcessosStore} from "@/stores/processos";
import {useProcessosCoreStore} from "@/stores/processos/core";
import {useFeedbackStore} from "@/stores/feedback";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil, SituacaoSubprocesso} from "@/types/tipos";
import {nextTick} from "vue";
import {checkA11y} from "@/test-utils/a11yTestHelpers";

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
    props: ["podeAceitarBloco", "podeHomologarBloco", "podeFinalizar"],
    emits: ["finalizar"],
};

const modalSpies = {
    abrir: vi.fn(),
    fechar: vi.fn(),
    setErro: vi.fn(),
    setProcessando: vi.fn(),
};

const ModalAcaoBlocoStub = {
    name: "ModalAcaoBloco",
    template: '<div data-testid="modal-acao-bloco"></div>',
    props: ["id", "titulo", "texto", "rotuloBotao", "unidades", "mostrar", "tipo", "unidadesPreSelecionadas", "mostrarDataLimite"],
    setup(props: any, { expose }: any) {
        expose(modalSpies);
        return modalSpies;
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

describe("Processo.vue", () => {
    let wrapper: any;
    let processosStore: any;
    let feedbackStore: any;
    let perfilStore: any;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
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
        podeHomologarCadastro: true,
        podeHomologarMapa: true,
        podeAceitarCadastroBloco: true,
        podeDisponibilizarMapaBloco: true,
        podeFinalizar: true,
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
            },
             {
                codUnidade: 103,
                sigla: "UNI3",
                nome: "Unidade 3",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                codSubprocesso: 1003,
                filhos: [],
            },
             {
                codUnidade: 104,
                sigla: "UNI4",
                nome: "Unidade 4",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                codSubprocesso: 1004,
                filhos: [],
            }
        ]
    };

    const createWrapper = () => {
        return mount(Processo, {
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
        modalSpies.abrir.mockClear();
        modalSpies.fechar.mockClear();
        modalSpies.setErro.mockClear();
        modalSpies.setProcessando.mockClear();
        // Reset router mock push spy
        mocks.push.mockClear();
    });

    it("deve carregar e exibir os detalhes do processo ao montar", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await flushPromises();

        expect(processosStore.buscarContextoCompleto).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain("Processo de Teste");
        expect(wrapper.findComponent(TreeTableStub).exists()).toBe(true);
    });

    it("deve exibir alerta de erro da store", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        const coreStore = useProcessosCoreStore();
        coreStore.$patch({ 
            lastError: { 
                kind: 'unexpected' as const, 
                message: "Erro ao carregar", 
                details: { info: "Detalhes do erro" } as Record<string, any>
            } 
        });

        await nextTick();
        await flushPromises();

        const alert = wrapper.findComponent(BAlertStub);
        expect(alert.exists()).toBe(true);
        expect(alert.text()).toContain("Erro ao carregar");
        expect(alert.text()).toContain("Detalhes do erro");

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

        // Check if buttons exist directly as the container class might have changed
        const btnAceitar = wrapper.find("button.btn-success");
        expect(btnAceitar.exists()).toBe(true);
        expect(btnAceitar.text()).toContain("Aceitar em bloco");

        // Homologar
        const btnHomologar = wrapper.find("button.btn-warning");
        expect(btnHomologar.exists()).toBe(true);

        // Disponibilizar
        const btnDisponibilizar = wrapper.find("button.btn-info");
        expect(btnDisponibilizar.exists()).toBe(true);
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
        expect(modalSpies.abrir).toHaveBeenCalled();
        expect(modal.props("titulo")).toBe("Aceitar em Bloco");
    });

    // --- Testes para Aceitar em Bloco ---

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

        // Simular confirmação do modal com ID 101 (Mapeamento Cadastro Disponibilizado)
        const dadosConfirmacao = { ids: [101] };
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('aceitar', [101], undefined);
        expect(feedbackStore.show).toHaveBeenCalledWith("Sucesso", "Cadastros aceitos em bloco", "success");
        expect(modalSpies.fechar).toHaveBeenCalled();
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Aceitar Validação)", async () => {
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

        // Simular confirmação com ID 103 (Mapa Validado)
        const dadosConfirmacao = { ids: [103] };
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('aceitar', [103], undefined);
    });

    // --- Testes para Homologar em Bloco ---

    it("deve executar ação em bloco com sucesso (Homologar Cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({ perfis: [Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-warning").trigger("click"); // Abrir modal 'homologar'

        // ID 101 -> Cadastro Disponibilizado
        await modal.vm.$emit("confirmar", { ids: [101] });

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('homologar', [101], undefined);
    });

    it("deve executar ação em bloco com sucesso (Homologar Validação)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({ perfis: [Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-warning").trigger("click"); // Abrir modal 'homologar'

        // ID 103 -> Mapa Validado
        await modal.vm.$emit("confirmar", { ids: [103] });

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('homologar', [103], undefined);
    });

    // --- Teste para Disponibilizar em Bloco ---

    it("deve executar ação em bloco com sucesso (Disponibilizar)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({ perfis: [Perfil.ADMIN] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-info").trigger("click"); // Abrir modal 'disponibilizar'

        // ID 104 -> Mapa Criado
        await modal.vm.$emit("confirmar", { ids: [104], dataLimite: '2024-12-31' });

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('disponibilizar', [104], '2024-12-31');
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
        // Mock implementation of the action to throw error
        processosStore.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal

        await modal.vm.$emit("confirmar", { ids: [101] });

        expect(processosStore.executarAcaoBloco).toHaveBeenCalled();
        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
        expect(modalSpies.setProcessando).toHaveBeenCalledWith(false);
    });

    it("deve mostrar erro se unidade não for encontrada para ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        feedbackStore = useFeedbackStore();

        perfilStore.$patch({ perfis: [Perfil.GESTOR] });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const errorMsg = "Unidade selecionada não encontrada no contexto do processo.";
        processosStore.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click");

        // ID inexistente
        await modal.vm.$emit("confirmar", { ids: [9999] });

        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
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
            unidadeAtual: "UNI1 - Unidade 1",
            sigla: "UNI1",
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

     it("deve redirecionar para detalhes da unidade mesmo como Servidor (controle é no backend)", async () => {
        mocks.push.mockClear(); // Limpa chamadas anteriores
        
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        // Servidor - o controle de acesso agora é no backend, não no frontend
        perfilStore.$patch({ 
            perfilSelecionado: Perfil.SERVIDOR, 
            unidadeSelecionada: 999,
            perfis: [Perfil.SERVIDOR]
        });
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        await treeTable.vm.$emit("row-click", { codigo: 101, unidadeAtual: "UNI1", sigla: "UNI1", clickable: true });

        expect(mocks.push).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1",
                siglaUnidade: "UNI1"
            }
        });
    });

    // --- Finalização ---

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

    it("deve ser acessível", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.$patch({ processoDetalhe: mockProcesso });

        await flushPromises();
        await checkA11y(wrapper.element as HTMLElement);
    });
});

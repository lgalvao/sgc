import {describe, it, expect, vi, beforeEach, afterEach} from "vitest";
import {mount, flushPromises, DOMWrapper} from "@vue/test-utils";
import ProcessoView from "../ProcessoDetalheView.vue";
import {createTestingPinia} from "@pinia/testing";
import {usePerfilStore} from "@/stores/perfil";
import {useToastStore} from "@/stores/toast";
import {nextTick, ref} from "vue";
import {Perfil, SituacaoProcesso, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

// Mocks
vi.mock("@/services/processoService", () => ({
    buscarContextoCompleto: vi.fn().mockResolvedValue({elegiveis: []}),
    obterDetalhesProcesso: vi.fn().mockResolvedValue({}),
    buscarSubprocessosElegiveis: vi.fn().mockResolvedValue([]),
    executarAcaoBloco: vi.fn().mockResolvedValue({}),
    finalizarProcesso: vi.fn().mockResolvedValue({}),
    enviarLembrete: vi.fn().mockResolvedValue({})
}));

const processosMock = {
    processoDetalhe: ref<any>(null),
    subprocessosElegiveis: ref<any[]>([]),
    lastError: ref<any>(null),
    clearError: vi.fn(),
    buscarContextoCompleto: vi.fn().mockResolvedValue(undefined),
    finalizarProcesso: vi.fn().mockResolvedValue(undefined),
    executarAcaoBloco: vi.fn().mockResolvedValue(undefined),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock
}));

const mocks = {
    push: vi.fn(),
    params: {codProcesso: "1"},
    query: {}
};

vi.mock("vue-router", () => ({
    useRoute: () => ({params: mocks.params, query: mocks.query}),
    useRouter: () => ({push: mocks.push})
}));

const mockProcesso = {
    codigo: 1,
    descricao: "Processo Teste",
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    unidades: [
        {
            codUnidade: 101,
            sigla: "UNI1",
            nome: "Unidade 1",
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            clickable: true,
            filhos: []
        },
        {
            codUnidade: 102,
            sigla: "UNI2",
            nome: "Unidade 2",
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            clickable: false,
            filhos: []
        },
        {
            codUnidade: 103,
            sigla: "UNI3",
            nome: "Unidade 3",
            situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
            clickable: true,
            filhos: []
        }
    ],
    podeFinalizar: true,
    podeAceitarCadastroBloco: true,
    podeHomologarCadastro: true,
    podeHomologarMapa: true,
    podeDisponibilizarMapaBloco: true
};

const mockElegiveis = [
    {
        unidadeCodigo: 101,
        unidadeSigla: "UNI1",
        unidadeNome: "Unidade 1",
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
    },
    {
        unidadeCodigo: 103,
        unidadeSigla: "UNI3",
        unidadeNome: "Unidade 3",
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO
    },
    {
        unidadeCodigo: 104,
        unidadeSigla: "UNI4",
        unidadeNome: "Unidade 4",
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO
    }
];

const modalSpies = {
    abrir: vi.fn(),
    fechar: vi.fn(),
    setProcessando: vi.fn(),
    setErro: vi.fn()
};

const ModalAcaoBlocoStub = {
    name: "ModalAcaoBloco",
    template: '<div id="modal-acao-bloco-stub"><slot></slot></div>',
    props: ["unidades", "titulo", "texto", "rotuloBotao", "mostrarDataLimite", "unidadesPreSelecionadas"],
    methods: modalSpies
};

const ModalConfirmacaoStub = {
    name: "ModalConfirmacao",
    template: '<div id="modal-confirmacao-stub"><slot></slot></div>',
    props: ["modelValue", "titulo", "variant", "okTitle"]
};

const TreeTableStub = {
    name: "ProcessoSubprocessosTable",
    template: '<div id="tree-table-stub"></div>',
    props: ["participantesHierarquia"]
};

const PageHeaderStub = {
    name: "PageHeader",
    template: '<div><slot></slot><slot name="actions"></slot></div>',
    props: ["title"]
};

const ProcessoInfoStub = {
    name: "ProcessoInfo",
    template: "<div></div>",
    props: ["situacao", "tipo", "showDataLimite"]
};

const BAlertStub = {
    name: "BAlert",
    template: "<div><slot></slot></div>",
    props: ["variant", "dismissible", "modelValue"]
};

const BSpinnerStub = {
    name: "BSpinner",
    template: "<div></div>",
    props: ["label", "variant"]
};

describe("Processo.vue", () => {
    let wrapper: any;
    let perfilStore: any;
    let toastStore: any;

    const createWrapper = () => {
        return mount(ProcessoView, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: true
                    })
                ],
                stubs: {
                    ModalAcaoBloco: ModalAcaoBlocoStub,
                    ModalConfirmacao: ModalConfirmacaoStub,
                    ProcessoSubprocessosTable: TreeTableStub,
                    PageHeader: PageHeaderStub,
                    ProcessoInfo: ProcessoInfoStub,
                    BAlert: BAlertStub,
                    BSpinner: BSpinnerStub,
                    BButton: {template: "<button><slot></slot></button>"}
                }
            }
        });
    };

    const aplicarContextoProcesso = (processo = mockProcesso, elegiveis = mockElegiveis) => {
        processosMock.processoDetalhe.value = processo;
        processosMock.subprocessosElegiveis.value = elegiveis;
        processosMock.lastError.value = null;
    };

    beforeEach(() => {
        vi.clearAllMocks();
        processosMock.processoDetalhe.value = null;
        processosMock.subprocessosElegiveis.value = [];
        processosMock.lastError.value = null;
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        toastStore = useToastStore();
    });

    afterEach(() => {
        wrapper.unmount();
    });

    it("deve carregar detalhes do processo ao montar", async () => {
        aplicarContextoProcesso();
        expect(processosMock.buscarContextoCompleto).toHaveBeenCalledWith(1);
    });

    it("deve exibir erro se falhar ao carregar processo", async () => {
        processosMock.lastError.value = {message: "Erro ao carregar"};
        await nextTick();

        const alert = wrapper.find('[data-testid="app-alert"]');
        expect(alert.exists()).toBe(true);
        expect(alert.text()).toContain("Erro ao carregar");

        const alertCmp = wrapper.findComponent(BAlertStub);
        await alertCmp.vm.$emit("dismissed");
        expect(processosMock.clearError).toHaveBeenCalled();
    });

    it("deve exibir botões de ação em bloco se houver unidades elegíveis", async () => {
        aplicarContextoProcesso();

        // Test GESTOR buttons
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').text()).toContain(TEXTOS.acaoBloco.aceitar.ROTULO_MISTO);

        // Test ADMIN buttons
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-disponibilizar-bloco"]').exists()).toBe(true);
    });

    it("não deve exibir botões de ação em bloco duplicados (apenas um conjunto)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        aplicarContextoProcesso();

        // Test GESTOR
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        const botoesAceitar = wrapper.findAll('[data-testid="btn-processo-aceitar-bloco"]').filter(
            (b: DOMWrapper<Element>) => b.text().includes(TEXTOS.acaoBloco.aceitar.ROTULO_MISTO)
        );
        expect(botoesAceitar.length).toBe(1);

        // Test ADMIN
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        const botoesHomologar = wrapper.findAll('[data-testid="btn-processo-homologar-bloco"]').filter(
            (b: DOMWrapper<Element>) => b.text().includes(TEXTOS.acaoBloco.homologar.ROTULO_MISTO)
        );
        expect(botoesHomologar.length).toBe(1);
    });

    it("deve abrir modal de ação em bloco ao clicar no botão", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const btnAceitar = wrapper.find('#btn-aceitar-bloco');
        await btnAceitar.trigger("click");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.aceitar.TITULO_MISTO);
    });

    it("deve executar ação em bloco com sucesso (Aceitar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-aceitar-bloco').trigger("click"); // Abrir modal 'aceitar'

        // Simular confirmação do modal com ID 101 (Mapeamento cadastro disponibilizado)
        const dadosConfirmacao = {ids: [101]};
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processosMock.executarAcaoBloco).toHaveBeenCalledWith('aceitar', [101], undefined);
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS.sucesso.CADASTROS_ACEITOS_EM_BLOCO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Homologar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-homologar-bloco').trigger("click"); // Abrir modal 'homologar'

        // ID 101 -> Cadastro disponibilizado
        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processosMock.executarAcaoBloco).toHaveBeenCalledWith('homologar', [101], undefined);
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.homologar.TITULO_MISTO);
    });

    it("deve executar ação em bloco com sucesso (Homologar validação)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-homologar-bloco').trigger("click"); // Abrir modal 'homologar'

        // ID 103 -> Mapa validado
        await modal.vm.$emit("confirmar", {ids: [103]});

        expect(processosMock.executarAcaoBloco).toHaveBeenCalledWith('homologar', [103], undefined);
    });

    it("deve usar textos de CDU-23 quando houver apenas cadastro elegível para homologacao", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso(
            {
                ...mockProcesso,
                unidades: [mockProcesso.unidades[0]]
            },
            [mockElegiveis[0]]
        );

        await nextTick();
        await flushPromises();

        await wrapper.find('#btn-homologar-bloco').trigger("click");
        expect(wrapper.find('#btn-homologar-bloco').text()).toContain(TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO);
        expect((wrapper.vm).tituloModalBloco).toBe(TEXTOS.acaoBloco.homologar.TITULO_CADASTRO);
        expect((wrapper.vm).textoModalBloco).toBe(TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO);
        expect((wrapper.vm).rotuloBotaoBloco).toBe(TEXTOS.acaoBloco.homologar.BOTAO);
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO);

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [101]});
        await flushPromises();

        expect(toastStore.setPending).not.toHaveBeenCalled();
        expect(mocks.push).not.toHaveBeenCalledWith("/painel");
        expect(wrapper.find('[data-testid="app-alert"]').text()).toContain(TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO);
    });

    it("deve usar textos de CDU-26 quando houver apenas validacao elegível para homologacao", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso(
            {
                ...mockProcesso,
                unidades: [{
                    ...mockProcesso.unidades[2]
                }]
            },
            [mockElegiveis[1]]
        );

        await nextTick();
        await flushPromises();

        expect(wrapper.find('#btn-homologar-bloco').text()).toContain(TEXTOS.acaoBloco.homologar.ROTULO_VALIDACAO);
        await wrapper.find('#btn-homologar-bloco').trigger("click");
        expect((wrapper.vm).tituloModalBloco).toBe(TEXTOS.acaoBloco.homologar.TITULO_VALIDACAO);
        expect((wrapper.vm).textoModalBloco).toBe(TEXTOS.acaoBloco.homologar.TEXTO_VALIDACAO);
        expect((wrapper.vm).rotuloBotaoBloco).toBe(TEXTOS.acaoBloco.homologar.BOTAO);
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe(TEXTOS.sucesso.MAPAS_HOMOLOGADOS_EM_BLOCO);

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [103]});
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS.sucesso.MAPAS_HOMOLOGADOS_EM_BLOCO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Disponibilizar)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-disponibilizar-bloco').trigger("click"); // Abrir modal 'disponibilizar'

        // ID 104 -> Mapa criado
        await modal.vm.$emit("confirmar", {ids: [104], dataLimite: '2024-12-31'});

        expect(processosMock.executarAcaoBloco).toHaveBeenCalledWith('disponibilizar', [104], '2024-12-31');
        expect(wrapper.find('#btn-disponibilizar-bloco').text()).toContain(TEXTOS.acaoBloco.disponibilizar.ROTULO);
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.disponibilizar.TITULO);
    });

    it("deve lidar com erro na execução da ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const errorMsg = "Falha ao aceitar";
        processosMock.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-aceitar-bloco').trigger("click"); // Abrir modal

        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processosMock.executarAcaoBloco).toHaveBeenCalled();
        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
        expect(modalSpies.setProcessando).toHaveBeenCalledWith(false);
    });

    it("deve mostrar erro se unidade não for encontrada para ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const errorMsg = "Unidade selecionada não encontrada no contexto do processo.";
        processosMock.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-aceitar-bloco').trigger("click");

        // ID inexistente
        await modal.vm.$emit("confirmar", {ids: [9999]});

        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
    });

    it("deve exibir apenas o botão aceitar quando permissões do processo focam nisso (ex: Gestor)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso({
            ...mockProcesso,
            podeAceitarCadastroBloco: true,
            podeHomologarCadastro: false,
            podeHomologarMapa: false,
            podeDisponibilizarMapaBloco: false,
        });

        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-processo-disponibilizar-bloco"]').exists()).toBe(false);
    });

    it("deve exibir botões homologar e disponibilizar quando permissões focam nisso (ex: Admin)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso({
            ...mockProcesso,
            podeAceitarCadastroBloco: false,
            podeHomologarCadastro: true,
            podeHomologarMapa: true,
            podeDisponibilizarMapaBloco: true,
        });

        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-disponibilizar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').exists()).toBe(false);
    });

    it("deve redirecionar para detalhes da unidade ao clicar na tabela (Gestor)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({
            perfilSelecionado: Perfil.GESTOR,
            perfis: [Perfil.GESTOR]
        });
        aplicarContextoProcesso();

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

        // Servidor - o controle de acesso agora é no backend, não no frontend
        perfilStore.$patch({
            perfilSelecionado: Perfil.SERVIDOR,
            unidadeSelecionada: 999,
        });
        aplicarContextoProcesso();

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

    it("deve abrir modal de finalização de processo", async () => {
        wrapper = createWrapper();
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const btnFinalizar = wrapper.find('[data-testid="btn-processo-finalizar"]');
        await btnFinalizar.trigger("click");

        expect((wrapper.vm).mostrarModalFinalizacao).toBe(true);
    });

    it("deve chamar API de finalizar processo com sucesso", async () => {
        wrapper = createWrapper();
        toastStore = useToastStore();
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        await (wrapper.vm).confirmarFinalizacao();

        expect(processosMock.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS.sucesso.PROCESSO_FINALIZADO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });
});

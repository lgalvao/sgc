import {beforeEach, describe, expect, it, vi} from "vitest";
import {type DOMWrapper, flushPromises, mount} from "@vue/test-utils";
import Processo from "@/views/ProcessoDetalheView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useProcessosStore} from "@/stores/processos";
import {useToastStore} from "@/stores/toast";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil, SituacaoSubprocesso, type SubprocessoElegivel} from "@/types/tipos";
import {nextTick} from "vue";

type UnidadeParticipanteMock = {
    codUnidade: number;
    sigla: string;
    nome: string;
    situacaoSubprocesso: SituacaoSubprocesso;
    localizacaoAtualCodigo: number;
    codSubprocesso: number;
    filhos: UnidadeParticipanteMock[];
};

type ProcessoDetalheMock = {
    codigo: number;
    descricao: string;
    tipo: string;
    situacao: string;
    podeHomologarCadastro: boolean;
    podeHomologarMapa: boolean;
    podeAceitarCadastroBloco: boolean;
    podeDisponibilizarMapaBloco: boolean;
    podeFinalizar: boolean;
    unidades: UnidadeParticipanteMock[];
};

const mocks = vi.hoisted(() => ({
    push: vi.fn(),
}));

vi.mock("vue-router", () => ({
    useRoute: () => ({
        params: {
            codProcesso: "1",
        },
        query: {codProcesso: "1"}
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

vi.mock("@/services/subprocessoService", () => ({
    aceitarCadastroEmBloco: vi.fn(),
    aceitarValidacaoEmBloco: vi.fn(),
    homologarCadastroEmBloco: vi.fn(),
    homologarValidacaoEmBloco: vi.fn(),
    disponibilizarMapaEmBloco: vi.fn(),
}));

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
    setup(props: any, {expose}: any) {
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
    let toastStore: any;
    let perfilStore: any;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    let router: any;

    const commonStubs = {
        ModalAcaoBloco: ModalAcaoBlocoStub,
        TreeTable: TreeTableStub,
        ModalConfirmacao: ModalConfirmacaoStub,
        BAlert: BAlertStub,
        BContainer: {template: '<div><slot /></div>'},
        BBadge: {template: '<span><slot /></span>'},
    };

    const mockProcesso: ProcessoDetalheMock = {
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
                localizacaoAtualCodigo: 999,
                codSubprocesso: 1001,
                filhos: [],
            },
            {
                codUnidade: 102,
                sigla: "UNI2",
                nome: "Unidade 2",
                situacaoSubprocesso: SituacaoSubprocesso.NAO_INICIADO,
                localizacaoAtualCodigo: 999,
                codSubprocesso: 1002,
                filhos: [],
            },
            {
                codUnidade: 103,
                sigla: "UNI3",
                nome: "Unidade 3",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                localizacaoAtualCodigo: 999,
                codSubprocesso: 1003,
                filhos: [],
            },
            {
                codUnidade: 104,
                sigla: "UNI4",
                nome: "Unidade 4",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                localizacaoAtualCodigo: 999,
                codSubprocesso: 1004,
                filhos: [],
            }
        ]
    };

    const mockElegiveis: SubprocessoElegivel[] = [
        {
            codigo: 1001,
            unidadeCodigo: 101,
            unidadeSigla: "UNI1",
            unidadeNome: "Unidade 1",
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        },
        {
            codigo: 1003,
            unidadeCodigo: 103,
            unidadeSigla: "UNI3",
            unidadeNome: "Unidade 3",
            situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
        },
        {
            codigo: 1004,
            unidadeCodigo: 104,
            unidadeSigla: "UNI4",
            unidadeNome: "Unidade 4",
            situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
        }
    ];

    const aplicarContextoProcesso = (processo: ProcessoDetalheMock = mockProcesso, elegiveis: SubprocessoElegivel[] = mockElegiveis) => {
        processosStore.$patch({
            processoDetalhe: processo,
            subprocessosElegiveis: elegiveis,
        });
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
        aplicarContextoProcesso();

        await flushPromises();

        expect(processosStore.buscarContextoCompleto).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain("Processo de Teste");
        expect(wrapper.findComponent(TreeTableStub).exists()).toBe(true);
    });

    it("deve exibir alerta de erro da store", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        processosStore.lastError = {
            kind: 'unexpected' as const,
            message: "Erro ao carregar",
            details: {info: "Detalhes do erro"} as Record<string, any>
        };

        await nextTick();
        await flushPromises();

        const alert = wrapper.findComponent(BAlertStub);
        expect(alert.exists()).toBe(true);
        expect(alert.text()).toContain("Erro ao carregar");

        const alertCmp = wrapper.findComponent({name: "BAlert"});
        await alertCmp.vm.$emit("dismissed");
        expect(processosStore.clearError).toHaveBeenCalled();
    });

    it("deve exibir botões de ação em bloco se houver unidades elegíveis", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        aplicarContextoProcesso();

        // Test GESTOR buttons
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        expect(wrapper.find("button.btn-success").exists()).toBe(true);
        expect(wrapper.find("button.btn-success").text()).toContain("Registrar aceite em bloco");

        // Test ADMIN buttons
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        expect(wrapper.find("button.btn-warning").exists()).toBe(true);
        expect(wrapper.find("button.btn-info").exists()).toBe(true);
    });

    it("não deve exibir botões de ação em bloco duplicados (apenas um conjunto)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        aplicarContextoProcesso();

        // Test GESTOR
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        const botoesAceitar = wrapper.findAll("button.btn-success").filter(
            (b: DOMWrapper<Element>) => b.text().includes("Registrar aceite em bloco")
        );
        expect(botoesAceitar.length).toBe(1);

        // Test ADMIN
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        const botoesHomologar = wrapper.findAll("button.btn-warning").filter(
            (b: DOMWrapper<Element>) => b.text().includes("Homologar em bloco")
        );
        expect(botoesHomologar.length).toBe(1);
    });

    it("deve abrir modal de ação em bloco ao clicar no botão", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const btnAceitar = wrapper.find("button.btn-success");
        await btnAceitar.trigger("click");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props("titulo")).toBe("Aceite em bloco");
    });

    it("deve executar ação em bloco com sucesso (Aceitar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal 'aceitar'

        // Simular confirmação do modal com ID 101 (Mapeamento cadastro disponibilizado)
        const dadosConfirmacao = {ids: [101]};
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('aceitar', [101], undefined);
        expect(toastStore.setPending).toHaveBeenCalledWith("Cadastros aceitos em bloco");
        expect(modalSpies.fechar).toHaveBeenCalled();
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Aceitar validação)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal 'aceitar'

        // Simular confirmação com ID 103 (Mapa validado)
        const dadosConfirmacao = {ids: [103]};
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('aceitar', [103], undefined);
        expect(modal.props("titulo")).toBe("Aceite em bloco");
    });

    it("deve usar textos de CDU-22 quando houver apenas cadastro elegível para aceite", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso(
            {
                ...mockProcesso,
                unidades: [mockProcesso.unidades[0]]
            },
            [mockElegiveis[0]]
        );

        await nextTick();
        await flushPromises();

        expect(wrapper.find("button.btn-success").text()).toContain("Aceitar cadastro em bloco");
        await wrapper.find("button.btn-success").trigger("click");
        expect((wrapper.vm).tituloModalBloco).toBe("Aceite de cadastro em bloco");
        expect((wrapper.vm).textoModalBloco).toBe("Selecione as unidades cujos cadastros deverão ser aceitos:");
        expect((wrapper.vm).rotuloBotaoBloco).toBe("Registrar aceite");
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe("Cadastros aceitos em bloco");
    });

    it("deve usar textos de CDU-25 quando houver apenas validacao elegível para aceite", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
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

        expect(wrapper.find("button.btn-success").text()).toContain("Aceitar mapas em bloco");
        await wrapper.find("button.btn-success").trigger("click");
        expect((wrapper.vm).tituloModalBloco).toBe("Aceite de mapas em bloco");
        expect((wrapper.vm).textoModalBloco).toBe("Selecione as unidades para aceite dos mapas correspondentes");
        expect((wrapper.vm).rotuloBotaoBloco).toBe("Registrar aceite");
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe("Mapas aceitos em bloco");
    });

    it("deve executar ação em bloco com sucesso (Homologar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-warning").trigger("click"); // Abrir modal 'homologar'

        // ID 101 -> Cadastro disponibilizado
        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('homologar', [101], undefined);
        expect(modal.props("titulo")).toBe("Homologação em bloco");
    });

    it("deve executar ação em bloco com sucesso (Homologar validação)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-warning").trigger("click"); // Abrir modal 'homologar'

        // ID 103 -> Mapa validado
        await modal.vm.$emit("confirmar", {ids: [103]});

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('homologar', [103], undefined);
    });

    it("deve usar textos de CDU-23 quando houver apenas cadastro elegível para homologacao", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
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

        await wrapper.find("button.btn-warning").trigger("click");
        expect(wrapper.find("button.btn-warning").text()).toContain("Homologar em bloco");
        expect((wrapper.vm).tituloModalBloco).toBe("Homologação de cadastro em bloco");
        expect((wrapper.vm).textoModalBloco).toBe("Selecione abaixo as unidades cujos cadastros deverão ser homologados:");
        expect((wrapper.vm).rotuloBotaoBloco).toBe("Homologar");
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe("Cadastros homologados em bloco");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [101]});
        await flushPromises();

        expect(toastStore.setPending).not.toHaveBeenCalled();
        expect(mocks.push).not.toHaveBeenCalledWith("/painel");
        expect(wrapper.find('[data-testid="app-alert"]').text()).toContain("Cadastros homologados em bloco");
    });

    it("deve usar textos de CDU-26 quando houver apenas validacao elegível para homologacao", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
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

        expect(wrapper.find("button.btn-warning").text()).toContain("Homologar mapa de competências em bloco");
        await wrapper.find("button.btn-warning").trigger("click");
        expect((wrapper.vm).tituloModalBloco).toBe("Homologação de mapa em bloco");
        expect((wrapper.vm).textoModalBloco).toBe("Selecione abaixo as unidades cujos mapas deverão ser homologados:");
        expect((wrapper.vm).rotuloBotaoBloco).toBe("Homologar");
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe("Mapas de competências homologados em bloco");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [103]});
        expect(toastStore.setPending).toHaveBeenCalledWith("Mapas de competências homologados em bloco");
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Disponibilizar)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-info").trigger("click"); // Abrir modal 'disponibilizar'

        // ID 104 -> Mapa criado
        await modal.vm.$emit("confirmar", {ids: [104], dataLimite: '2024-12-31'});

        expect(processosStore.executarAcaoBloco).toHaveBeenCalledWith('disponibilizar', [104], '2024-12-31');
        expect(wrapper.find("button.btn-info").text()).toContain("Disponibilizar mapas em bloco");
        expect(modal.props("titulo")).toBe("Disponibilização de mapa em bloco");
    });

    it("deve lidar com erro na execução da ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const errorMsg = "Falha ao aceitar";
        processosStore.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click"); // Abrir modal

        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processosStore.executarAcaoBloco).toHaveBeenCalled();
        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
        expect(modalSpies.setProcessando).toHaveBeenCalledWith(false);
    });

    it("deve mostrar erro se unidade não for encontrada para ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const errorMsg = "Unidade selecionada não encontrada no contexto do processo.";
        processosStore.executarAcaoBloco.mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find("button.btn-success").trigger("click");

        // ID inexistente
        await modal.vm.$emit("confirmar", {ids: [9999]});

        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
    });

    it("deve exibir apenas o botão aceitar quando permissões do processo focam nisso (ex: Gestor)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

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

        expect(wrapper.find("button.btn-success").exists()).toBe(true);
        expect(wrapper.find("button.btn-warning").exists()).toBe(false);
        expect(wrapper.find("button.btn-info").exists()).toBe(false);
    });

    it("deve exibir botões homologar e disponibilizar quando permissões focam nisso (ex: Admin)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

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

        expect(wrapper.find("button.btn-warning").exists()).toBe(true);
        expect(wrapper.find("button.btn-info").exists()).toBe(true);
        expect(wrapper.find("button.btn-success").exists()).toBe(false);
    });

    it("deve redirecionar para detalhes da unidade ao clicar na tabela (Gestor)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        processosStore = useProcessosStore();

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
        processosStore = useProcessosStore();

        // Servidor - o controle de acesso agora é no backend, não no frontend
        perfilStore.$patch({
            perfilSelecionado: Perfil.SERVIDOR,
            unidadeSelecionada: 999,
        });
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        await treeTable.vm.$emit("row-click", {codigo: 101, unidadeAtual: "UNI1", sigla: "UNI1", clickable: true});

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
        processosStore = useProcessosStore();
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        await wrapper.find('[data-testid="btn-processo-finalizar"]').trigger('click');

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        expect(modalConfirmacao.props("modelValue")).toBe(true);
    });

    it("deve confirmar finalização de processo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        toastStore = useToastStore();

        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        await wrapper.find('[data-testid="btn-processo-finalizar"]').trigger('click'); // Abre modal

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        await modalConfirmacao.vm.$emit("confirmar");

        expect(processosStore.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(toastStore.setPending).toHaveBeenCalledWith("Processo finalizado");
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve tratar erro na finalização do processo", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();

        aplicarContextoProcesso();

        // Force error on the spy
        processosStore.finalizarProcesso.mockRejectedValue(new Error("Erro finalização"));

        await nextTick();
        await flushPromises();

        await wrapper.find('[data-testid="btn-processo-finalizar"]').trigger('click');

        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        await modalConfirmacao.vm.$emit("confirmar");

        expect(processosStore.finalizarProcesso).toHaveBeenCalled();
        expect(mocks.push).not.toHaveBeenCalled();
    });

    it("não deve redirecionar se unidade não for clicável", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        aplicarContextoProcesso();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        await treeTable.vm.$emit("row-click", {sigla: "UNI1", clickable: false});

        expect(mocks.push).not.toHaveBeenCalled();
    });

    it("deve usar mensagem de erro da store ao falhar finalização", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        aplicarContextoProcesso();

        await nextTick();
        await flushPromises();

        processosStore.finalizarProcesso.mockRejectedValue(new Error("Erro original"));
        processosStore.lastError = {message: "Erro customizado da store"};

        await wrapper.find('[data-testid="btn-processo-finalizar"]').trigger('click'); // Abre modal
        const modalConfirmacao = wrapper.findComponent(ModalConfirmacaoStub);
        await modalConfirmacao.vm.$emit("confirmar");

        expect(mocks.push).not.toHaveBeenCalledWith("/painel");
    });

    it("deve cobrir default cases nos switches de useProcessoView", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        aplicarContextoProcesso();
        await flushPromises();

        // Acessamos as computeds via wrapper.vm
        (wrapper.vm).acaoBlocoAtual = "invalido";
        expect((wrapper.vm).tituloModalBloco).toBe("");
        expect((wrapper.vm).textoModalBloco).toBe("");
        expect((wrapper.vm).rotuloBotaoBloco).toBe("");
        expect((wrapper.vm).mensagemSucessoAcaoBloco).toBe("Ação em bloco realizada");
    });

    it("deve achatar unidades recursivamente", async () => {
        wrapper = createWrapper();
        processosStore = useProcessosStore();
        const processoComFilhos = {
            ...mockProcesso,
            unidades: [{
                codUnidade: 201,
                sigla: "PAI",
                nome: "Unidade pai",
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                localizacaoAtualCodigo: 999,
                codSubprocesso: 2001,
                filhos: [{
                    codUnidade: 202,
                    sigla: "FILHO",
                    nome: "Unidade filho",
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                    localizacaoAtualCodigo: 999,
                    codSubprocesso: 2002,
                    filhos: []
                }]
            }]
        };
        aplicarContextoProcesso(processoComFilhos, [
            {
                codigo: 2001,
                unidadeCodigo: 201,
                unidadeSigla: "PAI",
                unidadeNome: "Unidade pai",
                situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
            },
            {
                codigo: 2002,
                unidadeCodigo: 202,
                unidadeSigla: "FILHO",
                unidadeNome: "Unidade filho",
                situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
            }
        ]);

        (wrapper.vm).acaoBlocoAtual = 'disponibilizar';
        await nextTick();

        expect((wrapper.vm).unidadesElegiveis).toHaveLength(2);
    });
});

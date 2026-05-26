import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {DOMWrapper, flushPromises, mount} from "@vue/test-utils";
import ProcessoView from "../ProcessoDetalheView.vue";
import {createTestingPinia} from "@pinia/testing";
import {usePerfilStore} from "@/stores/perfil";
import {useToastStore} from "@/stores/toast";
import {nextTick, ref} from "vue";
import {Perfil, SituacaoProcesso, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import * as processoService from "@/services/processo";

// Mocks
vi.mock("@/services/processo", () => ({
    buscarContextoCompleto: vi.fn().mockResolvedValue({elegiveis: []}),
    executarAcaoEmBloco: vi.fn().mockResolvedValue({}),
    finalizarProcesso: vi.fn().mockResolvedValue({}),
    enviarLembrete: vi.fn().mockResolvedValue({})
}));

const mocks = {
    push: vi.fn(),
    params: {codProcesso: "1"},
    query: {}
};

const processoQueryData = ref<any>(null);
let processoQueryEstaStale = false;
const processoQueryMock = {
    data: processoQueryData,
    refetch: vi.fn(async () => {
        const data = await processoService.buscarContextoCompleto(1);
        processoQueryData.value = data;
        return {data};
    }),
    refresh: vi.fn(async () => {
        if (!processoQueryEstaStale) {
            return {data: processoQueryData.value};
        }
        const data = await processoService.buscarContextoCompleto(1);
        processoQueryData.value = data;
        return {data};
    }),
};

vi.mock("vue-router", () => ({
    useRoute: () => ({params: mocks.params, query: mocks.query}),
    useRouter: () => ({push: mocks.push})
}));

vi.mock("@/composables/useProcessoQuery", async () => {
    return {
        CHAVE_QUERY_PROCESSO: ["processo"],
        useInvalidacaoProcesso: () => ({
            invalidarProcesso: vi.fn(),
        }),
        useProcessoQuery: () => processoQueryMock,
    };
});

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
    acoesBloco: []
};

const mockElegiveis = [
    {
        unidadeCodigo: 101,
        unidadeSigla: "UNI1",
        unidadeNome: "Unidade 1",
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
        localizacaoCodigo: 999,
        habilitarAceitarCadastroBloco: true,
        habilitarAceitarMapaBloco: false,
        habilitarHomologarCadastroBloco: true,
        habilitarHomologarMapaBloco: false,
        habilitarDisponibilizarMapaBloco: false
    },
    {
        unidadeCodigo: 103,
        unidadeSigla: "UNI3",
        unidadeNome: "Unidade 3",
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
        localizacaoCodigo: 999,
        habilitarAceitarCadastroBloco: false,
        habilitarAceitarMapaBloco: true,
        habilitarHomologarCadastroBloco: false,
        habilitarHomologarMapaBloco: true,
        habilitarDisponibilizarMapaBloco: false
    },
    {
        unidadeCodigo: 104,
        unidadeSigla: "UNI4",
        unidadeNome: "Unidade 4",
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
        localizacaoCodigo: 999,
        habilitarAceitarCadastroBloco: false,
        habilitarAceitarMapaBloco: false,
        habilitarHomologarCadastroBloco: false,
        habilitarHomologarMapaBloco: false,
        habilitarDisponibilizarMapaBloco: true
    }
];

function criarAcoesBloco(elegiveis: any[]) {
    const filtrar = (campo: string) => elegiveis.filter((item) => item[campo]);
    const criarAcao = (params: {
        codigo: string,
        acao: "ACEITAR" | "HOMOLOGAR" | "DISPONIBILIZAR",
        campo: string,
        rotulo: string,
        titulo: string,
        texto: string,
        rotuloBotao: string,
        mensagemSucesso: string,
        redirecionarPainel: boolean,
        requerDataLimite?: boolean
    }) => {
        const {
            codigo,
            acao,
            campo,
            rotulo,
            titulo,
            texto,
            rotuloBotao,
            mensagemSucesso,
            redirecionarPainel,
            requerDataLimite = false
        } = params;
        const unidades = filtrar(campo);
        return {
            codigo,
            acao,
            mostrar: unidades.length > 0,
            habilitar: unidades.length > 0,
            requerDataLimite,
            redirecionarPainel,
            rotulo,
            titulo,
            texto,
            rotuloBotao,
            mensagemSucesso,
            unidades,
        };
    };

    return [
        criarAcao({
            codigo: "aceitar-cadastro",
            acao: "ACEITAR",
            campo: "habilitarAceitarCadastroBloco",
            rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO,
            titulo: TEXTOS.acaoBloco.aceitar.TITULO_CADASTRO,
            texto: TEXTOS.acaoBloco.aceitar.TEXTO_CADASTRO,
            rotuloBotao: TEXTOS.acaoBloco.aceitar.BOTAO,
            mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_ACEITOS_EM_BLOCO,
            redirecionarPainel: true,
        }),
        criarAcao({
            codigo: "aceitar-mapa",
            acao: "ACEITAR",
            campo: "habilitarAceitarMapaBloco",
            rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_VALIDACAO,
            titulo: TEXTOS.acaoBloco.aceitar.TITULO_VALIDACAO,
            texto: TEXTOS.acaoBloco.aceitar.TEXTO_VALIDACAO,
            rotuloBotao: TEXTOS.acaoBloco.aceitar.BOTAO,
            mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_ACEITOS_EM_BLOCO,
            redirecionarPainel: true,
        }),
        criarAcao({
            codigo: "homologar-cadastro",
            acao: "HOMOLOGAR",
            campo: "habilitarHomologarCadastroBloco",
            rotulo: TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO,
            titulo: TEXTOS.acaoBloco.homologar.TITULO_CADASTRO,
            texto: TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO,
            rotuloBotao: TEXTOS.acaoBloco.homologar.BOTAO,
            mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_HOMOLOGADOS_EM_BLOCO,
            redirecionarPainel: false,
        }),
        criarAcao({
            codigo: "homologar-mapa",
            acao: "HOMOLOGAR",
            campo: "habilitarHomologarMapaBloco",
            rotulo: TEXTOS.acaoBloco.homologar.ROTULO_VALIDACAO,
            titulo: TEXTOS.acaoBloco.homologar.TITULO_VALIDACAO,
            texto: TEXTOS.acaoBloco.homologar.TEXTO_VALIDACAO,
            rotuloBotao: TEXTOS.acaoBloco.homologar.BOTAO,
            mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_HOMOLOGADOS_EM_BLOCO,
            redirecionarPainel: true,
        }),
        criarAcao({
            codigo: "disponibilizar-mapa",
            acao: "DISPONIBILIZAR",
            campo: "habilitarDisponibilizarMapaBloco",
            rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
            titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
            texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
            rotuloBotao: TEXTOS.acaoBloco.disponibilizar.BOTAO,
            mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
            redirecionarPainel: true,
            requerDataLimite: true,
        }),
    ];
}

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
    template: '<div v-if="modelValue" id="modal-confirmacao-stub"><slot /></div>',
    props: ["modelValue", "titulo", "variant", "okTitle", "loading"]
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

const BDropdownStub = {
    name: "BDropdown",
    template: `
      <div :data-testid="$attrs['data-testid']">
        <button :disabled="disabled" type="button">{{ text }}</button>
        <slot />
      </div>
    `,
    props: ["text", "variant", "disabled", "toggleClass"]
};

const BDropdownItemButtonStub = {
    name: "BDropdownItemButton",
    template: `
      <button
        :id="$attrs.id"
        :data-testid="$attrs['data-testid']"
        :disabled="disabled"
        type="button"
        @click="$emit('click', $event)">
        <slot />
      </button>
    `,
    props: ["disabled"]
};

const BButtonStub = {
    template: '<button :disabled="disabled" @click="$emit(\'click\', $event)"><slot /></button>',
    props: ['disabled']
};

const stubsProcessoDetalhe = {
    ModalAcaoBloco: ModalAcaoBlocoStub,
    ModalConfirmacao: ModalConfirmacaoStub,
    ProcessoSubprocessosTable: TreeTableStub,
    PageHeader: PageHeaderStub,
    ProcessoInfo: ProcessoInfoStub,
    BAlert: BAlertStub,
    BSpinner: BSpinnerStub,
    BButton: BButtonStub,
    BDropdown: BDropdownStub,
    BDropdownItemButton: BDropdownItemButtonStub
};

describe("Processo.vue", () => {
    let wrapper: any;
    let perfilStore: any;
    let toastStore: any;

    const montarKeepAliveProcessoView = (manterMontado: { value: boolean }) => {
        return mount({
            components: {ProcessoView},
            setup() {
                return {manterMontado};
            },
            template: "<keep-alive><ProcessoView v-if='manterMontado' /></keep-alive>",
        }, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: true
                    })
                ],
                stubs: stubsProcessoDetalhe
            }
        });
    };

    const createWrapper = (opcoes: { processo?: any; elegiveis?: any[]; erroCarregamento?: Error | null } = {}) => {
        const processo = opcoes.processo ?? mockProcesso;
        const elegiveis = opcoes.elegiveis ?? mockElegiveis;

        if (opcoes.erroCarregamento) {
            vi.mocked(processoService.buscarContextoCompleto).mockRejectedValue(opcoes.erroCarregamento);
        } else {
            vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
                ...processo,
                elegiveis,
                acoesBloco: processo.acoesBloco?.length ? processo.acoesBloco : criarAcoesBloco(elegiveis),
            });
        }

        vi.mocked(processoService.finalizarProcesso).mockResolvedValue(undefined);
        vi.mocked(processoService.executarAcaoEmBloco).mockResolvedValue(undefined);

        return mount(ProcessoView, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: true
                    })
                ],
                stubs: stubsProcessoDetalhe
            }
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
        processoQueryData.value = null;
        processoQueryEstaStale = false;
        processoQueryMock.refetch.mockClear();
        processoQueryMock.refresh.mockClear();
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        toastStore = useToastStore();
    });

    afterEach(() => {
        wrapper.unmount();
    });

    it("deve carregar detalhes do processo ao montar", async () => {
        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
    });

    it("não recarrega o contexto ao reativar quando o cache ainda está válido", async () => {
        const manterMontado = ref(true);
        const wrapperKeepAlive = montarKeepAliveProcessoView(manterMontado);

        await flushPromises();
        vi.mocked(processoService.buscarContextoCompleto).mockClear();

        manterMontado.value = false;
        await nextTick();
        manterMontado.value = true;
        await flushPromises();

        expect(processoService.buscarContextoCompleto).not.toHaveBeenCalled();
        wrapperKeepAlive.unmount();
    });

    it("recarrega o contexto ao reativar quando o cache está inválido", async () => {
        processoQueryEstaStale = true;
        const manterMontado = ref(true);
        const wrapperKeepAlive = montarKeepAliveProcessoView(manterMontado);

        await flushPromises();
        vi.mocked(processoService.buscarContextoCompleto).mockClear();

        manterMontado.value = false;
        await nextTick();
        manterMontado.value = true;
        await flushPromises();

        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
        wrapperKeepAlive.unmount();
    });

    it("mantém o snapshot e exibe erro se a recarga em background falhar", async () => {
        processoQueryEstaStale = true;
        const manterMontado = ref(true);
        vi.mocked(processoService.buscarContextoCompleto)
            .mockResolvedValueOnce({
                ...mockProcesso,
                descricao: "Processo Teste",
                elegiveis: mockElegiveis,
                acoesBloco: criarAcoesBloco(mockElegiveis),
            } as any)
            .mockRejectedValueOnce(new Error("Falha na recarga"));
        const wrapperKeepAlive = montarKeepAliveProcessoView(manterMontado);
        await flushPromises();
        vi.mocked(processoService.buscarContextoCompleto).mockClear();

        manterMontado.value = false;
        await nextTick();
        manterMontado.value = true;
        await flushPromises();

        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
        expect(wrapperKeepAlive.findComponent(TreeTableStub).exists()).toBe(true);
        expect(wrapperKeepAlive.text()).toContain("Falha na recarga");
        wrapperKeepAlive.unmount();
    });

    it("deve exibir erro se falhar ao carregar processo", async () => {
        wrapper.unmount();
        wrapper = createWrapper({erroCarregamento: new Error("Erro ao carregar")});
        perfilStore = usePerfilStore();
        toastStore = useToastStore();
        await nextTick();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao carregar");

        const alertCmp = wrapper.findComponent(BAlertStub);
        await alertCmp.vm.$emit("dismissed");
        expect(wrapper.vm.ultimoErro).toBeNull();
    });

    it("deve exibir botões de ação em bloco se houver unidades elegíveis", async () => {
        // Test GESTOR buttons
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-acoes-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').text()).toContain(TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-mapas-bloco"]').exists()).toBe(true);

        // Test ADMIN buttons
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-acoes-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-homologar-mapas-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-disponibilizar-bloco"]').exists()).toBe(true);
    });

    it("não deve exibir botões de ação em bloco duplicados (apenas um conjunto)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        // Test GESTOR
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});
        await nextTick();
        await flushPromises();

        const botoesAceitar = wrapper.findAll('[data-testid="btn-processo-aceitar-bloco"]').filter(
            (b: DOMWrapper<Element>) => b.text().includes(TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO)
        );
        expect(botoesAceitar.length).toBe(1);

        // Test ADMIN
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        await nextTick();
        await flushPromises();

        const botoesHomologar = wrapper.findAll('[data-testid="btn-processo-homologar-bloco"]').filter(
            (b: DOMWrapper<Element>) => b.text().includes(TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO)
        );
        expect(botoesHomologar.length).toBe(1);
    });

    it("deve abrir modal de ação em bloco ao clicar no botão", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});

        await nextTick();
        await flushPromises();

        const btnAceitar = wrapper.find('#btn-aceitar-bloco');
        await btnAceitar.trigger("click");

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.aceitar.TITULO_CADASTRO);
    });

    it("deve executar ação em bloco com sucesso (Aceitar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-aceitar-bloco').trigger("click"); // Abrir modal 'aceitar'

        // Simular confirmação do modal com ID 101 (Mapeamento cadastro disponibilizado)
        const dadosConfirmacao = {ids: [101]};
        await modal.vm.$emit("confirmar", dadosConfirmacao);

        expect(processoService.executarAcaoEmBloco).toHaveBeenCalledWith(1, {
            unidadeCodigos: [101],
            acao: 'ACEITAR',
            dataLimite: undefined,
        });
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS_SUCESSO_PROCESSO.CADASTROS_ACEITOS_EM_BLOCO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Homologar cadastro)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-homologar-bloco').trigger("click"); // Abrir modal 'homologar'

        // ID 101 -> Cadastro disponibilizado
        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processoService.executarAcaoEmBloco).toHaveBeenCalledWith(1, {
            unidadeCodigos: [101],
            acao: 'HOMOLOGAR',
            dataLimite: undefined,
        });
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.homologar.TITULO_CADASTRO);
    });

    it("deve executar ação em bloco com sucesso (Homologar validação)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-homologar-mapas-bloco').trigger("click"); // Abrir modal 'homologar mapa'

        // ID 103 -> Mapa validado
        await modal.vm.$emit("confirmar", {ids: [103]});

        expect(processoService.executarAcaoEmBloco).toHaveBeenCalledWith(1, {
            unidadeCodigos: [103],
            acao: 'HOMOLOGAR',
            dataLimite: undefined,
        });
        expect(wrapper.vm.processo).toBeNull();
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve usar textos de CDU-23 quando houver apenas cadastro elegível para homologacao", async () => {
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                unidades: [mockProcesso.unidades[0]]
            },
            elegiveis: [mockElegiveis[0]],
        });
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        await wrapper.find('#btn-homologar-bloco').trigger("click");
        expect(wrapper.find('#btn-homologar-bloco').text()).toContain(TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO);
        expect((wrapper.vm).acaoBlocoAtual.titulo).toBe(TEXTOS.acaoBloco.homologar.TITULO_CADASTRO);
        expect((wrapper.vm).acaoBlocoAtual.texto).toBe(TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO);
        expect((wrapper.vm).acaoBlocoAtual.rotuloBotao).toBe(TEXTOS.acaoBloco.homologar.BOTAO);
        expect((wrapper.vm).acaoBlocoAtual.mensagemSucesso).toBe(TEXTOS_SUCESSO_PROCESSO.CADASTROS_HOMOLOGADOS_EM_BLOCO);

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [101]});
        await flushPromises();

        expect(toastStore.setPending).not.toHaveBeenCalled();
        expect(mocks.push).not.toHaveBeenCalledWith("/painel");
        expect(wrapper.find('[data-testid="app-alert"]').text()).toContain(TEXTOS_SUCESSO_PROCESSO.CADASTROS_HOMOLOGADOS_EM_BLOCO);
    });

    it("deve usar textos de CDU-26 quando houver apenas validacao elegível para homologacao", async () => {
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                unidades: [{
                    ...mockProcesso.unidades[2]
                }]
            },
            elegiveis: [mockElegiveis[1]],
        });
        perfilStore = usePerfilStore();
        toastStore = useToastStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        expect(wrapper.find('#btn-homologar-mapas-bloco').text()).toContain(TEXTOS.acaoBloco.homologar.ROTULO_VALIDACAO);
        await wrapper.find('#btn-homologar-mapas-bloco').trigger("click");
        expect((wrapper.vm).acaoBlocoAtual.titulo).toBe(TEXTOS.acaoBloco.homologar.TITULO_VALIDACAO);
        expect((wrapper.vm).acaoBlocoAtual.texto).toBe(TEXTOS.acaoBloco.homologar.TEXTO_VALIDACAO);
        expect((wrapper.vm).acaoBlocoAtual.rotuloBotao).toBe(TEXTOS.acaoBloco.homologar.BOTAO);
        expect((wrapper.vm).acaoBlocoAtual.mensagemSucesso).toBe(TEXTOS_SUCESSO_PROCESSO.MAPAS_HOMOLOGADOS_EM_BLOCO);

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await modal.vm.$emit("confirmar", {ids: [103]});
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS_SUCESSO_PROCESSO.MAPAS_HOMOLOGADOS_EM_BLOCO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve executar ação em bloco com sucesso (Disponibilizar)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-disponibilizar-bloco').trigger("click"); // Abrir modal 'disponibilizar'

        // ID 104 -> Mapa criado
        await modal.vm.$emit("confirmar", {ids: [104], dataLimite: '2024-12-31'});

        expect(processoService.executarAcaoEmBloco).toHaveBeenCalledWith(1, {
            unidadeCodigos: [104],
            acao: 'DISPONIBILIZAR',
            dataLimite: '2024-12-31',
        });
        expect(wrapper.find('#btn-disponibilizar-bloco').text()).toContain(TEXTOS.acaoBloco.disponibilizar.ROTULO);
        expect(modal.props("titulo")).toBe(TEXTOS.acaoBloco.disponibilizar.TITULO);
    });

    it("deve lidar com erro na execução da ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});

        await nextTick();
        await flushPromises();

        const errorMsg = "Falha ao aceitar";
        vi.mocked(processoService.executarAcaoEmBloco).mockRejectedValue(new Error(errorMsg));

        const modal = wrapper.findComponent(ModalAcaoBlocoStub);
        await wrapper.find('#btn-aceitar-bloco').trigger("click"); // Abrir modal

        await modal.vm.$emit("confirmar", {ids: [101]});

        expect(processoService.executarAcaoEmBloco).toHaveBeenCalled();
        expect(modalSpies.setErro).toHaveBeenCalledWith(errorMsg);
        expect(modalSpies.setProcessando).toHaveBeenCalledWith(false);
    });

    it("deve mostrar erro se unidade não for encontrada para ação em bloco", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});

        await nextTick();
        await flushPromises();

        const errorMsg = "Unidade selecionada não encontrada no contexto do processo.";
        vi.mocked(processoService.executarAcaoEmBloco).mockRejectedValue(new Error(errorMsg));

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
        wrapper.unmount();
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                acoesBloco: [
                    {
                        codigo: "aceitar-cadastro",
                        acao: "ACEITAR",
                        mostrar: true,
                        habilitar: true,
                        requerDataLimite: false,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO,
                        titulo: TEXTOS.acaoBloco.aceitar.TITULO_CADASTRO,
                        texto: TEXTOS.acaoBloco.aceitar.TEXTO_CADASTRO,
                        rotuloBotao: TEXTOS.acaoBloco.aceitar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_ACEITOS_EM_BLOCO,
                        unidades: [mockElegiveis[0]],
                    },
                    {
                        codigo: "homologar-cadastro",
                        acao: "HOMOLOGAR",
                        mostrar: false,
                        habilitar: false,
                        requerDataLimite: false,
                        redirecionarPainel: false,
                        rotulo: TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO,
                        titulo: TEXTOS.acaoBloco.homologar.TITULO_CADASTRO,
                        texto: TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO,
                        rotuloBotao: TEXTOS.acaoBloco.homologar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_HOMOLOGADOS_EM_BLOCO,
                        unidades: [mockElegiveis[0]],
                    },
                    {
                        codigo: "disponibilizar-mapa",
                        acao: "DISPONIBILIZAR",
                        mostrar: false,
                        habilitar: false,
                        requerDataLimite: true,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
                        titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
                        texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
                        rotuloBotao: TEXTOS.acaoBloco.disponibilizar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
                        unidades: [],
                    },
                ],
            },
            elegiveis: [mockElegiveis[0]],
        });
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.GESTOR, unidadeSelecionada: 999});

        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-acoes-bloco"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-processo-aceitar-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-processo-disponibilizar-bloco"]').exists()).toBe(false);
    });

    it("deve exibir botões homologar e disponibilizar quando permissões focam nisso (ex: Admin)", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();

        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        wrapper.unmount();
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                acoesBloco: [
                    {
                        codigo: "aceitar-cadastro",
                        acao: "ACEITAR",
                        mostrar: false,
                        habilitar: false,
                        requerDataLimite: false,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.aceitar.ROTULO_CADASTRO,
                        titulo: TEXTOS.acaoBloco.aceitar.TITULO_CADASTRO,
                        texto: TEXTOS.acaoBloco.aceitar.TEXTO_CADASTRO,
                        rotuloBotao: TEXTOS.acaoBloco.aceitar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_ACEITOS_EM_BLOCO,
                        unidades: [],
                    },
                    {
                        codigo: "homologar-mapa",
                        acao: "HOMOLOGAR",
                        mostrar: true,
                        habilitar: true,
                        requerDataLimite: false,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.homologar.ROTULO_VALIDACAO,
                        titulo: TEXTOS.acaoBloco.homologar.TITULO_VALIDACAO,
                        texto: TEXTOS.acaoBloco.homologar.TEXTO_VALIDACAO,
                        rotuloBotao: TEXTOS.acaoBloco.homologar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_HOMOLOGADOS_EM_BLOCO,
                        unidades: [mockElegiveis[1]],
                    },
                    {
                        codigo: "disponibilizar-mapa",
                        acao: "DISPONIBILIZAR",
                        mostrar: true,
                        habilitar: true,
                        requerDataLimite: true,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
                        titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
                        texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
                        rotuloBotao: TEXTOS.acaoBloco.disponibilizar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
                        unidades: [mockElegiveis[2]],
                    },
                ],
            },
            elegiveis: [mockElegiveis[1], mockElegiveis[2]],
        });
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-processo-acoes-bloco"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-homologar-bloco"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-processo-homologar-mapas-bloco"]').exists()).toBe(true);
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

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);

        const rowItem = {
            codigo: 101,
            codSubprocesso: 201,
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
            },
            query: {
                codSubprocesso: "201"
            },
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

        await nextTick();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        const rowItem = {
            codigo: 101,
            codSubprocesso: 201,
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
            },
            query: {
                codSubprocesso: "201"
            },
        });
    });

    it("deve abrir modal de finalização de processo", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const btnFinalizar = wrapper.find('[data-testid="btn-processo-finalizar"]');
        await btnFinalizar.trigger("click");

        expect((wrapper.vm).mostrarModalFinalizacao).toBe(true);
    });

    it("deve exibir o texto simples no modal de finalização", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const btnFinalizar = wrapper.find('[data-testid="btn-processo-finalizar"]');
        await btnFinalizar.trigger("click");
        await nextTick();

        const paragrafos = wrapper.findAll('#modal-confirmacao-stub p');

        expect(paragrafos).toHaveLength(2);
        expect(paragrafos[0].text()).toContain(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO);
        expect(paragrafos[0].text()).toContain(mockProcesso.descricao);
        expect(paragrafos[1].text()).toBe(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO);
    });

    it("deve chamar API de finalizar processo com sucesso", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});
        toastStore = useToastStore();

        await nextTick();
        await flushPromises();

        await (wrapper.vm).confirmarFinalizacao();

        expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
        expect(toastStore.setPending).toHaveBeenCalledWith(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("ignora confirmações repetidas enquanto a finalização está em andamento", async () => {
        wrapper = createWrapper();
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        let resolver!: () => void;
        vi.mocked(processoService.finalizarProcesso).mockImplementation(() => new Promise<void>((resolve) => {
            resolver = resolve;
        }) as any);

        const primeiraExecucao = (wrapper.vm).confirmarFinalizacao();
        const segundaExecucao = (wrapper.vm).confirmarFinalizacao();

        expect(processoService.finalizarProcesso).toHaveBeenCalledTimes(1);

        resolver();
        await primeiraExecucao;
        await segundaExecucao;
    });

    it("deve lidar com atualizações de estado do modal, descarte de notificações, erros de navegação e exibição de carregamento", async () => {
        const vm = wrapper.vm;

        // v-model cover (95)
        const modalConf = wrapper.findComponent(ModalConfirmacaoStub);
        if (modalConf.exists()) await modalConf.vm.$emit('update:modelValue', true);
        expect(vm.mostrarModalFinalizacao).toBe(true);

        // notificacao cover (9-16)
        vm.notificacao = {message: "Msg", variant: "info"};
        await nextTick();
        wrapper.find('[data-testid="app-alert"]');
        // If app-alert is covered via BAlert stub or real AppAlert
        const bAlerts = wrapper.findAllComponents(BAlertStub);
        if (bAlerts.length > 0) await bAlerts[0].vm.$emit('dismissed');

        // error branches (428-431)
        vi.mocked(processoService.finalizarProcesso).mockRejectedValue(new Error("Erro final"));
        await vm.confirmarFinalizacao();

        // branch 402-404 (abrirDetalhesUnidade not clickable)
        await vm.abrirDetalhesUnidade({clickable: false});
        expect(mocks.push).not.toHaveBeenCalledWith(expect.objectContaining({name: "Subprocesso"}));

        // branch (navigation error - router.push pode rejeitar; erro propaga ao chamador)
        mocks.push.mockRejectedValueOnce(new Error("Nav error"));
        await expect(
            vm.abrirDetalhesUnidade({clickable: true, sigla: "ERR", codSubprocesso: 999})
        ).rejects.toThrow("Nav error");

        // loading state (77-80)
        wrapper.unmount();
        vi.mocked(processoService.buscarContextoCompleto).mockReturnValue(new Promise(() => {
        }) as any);
        wrapper = createWrapper();
        await nextTick();
        expect(wrapper.findComponent(BSpinnerStub).exists()).toBe(true);
    });

    it("deve manter botão de finalizar visível e desabilitado para ADMIN quando o processo não puder ser finalizado", async () => {
        wrapper.unmount();
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                podeFinalizar: false,
            }
        });
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const btnFinalizar = wrapper.find('[data-testid="btn-processo-finalizar"]');
        expect(btnFinalizar.exists()).toBe(true);
        expect(btnFinalizar.attributes('disabled')).toBeDefined();
    });

    it("deve manter menu de ações em bloco visível e habilitado mesmo quando nenhuma ação estiver habilitada", async () => {
        wrapper.unmount();
        wrapper = createWrapper({
            processo: {
                ...mockProcesso,
                acoesBloco: [
                    {
                        codigo: "homologar-cadastro",
                        acao: "HOMOLOGAR",
                        mostrar: true,
                        habilitar: false,
                        requerDataLimite: false,
                        redirecionarPainel: false,
                        rotulo: TEXTOS.acaoBloco.homologar.ROTULO_CADASTRO,
                        titulo: TEXTOS.acaoBloco.homologar.TITULO_CADASTRO,
                        texto: TEXTOS.acaoBloco.homologar.TEXTO_CADASTRO,
                        rotuloBotao: TEXTOS.acaoBloco.homologar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.CADASTROS_HOMOLOGADOS_EM_BLOCO,
                        unidades: []
                    },
                    {
                        codigo: "disponibilizar-mapa",
                        acao: "DISPONIBILIZAR",
                        mostrar: true,
                        habilitar: false,
                        requerDataLimite: true,
                        redirecionarPainel: true,
                        rotulo: TEXTOS.acaoBloco.disponibilizar.ROTULO,
                        titulo: TEXTOS.acaoBloco.disponibilizar.TITULO,
                        texto: TEXTOS.acaoBloco.disponibilizar.TEXTO,
                        rotuloBotao: TEXTOS.acaoBloco.disponibilizar.BOTAO,
                        mensagemSucesso: TEXTOS_SUCESSO_PROCESSO.MAPAS_DISPONIBILIZADOS_EM_BLOCO,
                        unidades: []
                    }
                ]
            }
        });
        perfilStore = usePerfilStore();
        perfilStore.$patch({perfilSelecionado: Perfil.ADMIN});

        await nextTick();
        await flushPromises();

        const menuAcoes = wrapper.find('[data-testid="btn-processo-acoes-bloco"] button');
        expect(menuAcoes.exists()).toBe(true);
        expect(menuAcoes.attributes('disabled')).toBeUndefined();
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Bloco de testes originados do ProcessoViewCoverage.spec.ts (mesclado)
// ─────────────────────────────────────────────────────────────────────────────

const coberturaStubs = {
    PageHeader: {template: '<div><slot/><slot name="actions"/></div>'},
    ProcessoAcoes: {
        name: 'ProcessoAcoes',
        props: [
            'mostrarFinalizarProcesso',
            'podeFinalizar',
            'usarMenuAcoesBloco',
            'acoesBlocoVisiveis',
            'acaoBlocoPrincipal',
            'processandoAcaoBloco'
        ],
        emits: ['finalizar', 'abrir-acao-bloco'],
        methods: {
            obterId(codigo: string) {
                switch (codigo) {
                    case 'aceitar-cadastro': return 'btn-aceitar-bloco';
                    case 'aceitar-mapa': return 'btn-aceitar-mapas-bloco';
                    case 'homologar-cadastro': return 'btn-homologar-bloco';
                    case 'homologar-mapa': return 'btn-homologar-mapas-bloco';
                    case 'disponibilizar-mapa': return 'btn-disponibilizar-bloco';
                    default: return `btn-${codigo}`;
                }
            },
            obterTestId(codigo: string) {
                switch (codigo) {
                    case 'aceitar-cadastro': return 'btn-processo-aceitar-bloco';
                    case 'aceitar-mapa': return 'btn-processo-aceitar-mapas-bloco';
                    case 'homologar-cadastro': return 'btn-processo-homologar-bloco';
                    case 'homologar-mapa': return 'btn-processo-homologar-mapas-bloco';
                    case 'disponibilizar-mapa': return 'btn-processo-disponibilizar-bloco';
                    default: return `btn-processo-${codigo}`;
                }
            }
        },
        template: `
          <div>
            <button
                v-if="mostrarFinalizarProcesso"
                data-testid="btn-processo-finalizar"
                :disabled="!podeFinalizar"
                @click="$emit('finalizar')"
            >
              Finalizar
            </button>
            <div v-if="usarMenuAcoesBloco" data-testid="btn-processo-acoes-bloco">
              <button type="button">Ações em bloco</button>
              <button
                  v-for="acao in acoesBlocoVisiveis"
                  :id="obterId(acao.codigo)"
                  :key="acao.codigo"
                  :data-testid="obterTestId(acao.codigo)"
                  :disabled="!acao.habilitar || processandoAcaoBloco"
                  @click="$emit('abrir-acao-bloco', acao)"
              >
                {{ acao.rotulo }}
              </button>
            </div>
            <button
                v-else-if="acaoBlocoPrincipal"
                :id="obterId(acaoBlocoPrincipal.codigo)"
                :data-testid="obterTestId(acaoBlocoPrincipal.codigo)"
                :disabled="!acaoBlocoPrincipal.habilitar || processandoAcaoBloco"
                @click="$emit('abrir-acao-bloco', acaoBlocoPrincipal)"
            >
              {{ acaoBlocoPrincipal.rotulo }}
            </button>
          </div>
        `
    },
    TreeTable: {template: '<div>TreeTable</div>'},
    ModalAcaoBloco: {
        name: 'ModalAcaoBloco',
        template: '<div>ModalAcaoBloco</div>',
        setup(_props: unknown, {expose}: { expose: (exposed: Record<string, any>) => void }) {
            expose({
                abrir: vi.fn(),
                fechar: vi.fn(),
                setErro: vi.fn(),
                setProcessando: vi.fn()
            });
            return {};
        }
    },
    ModalConfirmacao: {
        template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
        props: ['modelValue'],
        emits: ['confirmar', 'update:modelValue']
    },
    BAlert: {template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue']},
    BBadge: {template: '<span><slot /></span>'},
    BButton: {
        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
        props: ['disabled']
    },
    BDropdown: {
        template: '<div><button :disabled="disabled">{{ text }}</button><slot /></div>',
        props: ['text', 'disabled']
    },
    BDropdownItemButton: {
        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
        props: ['disabled']
    },
    BContainer: {template: '<div><slot /></div>'},
    BSpinner: {template: '<span>Loading</span>'}
};

function criarAcoesBlocoCobertura(subprocessosElegiveis: any[]) {
    const filtrar = (campo: string) => subprocessosElegiveis.filter((item) => item[campo]);
    const criarAcao = (
        codigo: string,
        acao: "ACEITAR" | "HOMOLOGAR" | "DISPONIBILIZAR",
        campo: string,
        redirecionarPainel: boolean,
        requerDataLimite = false,
    ) => {
        const unidades = filtrar(campo);
        return {
            codigo,
            acao,
            mostrar: unidades.length > 0,
            habilitar: unidades.length > 0,
            requerDataLimite,
            redirecionarPainel,
            rotulo: codigo,
            titulo: codigo,
            texto: codigo,
            rotuloBotao: codigo,
            mensagemSucesso: codigo,
            unidades,
        };
    };
    return [
        criarAcao("aceitar-cadastro", "ACEITAR", "habilitarAceitarCadastroBloco", true),
        criarAcao("aceitar-mapa", "ACEITAR", "habilitarAceitarMapaBloco", true),
        criarAcao("homologar-cadastro", "HOMOLOGAR", "habilitarHomologarCadastroBloco", false),
        criarAcao("homologar-mapa", "HOMOLOGAR", "habilitarHomologarMapaBloco", true),
        criarAcao("disponibilizar-mapa", "DISPONIBILIZAR", "habilitarDisponibilizarMapaBloco", true, true),
    ];
}

describe("ProcessoDetalheView — cobertura adicional", () => {
    const criarWrapperCobertura = (initialState: any = {}) => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: "GESTOR",
                    unidadeSelecionada: 100,
                    perfisUnidades: [{perfil: "GESTOR", unidade: {codigo: 100}}],
                    perfis: ["GESTOR"]
                }
            },
            stubActions: true
        });

        if (initialState.perfil) {
            const store = usePerfilStore(pinia);
            store.$patch(initialState.perfil);
        }

        const processo = initialState.processos?.processoDetalhe ?? {
            codigo: 1,
            descricao: "Processo teste",
            tipo: TipoProcesso.MAPEAMENTO,
            situacao: "EM_ANDAMENTO",
            unidades: []
        };
        const subprocessosElegiveis = initialState.processos?.subprocessosElegiveis ?? [];

        if (initialState.processos?.ultimoErro) {
            vi.mocked(processoService.buscarContextoCompleto).mockRejectedValue(
                new Error(initialState.processos.ultimoErro.message)
            );
        } else {
            vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
                ...processo,
                elegiveis: subprocessosElegiveis,
                acoesBloco: processo.acoesBloco ?? criarAcoesBlocoCobertura(subprocessosElegiveis),
            });
        }
        vi.mocked(processoService.finalizarProcesso).mockResolvedValue(undefined);
        vi.mocked(processoService.executarAcaoEmBloco).mockResolvedValue(undefined);

        return mount(ProcessoView, {
            global: {
                plugins: [pinia],
                stubs: coberturaStubs
            }
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve lidar com erro ao finalizar processo", async () => {
        const wrapper = criarWrapperCobertura({
            perfil: {perfilSelecionado: "ADMIN", unidadeSelecionada: 999, perfis: ["ADMIN"]}
        });

        vi.mocked(processoService.finalizarProcesso).mockRejectedValue(new Error("Erro ao finalizar"));
        await flushPromises();

        const acoes = wrapper.find('[data-testid="btn-processo-finalizar"]');
        if (acoes.exists()) await acoes.trigger('click');
        await flushPromises();

        const modal = wrapper.findComponent({name: "ModalConfirmacao"});
        if (modal.exists()) await modal.vm.$emit("confirmar");
        await flushPromises();
    });

    it("não deve exibir erro quando o carregamento inicial for cancelado", async () => {
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValueOnce(null as never);

        const wrapper = criarWrapperCobertura();
        await flushPromises();

        expect((wrapper.vm as any).ultimoErro).toBeNull();
    });

    it("deve abrir detalhes da unidade (navegação) para ADMIN", async () => {
        const wrapper = criarWrapperCobertura({
            perfil: {perfilSelecionado: "ADMIN", unidadeSelecionada: 999, perfis: ["ADMIN"]}
        });
        await flushPromises();

        const item = {clickable: true, sigla: "U1", codigo: 10};
        await (wrapper.vm as any).abrirDetalhesUnidade(item);

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            name: "Subprocesso",
            params: {codProcesso: "1", siglaUnidade: "U1"}
        }));
    });

    it("não deve navegar se item não clicável", async () => {
        const wrapper = criarWrapperCobertura();
        await flushPromises();

        await (wrapper.vm as any).abrirDetalhesUnidade({clickable: false});
        expect(mocks.push).not.toHaveBeenCalled();
    });

    it("deve navegar para unidade de terceiros se CHEFE (controle é no backend)", async () => {
        const wrapper = criarWrapperCobertura({
            perfil: {perfilSelecionado: "CHEFE", unidadeSelecionada: 200, perfis: ["CHEFE"]}
        });
        await flushPromises();

        await (wrapper.vm as any).abrirDetalhesUnidade({clickable: true, codigo: 10, sigla: "U1", unidadeAtual: "U1"});

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: {codProcesso: "1", siglaUnidade: "U1"}
        }));
    });

    it("deve navegar para própria unidade se CHEFE", async () => {
        const wrapper = criarWrapperCobertura({
            perfil: {perfilSelecionado: "CHEFE", unidadeSelecionada: 10, perfis: ["CHEFE"]}
        });
        await flushPromises();

        await (wrapper.vm as any).abrirDetalhesUnidade({clickable: true, codigo: 10, sigla: "U1"});

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: {codProcesso: "1", siglaUnidade: "U1"}
        }));
    });

    it("deve lidar com erro na ação em bloco", async () => {
        const wrapper = criarWrapperCobertura({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        habilitarAceitarCadastroBloco: true,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: true,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    }
                ]
            }
        });
        await flushPromises();

        const modal = wrapper.findComponent({name: 'ModalAcaoBloco'});
        (wrapper.vm as any).acaoBlocoAtual = (wrapper.vm as any).acoesBlocoVisiveis.find(
            (acao: any) => acao.codigo === 'aceitar-cadastro'
        );
        vi.mocked(processoService.executarAcaoEmBloco).mockRejectedValue(new Error("Erro bloco"));

        await modal.vm.$emit("confirmar", {ids: [1]});
        await flushPromises();

        if (modal.exists()) {
            expect(modal.vm.setErro).toHaveBeenCalledWith("Erro bloco");
        }
    });

    it("deve filtrar unidades elegíveis para Disponibilizar a partir do backend", async () => {
        const wrapper = criarWrapperCobertura({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: false,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: false,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: true
                    },
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: false,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: false,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    }
                ]
            }
        });
        await flushPromises();

        (wrapper.vm as any).acaoBlocoAtual = (wrapper.vm as any).acoesBlocoVisiveis.find(
            (acao: any) => acao.codigo === 'disponibilizar-mapa'
        );
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });

    it("deve filtrar unidades elegíveis para Homologar a partir do backend", async () => {
        const wrapper = criarWrapperCobertura({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: false,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: true,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    },
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: false,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: false,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    }
                ]
            }
        });
        await flushPromises();

        (wrapper.vm as any).acaoBlocoAtual = (wrapper.vm as any).acoesBlocoVisiveis.find(
            (acao: any) => acao.codigo === 'homologar-cadastro'
        );
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
    });

    it("deve filtrar unidades elegíveis para Aceitar a partir do backend", async () => {
        const wrapper = criarWrapperCobertura({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: true,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: false,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    },
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100,
                        habilitarAceitarCadastroBloco: false,
                        habilitarAceitarMapaBloco: false,
                        habilitarHomologarCadastroBloco: false,
                        habilitarHomologarMapaBloco: false,
                        habilitarDisponibilizarMapaBloco: false
                    }
                ]
            }
        });
        await flushPromises();

        (wrapper.vm as any).acaoBlocoAtual = (wrapper.vm as any).acoesBlocoVisiveis.find(
            (acao: any) => acao.codigo === 'aceitar-cadastro'
        );
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });

    it("deve cobrir branches de erro e estados de carregamento", async () => {
        const wrapper = criarWrapperCobertura({
            processos: {
                processoDetalhe: {codigo: 1, situacao: 'EM_ANDAMENTO'},
                ultimoErro: {message: 'Erro de teste'}
            }
        });
        await flushPromises();

        expect(wrapper.text()).toContain('Erro de teste');

        (wrapper.vm as any).notify('Erro Manual', 'danger');
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain('Erro Manual');
    });
});

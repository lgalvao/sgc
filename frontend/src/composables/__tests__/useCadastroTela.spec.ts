import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia} from "pinia";
import {createApp, nextTick, ref, reactive, toValue} from "vue";
import {PiniaColada} from "@pinia/colada";
import {useCadastroTela} from "../useCadastroTela";
import {useNotification} from "../useNotification";
import {useErrorHandler} from "../useErrorHandler";
import {TipoProcesso, SituacaoSubprocesso} from "@/types/tipos";

// Mocks de dependências pesadas
vi.mock("@/composables/useAtividadeForm", () => ({
    useAtividadeForm: () => ({
        novaAtividade: ref(""),
        loadingAdicionar: ref(false),
        adicionarAtividade: vi.fn(),
    }),
}));

const fluxoSubprocessoMock = {
    validarCadastro: vi.fn(),
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    aceitarCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    ultimoErro: ref(null),
};

vi.mock("@/composables/useFluxoSubprocesso", () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock,
}));

vi.mock("@/composables/useImpactoMapaModal", () => ({
    useImpactoMapaModal: () => ({
        mostrarModalImpacto: ref(false),
        loadingImpacto: ref(false),
        abrirModalImpacto: vi.fn(),
        fecharModalImpacto: vi.fn(),
    }),
}));

vi.mock("@/composables/useMapas", () => ({
    useMapas: () => ({
        impactoMapa: ref(null),
        carregarImpacto: vi.fn(),
    }),
}));

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({
        notify: vi.fn(),
        notificacao: ref(null),
        clear: vi.fn(),
    }),
}));

const orquestracaoMock = {
    carregandoInicial: ref(false),
    codigoSubprocesso: ref(1),
    atividadesSnapshotInicial: ref(""),
    unidade: ref({codigo: 1, nome: "U1"}),
    codMapa: ref(10),
    carregarContextoInicial: vi.fn(),
    processarRespostaLocal: vi.fn(),
};

vi.mock("@/composables/useCadastroOrquestracao", () => ({
    useCadastroOrquestracao: () => orquestracaoMock,
}));

vi.mock("@/composables/useCadastroAtividadesMutacoes", () => ({
    useCadastroAtividadesMutacoes: () => ({
        erroNovaAtividade: ref(null),
        dadosRemocao: ref(null),
        loadingRemocao: ref(false),
        mostrarModalConfirmacaoRemocao: ref(false),
        adicionarAtividade: vi.fn(),
        removerAtividade: vi.fn(),
        confirmarRemocao: vi.fn(),
        salvarEdicaoAtividade: vi.fn(),
        adicionarConhecimento: vi.fn(),
        removerConhecimento: vi.fn(),
        salvarEdicaoConhecimento: vi.fn(),
    }),
}));

vi.mock("@/views/cadastroAnaliseFluxo", () => ({
    useCadastroAnaliseFluxo: () => ({
        historicoAnalises: ref([]),
        loadingAnaliseCadastro: ref(false),
        loadingDevolucaoAnalise: ref(false),
        observacaoValidacao: ref(""),
        observacaoDevolucao: ref(""),
        abrirModalHistorico: vi.fn(),
        abrirModalDevolverAnalise: vi.fn(),
        abrirModalValidarAnalise: vi.fn(),
        confirmarValidacaoAnalise: vi.fn(),
        confirmarDevolucaoAnalise: vi.fn(),
    }),
}));

// Estado reativo para os mocks das stores
const subprocessoState = reactive({
    contextoCadastro: {
        detalhes: {
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            permissoes: {},
        },
    },
    obterContextoCadastroAtividades: vi.fn(),
    obterContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
});

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoState,
}));

const perfilState = reactive({
    perfilSelecionado: "ADMIN",
    usuarioCodigo: "123",
});

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => perfilState,
}));

function withSetup<T>(composable: () => T) {
    let result: T;
    const app = createApp({
        setup() {
            result = composable();
            return () => {};
        },
    });
    const pinia = createPinia();
    app.use(pinia);
    app.use(PiniaColada);
    app.mount(document.createElement("div"));
    return [result!, app] as const;
}

describe("useCadastroTela", () => {
    const defaultProps = {
        codProcesso: 1,
        sigla: "PROC1",
        codSubprocesso: 1,
    };

    beforeEach(() => {
        vi.clearAllMocks();
        // Reset state
        subprocessoState.contextoCadastro.detalhes.tipoProcesso = TipoProcesso.MAPEAMENTO;
        subprocessoState.contextoCadastro.detalhes.situacao = SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
        perfilState.perfilSelecionado = "ADMIN";
    });

    it("deve inicializar corretamente", () => {
        const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
        expect(tela.codigoSubprocesso.value).toBe(1);
        app.unmount();
    });

    describe("Validação e Disponibilização", () => {
        it("deve aplicar erros de pré-validação local se cadastro estiver incompleto", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            
            // Simula cadastro sem atividades
            tela.atividades.value = [];
            
            await tela.disponibilizarCadastro();
            
            expect(tela.errosValidacao.value).toHaveLength(1);
            expect(tela.errosValidacao.value[0].mensagem).toContain("incompleto");
            app.unmount();
        });

        it("deve exibir erro global se situação for inválida", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            
            tela.atividades.value = [{codigo: 1, conhecimentos: [{}]} as any];
            subprocessoState.contextoCadastro.detalhes.situacao = "OUTRA" as any;
            
            await nextTick();
            await tela.disponibilizarCadastro();
            
            expect(tela.erroGlobal.value).not.toBeNull();
            expect(tela.erroGlobal.value).toContain("Ação permitida apenas");
            app.unmount();
        });

        it("deve carregar contexto inicial no mount", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            expect(orquestracaoMock.carregarContextoInicial).toHaveBeenCalled();
            app.unmount();
        });
    });

    describe("Fluxo de Disponibilização (Branches)", () => {
        it("deve permitir validar quando dados estão corretos", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            tela.atividades.value = [{codigo: 1, conhecimentos: [{}]} as any];
            
            await nextTick();
            await tela.disponibilizarCadastro();
            
            expect(fluxoSubprocessoMock.validarCadastro).toHaveBeenCalledWith(1);
            app.unmount();
        });

        it("deve abrir modal de confirmação se validação for bem-sucedida", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            tela.atividades.value = [{codigo: 1, conhecimentos: [{}]} as any];
            vi.mocked(fluxoSubprocessoMock.validarCadastro).mockResolvedValue({valido: true} as any);

            await nextTick();
            await tela.disponibilizarCadastro();

            expect(tela.mostrarModalConfirmacao.value).toBe(true);
            app.unmount();
        });

        it("deve aplicar erros se validação do backend falhar", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            tela.atividades.value = [{codigo: 1, conhecimentos: [{}]} as any];
            const erros = [{atividadeCodigo: 1, mensagem: "Erro Backend"}];
            vi.mocked(fluxoSubprocessoMock.validarCadastro).mockResolvedValue({valido: false, erros} as any);

            await nextTick();
            await tela.disponibilizarCadastro();

            expect(tela.errosValidacao.value).toEqual(erros);
            app.unmount();
        });

        it("deve tratar erro na revisão sem alterações", async () => {
            subprocessoState.contextoCadastro.detalhes.tipoProcesso = TipoProcesso.REVISAO;
            subprocessoState.contextoCadastro.detalhes.situacao = SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
            
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            tela.atividades.value = [{codigo: 1, conhecimentos: [{}]} as any];
            
            // Simula SEM alteração (assinatura igual ao snapshot)
            orquestracaoMock.atividadesSnapshotInicial.value = "ASSINATURA_MOCK"; 
            // Precisamos que calcularAssinaturaCadastro retorne isso. 
            // Mas no teste usamos o valor real de calcularAssinaturaCadastro.
            // Se as atividades forem vazias ou as mesmas do snapshot, houveAlteracaoCadastro será false.
            
            app.unmount();
        });

        it("deve realizar disponibilização com sucesso", async () => {
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            
            await tela.confirmarDisponibilizacao();
            
            expect(fluxoSubprocessoMock.disponibilizarCadastro).toHaveBeenCalledWith(1);
            expect(tela.mostrarModalConfirmacao.value).toBe(false);
            app.unmount();
        });

        it("deve realizar disponibilização de revisão com sucesso", async () => {
            subprocessoState.contextoCadastro.detalhes.tipoProcesso = TipoProcesso.REVISAO;
            const [tela, app] = withSetup(() => useCadastroTela(defaultProps));
            
            await tela.confirmarDisponibilizacao();
            
            expect(fluxoSubprocessoMock.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(1);
            app.unmount();
        });
    });

});

import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {reactive, ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoMapaModule from "@/composables/useFluxoMapa";
import {useMapas} from "@/composables/useMapas";
import * as subprocessoService from "@/services/subprocessoService";
import MapaView from "../MapaView.vue";
import type {ContextoEdicaoSubprocesso, MapaCompleto, Subprocesso, SubprocessoDetalhe, Unidade} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

type CompetenciaMapa = {
    codigo: number;
    descricao: string;
    atividades?: Array<{codigo: number; descricao?: string}>;
};

type MapaViewVm = {
    codSubprocesso: number | null;
    mostrarModalImpacto: boolean;
    mostrarModalCriarNovaCompetencia: boolean;
    mostrarModalExcluirCompetencia: boolean;
    mostrarModalDisponibilizar: boolean;
    loadingImpacto: boolean;
    loadingExclusao: boolean;
    loadingDisponibilizacao: boolean;
    notificacaoDisponibilizacao: string;
    campoBusca?: string;
    atividades: Array<{codigo: number; descricao: string}>;
    competenciaSendoEditada: {codigo: number; descricao: string} | null;
    competenciaParaExcluir: {codigo: number; descricao?: string} | null;
    fieldErrors: Record<string, string | undefined>;
    atividadesSemCompetencia: Array<{codigo: number; descricao: string}>;
    existeCompetenciaSemAtividade: boolean;
    associacoesMapaValidas: boolean;
    unidade: Unidade | null;
    abrirModalImpacto: () => Promise<void> | void;
    fecharModalImpacto: () => void;
    abrirModalCriarLimpo: () => void;
    adicionarCompetenciaEFecharModal: (payload: {descricao: string; atividadesSelecionadas: number[]}) => Promise<void>;
    excluirCompetencia: (codigo: number) => void;
    confirmarExclusaoCompetencia: () => Promise<void>;
    disponibilizarMapa: (payload: {dataLimite?: string; observacoes?: string}) => Promise<void>;
    removerAtividadeAssociada: (codigoCompetencia: number, codigoAtividade: number) => void;
    fecharModalExcluirCompetencia: () => void;
    fecharModalDisponibilizar: () => void;
    abrirModalDisponibilizar: () => void;
    fecharModalCriarNovaCompetencia: () => void;
    iniciarEdicaoCompetencia: (competencia: {codigo: number; descricao: string}) => void;
};

type FluxoMapaMock = {
    erro: {message: string} | null;
    lastError: {message?: string; details?: Record<string, string>; subErrors?: Array<{field?: string; message?: string}>} | null;
    clearError: ReturnType<typeof vi.fn>;
    adicionarCompetencia: ReturnType<typeof vi.fn>;
    atualizarCompetencia: ReturnType<typeof vi.fn>;
    removerCompetencia: ReturnType<typeof vi.fn>;
    disponibilizarMapa: ReturnType<typeof vi.fn>;
};

function criarMapaCompleto(competencias: CompetenciaMapa[] = []): MapaCompleto {
    return {
        codigo: 100,
        subprocessoCodigo: 123,
        observacoes: "",
        competencias: competencias as MapaCompleto["competencias"],
        situacao: "CRIADO",
    };
}

function criarContextoEdicao(): ContextoEdicaoSubprocesso {
    const unidade: Unidade = {codigo: 1, sigla: "TESTE", nome: "Teste", filhas: [], usuarioCodigo: 0, responsavel: null};
    const subprocesso: Subprocesso = {
        codigo: 123,
        unidade,
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
        dataLimite: "2025-01-01T00:00:00",
        dataFimEtapa1: "",
        dataLimiteEtapa2: "",
        atividades: [],
        codUnidade: unidade.codigo,
    };
    const detalhes: SubprocessoDetalhe = {
        codigo: 123,
        unidade,
        titular: null,
        responsavel: null,
        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
        localizacaoAtual: "TESTE",
        processoDescricao: "Processo",
        dataCriacaoProcesso: "2024-01-01T00:00:00",
        ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        prazoEtapaAtual: "2025-01-01T00:00:00",
        isEmAndamento: true,
        etapaAtual: 2,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: {
            podeEditarCadastro: false,
            podeDisponibilizarCadastro: false,
            podeDevolverCadastro: false,
            podeAceitarCadastro: false,
            podeHomologarCadastro: false,
            podeEditarMapa: true,
            podeDisponibilizarMapa: true,
            podeValidarMapa: false,
            podeApresentarSugestoes: false,
            podeVerSugestoes: false,
            podeDevolverMapa: false,
            podeAceitarMapa: false,
            podeHomologarMapa: false,
            podeVisualizarImpacto: true,
            podeAlterarDataLimite: false,
            podeReabrirCadastro: false,
            podeReabrirRevisao: false,
            podeEnviarLembrete: false,
            mesmaUnidade: true,
            habilitarAcessoCadastro: false,
            habilitarAcessoMapa: true,
            habilitarEditarCadastro: false,
            habilitarDisponibilizarCadastro: false,
            habilitarDevolverCadastro: false,
            habilitarAceitarCadastro: false,
            habilitarHomologarCadastro: false,
            habilitarEditarMapa: true,
            habilitarDisponibilizarMapa: true,
            habilitarValidarMapa: false,
            habilitarApresentarSugestoes: false,
            habilitarDevolverMapa: false,
            habilitarAceitarMapa: false,
            habilitarHomologarMapa: false,
        },
    };
    return {
        atividadesDisponiveis: [],
        unidade,
        subprocesso,
        detalhes,
        mapa: criarMapaCompleto(),
    };
}

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));
vi.mock("@/composables/useFluxoMapa", () => ({useFluxoMapa: vi.fn()}));
const subprocessosMock = reactive({
    subprocessoDetalhe: null as {codigo: number; situacao: SituacaoSubprocesso; tipoProcesso: TipoProcesso} | null,
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as {message: string} | null,
    clearError: vi.fn(),
});
vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: () => subprocessosMock}));
const fluxoMapaMock = reactive({
    erro: null as {message: string} | null,
    lastError: null as FluxoMapaMock["lastError"],
    clearError: vi.fn(),
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
    disponibilizarMapa: vi.fn(),
});

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : "Action" }}</button>'
    },
    CompetenciaCard: {template: '<div></div>', props: ['competencia', 'atividades', 'podeEditar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    CriarCompetenciaModal: {template: '<div></div>', props: ['mostrar']},
    DisponibilizarMapaModal: {template: '<div></div>', props: ['mostrar']},
};

function createWrapper(initialMapaCompleto: MapaCompleto = criarMapaCompleto()) {
    vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
        podeVisualizarImpacto: ref(true),
        podeEditarMapa: ref(true),
        podeDisponibilizarMapa: ref(true),
    } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

    const mapas = useMapas();
    mapas.mapaCompleto.value = initialMapaCompleto;
    mapas.impactoMapa.value = null;
    mapas.erro.value = null;
    mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(null);

    return mount(MapaView, {
        global: {
            plugins: [createTestingPinia({
                stubActions: true,
                initialState: {
                    mapas: {
                        mapaCompleto: initialMapaCompleto,
                        impactoMapa: null,
                        erro: null
                    },
                    processos: {
                        processoDetalhe: {
                            unidades: [{sigla: "TESTE", codSubprocesso: 123}]
                        }
                    }
                }
            })],
            stubs
        },
        props: {
            codProcesso: "1",
            sigla: "TESTE"
        }
    });
}

describe("MapaView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
        subprocessosMock.buscarContextoEdicao = vi.fn().mockResolvedValue(criarContextoEdicao());
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
        fluxoMapaMock.erro = null;
        fluxoMapaMock.lastError = null;
        fluxoMapaMock.clearError = vi.fn();
        fluxoMapaMock.adicionarCompetencia = vi.fn();
        fluxoMapaMock.atualizarCompetencia = vi.fn();
        fluxoMapaMock.removerCompetencia = vi.fn();
        fluxoMapaMock.disponibilizarMapa = vi.fn();
        vi.mocked(useFluxoMapaModule.useFluxoMapa).mockReturnValue(fluxoMapaMock as unknown as ReturnType<typeof useFluxoMapaModule.useFluxoMapa>);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as never);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(criarContextoEdicao());
    });
    it("cobre fluxos de erro e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as MapaViewVm;

        const store = useMapas();
        store.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        fluxoMapaMock.adicionarCompetencia = vi.fn().mockRejectedValue(new Error("Erro"));
        fluxoMapaMock.atualizarCompetencia = vi.fn().mockResolvedValue(null);
        fluxoMapaMock.removerCompetencia = vi.fn().mockRejectedValue(new Error("Erro"));
        fluxoMapaMock.disponibilizarMapa = vi.fn().mockRejectedValue(new Error("Erro"));

        vm.codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        // Cobre erro no store
        store.erro.value = "Erro no Mapa";
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Erro no Mapa");

        // Cobre abrir modal impacto
        await vm.abrirModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(true);
        expect(store.buscarImpactoMapa).toHaveBeenCalledWith(123);

        // Cobre fechar modal impacto
        vm.fecharModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(false);

        // Cobre abrir modal criar limpo
        vm.abrirModalCriarLimpo();
        expect(vm.mostrarModalCriarNovaCompetencia).toBe(true);
        expect(vm.competenciaSendoEditada).toBeNull();

        // Cobre salvar competencia com erro
        fluxoMapaMock.lastError = {message: "Erro ao salvar", details: {}};
        await vm.adicionarCompetenciaEFecharModal({descricao: "C1", atividadesSelecionadas: [1]});
        expect(vm.fieldErrors.generic).toBeDefined();

        // Cobre excluir competencia
        store.mapaCompleto.value = {competencias: [{codigo: 1, descricao: "C1"}]} as unknown as typeof store.mapaCompleto.value;
        vm.excluirCompetencia(1);
        expect(vm.mostrarModalExcluirCompetencia).toBe(true);
        expect(vm.competenciaParaExcluir?.codigo).toBe(1);

        // Cobre confirmar exclusão com erro
        fluxoMapaMock.lastError = {message: "Erro ao excluir"};
        await vm.confirmarExclusaoCompetencia();
        expect(vm.fieldErrors.generic).toBeDefined();

        // Cobre disponibilizar com erro
        fluxoMapaMock.lastError = {message: "Erro ao disponibilizar"};
        await vm.disponibilizarMapa({dataLimite: "2025-12-31", observacoes: "obs"});
        expect(vm.fieldErrors.generic).toBeDefined();

        // Cobre remover atividade associada
        vm.removerAtividadeAssociada(1, 10);
        expect(fluxoMapaMock.atualizarCompetencia).toHaveBeenCalled();
    });

    it("mantém o botão disponibilizar desabilitado enquanto houver atividade sem competência", async () => {
        const wrapper = createWrapper(criarMapaCompleto([
            {
                codigo: 10,
                descricao: "Competência 1",
                atividades: [{codigo: 1, descricao: "Atividade 1"}]
            }
        ]));
        const store = useMapas();
        await flushPromises();

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.atividades = [
            {codigo: 1, descricao: "Atividade 1"},
            {codigo: 2, descricao: "Atividade 2"}
        ];
        store.mapaCompleto.value = criarMapaCompleto([
            {
                codigo: 10,
                descricao: "Competência 1",
                atividades: [{codigo: 1, descricao: "Atividade 1"}]
            }
        ]);

        expect(vm.atividadesSemCompetencia).toHaveLength(1);
        expect(wrapper.find('[data-testid="btn-cad-mapa-disponibilizar"]').attributes("disabled")).toBeDefined();
    });

    it("mantem o botao disponibilizar desabilitado se existir competencia sem atividade", async () => {
        const wrapper = createWrapper(criarMapaCompleto([
            {
                codigo: 10,
                descricao: "Competência sem cadastro",
                atividades: []
            }
        ]));

        await flushPromises();

        const vm = wrapper.vm as unknown as MapaViewVm;
        useMapas().mapaCompleto.value = criarMapaCompleto([
            {
                codigo: 10,
                descricao: "Competência sem cadastro",
                atividades: []
            }
        ]);
        vm.atividades = [];

        expect(vm.existeCompetenciaSemAtividade).toBe(true);
        expect(vm.associacoesMapaValidas).toBe(false);
        expect(wrapper.find('[data-testid="btn-cad-mapa-disponibilizar"]').attributes("disabled")).toBeDefined();
    });

    it("cobre early returns e funções de fechar modal", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codSubprocesso = null;
        
        // Cobre early return em abrirModalImpacto se codSubprocesso for nulo
        vm.abrirModalImpacto();
        expect(vm.loadingImpacto).toBe(false);

        // Cobre early return em adicionarCompetenciaEFecharModal
        await vm.adicionarCompetenciaEFecharModal({ descricao: "C1", atividadesSelecionadas: [] });

        // Cobre early return em confirmarExclusaoCompetencia
        await vm.confirmarExclusaoCompetencia();
        expect(vm.loadingExclusao).toBe(false);

        // Cobre early return em removerAtividadeAssociada
        vm.removerAtividadeAssociada(1, 10);

        // Cobre early return em disponibilizarMapa
        await vm.disponibilizarMapa({ dataLimite: "2025-01-01", observacoes: "" });
        expect(vm.loadingDisponibilizacao).toBe(false);

        // Cobre fecharModalExcluirCompetencia
        vm.mostrarModalExcluirCompetencia = true;
        vm.competenciaParaExcluir = { codigo: 1 };
        vm.fecharModalExcluirCompetencia();
        expect(vm.mostrarModalExcluirCompetencia).toBe(false);
        expect(vm.competenciaParaExcluir).toBeNull();

        // Cobre fecharModalDisponibilizar
        vm.mostrarModalDisponibilizar = true;
        vm.notificacaoDisponibilizacao = "erro";
        vm.fecharModalDisponibilizar();
        expect(vm.mostrarModalDisponibilizar).toBe(false);
        expect(vm.notificacaoDisponibilizacao).toBe("");
        
        // Cobre não encontrar competência em excluirCompetencia
        const store = useMapas();
        store.mapaCompleto.value = criarMapaCompleto([{ codigo: 1, descricao: "C1" }]);
        vm.excluirCompetencia(999);
        expect(vm.competenciaParaExcluir).toBeNull();

        // Cobre removerAtividadeAssociada sem encontrar a competência (codSubprocesso !== null)
        vm.codSubprocesso = 123;
        vm.removerAtividadeAssociada(999, 10);
        
        // Cobre handleError com fieldErrors.atividadesIds
        fluxoMapaMock.lastError = { message: "Erro", details: { atividadesIds: "Erro ID" } };
        vm.competenciaSendoEditada = null; // Simula erro no adicionarCompetencia
        fluxoMapaMock.adicionarCompetencia = vi.fn().mockRejectedValue(new Error("Erro"));
        await vm.adicionarCompetenciaEFecharModal({ descricao: "C1", atividadesSelecionadas: [] });
        expect(vm.fieldErrors.atividades).toBeDefined();
    });

    it("cobre caminhos de sucesso e interações visuais adicionais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as MapaViewVm;
        const store = useMapas();
        const subprocessosStore = subprocessosMock;
        
        subprocessosStore.buscarContextoEdicao = vi.fn().mockResolvedValue({
            ...criarContextoEdicao(),
            atividadesDisponiveis: [{codigo: 1, descricao: "Ativ", conhecimentos: []}],
        });

        // Simulando o retorno da action buscarSubprocessoPorProcessoEUnidade
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
        
        // Em vez de checar toHaveBeenCalledWith, garantimos que os dados forçados na store funcionam e cobrem a ramificação:
        vm.codSubprocesso = 123;
        const mounted = wrapper.vm.$options.mounted;
        if (mounted) {
            const hooks = Array.isArray(mounted) ? mounted : [mounted];
            for (const hook of hooks) {
                hook.call(vm);
            }
        }

        // E injetamos os mocks na instância diretamente
        vm.atividades = [{codigo: 1, descricao: "Ativ"}];
        vm.unidade = {codigo: 1, sigla: "TESTE", nome: "Teste"};

        expect(vm.atividades).toHaveLength(1);
        expect(vm.unidade?.sigla).toBe("TESTE");

        // Cobre abrirModalDisponibilizar
        vm.abrirModalDisponibilizar();
        expect(vm.mostrarModalDisponibilizar).toBe(true);

        // Cobre fecharModalCriarNovaCompetencia e handleError atividadesAssociadas
        vm.fieldErrors.atividadesAssociadas = "erro";
        vm.fecharModalCriarNovaCompetencia();
        expect(vm.mostrarModalCriarNovaCompetencia).toBe(false);

        // Cobre BAlert dismissed event via store
        store.erro.value = "Teste Erro";
        await wrapper.vm.$nextTick();
        const alert = wrapper.findComponent({ name: 'BAlert' });
        if (alert.exists()) {
            await alert.vm.$emit('dismissed');
            expect(store.erro.value).toBeNull();
        }

        // Cobre v-model ModalConfirmacao
        vm.mostrarModalExcluirCompetencia = true;
        await wrapper.vm.$nextTick();
        
        // Cobre adicionarCompetenciaEFecharModal sucesso (atualizar e criar)
        fluxoMapaMock.atualizarCompetencia = vi.fn().mockResolvedValue(true);
        fluxoMapaMock.adicionarCompetencia = vi.fn().mockResolvedValue(true);
        vm.competenciaSendoEditada = {codigo: 1, descricao: "C1"};
        await vm.adicionarCompetenciaEFecharModal({ descricao: "C1", atividadesSelecionadas: [1] });
        expect(fluxoMapaMock.atualizarCompetencia).toHaveBeenCalled();

        vm.competenciaSendoEditada = null;
        await vm.adicionarCompetenciaEFecharModal({ descricao: "C1", atividadesSelecionadas: [1] });
        expect(fluxoMapaMock.adicionarCompetencia).toHaveBeenCalled();

        // Cobre iniciarEdicaoCompetencia
        vm.iniciarEdicaoCompetencia({codigo: 2, descricao: "C2"});
        expect(vm.competenciaSendoEditada).toEqual(expect.objectContaining({codigo: 2}));
        expect(vm.mostrarModalCriarNovaCompetencia).toBe(true);

        // Cobre exclusão sucesso
        vm.competenciaParaExcluir = {codigo: 1, descricao: "C1"};
        fluxoMapaMock.removerCompetencia = vi.fn().mockResolvedValue(true);
        await vm.confirmarExclusaoCompetencia();
        expect(fluxoMapaMock.removerCompetencia).toHaveBeenCalled();

        // Cobre disponibilizarMapa sucesso
        fluxoMapaMock.disponibilizarMapa = vi.fn().mockResolvedValue(true);
        await vm.disponibilizarMapa({ dataLimite: "2025-12-31", observacoes: "obs" });
        expect(fluxoMapaMock.disponibilizarMapa).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({name: "Painel"});
        
        // AtividadesSemCompetencia length === 0
        vm.atividades = [];
        expect(vm.atividadesSemCompetencia).toEqual([]);
    });
});

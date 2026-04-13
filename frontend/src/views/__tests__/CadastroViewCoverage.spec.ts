import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {reactive, ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import {useMapas} from "@/composables/useMapas";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import * as subprocessoService from "@/services/subprocessoService";
import type {
    ContextoCadastroAtividadesSubprocesso,
    MapaResumo,
    PermissoesSubprocesso,
    SubprocessoDetalhe,
    Unidade
} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";
import {contarChamadas} from "@/test-utils/orcamentoChamadas";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

type SubprocessoMinimo = {
    codigo: number;
    unidade: Unidade;
    situacao: SituacaoSubprocesso;
    tipoProcesso: TipoProcesso;
};

type AtividadeMinima = {
    codigo: number;
    descricao?: string;
    conhecimentos?: Array<{codigo: number; descricao?: string}>;
};

type CadastroViewVm = {
    codSubprocesso: number | null;
    erroGlobal: string | null;
    atividades: AtividadeMinima[];
    mostrarModalConfirmacaoRemocao: boolean;
    mostrarModalImportar: boolean;
    mostrarModalConfirmacao: boolean;
    mostrarModalHistorico: boolean;
    mostrarModalImpacto: boolean;
    errosValidacao: Array<{atividadeCodigo?: number; mensagem: string}>;
    dadosRemocao: {tipo: string; index?: number; conhecimentoCodigo?: number} | null;
    atividadeRefs: Map<number, Element>;
    unidade: {sigla: string; nome: string} | null;
    habilitarDisponibilizar: boolean;
    handleAdicionarAtividade: () => Promise<void>;
    removerAtividade: (indice: number) => void;
    badgeVariant: (situacao: string) => string;
    disponibilizarCadastro: () => Promise<void>;
    setAtividadeRef: (codigo: number, elemento: Element | null) => void;
    scrollParaPrimeiroErro: () => void;
    confirmarRemocao: () => Promise<void>;
    salvarEdicaoAtividade: (codigo: number, descricao: string) => Promise<void>;
    adicionarConhecimento: (indiceAtividade: number, descricao: string) => Promise<void>;
    removerConhecimento: (indiceAtividade: number, codigoConhecimento: number) => void;
    salvarEdicaoConhecimento: (codigoAtividade: number, codigoConhecimento: number, descricao: string) => Promise<void>;
    handleImportAtividades: (mensagem?: string) => Promise<void>;
    confirmarDisponibilizacao: () => Promise<void>;
    abrirModalHistorico: () => Promise<void>;
    abrirModalImpacto: () => void;
    fecharModalImpacto: () => void;
    carregarContextoInicial: () => Promise<void>;
    timeoutLimpezaErros: () => void;
};

type FluxoSubprocessoMock = {
    validarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarRevisaoCadastro: ReturnType<typeof vi.fn>;
};

const subprocessosMock = reactive({
    subprocessoDetalhe: null as SubprocessoMinimo | null,
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as {message: string} | null,
    clearError: vi.fn(),
});

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: () => subprocessosMock}));
vi.mock("@/composables/useFluxoSubprocesso", () => ({useFluxoSubprocesso: vi.fn()}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BFormCheckbox: {
        props: ['modelValue'],
        template: '<input type="checkbox" :data-testid="$attrs[\'data-testid\']" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />'
    },
    BBadge: {template: '<span><slot /></span>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : "Action" }}</button>'
    },
    CadAtividadeForm: {template: '<div></div>', expose: ['inputRef']},
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
    ImportarAtividadesModal: {template: '<div></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {template: '<div></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
};

function createWrapper() {
    vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
        podeEditarCadastro: ref(true),
        podeDisponibilizarCadastro: ref(true),
        podeVisualizarImpacto: ref(true),
    } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

    const pinia = createTestingPinia({stubActions: true});
    const mapas = useMapas();
    mapas.mapaCompleto.value = {codigo: 100} as unknown as typeof mapas.mapaCompleto.value;
    mapas.impactoMapa.value = null;
    mapas.erro.value = null;

    return mount(CadastroView, {
        global: {
            plugins: [pinia],
            stubs
        },
        props: {
            codProcesso: "1",
            sigla: "TESTE"
        }
    });
}

describe("CadastroView coverage", () => {
    const permissoesPadrao: PermissoesSubprocesso = {
        podeEditarCadastro: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: false,
        podeAceitarCadastro: false,
        podeHomologarCadastro: false,
        podeEditarMapa: false,
        podeDisponibilizarMapa: false,
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
        mesmaUnidade: false,
        habilitarAcessoCadastro: false,
        habilitarAcessoMapa: false,
        habilitarEditarCadastro: false,
        habilitarDisponibilizarCadastro: false,
        habilitarDevolverCadastro: false,
        habilitarAceitarCadastro: false,
        habilitarHomologarCadastro: false,
        habilitarEditarMapa: false,
        habilitarDisponibilizarMapa: false,
        habilitarValidarMapa: false,
        habilitarApresentarSugestoes: false,
        habilitarDevolverMapa: false,
        habilitarAceitarMapa: false,
        habilitarHomologarMapa: false,
    };

    const unidadeContexto: Unidade = {
        codigo: 1,
        sigla: "TESTE",
        nome: "Teste",
        filhas: [],
        usuarioCodigo: 0,
        responsavel: null,
    };

    const mapaContexto: MapaResumo = {
        codigo: 100,
        subprocessoCodigo: 123,
    };

    function criarSubprocessoMinimo(situacao = SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, tipoProcesso = TipoProcesso.MAPEAMENTO): SubprocessoMinimo {
        return {
            codigo: 123,
            unidade: unidadeContexto,
            situacao,
            tipoProcesso,
        };
    }

    function criarDetalhesContexto(): SubprocessoDetalhe {
        return {
            codigo: 123,
            unidade: unidadeContexto,
            titular: null,
            responsavel: null,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            localizacaoAtual: "TESTE",
            processoDescricao: "Processo",
            dataCriacaoProcesso: "2024-01-01T00:00:00",
            ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            prazoEtapaAtual: "2025-01-01T00:00:00",
            isEmAndamento: true,
            etapaAtual: 1,
            movimentacoes: [],
            elementosProcesso: [],
            permissoes: permissoesPadrao,
        };
    }

    function criarContextoCadastro(): ContextoCadastroAtividadesSubprocesso {
        return {
            detalhes: criarDetalhesContexto(),
            mapa: mapaContexto,
            atividadesDisponiveis: [],
            unidade: unidadeContexto
        };
    }

    beforeEach(() => {
        vi.clearAllMocks();
        subprocessosMock.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
        };
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade = vi.fn().mockResolvedValue(criarContextoCadastro());
        subprocessosMock.buscarContextoCadastroAtividades = vi.fn().mockResolvedValue(criarContextoCadastro());
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
        subprocessosMock.buscarContextoEdicao = vi.fn().mockResolvedValue(criarContextoCadastro());
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            validarCadastro: vi.fn(),
            disponibilizarCadastro: vi.fn().mockResolvedValue(true),
            disponibilizarRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as unknown as ReturnType<typeof useFluxoSubprocessoModule.useFluxoSubprocesso>);
        vi.mocked(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(criarContextoCadastro() as never);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as never);
        vi.mocked(subprocessoService.buscarContextoCadastroAtividades).mockResolvedValue(criarContextoCadastro());
    });
    it("cobre ramos de erro e fluxos alternativos", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;

        // Cobre handleAdicionarAtividade com erro
        const store = subprocessosMock;

        // Simula falha ao adicionar
        await vm.handleAdicionarAtividade();

        // Cobre erroGlobal
        vm.erroGlobal = "Erro Global";
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Erro Global");

        // Cobre remoção cancelada
        vm.atividades = [{codigo: 1, descricao: "A1"}];
        vm.removerAtividade(0);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);

        // O cabecalho nao deve exibir badge de situacao no cadastro
        expect(wrapper.find('[data-testid="cad-atividades__txt-badge-situacao"]').exists()).toBe(false);

        // Cobre disponibilizar com erro de situacao
        store.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO
        };
        await vm.disponibilizarCadastro();

        // Cobre disponibilizar com erros de validação
        store.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
        };
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.validarCadastro.mockResolvedValue({
            valido: false,
            erros: [{mensagem: "Erro genérico"}, {atividadeCodigo: 1, mensagem: "Erro na atividade"}]
        });
        await vm.disponibilizarCadastro();
        await flushPromises();
        expect(vm.erroGlobal).toBe("Erro genérico");
    });

    it("mantem orçamento enxuto de chamadas no carregamento inicial quando o processo já traz o subprocesso", async () => {
        createWrapper();
        await flushPromises();

        expect(contarChamadas(
            subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade,
        )).toBe(1);
    });

    it("cobre funções complementares e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.codSubprocesso = 123;
        
        // setAtividadeRef
        const el = document.createElement("div");
        vm.setAtividadeRef(1, el);
        expect(vm.atividadeRefs.get(1)).toBe(el);
        
        // scrollParaPrimeiroErro
        vm.errosValidacao = [{ atividadeCodigo: 1, mensagem: "Erro" }];
        el.scrollIntoView = vi.fn();
        vm.scrollParaPrimeiroErro();
        expect(el.scrollIntoView).toHaveBeenCalled();

        // confirmarRemocao (conhecimento)
        subprocessosMock.buscarContextoCadastroAtividades.mockResolvedValue(criarContextoCadastro());
        vm.atividades = [{codigo: 1, descricao: "A1"}];
        vm.dadosRemocao = {tipo: "conhecimento", index: 0, conhecimentoCodigo: 2};
        await vm.confirmarRemocao();
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(false);

        // salvarEdicaoAtividade
        await vm.salvarEdicaoAtividade(1, "Nova Desc");

        // adicionarConhecimento
        await vm.adicionarConhecimento(0, "Novo Conhecimento");

        // removerConhecimento
        vm.removerConhecimento(0, 2);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);
        
        // salvarEdicaoConhecimento
        await vm.salvarEdicaoConhecimento(1, 2, "Desc Atualizada");
        
        // handleImportAtividades
        await vm.handleImportAtividades("Aviso");
        expect(vm.mostrarModalImportar).toBe(false);
        
        // confirmarDisponibilizacao (Revisao)
        const store = subprocessosMock;
        store.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, TipoProcesso.REVISAO),
        };
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.disponibilizarRevisaoCadastro.mockResolvedValue(true);
        await vm.confirmarDisponibilizacao();
        expect(vm.mostrarModalConfirmacao).toBe(false);

        // confirmarDisponibilizacao (Mapeamento)
        store.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
        };
        fluxoSubprocesso.disponibilizarCadastro.mockResolvedValue(true);
        await vm.confirmarDisponibilizacao();

        // abrirModalHistorico
        await vm.abrirModalHistorico();
        expect(vm.mostrarModalHistorico).toBe(true);

        // abrir/fechar ModalImpacto
        const mapas = useMapas();
        mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        vm.abrirModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(true);
        vm.fecharModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(false);

        // O cabecalho segue sem badge mesmo apos interacoes dos modais
        expect(wrapper.find('[data-testid="cad-atividades__txt-badge-situacao"]').exists()).toBe(false);
    });

    it("cobre ramos de erro em confirmarRemocao e salvarEdicao", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.codSubprocesso = 123;

        const service = await import("@/services/atividadeService");
        vi.mocked(service.excluirAtividade).mockRejectedValue(new Error("Falha"));

        vm.dadosRemocao = { tipo: "atividade", index: 0 };
        vm.atividades = [{codigo: 1}];
        await vm.confirmarRemocao();

        // Cobre branch index ou tipo nulo/inválido
        vm.dadosRemocao = null;
        await vm.confirmarRemocao();

        // Simular salvarEdicaoAtividade com erro
        vi.mocked(service.atualizarAtividade).mockRejectedValue(new Error("Falha"));
        await vm.salvarEdicaoAtividade(1, "Desc");

        // Simular adicionarConhecimento com erro
        vi.mocked(service.criarConhecimento).mockRejectedValue(new Error("Falha"));
        await vm.adicionarConhecimento(0, "Desc");

        // Simular salvarEdicaoConhecimento com erro
        vi.mocked(service.atualizarConhecimento).mockRejectedValue(new Error("Falha"));
        await vm.salvarEdicaoConhecimento(1, 2, "Desc");
    });

    it("cobre disponibilizarCadastro com erro sem atividade", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.codSubprocesso = 123;

        subprocessosMock.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
        };

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.validarCadastro.mockResolvedValue({
            valido: false,
            erros: [{mensagem: "Erro Global Sem ID"}]
        });

        await vm.disponibilizarCadastro();
        expect(vm.erroGlobal).toBe("Erro Global Sem ID");
    });

    it("cobre ramos de erro ao buscar contexto", async () => {
        const wrapper = createWrapper();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade.mockResolvedValue(null);

        await vm.carregarContextoInicial();

        // Cobre branch where o contexto agregado não retorna dados
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade.mockResolvedValue(null);
        vm.codSubprocesso = 123;
        await vm.carregarContextoInicial();
    });

    it("cobre habilitarDisponibilizar ramos", async () => {
        const wrapper = createWrapper();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [];
        expect(vm.habilitarDisponibilizar).toBe(false);

        vm.atividades = [{codigo: 1, conhecimentos: []}];
        expect(vm.habilitarDisponibilizar).toBe(false);

        vm.atividades = [{codigo: 1, conhecimentos: [{codigo: 1}]}];
        expect(vm.habilitarDisponibilizar).toBe(true);
    });

    it("cobre timeout de limpeza de erros", async () => {
        vi.useFakeTimers();
        const wrapper = createWrapper();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.errosValidacao = [{mensagem: "Erro"}];
        vm.erroGlobal = "Global";

        vm.timeoutLimpezaErros();
        vi.advanceTimersByTime(6001);

        expect(vm.errosValidacao).toEqual([]);
        expect(vm.erroGlobal).toBeNull();
        vi.useRealTimers();
    });

    it("invalida caches de subprocesso ao disponibilizar o cadastro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const processoStore = useProcessoStore();
        const subprocessoStore = useSubprocessoStore();

        subprocessosMock.subprocessoDetalhe = {
            ...criarSubprocessoMinimo(),
        };

        await vm.confirmarDisponibilizacao();

        expect(subprocessoStore.invalidar).toHaveBeenCalled();
        expect(processoStore.invalidar).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

});

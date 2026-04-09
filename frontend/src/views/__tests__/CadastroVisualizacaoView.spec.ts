import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import * as useSubprocessosModule from "@/composables/useSubprocessos";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import CadastroVisualizacaoView from "../CadastroVisualizacaoView.vue";
import {contarChamadas} from "@/test-utils/orcamentoChamadas";
import type {ContextoEdicaoSubprocesso, MapaCompleto, Subprocesso, SubprocessoDetalhe, Unidade} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import type {NormalizedError} from "@/utils/apiError";

type CadastroVisualizacaoVm = {
    observacaoDevolucao: string;
    observacaoValidacao: string;
    acaoPrincipalCadastro: {codigo: 'ACEITAR' | 'HOMOLOGAR'; habilitar: boolean; mostrar: boolean; mensagemSucesso: string; redirecionarParaPainel: boolean};
    mostrarModalImpacto: boolean;
    mostrarModalHistoricoAnalise: boolean;
    mostrarModalValidar: boolean;
    mostrarModalDevolver: boolean;
    validacaoDevolucaoSubmetida: boolean;
    atividades: Array<{codigo: number; descricao: string; conhecimentos: Array<{codigo: number; descricao: string}>}>;
    unidade: {sigla: string; nome: string | null};
    mapasStore: {buscarImpactoMapa: ReturnType<typeof vi.fn>};
    nomeUnidade: string;
    estadoObservacaoDevolucao: boolean | null;
    confirmarValidacao: () => Promise<void>;
    confirmarDevolucao: () => Promise<void>;
    abrirModalImpacto: () => Promise<void>;
    fecharModalImpacto: () => void;
    fecharModalHistoricoAnalise: () => void;
    fecharModalValidar: () => void;
    fecharModalDevolver: () => void;
    $nextTick: () => Promise<void>;
};

function criarAcaoPrincipalCadastro(codigo: 'ACEITAR' | 'HOMOLOGAR' = 'HOMOLOGAR') {
    return {
        codigo,
        mostrar: true,
        habilitar: true,
        tituloModal: codigo === 'HOMOLOGAR' ? 'Homologar cadastro' : 'Aceitar cadastro',
        textoModal: codigo === 'HOMOLOGAR' ? 'Homologar cadastro' : 'Aceitar cadastro',
        rotuloBotao: codigo === 'HOMOLOGAR' ? 'Homologar' : 'Aceitar',
        rotuloConfirmacao: codigo === 'HOMOLOGAR' ? 'Homologar' : 'Aceitar',
        mensagemSucesso: codigo === 'HOMOLOGAR' ? 'Homologado' : 'Aceito',
        redirecionarParaPainel: codigo === 'ACEITAR',
    };
}

type FluxoSubprocessoMock = {
    aceitarCadastro: ReturnType<typeof vi.fn>;
    devolverCadastro: ReturnType<typeof vi.fn>;
    homologarCadastro: ReturnType<typeof vi.fn>;
    homologarRevisaoCadastro: ReturnType<typeof vi.fn>;
    aceitarRevisaoCadastro: ReturnType<typeof vi.fn>;
    devolverRevisaoCadastro: ReturnType<typeof vi.fn>;
};

type SubprocessoStoreMock = {
    subprocessoDetalhe: SubprocessoDetalhe | null;
    buscarSubprocessoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarContextoEdicao: ReturnType<typeof vi.fn>;
    buscarSubprocessoDetalhe: ReturnType<typeof vi.fn>;
    atualizarStatusLocal: ReturnType<typeof vi.fn>;
    lastError: NormalizedError | null;
    clearError: ReturnType<typeof vi.fn>;
};

function criarContextoEdicao(tipoProcesso: TipoProcesso = TipoProcesso.MAPEAMENTO): ContextoEdicaoSubprocesso {
    const unidade: Unidade = {codigo: 1, sigla: "TESTE", nome: "Teste", filhas: [], usuarioCodigo: 0, responsavel: null};
    const mapa: MapaCompleto = {codigo: 100, subprocessoCodigo: 123, observacoes: "", competencias: [], situacao: "CRIADO"};
    const subprocesso: Subprocesso = {
        codigo: 123,
        unidade,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        dataLimite: "2025-01-01T00:00:00",
        dataFimEtapa1: "",
        dataLimiteEtapa2: "",
        atividades: [],
        codUnidade: unidade.codigo,
    };
    return {
        subprocesso,
        detalhes: {
            codigo: 123,
            unidade,
            titular: null,
            responsavel: null,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            localizacaoAtual: "TESTE",
            processoDescricao: "Processo",
            dataCriacaoProcesso: "2024-01-01T00:00:00",
            ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
            tipoProcesso,
            prazoEtapaAtual: "2025-01-01T00:00:00",
            isEmAndamento: true,
            etapaAtual: 1,
            movimentacoes: [],
            elementosProcesso: [],
            permissoes: {
                podeEditarCadastro: false,
                podeDisponibilizarCadastro: false,
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
                podeVisualizarImpacto: false,
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
            }
        },
        mapa,
        atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: []}],
        unidade
    };
}

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: vi.fn()}));
vi.mock("@/composables/useFluxoSubprocesso", () => ({useFluxoSubprocesso: vi.fn()}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {
        props: ['disabled'],
        template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
    },
    BCard: {template: '<div><slot /></div>'},
    BCardBody: {template: '<div><slot /></div>'},
    BCardTitle: {template: '<div><slot /></div>'},
    BFormGroup: {template: '<div><label><slot name="label" /></label><slot /></div>'},
    BFormTextarea: {
        name: 'BFormTextarea',
        props: ['modelValue'],
        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
    },
    BFormInvalidFeedback: {template: '<div><slot /></div>'},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div></div>', props: ['mostrar']},
    ModalConfirmacao: {
        name: 'ModalConfirmacao',
        template: '<div v-if="modelValue"> <slot /> <button :data-testid="$attrs[\'test-codigo-confirmar\']" @click="$emit(\'confirmar\')">Confirmar</button> </div>',
        props: ['modelValue']
    },
};

describe("CadastroVisualizacaoView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(useSubprocessosModule.useSubprocessos).mockReturnValue({
            subprocessoDetalhe: null,
            buscarSubprocessoPorProcessoEUnidade: vi.fn().mockResolvedValue(123),
            buscarContextoEdicao: vi.fn().mockResolvedValue(criarContextoEdicao()),
            buscarSubprocessoDetalhe: vi.fn(),
            atualizarStatusLocal: vi.fn(),
            lastError: null,
            clearError: vi.fn(),
        } as unknown as ReturnType<typeof useSubprocessosModule.useSubprocessos>);
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            aceitarCadastro: vi.fn().mockResolvedValue(true),
            devolverCadastro: vi.fn().mockResolvedValue(true),
            homologarCadastro: vi.fn().mockResolvedValue(true),
            homologarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            aceitarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            devolverRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as unknown as ReturnType<typeof useFluxoSubprocessoModule.useFluxoSubprocesso>);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as never);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(criarContextoEdicao());
    });

    function createWrapper(accessOverrides = {}, tipoProcesso: TipoProcesso = TipoProcesso.MAPEAMENTO) {
        const subprocessosStore = useSubprocessosModule.useSubprocessos() as unknown as SubprocessoStoreMock;
        subprocessosStore.subprocessoDetalhe = {
            ...criarContextoEdicao(tipoProcesso).detalhes,
            tipoProcesso,
        };

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeDevolverCadastro: ref(true),
            podeVisualizarImpacto: ref(true),
            habilitarDevolverCadastro: ref(true),
            acaoPrincipalCadastro: ref(criarAcaoPrincipalCadastro()),
            ...accessOverrides
        } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

        return mount(CadastroVisualizacaoView, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: true,
                    initialState: {}
                })],
                stubs
            },
            props: {
                codProcesso: "1",
                sigla: "TESTE"
            }
        });
    }

    it("cobre fluxos de análise e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        const vm = wrapper.vm as unknown as CadastroVisualizacaoVm;

        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
        await wrapper.find('[data-testid="btn-vis-atividades-historico"]').trigger("click");
        await flushPromises();
        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);

        await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
        vm.observacaoDevolucao = "Obs devolução";
        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.devolverCadastro).toHaveBeenCalled();

        vm.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('ACEITAR');
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.aceitarCadastro).toHaveBeenCalled();

        vm.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('HOMOLOGAR');
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.homologarCadastro).toHaveBeenCalled();

        const wrapperRevisao = createWrapper({}, TipoProcesso.REVISAO);
        await flushPromises();

        const vmRevisao = wrapperRevisao.vm as unknown as CadastroVisualizacaoVm;
        vmRevisao.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('HOMOLOGAR');
        await vmRevisao.confirmarValidacao();
        expect(fluxoSubprocesso.homologarRevisaoCadastro).toHaveBeenCalled();

        // Aceitar Revisão
        vmRevisao.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('ACEITAR');
        await vmRevisao.confirmarValidacao();
        expect(fluxoSubprocesso.aceitarRevisaoCadastro).toHaveBeenCalled();

        vmRevisao.observacaoDevolucao = "Rev";
        await vmRevisao.confirmarDevolucao();
        expect(fluxoSubprocesso.devolverRevisaoCadastro).toHaveBeenCalled();

        const mapsStore = vm.mapasStore;
        mapsStore.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        await vm.abrirModalImpacto();
        expect(mapsStore.buscarImpactoMapa).toHaveBeenCalledWith(123);
    });

    it("mantem orçamento enxuto de chamadas no carregamento inicial quando o processo já traz o subprocesso", async () => {
        createWrapper();
        await flushPromises();

        expect(contarChamadas(
            vi.mocked(useSubprocessosModule.useSubprocessos)().buscarContextoEdicao as unknown as {mock?: {calls: unknown[][]}},
            vi.mocked(useSubprocessosModule.useSubprocessos)().buscarSubprocessoPorProcessoEUnidade as unknown as {mock?: {calls: unknown[][]}},
        )).toBe(2);
    });

    it("cobre ramos condicionais adicionais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroVisualizacaoVm;

        // Testar computed nomeUnidade
        vm.unidade = { sigla: "TESTE", nome: "Unidade de Teste" };
        expect(vm.nomeUnidade).toBe("Unidade de Teste");
        vm.unidade = { sigla: "TESTE", nome: null };
        expect(vm.nomeUnidade).toBe("");

        // Testar computed estadoObservacaoDevolucao
        vm.validacaoDevolucaoSubmetida = true;
        vm.observacaoDevolucao = "   ";
        expect(vm.estadoObservacaoDevolucao).toBe(false);
        vm.observacaoDevolucao = "texto";
        expect(vm.estadoObservacaoDevolucao).toBeNull();
        
        // Testar early returns
        vm.mostrarModalImpacto = true;
        vm.fecharModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(false);

        vm.mostrarModalHistoricoAnalise = true;
        vm.fecharModalHistoricoAnalise();
        expect(vm.mostrarModalHistoricoAnalise).toBe(false);

        // Retornos antecipados (early return) nas funcoes assincronas
        Object.defineProperty(vm, 'codSubprocesso', { get: () => null });
        await vm.confirmarValidacao();
        
        vm.validacaoDevolucaoSubmetida = true;
        vm.observacaoDevolucao = "";
        await vm.confirmarDevolucao();
        
        // Renderização do v-for de atividades e modais via v-model não precisam de interações específicas
        // desde que as variáveis sejam preenchidas e lidas pelo wrapper.
        vm.atividades = [
            { codigo: 1, descricao: "A1", conhecimentos: [{ codigo: 1, descricao: "C1" }] }
        ];
        vm.mostrarModalValidar = true;
        vm.mostrarModalDevolver = true;
        await wrapper.vm.$nextTick();
    });

    it("deve lidar com onMounted quando o subprocesso não é encontrado", async () => {
        vi.mocked(useSubprocessosModule.useSubprocessos).mockReturnValue({
            subprocessoDetalhe: null,
            buscarSubprocessoPorProcessoEUnidade: vi.fn().mockResolvedValue(null),
            buscarContextoEdicao: vi.fn(),
            buscarSubprocessoDetalhe: vi.fn(),
            atualizarStatusLocal: vi.fn(),
            lastError: null,
            clearError: vi.fn(),
        } as unknown as ReturnType<typeof useSubprocessosModule.useSubprocessos>);

        createWrapper();
        
        await flushPromises();
        const store = useSubprocessosModule.useSubprocessos() as unknown as SubprocessoStoreMock;
        expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TESTE");
        expect(store.buscarContextoEdicao).not.toHaveBeenCalled();
    });

    it("deve tratar falhas de sucesso nas validações e não fechar modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroVisualizacaoVm;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;

        // Falha no aceite
        fluxoSubprocesso.aceitarCadastro.mockResolvedValue(false);
        vm.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('ACEITAR');
        vm.mostrarModalValidar = true;
        vm.observacaoValidacao = "Teste falha";
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true); // Permanece aberto

        // Falha na homologação
        fluxoSubprocesso.homologarCadastro.mockResolvedValue(false);
        vm.acaoPrincipalCadastro = criarAcaoPrincipalCadastro('HOMOLOGAR');
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true);

        // Falha na devolução
        fluxoSubprocesso.devolverCadastro.mockResolvedValue(false);
        vm.mostrarModalDevolver = true;
        vm.observacaoDevolucao = "Obs";
        await vm.confirmarDevolucao();
        expect(vm.mostrarModalDevolver).toBe(true);
        
        // Fechamento manual dos modais
        vm.fecharModalValidar();
        expect(vm.mostrarModalValidar).toBe(false);
        expect(vm.observacaoValidacao).toBe("");

        vm.fecharModalDevolver();
        expect(vm.mostrarModalDevolver).toBe(false);
        expect(vm.observacaoDevolucao).toBe("");
    });

    it("deve manter devolucao e homologacao visiveis, porem desabilitadas, fora da localizacao permitida", async () => {
        const wrapper = createWrapper({
            podeDevolverCadastro: ref(true),
            habilitarDevolverCadastro: ref(false),
            acaoPrincipalCadastro: ref({...criarAcaoPrincipalCadastro('HOMOLOGAR'), habilitar: false}),
        });
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-acao-devolver"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('[data-testid="btn-acao-analisar-principal"]').attributes('disabled')).toBeDefined();
    });

    it("deve gerenciar atualizações de estado do modal de confirmação e campos de observação", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroVisualizacaoVm;

        // v-model cover (92, 116)
        const modals = wrapper.findAllComponents({name: 'ModalConfirmacao'});
        for (const modal of modals) {
            await modal.vm.$emit('update:modelValue', true);
        }
        expect(vm.mostrarModalValidar).toBe(true);
        expect(vm.mostrarModalDevolver).toBe(true);

        // Textarea v-model (107, 135)
        const textareas = wrapper.findAllComponents({name: 'BFormTextarea'});
        if (textareas.length > 0) {
            await textareas[0].vm.$emit('update:modelValue', 'Obs val');
            expect(vm.observacaoValidacao).toBe('Obs val');
        }
        if (textareas.length > 1) {
            await textareas[1].vm.$emit('update:modelValue', 'Obs dev');
            expect(vm.observacaoDevolucao).toBe('Obs dev');
        }
        
        // Ensure feedback is covered (140-141)
        vm.validacaoDevolucaoSubmetida = true;
        vm.observacaoDevolucao = "";
        await vm.$nextTick();
    });
});

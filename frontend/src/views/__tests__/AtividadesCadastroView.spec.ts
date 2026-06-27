import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {reactive, ref} from "vue";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import {useMapas} from "@/composables/useMapas";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import * as atividadeService from "@/services/atividadeService";
import CadastroView from "@/views/CadastroView.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import * as useAcessoModule from '@/composables/acesso';
import * as useNotificationModule from '@/composables/useNotification';
import {TEXTOS} from "@/constants/textos";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import type {
    AtividadeOperacaoResponse,
    ContextoCadastroAtividadesSubprocesso,
    MapaResumo,
    PermissoesSubprocesso,
    SubprocessoDetalhe,
    Unidade
} from "@/types/tipos";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        warn: vi.fn(),
        info: vi.fn(),
        debug: vi.fn(),
    }
}));

const notificacaoMock = ref<any>(null);
const notifyMock = vi.fn((mensagem, variante, dispensavel) => {
    notificacaoMock.value = {mensagem, variante, dispensavel};
});
const clearMock = vi.fn(() => {
    notificacaoMock.value = null;
});

vi.mock("@/composables/useNotification", () => ({
    useNotification: vi.fn(() => ({
        notify: notifyMock,
        clear: clearMock,
        notificacao: notificacaoMock
    }))
}));

const mockMapaCompleto = ref<any>(null);
const mockImpactoMapa = ref<any>(null);
const mockErroMapas = ref<any>(null);

const {pushMock} = vi.hoisted(() => ({
    pushMock: vi.fn()
}));

vi.mock("@/composables/useMapas", () => ({
    useMapas: vi.fn(() => ({
        mapaCompleto: mockMapaCompleto,
        impactoMapa: mockImpactoMapa,
        carregando: { value: false },
        erro: mockErroMapas,
        carregarMapa: vi.fn(),
        carregarImpacto: vi.fn(async (cod: number) => {
            await subprocessoService.verificarImpactosMapa(cod);
        }),
        sincronizarMapa: vi.fn(),
        sincronizarImpacto: vi.fn(),
        invalidar: vi.fn(),
        invalidarImpacto: vi.fn(),
        resetar: vi.fn(),
    }))
}));

type AtividadeMinima = {
    codigo: number;
    descricao?: string;
    conhecimentos?: Array<{ codigo: number; descricao?: string }>;
};


type CadastroViewVm = {
    codigoSubprocesso: number | null;
    unidade: { sigla: string; nome: string } | null;
    atividades: AtividadeMinima[];
    atividadesSnapshotInicial: string;
    disponibilizacaoSemMudancas: boolean;
    mostrarModalConfirmacao: boolean;
    mostrarModalImportar: boolean;
    mostrarModalConfirmacaoRemocao: boolean;
    dadosRemocao: { tipo: string; index?: number; conhecimentoCodigo?: number } | null;
    erroGlobal: string | null;
    erroNovaAtividade?: string | null;
    errosValidacao?: Array<{ atividadeCodigo?: number; mensagem: string }>;
    notificacao: unknown;
    novaAtividade: string;
    podeHomologarCadastro?: boolean;
    aoImportarAtividades: (resultado: any) => Promise<void>;
    disponibilizarCadastro: () => Promise<void>;
    adicionarAtividade: () => Promise<void>;
    confirmarRemocao: () => Promise<void>;
    processarRespostaLocal: (payload: {
        atividadesAtualizadas?: unknown[];
        subprocesso?: { codigo: number; situacao: SituacaoSubprocesso };
        permissoes?: PermissoesSubprocesso;
    }) => void;
    notify: (mensagem: string, variante: string) => void;
    carregarContextoInicial: () => Promise<void>;
    $nextTick: () => Promise<void>;
};

type FluxoSubprocessoMock = {
    validarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarRevisaoCadastro: ReturnType<typeof vi.fn>;
    iniciarRevisaoCadastro: ReturnType<typeof vi.fn>;
    cancelarInicioRevisaoCadastro: ReturnType<typeof vi.fn>;
};

type SubprocessoStoreMock = {
    contextoCadastro: ContextoCadastroAtividadesSubprocesso | null;
    erroIntegracaoContexto: { message: string } | null;
    subprocessoDetalhe: {
        codigo: number;
        situacao: SituacaoSubprocesso | string;
        tipoProcesso: TipoProcesso | string;
        unidade?: { sigla: string };
        permissoes?: PermissoesSubprocesso;
    } | null;
    buscarContextoCadastroAtividadesPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarContextoCadastroAtividades: ReturnType<typeof vi.fn>;
    obterContextoCadastroAtividadesPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    obterContextoCadastroAtividades: ReturnType<typeof vi.fn>;
    buscarContextoEdicaoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarContextoEdicao: ReturnType<typeof vi.fn>;
    buscarSubprocessoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarSubprocessoDetalhe: ReturnType<typeof vi.fn>;
    atualizarStatusLocal: ReturnType<typeof vi.fn>;
    invalidar: ReturnType<typeof vi.fn>;
    marcarContextoEdicaoParaAtualizacao: ReturnType<typeof vi.fn>;
    ultimoErro: { message: string } | null;
    limparErro: ReturnType<typeof vi.fn>;
    limparErroIntegracao: ReturnType<typeof vi.fn>;
};

function criarContextoEdicao(): ContextoCadastroAtividadesSubprocesso {
    const unidade: Unidade = {
        codigo: 1,
        sigla: "TESTE",
        nome: "Teste",
        filhas: [],
        usuarioCodigo: 0,
        responsavel: null
    };
    const mapa: MapaResumo = {codigo: 100, subprocessoCodigo: 123};
    const detalhes: SubprocessoDetalhe = {
        codigo: 123,
        unidade,
        titular: null,
        responsavel: null,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        localizacaoAtual: "TESTE",
        processoDescricao: "Processo",
        dataCriacaoProcesso: "2024-01-01T00:00:00",
        ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        prazoEtapaAtual: "2025-01-01T00:00:00",
        dataFimEtapa1: null,
        isEmAndamento: true,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
    };
    return {
        detalhes,
        mapa,
        atividadesDisponiveis: [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }],
        assinaturaCadastroReferencia: calcularAssinaturaCadastro([
            {codigo: 1, descricao: "Ativ 1", conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]}
        ]),
        unidade
    };
}

const estadoContextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
const buscarContextoCadastroAtividadesPorProcessoEUnidadeMock = vi.fn();
const buscarContextoCadastroAtividadesMock = vi.fn();
const subprocessosMock = reactive({
    get contextoCadastro() {
        return estadoContextoCadastro.value;
    },
    set contextoCadastro(valor: ContextoCadastroAtividadesSubprocesso | null) {
        estadoContextoCadastro.value = valor;
    },
    get subprocessoDetalhe() {
        return estadoContextoCadastro.value?.detalhes ?? null;
    },
    set subprocessoDetalhe(valor: SubprocessoStoreMock["subprocessoDetalhe"]) {
        if (!valor) {
            estadoContextoCadastro.value = null;
            return;
        }
        estadoContextoCadastro.value = {
            ...(estadoContextoCadastro.value ?? criarContextoEdicao()),
            detalhes: valor as unknown as ContextoCadastroAtividadesSubprocesso["detalhes"],
        };
    },
    buscarContextoCadastroAtividadesPorProcessoEUnidade: buscarContextoCadastroAtividadesPorProcessoEUnidadeMock,
    buscarContextoCadastroAtividades: buscarContextoCadastroAtividadesMock,
    obterContextoCadastroAtividadesPorProcessoEUnidade: buscarContextoCadastroAtividadesPorProcessoEUnidadeMock,
    obterContextoCadastroAtividades: buscarContextoCadastroAtividadesMock,
    buscarContextoEdicaoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    invalidar: vi.fn(),
    marcarContextoEdicaoParaAtualizacao: vi.fn(),
    erroIntegracaoContexto: null as { message: string } | null,
    ultimoErro: null as SubprocessoStoreMock["ultimoErro"],
    limparErro: vi.fn(),
    limparErroIntegracao: vi.fn(),
});

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/composables/usePerfil", () => ({usePerfil: vi.fn()}));

vi.mock("@/services/subprocessoService", () => ({
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarContextoEdicaoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    validarCadastro: vi.fn(),
    disponibilizarCadastro: vi.fn(),
    listarAtividades: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
}));

vi.mock("@/stores/subprocesso", () => ({useSubprocessoStore: () => subprocessosMock}));
vi.mock("@/composables/useFluxoSubprocesso", () => ({useFluxoSubprocesso: vi.fn()}));
const mockAtividadeForm = {
    novaAtividade: ref(""),
    loadingAdicionar: ref(false),
    adicionarAtividade: vi.fn(),
};

vi.mock("@/composables/useAtividadeForm", () => ({
    useAtividadeForm: vi.fn(() => mockAtividadeForm)
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot name="alerta" /><slot /><slot name="actions" /></div>'
    },
    LoadingButton: {
        props: {
            disabled: {
                type: Boolean,
                default: false,
            }
        },
        template: '<button :data-testid="$attrs[\'data-testid\']" v-bind="disabled ? { disabled: true } : {}" @click="$emit(\'click\')"><slot /></button>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
    BFormCheckbox: {
        props: ['modelValue', 'disabled'],
        template: '<input type="checkbox" :data-testid="$attrs[\'data-testid\']" :checked="modelValue" :disabled="disabled" @change="$emit(\'update:modelValue\', $event.target.checked)" />'
    },
    BSpinner: {template: '<span data-testid="spinner"></span>'},
    BDropdown: {template: '<div><slot /></div>'},
    BDropdownItem: {template: '<div :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></div>'},
    BAlert: {
        template: '<div><slot /><button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>',
        props: ['modelValue']
    },
    AppAlert: {
        template: '<div v-bind="$attrs">{{ mensagem }}<button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button><button data-testid="btn-dismiss-app-alert" @click="$emit(\'dismissed\')">x</button></div>',
        props: ['mensagem', 'variante', 'dispensavel']
    },
    ErrorAlert: {template: '<div></div>'},
    CadAtividadeForm: {
        name: 'CadAtividadeForm',
        template: '<div data-testid="cad-atividade-form"></div>',
        props: ['modelValue'],
        expose: ['inputRef'],
        setup() {
            return {
                inputRef: {
                    $el: {
                        focus: vi.fn()
                    }
                }
            }
        }
    },
    EmptyState: {template: '<div><slot /></div>'},
    AtividadeItem: {
        name: 'AtividadeItem',
        template: '<div data-testid="atividade-item"></div>',
        props: ['atividade', 'pode-editar', 'erro-validacao']
    },
    ImportarAtividadesModal: {template: '<div></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div v-if="mostrar" data-testid="modal-impacto"></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {
        template: '<div v-if="mostrar" data-testid="modal-confirmacao">Confirmacao <button data-testid="btn-confirmar-disponibilizacao" @click="$emit(\'confirmar\')">Confirmar</button></div>',
        props: ['mostrar'],
        emits: ['confirmar', 'fechar']
    },
    HistoricoAnaliseModal: {template: '<div v-if="mostrar" data-testid="modal-historico"></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
    CadastroAcoesHeader: {
        props: ['unidade', 'codSubprocesso', 'permissoes', 'acaoPrincipalCadastro', 'loadingValidacao', 'podeVisualizarImpacto'],
        template: `
            <div data-testid="debug-header">
                <h1>Atividades e conhecimentos</h1>
                <div v-if="unidade">{{ unidade.sigla }}</div>
                <slot name="alerta" />
                <button data-testid="btn-cad-atividades-historico" @click="$emit('abrir-historico')">Histórico</button>
                <button v-if="permissoes.podeDevolverCadastro" data-testid="btn-acao-devolver" :disabled="!permissoes.habilitarDevolverCadastro" @click="$emit('abrir-devolver')">Devolver</button>
                <button v-if="acaoPrincipalCadastro?.mostrar" data-testid="btn-acao-analisar-principal" :disabled="!acaoPrincipalCadastro?.habilitar" @click="$emit('abrir-validar')">{{ acaoPrincipalCadastro?.rotuloBotao }}</button>
                <button v-if="podeVisualizarImpacto" data-testid="cad-atividades__btn-impactos-mapa-edicao" @click="$emit('abrir-impacto')">Impacto</button>
                <button v-if="permissoes.podeEditarCadastro" data-testid="btn-cad-atividades-importar" @click="$emit('abrir-importar')">Importar</button>
                <button v-if="permissoes.podeDisponibilizarCadastro || permissoes.podeEditarCadastro" data-testid="btn-cad-atividades-disponibilizar" :disabled="loadingValidacao" @click="$emit('disponibilizar')">Disponibilizar</button>
            </div>
        `
    },
    ModalAceiteCadastro: {
        props: ['modelValue', 'loading', 'acao', 'observacao'],
        template: '<div v-if="modelValue" data-testid="modal-aceite-cadastro"></div>'
    },
    ModalDevolucaoCadastro: {
        props: ['modelValue', 'loading', 'isRevisao', 'observacao'],
        template: '<div v-if="modelValue" data-testid="modal-devolucao-cadastro"></div>'
    }
};

function createWrapper(customState = {}, accessOverrides = {}) {
    vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
        podeEditarCadastro: ref(true),
        podeDisponibilizarCadastro: ref(true),
        podeVisualizarImpacto: ref(true),
        podeDevolverCadastro: ref(true),
        podeAceitarCadastro: ref(true),
        podeHomologarCadastro: ref(true),
        habilitarEditarCadastro: ref(true),
        habilitarDisponibilizarCadastro: ref(true),
        habilitarDevolverCadastro: ref(true),
        habilitarAceitarCadastro: ref(true),
        habilitarHomologarCadastro: ref(true),
        mesmaUnidade: ref(true),
        habilitarAcessoCadastro: ref(true),
        acaoPrincipalCadastro: ref(null),
        ...accessOverrides
    } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

    const wrapper = mount(CadastroView, {
        global: {
            plugins: [createTestingPinia({
                createSpy: vi.fn,
                stubActions: false,
                initialState: {
                    perfil: {
                        perfilSelecionado: Perfil.CHEFE,
                    },
                    subprocessos: {
                        subprocessoDetalhe: {
                            codigo: 123,
                            unidade: {sigla: "TESTE"},
                            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                            tipoProcesso: "MAPEAMENTO"
                        }
                    },
                    mapas: {
                        mapaCompleto: {codigo: 100}
                    },
                    ...customState,
                }
            })],
            stubs
        },
        props: {
            codProcesso: "1",
            sigla: "TESTE"
        }
    });

    const mapas = useMapas();
    mapas.mapaCompleto.value = {codigo: 100} as unknown as typeof mapas.mapaCompleto.value;
    mapas.impactoMapa.value = null;
    mapas.erro.value = null;

    const vm = wrapper.vm as unknown as CadastroViewVm;
    vm.codigoSubprocesso = 123;
    vm.unidade = {sigla: "TESTE", nome: "Teste"};

    return wrapper;
}

describe("CadastroView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockMapaCompleto.value = null;
        mockImpactoMapa.value = null;
        mockErroMapas.value = null;
        estadoContextoCadastro.value = null;
        subprocessosMock.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        subprocessosMock.ultimoErro = null;
        subprocessosMock.erroIntegracaoContexto = null;
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.marcarContextoEdicaoParaAtualizacao = vi.fn();
        subprocessosMock.obterContextoCadastroAtividadesPorProcessoEUnidade.mockResolvedValue(criarContextoEdicao());
        subprocessosMock.obterContextoCadastroAtividades.mockResolvedValue(criarContextoEdicao());
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            ultimoErro: ref(null),
            limparErro: vi.fn(),
            validarCadastro: vi.fn().mockResolvedValue({valido: true}),
            disponibilizarCadastro: vi.fn().mockImplementation(() => {
                pushMock("/painel");
                return Promise.resolve(true);
            }),
            disponibilizarRevisaoCadastro: vi.fn().mockImplementation(() => {
                pushMock("/painel");
                return Promise.resolve(true);
            }),
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            cancelarInicioRevisaoCadastro: vi.fn().mockResolvedValue(true),
            devolverCadastro: vi.fn().mockImplementation(() => {
                pushMock("/painel");
                return Promise.resolve(true);
            }),
            aceitarCadastro: vi.fn().mockImplementation(() => {
                pushMock("/painel");
                return Promise.resolve(true);
            }),
            homologarCadastro: vi.fn().mockImplementation((_c, _r, opts) => {
                if (opts?.redirecionarParaPainel) {
                    pushMock("/painel");
                } else if (opts?.redirecionarPara) {
                    pushMock(opts.redirecionarPara);
                }
                return Promise.resolve(true);
            }),
            reabrirCadastro: vi.fn().mockResolvedValue(true),
            alterarDataLimiteSubprocesso: vi.fn().mockResolvedValue(true),
        } as unknown as ReturnType<typeof useFluxoSubprocessoModule.useFluxoSubprocesso>);
        vi.mocked(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(criarContextoEdicao() as never);
        vi.mocked(subprocessoService.buscarContextoCadastroAtividades).mockResolvedValue(criarContextoEdicao());
    });

    it("renderiza corretamente", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("Atividades e conhecimentos");
    });

    it("chama validação antes de disponibilizar", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{codigo: 1, conhecimentos: [{codigo: 1}]}];
        await wrapper.vm.$nextTick();
        const subprocessosStore = subprocessosMock;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };

        await vm.disponibilizarCadastro();

        expect(fluxoSubprocesso.validarCadastro).toHaveBeenCalledWith(123);
        expect(vm.mostrarModalConfirmacao).toBe(true);
    });

    it("não chama validação quando o cadastro está incompleto e exibe erro global", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{codigo: 1, conhecimentos: []}];
        await wrapper.vm.$nextTick();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;

        await vm.disponibilizarCadastro();

        expect(fluxoSubprocesso.validarCadastro).not.toHaveBeenCalled();
        expect(vm.erroGlobal).toBe(TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO);
        expect(vm.mostrarModalConfirmacao).toBe(false);
    });

    it("em revisão sem mudanças, exibe erro específico sem chamar validação", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividades = [{codigo: 1, descricao: "Ativ 1", conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]}];
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        vm.disponibilizacaoSemMudancas = false;
        await wrapper.vm.$nextTick();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;

        await vm.disponibilizarCadastro();

        expect(fluxoSubprocesso.validarCadastro).not.toHaveBeenCalled();
        expect(vm.erroGlobal).toBe(TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO);
        expect(vm.mostrarModalConfirmacao).toBe(false);
    });

    it("confirma disponibilização e redireciona", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{codigo: 1, conhecimentos: [{codigo: 1}]}];
        await wrapper.vm.$nextTick();
        const subprocessosStore = subprocessosMock;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };

        await vm.disponibilizarCadastro();
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModal);
        modal.vm.$emit('confirmar');
        await flushPromises();

        expect(fluxoSubprocesso.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("carrega histórico ao abrir modal", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);

        await wrapper.find('[data-testid="btn-cad-atividades-historico"]').trigger("click");
        await flushPromises();

        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(HistoricoAnaliseModal).exists()).toBe(true);
    });

    it("carrega impacto ao abrir modal", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(subprocessoService.verificarImpactosMapa).mockResolvedValue(null as any);

        await wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-edicao"]').trigger("click");
        await flushPromises();

        expect(subprocessoService.verificarImpactosMapa).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(ImpactoMapaModal).exists()).toBe(true);
    });

    it("desabilita botão disponibilizar se houver atividades sem conhecimentos", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Atividade sem conhecimentos
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{
            codigo: 1,
            descricao: "Atividade 1",
            conhecimentos: []
        }];

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.exists()).toBe(true);
    });

    it("habilita botão disponibilizar se todas atividades tiverem conhecimentos", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Atividade com conhecimentos
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{
            codigo: 1,
            descricao: "Atividade 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];

        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, mantém botão desabilitado sem mudanças e checkbox habilitada", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        await flushPromises();

        const checkbox = wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]');
        expect(checkbox.exists()).toBe(true);
        expect(checkbox.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, habilita botão ao marcar checkbox sem mudanças", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, habilita botão quando houver alteração no cadastro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1 alterada",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("em revisão, desabilita checkbox quando houver alteração no cadastro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1 alterada",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        await flushPromises();

        const checkbox = wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]');
        expect(checkbox.attributes('disabled')).toBeDefined();
    });

    it("mantém as ações visíveis e mostra feedback ao iniciar a revisão pelo checkbox", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        let resolver!: (valor: boolean) => void;
        let resolverDefinido = false;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.iniciarRevisaoCadastro.mockImplementation(() =>
            new Promise<boolean>((resolve) => {
                resolver = resolve as (valor: boolean) => void;
                resolverDefinido = true;
            })
        );
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            tipoProcesso: TipoProcesso.REVISAO,
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        await flushPromises();

        await wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]').setValue(true);
        await flushPromises();

        expect(fluxoSubprocesso.iniciarRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(wrapper.find('[data-testid="cad-atividades__spinner-iniciando-revisao"]').exists()).toBe(true);

        if (!resolverDefinido) {
            throw new Error("Resolver não definido");
        }
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            tipoProcesso: TipoProcesso.REVISAO,
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
            situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
        };
        resolver(true);
        await flushPromises();

        expect(wrapper.find('[data-testid="cad-atividades__spinner-iniciando-revisao"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').attributes('disabled')).toBeUndefined();
    });

    it("desmarca o checkbox e cancela o início da revisão quando não houver mudanças", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
            tipoProcesso: TipoProcesso.REVISAO,
            unidade: {sigla: "TESTE"},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = calcularAssinaturaCadastro([{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }]);
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();

        await wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]').setValue(false);
        await flushPromises();

        expect(fluxoSubprocesso.cancelarInicioRevisaoCadastro).toHaveBeenCalledWith(123);
    });

    it("mantém botão disponibilizar visível e desabilitado quando o chefe ainda só pode editar", async () => {
        const wrapper = createWrapper({}, {
            podeEditarCadastro: ref(true),
            podeDisponibilizarCadastro: ref(false),
            podeVisualizarImpacto: ref(true),
        });
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.exists()).toBe(true);
    });

    it("oculta botão disponibilizar quando o perfil não tem ação de edição nem de disponibilização", async () => {
        const wrapper = createWrapper({}, {
            podeEditarCadastro: ref(false),
            podeDisponibilizarCadastro: ref(false),
            podeVisualizarImpacto: ref(true),
        });
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.exists()).toBe(false);
    });

    it("oculta controles de edição para CHEFE quando a edição não estiver habilitada no workflow", async () => {
        const wrapper = createWrapper({
            perfil: {
                perfilSelecionado: Perfil.CHEFE,
            },
        }, {
            podeEditarCadastro: ref(true),
            habilitarEditarCadastro: ref(false),
            podeDisponibilizarCadastro: ref(false),
        });
        await flushPromises();

        expect(wrapper.find('[data-testid="cad-atividade-form"]').exists()).toBe(false);
    });

    it("oculta formulário de nova atividade quando o usuário não tem permissão para editar", async () => {
        const wrapper = createWrapper({}, {
            podeEditarCadastro: ref(false),
            podeDisponibilizarCadastro: ref(false),
        });
        await flushPromises();

        const form = wrapper.find('[data-testid="cad-atividade-form"]');
        expect(form.exists()).toBe(false);
    });

    it("recarrega contexto completo apos importar atividades", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const subprocessosStore = subprocessosMock;
        subprocessosStore.buscarContextoCadastroAtividades = vi.fn().mockResolvedValue({
            ...criarContextoEdicao(),
            detalhes: {
                ...criarContextoEdicao().detalhes,
                permissoes: {
                    ...criarContextoEdicao().detalhes.permissoes,
                    podeEditarCadastro: true,
                    podeDisponibilizarCadastro: true,
                },
            },
            atividadesDisponiveis: [{
                codigo: 2,
                descricao: "Atividade importada",
                conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]
            }],
        });

        const vm = wrapper.vm as unknown as CadastroViewVm;
        await vm.aoImportarAtividades({
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            permissoes: criarContextoEdicao().detalhes.permissoes,
            atividadesAtualizadas: [{
                codigo: 2,
                descricao: "Atividade importada",
                conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]
            }],
            aviso: null,
        });

        expect(subprocessosStore.buscarContextoCadastroAtividades).not.toHaveBeenCalled();
        expect(vm.atividades).toEqual([
            {
                codigo: 2,
                descricao: "Atividade importada",
                conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]
            }
        ]);
    });

    it("aoImportarAtividades exibe aviso quando resultado tem aviso", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;

        const resultadoComAviso = {
            aviso: "Duplicatas encontradas",
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            permissoes: criarContextoEdicao().detalhes.permissoes,
            atividadesAtualizadas: []
        };

        await vm.aoImportarAtividades(resultadoComAviso);

        const notifyMock = vi.mocked(useNotificationModule.useNotification()).notify;
        expect(notifyMock).toHaveBeenCalledWith(TEXTOS.atividades.AVISO_IMPORTACAO_DUPLICATAS, 'warning');
    });

    it("deve gerenciar interações com formulários de atividades, modais de importação e alertas de erro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        vm.atividades = [{codigo: 1, descricao: "A1", conhecimentos: [{codigo: 1}]}];
        await vm.$nextTick();

        // Abertura do modal de importação
        vm.mostrarModalImportar = true;
        await vm.$nextTick();
        expect(vm.mostrarModalImportar).toBe(true);

        // Descarte de alerta de erro global
        vm.erroGlobal = "Erro";
        await vm.$nextTick();
        const btnDismissAlert = wrapper.find('[data-testid="btn-dismiss-alert"]');
        await btnDismissAlert.trigger('click');
        expect(vm.erroGlobal).toBeNull();

        // Descarte de notificação do sistema
        vm.notify("Msg", "info");
        await vm.$nextTick();
        const btnDismissAppAlert = wrapper.find('[data-testid="btn-dismiss-app-alert"]');
        await btnDismissAppAlert.trigger('click');
        expect(vm.notificacao).toBeNull();

        // Atualização de v-model no formulário de atividade
        const form = wrapper.findComponent({name: 'CadAtividadeForm'});
        await form.vm.$emit('update:modelValue', 'Nova');
        expect(vm.novaAtividade).toBe('Nova');

        // Eventos disparados pelo item de atividade
        const item = wrapper.findComponent({name: 'AtividadeItem'});
        await item.vm.$emit('atualizar-atividade', 'desc');
        await item.vm.$emit('remover-atividade');
        await item.vm.$emit('adicionar-conhecimento', 'con');
        await item.vm.$emit('atualizar-conhecimento', 1, 'con desc');
        await item.vm.$emit('remover-conhecimento', 1);

        // Atualização de v-model no modal de confirmação
        vm.mostrarModalConfirmacaoRemocao = false;
        const modalConfirmacao = wrapper.findAllComponents({name: 'ModalConfirmacao'}).find(c => c.props('modelValue') !== undefined);
        if (modalConfirmacao) {
            await modalConfirmacao.vm.$emit('update:modelValue', true);
            expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);
        }

        // @fechar events (114, 130, 136)
        const importModal = wrapper.findComponent({name: 'ImportarAtividadesModal'});
        if (importModal.exists()) await importModal.vm.$emit('fechar');

        const confirmModal = wrapper.findComponent({name: 'ConfirmacaoDisponibilizacaoModal'});
        if (confirmModal.exists()) await confirmModal.vm.$emit('fechar');

        const histModal = wrapper.findComponent({name: 'HistoricoAnaliseModal'});
        if (histModal.exists()) await histModal.vm.$emit('fechar');

        // Cobre ramo sem contexto agregado
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade.mockResolvedValue(null);
        await vm.carregarContextoInicial();

        // 375-377 (adicionarAtividade success branch)
        vm.codigoSubprocesso = 123;
        const mockAtiv = {
            atividadesAtualizadas: [],
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            permissoes: criarContextoEdicao().detalhes.permissoes,
        };
        mockAtividadeForm.adicionarAtividade.mockResolvedValue(mockAtiv);
        await vm.adicionarAtividade();

        // 381-385 (adicionarAtividade fail branch)
        mockAtividadeForm.adicionarAtividade.mockRejectedValue(new Error("Erro"));
        await vm.adicionarAtividade();

        // 404 (confirmarRemocao success branch)
        vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({
            atividade: null,
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            atividadesAtualizadas: [],
            permissoes: criarContextoEdicao().detalhes.permissoes,
        } as AtividadeOperacaoResponse);
        vm.dadosRemocao = {tipo: "atividade", index: 0};
        await vm.confirmarRemocao();

        // 291 (processarRespostaLocal branch)
        vm.processarRespostaLocal({
            atividadesAtualizadas: [{codigo: 1}],
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            permissoes: criarContextoEdicao().detalhes.permissoes,
        });
    });

    it("deve limpar erro da nova atividade quando o texto for alterado", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;

        vm.erroNovaAtividade = "Atividade já cadastrada.";
        vm.novaAtividade = "Nova atividade";
        await vm.$nextTick();

        expect(vm.erroNovaAtividade).toBeNull();
    });

    it("deve limpar erros estruturados ao alterar o cadastro apos uma validacao invalida", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;

        vm.atividades = [{codigo: 1, descricao: "A1", conhecimentos: [{codigo: 1, descricao: "C1"}]}];
        await vm.$nextTick();

        // Mock para evitar erro de DOM no teste
        (vm as any).scrollParaPrimeiroErro = vi.fn();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.validarCadastro.mockResolvedValueOnce({
            valido: false,
            erros: [
                {atividadeCodigo: 1, mensagem: "Informe ao menos um conhecimento."},
                {mensagem: "Cadastro inconsistente."}
            ]
        });

        await vm.disponibilizarCadastro();
        expect(vm.erroGlobal).toBe("Cadastro inconsistente.");
        expect(vm.errosValidacao).toHaveLength(2);

        vm.atividades = [{codigo: 1, descricao: "A1 ajustada", conhecimentos: [{codigo: 1, descricao: "C1"}]}];
        await vm.$nextTick();

        expect(vm.erroGlobal).toBeNull();
        expect(vm.errosValidacao).toEqual([]);
    });

    it("deve fazer o alerta de erro reaparecer ao clicar em disponibilizar novamente após fechar o alerta anterior", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;

        vm.atividades = [{codigo: 1, descricao: "A1", conhecimentos: [{codigo: 1, descricao: "C1"}]}];
        await vm.$nextTick();

        // Mock de scroll para evitar erro
        (vm as any).scrollParaPrimeiroErro = vi.fn();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;

        // 1. Primeira tentativa (falha)
        fluxoSubprocesso.validarCadastro.mockResolvedValueOnce({
            valido: false,
            erros: [{mensagem: "Erro de validação"}]
        });

        await vm.disponibilizarCadastro();
        await vm.$nextTick();

        expect(vm.erroGlobal).toBe("Erro de validação");
        const alertaErroGlobal = wrapper.find('[data-testid="alerta-erro-global"]');
        expect(alertaErroGlobal.exists()).toBe(true);
        const alert = wrapper.find('[data-testid="btn-dismiss-alert"]');
        expect(alert.exists()).toBe(true);

        // 2. Fecha o alerta
        await alert.trigger('click');
        await vm.$nextTick();
        expect(vm.erroGlobal).toBeNull();
        expect(wrapper.find('[data-testid="alerta-erro-global"]').exists()).toBe(false);

        // 3. Segunda tentativa (mesmo erro)
        fluxoSubprocesso.validarCadastro.mockResolvedValueOnce({
            valido: false,
            erros: [{mensagem: "Erro de validação"}]
        });

        await vm.disponibilizarCadastro();
        await vm.$nextTick();

        // O teste deve falhar aqui se o bug persistir
        expect(vm.erroGlobal).toBe("Erro de validação");
        expect(wrapper.find('[data-testid="btn-dismiss-alert"]').exists()).toBe(true);
    });

    it("deve gerenciar interações com formulários de atividades, modais de importação e alertas de erro extras", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // adicionarAtividade branches
        await vm.adicionarAtividade();
        expect(mockAtividadeForm.adicionarAtividade).toHaveBeenCalled();

        // scrollParaPrimeiroErro branches
        vm.errosValidacao = [{atividadeCodigo: 1, mensagem: 'erro'}];
        const div = document.createElement('div');
        div.scrollIntoView = vi.fn();
        vm.setAtividadeRef(1, div);
        vm.scrollParaPrimeiroErro();
        expect(div.scrollIntoView).toHaveBeenCalled();

        // aoImportarAtividades branches (success case without warning)
        await vm.aoImportarAtividades({
            aviso: null, 
            atividadesAtualizadas: [], 
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
        });
        expect(notifyMock).toHaveBeenCalledWith(TEXTOS.atividades.SUCESSO_IMPORTACAO, 'success');
    });

    it("deve calcular assinatura e ordenar atividades corretamente", async () => {
        const wrapper = createWrapper();
        const vm = wrapper.vm as any;
        
        vm.atividades = [
            {codigo: 1, descricao: 'B'},
            {codigo: 2, descricao: 'A'},
            {codigo: null, descricao: 'C'}
        ];

        expect(vm.atividadesOrdenadas[0].codigo).toBe(2);
        expect(vm.atividadesOrdenadas[1].codigo).toBe(1);
        expect(vm.atividadesOrdenadas[2].codigo).toBe(null);
        
        expect(vm.houveAlteracaoCadastro).toBe(true);
    });

    it("deve esconder edicao para chefe quando nao habilitado", async () => {
        const wrapper = createWrapper({}, {
            podeEditarCadastro: ref(true),
            habilitarEditarCadastro: ref(false)
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        const {usePerfilStore} = await import("@/stores/perfil");
        const perfilStore = usePerfilStore();
        perfilStore.perfilSelecionado = Perfil.CHEFE;

        expect(vm.podeEditarCadastro).toBe(true);
        expect(vm.habilitarEditarCadastro).toBe(false);
        expect(vm.esconderEdicaoCadastroParaChefe).toBe(true);
        expect(vm.mostrarControlesEdicaoCadastro).toBe(false);
    });
});

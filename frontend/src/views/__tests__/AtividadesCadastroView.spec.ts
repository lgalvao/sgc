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
import * as useAcessoModule from '@/composables/useAcesso';
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

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

type AtividadeMinima = {
    codigo: number;
    descricao?: string;
    conhecimentos?: Array<{codigo: number; descricao?: string}>;
};

type CadastroViewVm = {
    codSubprocesso: number | null;
    unidade: {sigla: string; nome: string} | null;
    atividades: AtividadeMinima[];
    atividadesSnapshotInicial: string;
    disponibilizacaoSemMudancas: boolean;
    mostrarModalConfirmacao: boolean;
    mostrarModalImportar: boolean;
    mostrarModalConfirmacaoRemocao: boolean;
    dadosRemocao: {tipo: string; index?: number; conhecimentoCodigo?: number} | null;
    erroGlobal: string | null;
    notificacao: unknown;
    novaAtividade: string;
    timeoutLimparErros: ReturnType<typeof setTimeout> | null;
    podeHomologarCadastro?: boolean;
    handleImportAtividades: () => Promise<void>;
    disponibilizarCadastro: () => Promise<void>;
    adicionarAtividade: () => Promise<void>;
    confirmarRemocao: () => Promise<void>;
    processarRespostaLocal: (payload: {atividadesAtualizadas?: unknown[]}) => void;
    notify: (mensagem: string, variante: string) => void;
    carregarContextoInicial: () => Promise<void>;
    timeoutLimpezaErros: () => void;
    $nextTick: () => Promise<void>;
};

type FluxoSubprocessoMock = {
    validarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarCadastro: ReturnType<typeof vi.fn>;
    disponibilizarRevisaoCadastro: ReturnType<typeof vi.fn>;
    iniciarRevisaoCadastro: ReturnType<typeof vi.fn>;
};

type SubprocessoStoreMock = {
    subprocessoDetalhe: {
        codigo: number;
        situacao: SituacaoSubprocesso | string;
        tipoProcesso: TipoProcesso | string;
        unidade?: {sigla: string};
        permissoes?: Record<string, boolean>;
    } | null;
    buscarContextoCadastroAtividadesPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarContextoCadastroAtividades: ReturnType<typeof vi.fn>;
    buscarContextoEdicaoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarContextoEdicao: ReturnType<typeof vi.fn>;
    buscarSubprocessoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarSubprocessoDetalhe: ReturnType<typeof vi.fn>;
    atualizarStatusLocal: ReturnType<typeof vi.fn>;
    lastError: {message: string} | null;
    clearError: ReturnType<typeof vi.fn>;
};

function criarContextoEdicao(): ContextoCadastroAtividadesSubprocesso {
    const permissoes: PermissoesSubprocesso = {
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
    };
    const unidade: Unidade = {codigo: 1, sigla: "TESTE", nome: "Teste", filhas: [], usuarioCodigo: 0, responsavel: null};
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
        isEmAndamento: true,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes,
    };
    return {
        detalhes,
        mapa,
        atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]}],
        unidade
    };
}

const subprocessosMock = reactive({
    subprocessoDetalhe: null as SubprocessoStoreMock["subprocessoDetalhe"],
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarContextoEdicaoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as SubprocessoStoreMock["lastError"],
    clearError: vi.fn(),
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

vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: () => subprocessosMock}));
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
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
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
    BAlert: {template: '<div><slot /><button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['modelValue']},
    AppAlert: {template: '<div><button data-testid="btn-dismiss-app-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['message', 'variant', 'dismissible']},
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
};

function createWrapper(customState = {}, accessOverrides = {}) {
    vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
        podeEditarCadastro: ref(true),
        podeDisponibilizarCadastro: ref(true),
        podeVisualizarImpacto: ref(true),
        habilitarEditarCadastro: ref(true),
        habilitarDisponibilizarCadastro: ref(true),
        mesmaUnidade: ref(true),
        habilitarAcessoCadastro: ref(true),
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
    vm.codSubprocesso = 123;
    vm.unidade = {sigla: "TESTE", nome: "Teste"};

    return wrapper;
}

describe("CadastroView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        subprocessosMock.subprocessoDetalhe = {
            codigo: 123,
            situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "MAPEAMENTO",
            unidade: {sigla: "TESTE"},
            permissoes: {}
        };
        subprocessosMock.lastError = null;
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade = vi.fn();
        subprocessosMock.buscarContextoCadastroAtividades = vi.fn();
        subprocessosMock.buscarContextoEdicaoPorProcessoEUnidade = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn();
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            validarCadastro: vi.fn().mockResolvedValue({valido: true}),
            disponibilizarCadastro: vi.fn().mockResolvedValue(true),
            disponibilizarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as unknown as ReturnType<typeof useFluxoSubprocessoModule.useFluxoSubprocesso>);
        vi.mocked(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(criarContextoEdicao() as never);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as never);
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
            permissoes: {}
        };

        await vm.disponibilizarCadastro();

        expect(fluxoSubprocesso.validarCadastro).toHaveBeenCalledWith(123);
        expect(vm.mostrarModalConfirmacao).toBe(true);
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
            permissoes: {}
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
        const mapas = useMapas();
        mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(null);

        await wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-edicao"]').trigger("click");
        await flushPromises();

        expect(mapas.buscarImpactoMapa).toHaveBeenCalledWith(123);
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
        expect(btn.attributes('disabled')).toBeDefined();
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

    it("em revisão, mantém botão desabilitado sem mudanças e checkbox desmarcada", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: "REVISAO_CADASTRO_EM_ANDAMENTO",
            tipoProcesso: "REVISAO",
            unidade: {sigla: "TESTE"},
            permissoes: {}
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        await flushPromises();

        const checkbox = wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]');
        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(checkbox.exists()).toBe(true);
        expect(btn.attributes('disabled')).toBeDefined();
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
            permissoes: {}
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
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
            permissoes: {}
        };
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1 alterada",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        expect(btn.attributes('disabled')).toBeUndefined();
    });

    it("mantém as ações visíveis e mostra feedback ao iniciar a revisão pelo checkbox", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as unknown as CadastroViewVm;
        const subprocessosStore = subprocessosMock;
        let resolver: ((valor: boolean) => void) | null = null;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as unknown as FluxoSubprocessoMock;
        fluxoSubprocesso.iniciarRevisaoCadastro.mockImplementation(() =>
            new Promise((resolve) => {
                resolver = resolve;
            })
        );
        subprocessosStore.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            tipoProcesso: TipoProcesso.REVISAO,
            unidade: {sigla: "TESTE"},
            permissoes: {}
        };
        vm.atividades = [{
            codigo: 1,
            descricao: "Ativ 1",
            conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
        }];
        vm.atividadesSnapshotInicial = JSON.stringify([{descricao: "Ativ 1", conhecimentos: ["Conhecimento 1"]}]);
        await flushPromises();

        await wrapper.find('[data-testid="chk-disponibilizacao-sem-mudancas"]').setValue(true);
        await flushPromises();

        expect(fluxoSubprocesso.iniciarRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="cad-atividades__txt-iniciando-revisao"]').exists()).toBe(true);

        resolver?.(true);
        await flushPromises();

        expect(wrapper.find('[data-testid="cad-atividades__txt-iniciando-revisao"]').exists()).toBe(false);
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
        expect(btn.attributes('disabled')).toBeDefined();
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
            atividadesDisponiveis: [{codigo: 2, descricao: "Atividade importada", conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]}],
        });

        const vm = wrapper.vm as unknown as CadastroViewVm;
        await vm.handleImportAtividades();

        expect(subprocessosStore.buscarContextoCadastroAtividades).toHaveBeenCalledWith(123);
        expect(vm.atividades).toEqual([
            {codigo: 2, descricao: "Atividade importada", conhecimentos: [{codigo: 2, descricao: "Conhecimento importado"}]}
        ]);
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

        // Branches de timeout e outros

        vm.timeoutLimparErros = setTimeout(() => {}, 100);
        vm.timeoutLimpezaErros();
        
        // Cobre ramo sem contexto agregado
        subprocessosMock.buscarContextoCadastroAtividadesPorProcessoEUnidade.mockResolvedValue(null);
        await vm.carregarContextoInicial();

        // 375-377 (adicionarAtividade success branch)
        vm.codSubprocesso = 123;
        const mockAtiv = {atividadesAtualizadas: []};
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
        vm.processarRespostaLocal({atividadesAtualizadas: [{codigo: 1}]});
    });
});

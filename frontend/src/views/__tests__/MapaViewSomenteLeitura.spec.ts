import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as analiseService from "@/services/analiseService";
import * as processoService from "@/services/processoService";
import * as subprocessoService from "@/services/subprocessoService";
import * as useAcessoModule from "@/composables/useAcesso";
import MapaView from "../MapaView.vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

const diagnosticoMock = vi.hoisted(() => vi.fn());
const subprocessoStoreCacheMock = {
    contextoEdicao: null as any,
    erroIntegracaoContexto: null,
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
    limparContextoAtual: vi.fn(),
    invalidar: vi.fn(),
};

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/composables/useContextoSubprocesso", () => ({
    diagnosticarCarregamentoContextoSubprocessoInicial: diagnosticoMock,
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
    apresentarSugestoes: vi.fn(),
    validarMapa: vi.fn(),
    aceitarValidacao: vi.fn(),
    homologarValidacao: vi.fn(),
    devolverValidacao: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    obterMapaVisualizacao: vi.fn(),
    obterSugestoesMapa: vi.fn(),
}));

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreCacheMock,
}));

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
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
    },
    BAlert: {template: '<div><slot /></div>'},
    BFormGroup: {template: '<div><slot name="label" /><slot /></div>'},
    BFormTextarea: {
        props: ['modelValue'],
        template: '<textarea :data-testid="$attrs[\'data-testid\']" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
    },
    BFormInvalidFeedback: {template: '<div><slot /></div>'},
    BBadge: {template: '<span><slot /></span>'},
    BCard: {template: '<div><slot /></div>'},
    BCardBody: {template: '<div><slot /></div>'},
    BCardHeader: {template: '<div><slot /></div>'},
    BCardTitle: {template: '<div><slot /></div>'},
    CarregamentoPagina: {template: '<div></div>'},
    EmptyState: {
        props: ['title', 'description'],
        template: '<div><div>{{ title }}</div><div>{{ description }}</div></div>',
    },
    CompetenciaCard: {template: '<div />'},
    CriarCompetenciaModal: {template: '<div />'},
    DisponibilizarMapaModal: {template: '<div />'},
    ImpactoMapaModal: {template: '<div />'},
    HistoricoAnaliseModal: {template: '<div v-if="mostrar"></div>', props: ['mostrar']},
    AceitarMapaModal: {template: '<div />'},
    ModalPadrao: {
        props: ['modelValue', 'testCodigoCancelar'],
        template: '<div v-if="modelValue"><slot /><button :data-testid="testCodigoCancelar" @click="$emit(\'fechar\')">Fechar</button></div>'
    },
    ModalConfirmacao: {
        props: ['modelValue', 'titulo'],
        template: '<div v-if="modelValue"><slot /><button data-testid="btn-confirmar-modal" @click="$emit(\'confirmar\')">Confirmar</button></div>'
    },
};

describe("MapaView somente leitura", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        subprocessoStoreCacheMock.contextoEdicao = {
            detalhes: {
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TESTE', nome: 'Unidade Teste'},
                situacao: 'MAPEAMENTO_MAPA_CRIADO',
                tipoProcesso: 'MAPEAMENTO',
                titular: null,
                responsavel: null,
                localizacaoAtual: 'TESTE',
                processoDescricao: 'Processo',
                dataCriacaoProcesso: '2024-01-01',
                ultimaDataLimiteSubprocesso: '2025-01-01',
                prazoEtapaAtual: '2025-01-01',
                isEmAndamento: true,
                etapaAtual: 2,
                movimentacoes: [],
                elementosProcesso: [],
                permissoes: {},
            },
            unidade: {codigo: 1, sigla: 'TESTE', nome: 'Unidade Teste'},
            atividadesDisponiveis: [],
            mapa: {codigo: 10},
            subprocesso: {
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TESTE', nome: 'Unidade Teste'},
                situacao: 'MAPEAMENTO_MAPA_CRIADO',
                dataLimite: '2025-01-01',
                dataFimEtapa1: '',
                dataLimiteEtapa2: '',
                atividades: [],
                codUnidade: 1,
            },
        };

        diagnosticoMock.mockResolvedValue({
            tipo: 'sucesso',
            resultado: {
                codigo: 123,
                contexto: {
                    detalhes: {
                        codigo: 123,
                        codSubprocesso: 123,
                        permissoes: {},
                    },
                    unidade: {sigla: 'TESTE', nome: 'Unidade Teste'},
                    atividadesDisponiveis: [],
                    mapa: {codigo: 10},
                },
            },
        });

        vi.mocked(subprocessoService.obterMapaVisualizacao).mockResolvedValue({
            codigo: 10,
            descricao: "Mapa de Teste",
            competencias: [
                {
                    codigo: 1,
                    descricao: "Competencia 1",
                    atividades: [
                        {
                            codigo: 2,
                            descricao: "Atividade 1",
                            conhecimentos: [{codigo: 3, descricao: "Conhecimento 1"}],
                        },
                    ],
                },
            ],
            sugestoes: "Sugestão persistida",
        } as any);
        vi.mocked(subprocessoService.obterSugestoesMapa).mockResolvedValue("Sugestão persistida");
        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
        vi.mocked(processoService.apresentarSugestoes).mockResolvedValue(undefined as never);

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarImpacto: ref(false),
            podeEditarMapa: ref(false),
            podeDisponibilizarMapa: ref(false),
            habilitarEditarMapa: ref(false),
            habilitarDisponibilizarMapa: ref(false),
            podeValidarMapa: ref(true),
            habilitarValidarMapa: ref(true),
            podeVerSugestoes: ref(true),
            podeAnalisarMapa: ref(true),
            habilitarDevolverMapa: ref(true),
            acaoPrincipalMapa: ref({
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: true,
                rotuloBotao: 'Homologar',
                mensagemSucesso: 'Mapa homologado',
            }),
        } as any);
    });

    function mountComponent() {
        return mount(MapaView, {
            global: {
                plugins: [createTestingPinia({stubActions: true})],
                stubs,
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });
    }

    it("renderiza o corpo somente leitura e nao exibe botoes de manutencao", async () => {
        const wrapper = mountComponent();
        await flushPromises();

        expect(subprocessoService.obterMapaVisualizacao).toHaveBeenCalledWith(123);
        expect(wrapper.text()).toContain("Competencia 1");
        expect(wrapper.text()).toContain("Atividade 1");
        expect(wrapper.text()).toContain("Conhecimento 1");
        expect((wrapper.vm as any).modoSomenteLeitura).toBe(true);
        expect(wrapper.find('[data-testid="btn-abrir-criar-competencia"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-cad-mapa-disponibilizar"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="btn-mapa-validar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-mapa-homologar-aceite"]').exists()).toBe(true);
    });

    it("carrega sugestoes e historico pelas acoes de analise", async () => {
        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-mapa-sugestoes"]').trigger("click");
        await flushPromises();

        expect(subprocessoService.obterSugestoesMapa).toHaveBeenCalledWith(123);
        await wrapper.find('[data-testid="inp-sugestoes-mapa-texto"]').setValue("Nova sugestão");
        await wrapper.find('[data-testid="btn-confirmar-modal"]').trigger("click");
        await flushPromises();

        expect(processoService.apresentarSugestoes).toHaveBeenCalledWith(123, {sugestoes: "Nova sugestão"});

        await wrapper.find('[data-testid="btn-mapa-historico"]').trigger("click");
        await flushPromises();

        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);
    });
});

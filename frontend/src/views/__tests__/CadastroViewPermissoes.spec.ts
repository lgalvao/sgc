import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as subprocessoService from "@/services/subprocessoService";
import * as processoService from "@/services/processoService";
import CadastroView from "@/views/CadastroView.vue";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/composables/usePerfil", () => ({usePerfil: vi.fn()}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    validarCadastro: vi.fn(),
    listarAtividades: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
    obterDetalhesProcesso: vi.fn(),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {template: '<div><slot /><slot name="actions" /></div>'},
    LoadingButton: {template: '<button :data-testid="$attrs[\'data-testid\']"><slot>{{ $attrs.text }}</slot></button>'},
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']"><slot /></button>'},
    BDropdown: {template: '<div><slot /></div>'},
    BDropdownItem: {template: '<div :data-testid="$attrs[\'data-testid\']"><slot /></div>'},
    BAlert: {template: '<div><slot /></div>'},
    CadAtividadeForm: {template: '<div></div>', expose: ['inputRef']},
    EmptyState: {template: '<div><slot /></div>'},
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
    ImportarAtividadesModal: {template: '<div></div>'},
    ImpactoMapaModal: {template: '<div></div>'},
    ConfirmacaoDisponibilizacaoModal: {template: '<div></div>'},
    HistoricoAnaliseModal: {template: '<div></div>'},
    ModalConfirmacao: {template: '<div></div>'},
};

describe("CadastroView - permissões no carregamento", () => {
    beforeEach(() => {
        vi.clearAllMocks();

        vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
            perfilSelecionado: ref(Perfil.CHEFE),
            isChefe: ref(true),
        } as any);

        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            unidades: []
        } as any);
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            detalhes: {
                subprocesso: {
                    codigo: 123,
                    unidade: {sigla: "TESTE"},
                    situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                    tipoProcesso: TipoProcesso.MAPEAMENTO,
                },
                permissoes: {
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
                    podeVisualizarImpacto: false,
                    podeAlterarDataLimite: false,
                    podeReabrirCadastro: false,
                    podeReabrirRevisao: false,
                    podeEnviarLembrete: false,
                    habilitarAcessoCadastro: true,
                    habilitarAcessoMapa: false,
                }
            },
            mapa: {codigo: 100},
            atividadesDisponiveis: [{
                codigo: 1,
                descricao: "Atividade 1",
                conhecimentos: [{codigo: 1, descricao: "Conhecimento 1"}]
            }],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    it("deve exibir o botão Disponibilizar no primeiro carregamento quando a permissão vier no contexto", async () => {
        const wrapper = mount(CadastroView, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        perfil: {
                            perfilSelecionado: Perfil.CHEFE,
                            unidadeSelecionada: 18,
                            unidadeSelecionadaSigla: "TESTE",
                        },
                        mapas: {
                            mapaCompleto: {codigo: 100}
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

        await flushPromises();

        expect(wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').exists()).toBe(true);
    });
});

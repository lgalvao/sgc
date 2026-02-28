import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as subprocessoService from "@/services/subprocessoService";
import {useAnalisesStore} from "@/stores/analises";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import AtividadesCadastroView from "@/views/processo/AtividadesCadastroView.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import * as useAcessoModule from '@/composables/useAcesso';
import {Perfil} from "@/types/tipos";

// Mocks
const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/composables/usePerfil", () => ({usePerfil: vi.fn()}));

// Mock services
vi.mock("@/services/subprocessoService", () => ({
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

// Mock processoService
vi.mock("@/services/processoService", () => ({
    buscarProcessoDetalhe: vi.fn(),
    obterDetalhesProcesso: vi.fn().mockResolvedValue({
        codigo: 1,
        unidades: [
            {sigla: "TESTE", codSubprocesso: 123, filhos: []}
        ]
    })
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {template: '<div><slot /><slot name="actions" /></div>'},
    LoadingButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BDropdown: {template: '<div><slot /></div>'},
    BDropdownItem: {template: '<div :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></div>'},
    ErrorAlert: {template: '<div></div>'},
    CadAtividadeForm: {template: '<div></div>', expose: ['inputRef']},
    EmptyState: {template: '<div><slot /></div>'},
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
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
        ...accessOverrides
    } as any);

    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
        perfilSelecionado: ref(Perfil.CHEFE),
    } as any);

    const wrapper = mount(AtividadesCadastroView, {
        global: {
            plugins: [createTestingPinia({
                createSpy: vi.fn,
                stubActions: false,
                initialState: {
                    perfil: {
                        perfilSelecionado: Perfil.CHEFE,
                    },
                    unidades: {
                        unidade: {codigo: 1, sigla: "TESTE", nome: "Teste"},
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

    // Manually set codSubprocesso to ensure buttons depending on it are rendered
    (wrapper.vm as any).codSubprocesso = 123;

    return wrapper;
}

describe("AtividadesCadastroView.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123} as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                unidade: {sigla: "TESTE"}
            },
            mapa: {codigo: 100},
            atividadesDisponiveis: [],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    it("renderiza corretamente", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("TESTE - Teste");
    });

    it("chama validação antes de disponibilizar", async () => {
        const wrapper = createWrapper();
        await wrapper.vm.$nextTick();
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.validarCadastro = vi.fn().mockResolvedValue({valido: true});

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");

        expect(subprocessosStore.validarCadastro).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalConfirmacao).toBe(true);
    });

    it("confirma disponibilização e redireciona", async () => {
        const wrapper = createWrapper();
        await wrapper.vm.$nextTick();
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.validarCadastro = vi.fn().mockResolvedValue({valido: true});
        subprocessosStore.disponibilizarCadastro = vi.fn().mockResolvedValue(true);

        // Open modal first
        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        // Confirm in modal
        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModal);
        await modal.vm.$emit('confirmar');

        expect(subprocessosStore.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("carrega histórico ao abrir modal", async () => {
        const wrapper = createWrapper();
        await wrapper.vm.$nextTick();
        const analisesStore = useAnalisesStore();
        analisesStore.carregarHistorico = vi.fn();

        await wrapper.find('[data-testid="btn-cad-atividades-historico"]').trigger("click");
        await flushPromises();

        expect(analisesStore.carregarHistorico).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(HistoricoAnaliseModal).exists()).toBe(true);
    });

    it("carrega impacto ao abrir modal", async () => {
        const wrapper = createWrapper();
        await wrapper.vm.$nextTick();
        const mapasStore = useMapasStore();
        mapasStore.buscarImpactoMapa = vi.fn().mockResolvedValue(null);

        await wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-edicao"]').trigger("click");
        await flushPromises();

        expect(mapasStore.buscarImpactoMapa).toHaveBeenCalledWith(123);
        expect(wrapper.findComponent(ImpactoMapaModal).exists()).toBe(true);
    });
});

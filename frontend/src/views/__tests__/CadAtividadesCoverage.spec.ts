import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import CadAtividades from "@/views/CadAtividades.vue";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAtividadesStore} from "@/stores/atividades";
import * as subprocessoService from "@/services/subprocessoService";
import {createTestingPinia} from "@pinia/testing";
import {nextTick} from "vue";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {logger} from "@/utils";

// Mocks
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: { query: {} as Record<string, string> }
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mocks.push,
        back: vi.fn(),
    }),
    useRoute: () => mocks.mockRoute,
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mocks.push,
        resolve: vi.fn(),
        currentRoute: { value: mocks.mockRoute }
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    validarCadastro: vi.fn(),
}));

vi.mock("@/services/processoService");

vi.mock("@/utils", async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        logger: {
            error: vi.fn(),
            info: vi.fn(),
            warn: vi.fn(),
        }
    }
});

// Stubs
const AtividadeItemStub = {
    template: `
    <div class="atividade-item">
      <button data-testid="btn-remover-conhecimento" @click="$emit('remover-conhecimento', 101)">Remover Conh</button>
    </div>
  `,
    props: ["atividade"],
    emits: ["remover-conhecimento"]
};

// Mock BFormInput to support ref and focus
const BFormInputStub = {
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue'],
    emits: ['update:modelValue'],
    setup(props, { expose }) {
        const focus = vi.fn();
        expose({ focus, $el: { focus } });
        return { focus };
    }
};

describe("CadAtividadesCoverage.spec.ts", () => {
    let subprocessosStore: any;
    let atividadesStore: any;

    const createWrapper = () => {
        mocks.mockRoute.query = {};

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: "CHEFE",
                },
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: TipoProcesso.MAPEAMENTO,
                        unidades: [{ codUnidade: 1, codSubprocesso: 123 }]
                    }
                },
                subprocessos: {
                    subprocessoDetalhe: {
                        codigo: 123,
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                         permissoes: {
                             podeEditarMapa: true,
                             podeDisponibilizarCadastro: true,
                        }
                    }
                },
                atividades: {
                    atividadesPorSubprocesso: new Map([[123, [{ codigo: 1, conhecimentos: [{codigo: 101}] }]]])
                },
                unidades: {
                    unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
                }
            },
            stubActions: true
        });

        subprocessosStore = useSubprocessosStore(pinia);
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);

        atividadesStore = useAtividadesStore(pinia);
        atividadesStore.removerConhecimento.mockResolvedValue({});

        const wrapper = mount(CadAtividades, {
            global: {
                plugins: [pinia],
                stubs: {
                    AtividadeItem: AtividadeItemStub,
                    ModalConfirmacao: {
                        name: "ModalConfirmacao",
                        template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
                        props: ['modelValue'],
                        emits: ['confirmar', 'update:modelValue']
                    },
                    BFormInput: BFormInputStub,
                    BContainer: { template: '<div><slot /></div>' },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    BForm: { template: '<form @submit="$emit(\'submit\', $event)"><slot /></form>' },
                    BCol: { template: '<div><slot /></div>' },
                    BDropdown: { template: '<div><slot /></div>' },
                    BDropdownItem: { template: '<div><slot /></div>' },
                    BAlert: { template: '<div><slot /></div>' },
                    EmptyState: { template: '<div><slot /></div>' },
                    ImpactoMapaModal: true,
                    ConfirmacaoDisponibilizacaoModal: true,
                    HistoricoAnaliseModal: true,
                    ImportarAtividadesModal: true,
                    LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' }
                },
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE",
            },
        });

        return { wrapper, subprocessosStore, atividadesStore };
    };

    it("deve tratar erro ao remover conhecimento", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        const feedbackStore = (wrapper.vm as any).feedbackStore;
        vi.spyOn(feedbackStore, "show");

        atividadesStore.removerConhecimento.mockRejectedValue(new Error("Erro ao remover conhecimento"));

        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-conhecimento', 101);
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro na remoção", "Erro ao remover conhecimento", "danger");
    });

    it("deve tratar erro na validação ao disponibilizar cadastro", async () => {
        const { wrapper } = createWrapper();
        const feedbackStore = (wrapper.vm as any).feedbackStore;
        vi.spyOn(feedbackStore, "show");

        vi.mocked(subprocessoService.validarCadastro).mockRejectedValue(new Error("Erro de validação"));

        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro na validação", "Não foi possível validar o cadastro.", "danger");
        expect((wrapper.vm as any).loadingValidacao).toBe(false);
    });

    it("deve logar erro se subprocesso não encontrado no onMounted (manual setup)", async () => {
        mocks.mockRoute.query = {};
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: { perfilSelecionado: "CHEFE" },
                processos: { processoDetalhe: { codigo: 1, tipo: TipoProcesso.MAPEAMENTO, unidades: [] } },
                subprocessos: { subprocessoDetalhe: null },
                atividades: { atividadesPorSubprocesso: new Map() },
                unidades: { unidade: null }
            },
            stubActions: true
        });

        const subprocessosStore = useSubprocessosStore(pinia);
        // Return null to simulate not found
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(null);

        const wrapper = mount(CadAtividades, {
            global: {
                plugins: [pinia],
                stubs: {
                    AtividadeItem: true, ModalConfirmacao: true, BFormInput: true,
                    BContainer: true, BButton: true, BForm: true, BCol: true,
                    BDropdown: true, BDropdownItem: true, BAlert: true, EmptyState: true,
                    ImpactoMapaModal: true, ConfirmacaoDisponibilizacaoModal: true,
                    HistoricoAnaliseModal: true, ImportarAtividadesModal: true, LoadingButton: true
                },
            },
            props: { codProcesso: 1, sigla: "TESTE" },
        });

        await flushPromises();

        expect(logger.error).toHaveBeenCalledWith('[CadAtividades] ERRO: Subprocesso não encontrado!');
    });
});

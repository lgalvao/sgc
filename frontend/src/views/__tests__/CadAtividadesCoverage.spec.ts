import {describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import CadAtividades from "@/views/CadAtividades.vue";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAtividadesStore} from "@/stores/atividades";
import {useFeedbackStore} from "@/stores/feedback";
import * as subprocessoService from "@/services/subprocessoService";
import {createTestingPinia} from "@pinia/testing";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import logger from "@/utils/logger";

// Mocks
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: { query: {} as Record<string, string>, params: { codProcesso: '1', siglaUnidade: 'TESTE' } }
}));

vi.mock("vue-router", async (importOriginal) => {
    const actual = await importOriginal<typeof import('vue-router')>();
    return {
        ...actual,
        useRouter: () => ({
            push: mocks.push,
            back: vi.fn(),
            currentRoute: { value: mocks.mockRoute }
        }),
        useRoute: () => mocks.mockRoute,
    };
});

vi.mock("@/services/subprocessoService", () => ({
    validarCadastro: vi.fn(),
}));

vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        info: vi.fn(),
        warn: vi.fn(),
    }
}));

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
                        unidades: [{ codUnidade: 1, sigla: 'TESTE', codSubprocesso: 123 }]
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

        const subprocessosStore = useSubprocessosStore(pinia) as any;
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);

        const atividadesStore = useAtividadesStore(pinia) as any;
        atividadesStore.removerConhecimento.mockResolvedValue({});

        const feedbackStore = useFeedbackStore(pinia) as any;

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

        return { wrapper, subprocessosStore, atividadesStore, feedbackStore, pinia };
    };

    it("deve tratar erro ao remover conhecimento", async () => {
        const { wrapper, atividadesStore, feedbackStore } = createWrapper();

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
        const { wrapper, feedbackStore } = createWrapper();

        vi.mocked(subprocessoService.validarCadastro).mockRejectedValue(new Error("Erro de validação"));

        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro na validação", "Não foi possível validar o cadastro.", "danger");
    });

    it("deve logar erro se subprocesso não encontrado no onMounted (manual setup)", async () => {
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

        const subprocessosStore = useSubprocessosStore(pinia) as any;
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(null);

        mount(CadAtividades, {
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

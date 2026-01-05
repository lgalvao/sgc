import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import VisAtividades from "@/views/VisAtividades.vue";
import { createTestingPinia } from "@pinia/testing";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { Perfil, SituacaoSubprocesso, TipoProcesso } from "@/types/tipos";
import { useRouter } from "vue-router";

// Hoist mocks to avoid ReferenceError
const { mockApiClient } = vi.hoisted(() => {
    const client = {
        get: vi.fn().mockResolvedValue({ data: {} }),
        post: vi.fn().mockResolvedValue({ data: {} }),
        put: vi.fn().mockResolvedValue({ data: {} }),
        delete: vi.fn().mockResolvedValue({ data: {} }),
    };
    return { mockApiClient: client };
});

// Mock router
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Mock axios with default export
vi.mock("@/axios-setup", () => ({
    apiClient: mockApiClient,
    default: mockApiClient,
}));

// Mock services
vi.mock("@/services/cadastroService", () => ({
    aceitarRevisaoCadastro: vi.fn().mockResolvedValue(true),
    homologarRevisaoCadastro: vi.fn().mockResolvedValue(true),
    devolverRevisaoCadastro: vi.fn().mockResolvedValue(true),
    aceitarCadastro: vi.fn().mockResolvedValue(true),
    homologarCadastro: vi.fn().mockResolvedValue(true),
    devolverCadastro: vi.fn().mockResolvedValue(true),
}));

// Fix: Mock obterDetalhesProcesso to return minimal valid data matching the test scenario
vi.mock("@/services/processoService", () => ({
    buscarProcessoDetalhe: vi.fn(),
    obterDetalhesProcesso: vi.fn().mockResolvedValue({
        codigo: 1,
        tipo: 'REVISAO',
        unidades: [{
            sigla: "U1",
            codSubprocesso: 10,
            situacaoSubprocesso: "REVISAO_CADASTRO_DISPONIBILIZADA"
        }]
    }),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
    verificarImpactosMapa: vi.fn().mockResolvedValue({ temImpactos: false, impactos: [] }),
}));

vi.mock("@/stores/atividades", () => ({
    useAtividadesStore: vi.fn(() => ({
        buscarAtividadesParaSubprocesso: vi.fn(),
        obterAtividadesPorSubprocesso: vi.fn().mockReturnValue([]),
    }))
}));

// Mock child components
const BModalStub = {
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['modelValue', 'title'],
    emits: ['update:modelValue']
};
const BButtonStub = {
    template: '<button @click="$emit(\'click\')"><slot></slot></button>',
    props: ['variant']
};

describe("VisAtividades.vue", () => {
    let subprocessosStore: any;
    let pushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: pushMock,
        });
    });

    const mountOptions = (initialState: any = {}) => ({
        props: {
            codProcesso: "1",
            sigla: "U1"
        },
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        processos: {
                            processoDetalhe: {
                                codigo: 1,
                                tipo: TipoProcesso.REVISAO,
                                unidades: [
                                    {
                                        sigla: "U1",
                                        nome: "Unidade 1",
                                        codSubprocesso: 10,
                                        situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                                    }
                                ]
                            }
                        },
                        perfil: {
                            perfilSelecionado: Perfil.ADMIN,
                        },
                        atividades: {
                            // ... activities mock if needed
                        },
                        ...initialState,
                    },
                    stubActions: false, // Allow actions to call services
                }),
            ],
            stubs: {
                BModal: BModalStub,
                BButton: BButtonStub,
                BFormTextarea: { template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>', props: ['modelValue'] },
                ImpactoMapaModal: { template: '<div></div>' },
                HistoricoAnaliseModal: { template: '<div></div>' },
                BContainer: { template: '<div><slot/></div>' },
                BCard: { template: '<div><slot/></div>' },
                BCardBody: { template: '<div><slot/></div>' }
            },
        },
    });

    it("deve validar cadastro (Homologar) e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        subprocessosStore = useSubprocessosStore();

        // Mock success response
        vi.spyOn(subprocessosStore, "homologarRevisaoCadastro").mockResolvedValue(true);

        // Open modal
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");

        // Confirm
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalledWith(
            10, // codSubprocesso
            { observacoes: "" }
        );
        expect(pushMock).toHaveBeenCalledWith({
            name: "Subprocesso",
            params: {
                codProcesso: "1", // props are string
                siglaUnidade: "U1"
            }
        });
    });

    it("deve validar cadastro (Aceitar) e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions({
            perfil: { perfilSelecionado: Perfil.GESTOR },
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                        }
                    ]
                }
            }
        }));
        subprocessosStore = useSubprocessosStore();
        vi.spyOn(subprocessosStore, "aceitarRevisaoCadastro").mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.aceitarRevisaoCadastro).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("deve devolver cadastro e redirecionar", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        subprocessosStore = useSubprocessosStore();
        vi.spyOn(subprocessosStore, "devolverRevisaoCadastro").mockResolvedValue(true);

        await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");

        // Fill observation
        const textarea = wrapper.find('[data-testid="inp-devolucao-cadastro-obs"]');
        await textarea.setValue("Devolvendo");

        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
        await flushPromises();

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(
            10,
            { observacoes: "Devolvendo" }
        );
        expect(pushMock).toHaveBeenCalledWith("/painel");
    });

    it("deve abrir modal de impacto ao clicar no botão", async () => {
        const wrapper = mount(VisAtividades, mountOptions());
        // Force button visibility
        const btn = wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]');
        await btn.trigger("click");

        await flushPromises();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
    });
});

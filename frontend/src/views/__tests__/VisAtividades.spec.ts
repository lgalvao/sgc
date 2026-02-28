import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import VisAtividades from "@/views/processo/AtividadesVisualizacaoView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAtividadesStore} from "@/stores/atividades";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {useRouter} from "vue-router";
import {obterDetalhesProcesso} from "@/services/processoService";
import * as useAcessoModule from '@/composables/useAcesso';

// Hoist mocks
const {mockApiClient} = vi.hoisted(() => {
    const client = {
        get: vi.fn().mockResolvedValue({data: {}}),
        post: vi.fn().mockResolvedValue({data: {}}),
        put: vi.fn().mockResolvedValue({data: {}}),
        delete: vi.fn().mockResolvedValue({data: {}}),
    };
    return {mockApiClient: client};
});

vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(),
}));

vi.mock("@/axios-setup", () => ({
    apiClient: mockApiClient,
    default: mockApiClient,
}));

vi.mock("@/services/processoService", () => ({
    buscarProcessoDetalhe: vi.fn(),
    obterDetalhesProcesso: vi.fn().mockResolvedValue({
        codigo: 1,
        tipo: 'REVISAO',
        unidades: []
    }),
}));

vi.mock("@/services/mapaService", () => ({
    verificarImpactosMapa: vi.fn().mockResolvedValue({temImpactos: false, impactos: []}),
}));

vi.mock("@/services/subprocessoService", () => ({
    listarAtividades: vi.fn().mockResolvedValue([]),
    buscarSubprocessoDetalhe: vi.fn().mockResolvedValue({
        codigo: 10,
    }),
    mapSubprocessoDetalheDtoToModel: vi.fn((dto) => dto),
    homologarRevisaoCadastro: vi.fn(), // Already mocking in store spy, but good to have
    devolverRevisaoCadastro: vi.fn(),
}));

// Components stubs
const BModalStub = {
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['modelValue', 'title'],
    emits: ['update:modelValue']
};

describe("VisAtividades.vue Coverage", () => {
    let pushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: pushMock,
        });
    });

    const mountOptions = (initialState: any = {}, propsData: any = {codProcesso: "1", sigla: "U1"}) => ({
        props: propsData,
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        processos: {
                            processoDetalhe: {
                                codigo: 1,
                                tipo: TipoProcesso.REVISAO,
                                unidades: []
                            }
                        },
                        perfil: {perfilSelecionado: Perfil.ADMIN},
                        ...initialState,
                    },
                    stubActions: false,
                }),
            ],
            stubs: {
                BModal: BModalStub,
                ImpactoMapaModal: {template: '<div></div>'},
                HistoricoAnaliseModal: {template: '<div></div>'},
                ModalConfirmacao: {
                    template: '<div><slot></slot></div>',
                    props: ['loading'],
                    emits: ['confirmar']
                },
                BContainer: {template: '<div><slot/></div>'},
                BCard: {template: '<div><slot/></div>'},
                BCardBody: {template: '<div><slot/></div>'},
                PageHeader: {template: '<div><slot/><slot name="subtitle"/><slot name="actions"/></div>'},
                BButton: {template: '<button @click="$emit(\'click\')"><slot/></button>'},
                BFormGroup: {template: '<div><slot/></div>'},
                BFormTextarea: {template: '<textarea></textarea>'}
            },
        },
    });

    const mountComponent = (initialState: any = {}, propsData: any = {
        codProcesso: "1",
        sigla: "U1"
    }, accessOverrides: Record<string, any> = {}) => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeHomologarCadastro: {value: true},
            podeVisualizarImpacto: {value: true},
            ...accessOverrides
        } as any);

        return mount(VisAtividades, mountOptions(initialState, propsData));
    };

    it("deve encontrar unidade aninhada recursivamente", async () => {
        const initialState = {
            unidades: {
                unidades: [
                    {
                        sigla: "ROOT",
                        nome: "Root Unit",
                        filhas: [
                            {
                                sigla: "NESTED",
                                nome: "Nested Unit",
                                filhas: []
                            }
                        ]
                    }
                ]
            },
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "NESTED",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                        }
                    ]
                }
            }
        };

        const wrapper = mountComponent(initialState, {codProcesso: "1", sigla: "NESTED"});
        await flushPromises();

        expect(wrapper.text()).toContain("Nested Unit");
    });

    it("deve resetar loadingValidacao quando ocorrer erro na homologação", async () => {
        (obterDetalhesProcesso as any).mockResolvedValue({
            codigo: 1,
            tipo: TipoProcesso.REVISAO,
            unidades: [
                {
                    sigla: "U1",
                    codSubprocesso: 10,
                    situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                }
            ]
        });

        const initialState = {
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "U1",
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                        }
                    ]
                }
            }
        };

        const wrapper = mountComponent(initialState);
        await flushPromises(); // Wait for onMounted to update store

        const subprocessosStore = useSubprocessosStore();

        // Mock throwing error
        vi.spyOn(subprocessosStore, "homologarRevisaoCadastro").mockRejectedValue(new Error("Erro simulado"));

        // Force open validation modal state
        (wrapper.vm as any).mostrarModalValidar = true;

        // Trigger validation
        try {
            await (wrapper.vm as any).confirmarValidacao();
        } catch {
        }

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalled();
        expect((wrapper.vm as any).loadingValidacao).toBe(false);
    });

    it("deve resetar loadingDevolucao quando ocorrer erro na devolução", async () => {
        (obterDetalhesProcesso as any).mockResolvedValue({
            codigo: 1,
            tipo: TipoProcesso.REVISAO,
            unidades: [
                {
                    sigla: "U1",
                    codSubprocesso: 10,
                    situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                }
            ]
        });

        const initialState = {
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
        };

        const wrapper = mountComponent(initialState);
        await flushPromises();

        const subprocessosStore = useSubprocessosStore();

        vi.spyOn(subprocessosStore, "devolverRevisaoCadastro").mockRejectedValue(new Error("Erro simulado"));

        (wrapper.vm as any).mostrarModalDevolver = true;
        (wrapper.vm as any).observacaoDevolucao = "Motivo da devolução";

        try {
            await (wrapper.vm as any).confirmarDevolucao();
        } catch {
            // Erro esperado
        }

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalled();
        expect((wrapper.vm as any).loadingDevolucao).toBe(false);
    });

    it("não deve buscar atividades no onMounted se codSubprocesso for undefined", async () => {
        (obterDetalhesProcesso as any).mockResolvedValue({
            codigo: 1,
            tipo: TipoProcesso.REVISAO,
            unidades: [] // Empty units in process
        });

        const initialState = {
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    tipo: TipoProcesso.REVISAO,
                    unidades: [] // Empty units in process
                }
            }
        };

        mountComponent(initialState);
        const atividadesStore = useAtividadesStore();

        await flushPromises();

        expect(atividadesStore.buscarAtividadesParaSubprocesso).not.toHaveBeenCalled();
    });
});

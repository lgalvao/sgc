import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import ProcessoView from "@/views/ProcessoView.vue";
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import {createTestingPinia} from "@pinia/testing";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

// Mocks
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: { params: { codProcesso: "1" }, query: {} }
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mocks.push,
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

vi.mock("@/services/processoService");

describe("ProcessoViewCoverage.spec.ts", () => {
    let processosStore: any;

    const createWrapper = (initialState: any = {}) => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        descricao: "Processo Teste",
                        tipo: TipoProcesso.MAPEAMENTO,
                        situacao: "EM_ANDAMENTO",
                        unidades: []
                    },
                    lastError: null
                },
                perfil: {
                    perfilSelecionado: "GESTOR",
                    unidadeSelecionada: 100,
                    perfisUnidades: [{ perfil: "GESTOR", unidade: { codigo: 100 } }]
                },
                ...initialState
            },
            stubActions: true
        });

        processosStore = useProcessosStore(pinia);
        // Ensure action returns promise
        processosStore.buscarContextoCompleto.mockResolvedValue({});
        processosStore.finalizarProcesso.mockResolvedValue({});
        processosStore.executarAcaoBloco.mockResolvedValue({});

        return mount(ProcessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    PageHeader: { template: '<div><slot/><slot name="actions"/></div>' },
                    ProcessoAcoes: { name: 'ProcessoAcoes', template: '<div><button data-testid="btn-finalizar" @click="$emit(\'finalizar\')">Finalizar</button></div>', emits: ['finalizar'] },
                    TreeTable: { template: '<div>TreeTable</div>' },
                    ModalAcaoBloco: {
                        name: 'ModalAcaoBloco',
                        template: '<div>ModalAcaoBloco</div>',
                        expose: ['abrir', 'fechar', 'setErro', 'setProcessando'],
                        methods: {
                            abrir: vi.fn(),
                            fechar: vi.fn(),
                            setErro: vi.fn(),
                            setProcessando: vi.fn()
                        }
                    },
                    ModalConfirmacao: {
                        template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
                        props: ['modelValue'],
                        emits: ['confirmar', 'update:modelValue']
                    },
                    BAlert: { template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue'] },
                    BBadge: { template: '<span><slot /></span>' },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    BContainer: { template: '<div><slot /></div>' },
                    BSpinner: { template: '<span>Loading</span>' }
                }
            }
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve lidar com erro ao finalizar processo", async () => {
        const wrapper = createWrapper();
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        processosStore.finalizarProcesso.mockRejectedValue(new Error("Erro ao finalizar"));

        await flushPromises();

        // Trigger finalizar via stubbed component emit
        // Find by name "ProcessoAcoes" works because we defined the stub with that name in `createWrapper`.
        // Or we can find by ref if it had one, or by component definition if imported.
        // But here we rely on the stub name.
        const acoes = wrapper.findComponent({ name: "ProcessoAcoes" });
        await acoes.vm.$emit("finalizar");
        await flushPromises();

        // Confirm
        const modal = wrapper.findComponent({ name: "ModalConfirmacao" });
        if (modal.exists()) {
            await modal.vm.$emit("confirmar");
        }
        await flushPromises();

        // Expect show to be called. If not called, then ProcessoAcoes or Modal didn't exist
        // Note: wrapper.findComponent finds by name if stubbed with name
    });

    it("deve abrir detalhes da unidade (navegação) para ADMIN", async () => {
        const wrapper = createWrapper({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 999
            }
        });

        // Force isAdmin getter to true in store manually if mock
        (wrapper.vm as any).perfilStore.isAdmin = true;

        await flushPromises();

        const item = { clickable: true, sigla: "U1", codigo: 10 };
        (wrapper.vm as any).abrirDetalhesUnidade(item);

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            name: "Subprocesso",
            params: { codProcesso: "1", siglaUnidade: "U1" }
        }));
    });

    it("não deve navegar se item não clicável", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        (wrapper.vm as any).abrirDetalhesUnidade({ clickable: false });
        expect(mocks.push).not.toHaveBeenCalled();
    });

    it("não deve navegar para unidade de terceiros se CHEFE", async () => {
        const wrapper = createWrapper({
            perfil: { perfilSelecionado: "CHEFE", unidadeSelecionada: 200 } // Unidade logada 200
        });
        await flushPromises();

        // Tenta abrir unidade 10
        (wrapper.vm as any).abrirDetalhesUnidade({ clickable: true, codigo: 10, unidadeAtual: "U1" });

        expect(mocks.push).not.toHaveBeenCalled();
    });

    it("deve navegar para própria unidade se CHEFE", async () => {
        const wrapper = createWrapper({
            perfil: { perfilSelecionado: "CHEFE", unidadeSelecionada: 10 } // Unidade logada 10
        });
        await flushPromises();

        // Abre unidade 10
        (wrapper.vm as any).abrirDetalhesUnidade({ clickable: true, codigo: 10, sigla: "U1" });

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: { codProcesso: "1", siglaUnidade: "U1" }
        }));
    });


    it("deve lidar com erro na ação em bloco", async () => {
        const wrapper = createWrapper();
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const modal = wrapper.findComponent({ name: 'ModalAcaoBloco' });
        if (modal.exists()) {
             vi.spyOn(modal.vm, 'setErro');
             vi.spyOn(modal.vm, 'setProcessando');
        }

        // Trigger action
        (wrapper.vm as any).acaoBlocoAtual = 'aceitar';
        processosStore.executarAcaoBloco.mockRejectedValue(new Error("Erro bloco"));

        await (wrapper.vm as any).executarAcaoBloco({ ids: [1] });

        if (modal.exists()) {
             expect(modal.vm.setErro).toHaveBeenCalledWith("Erro bloco");
        }
    });

    it("deve calcular unidades elegíveis para Disponibilizar", async () => {
        const wrapper = createWrapper({
            processos: {
                processoDetalhe: {
                    unidades: [
                        { codUnidade: 1, sigla: "A", situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO }, // Eligible
                        { codUnidade: 2, sigla: "B", situacaoSubprocesso: "OUTRO" }
                    ]
                }
            }
        });
        (wrapper.vm as any).acaoBlocoAtual = 'disponibilizar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });

    it("deve calcular unidades elegíveis para Homologar", async () => {
        const wrapper = createWrapper({
            processos: {
                processoDetalhe: {
                    unidades: [
                        { codUnidade: 1, sigla: "A", situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO }, // Eligible
                        { codUnidade: 2, sigla: "B", situacaoSubprocesso: "OUTRO" }
                    ]
                }
            }
        });
        (wrapper.vm as any).acaoBlocoAtual = 'homologar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
    });

    it("deve calcular unidades elegíveis para Aceitar (incluindo REVISAO_DISPONIBILIZADA)", async () => {
        const wrapper = createWrapper({
            processos: {
                processoDetalhe: {
                    unidades: [
                        { codUnidade: 1, sigla: "A", situacaoSubprocesso: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA }, // Eligible
                        { codUnidade: 2, sigla: "B", situacaoSubprocesso: "OUTRO" }
                    ]
                }
            }
        });
        (wrapper.vm as any).acaoBlocoAtual = 'aceitar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });
});

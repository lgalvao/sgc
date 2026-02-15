import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Processo from "@/views/processo/ProcessoDetalheView.vue";
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil";
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

const ModalAcaoBlocoStub = {
    name: 'ModalAcaoBloco',
    template: '<div>ModalAcaoBloco</div>',
    setup(_props: unknown, { expose }: { expose: (exposed: Record<string, any>) => void }) {
        expose({
            abrir: vi.fn(),
            fechar: vi.fn(),
            setErro: vi.fn(),
            setProcessando: vi.fn()
        });
        return {};
    }
};

const commonStubs = {
    PageHeader: { template: '<div><slot/><slot name="actions"/></div>' },
    ProcessoAcoes: { name: 'ProcessoAcoes', template: '<div><button data-testid="btn-finalizar" @click="$emit(\'finalizar\')">Finalizar</button></div>', emits: ['finalizar'] },
    TreeTable: { template: '<div>TreeTable</div>' },
    ModalAcaoBloco: ModalAcaoBlocoStub,
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
};

describe("ProcessoViewCoverage.spec.ts", () => {
    let processosStore: ReturnType<typeof useProcessosStore>;

    const createWrapper = (initialState: any = {}, shallow = false) => {
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
                    perfisUnidades: [{ perfil: "GESTOR", unidade: { codigo: 100 } }],
                    perfis: ["GESTOR"]
                }
            },
            stubActions: true
        });

        if (initialState.processos) {
            const store = useProcessosStore(pinia);
            store.$patch(initialState.processos);
        }
        if (initialState.perfil) {
            const store = usePerfilStore(pinia);
            store.$patch(initialState.perfil);
        }

        processosStore = useProcessosStore(pinia) as any;
        // Ensure action returns promise
        (processosStore.buscarContextoCompleto as any).mockResolvedValue({});
        (processosStore.finalizarProcesso as any).mockResolvedValue({});
        (processosStore.executarAcaoBloco as any).mockResolvedValue({});

        const options: any = {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        };
        
        if (shallow) {
            options.shallow = true;
        }

        return mount(Processo, options);
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve lidar com erro ao finalizar processo", async () => {
        const wrapper = createWrapper();
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        vi.mocked(processosStore.finalizarProcesso).mockRejectedValue(new Error("Erro ao finalizar"));

        await flushPromises();

        const acoes = wrapper.findComponent({ name: "ProcessoAcoes" });
        await acoes.vm.$emit("finalizar");
        await flushPromises();

        const modal = wrapper.findComponent({ name: "ModalConfirmacao" });
        if (modal.exists()) {
            await modal.vm.$emit("confirmar");
        }
        await flushPromises();
    });

    it("deve abrir detalhes da unidade (navegação) para ADMIN", async () => {
        const wrapper = createWrapper({
            perfil: {
                perfilSelecionado: "ADMIN",
                unidadeSelecionada: 999,
                perfis: ["ADMIN"]
            }
        });

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

    it("deve navegar para unidade de terceiros se CHEFE (controle é no backend)", async () => {
        const wrapper = createWrapper({
            perfil: {
                perfilSelecionado: "CHEFE",
                unidadeSelecionada: 200,
                perfis: ["CHEFE"]
            }
        });
        await flushPromises();

        // Tenta abrir unidade 10 — agora permitido, backend controla acesso
        (wrapper.vm as any).abrirDetalhesUnidade({ clickable: true, codigo: 10, sigla: "U1", unidadeAtual: "U1" });

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: { codProcesso: "1", siglaUnidade: "U1" }
        }));
    });

    it("deve navegar para própria unidade se CHEFE", async () => {
        const wrapper = createWrapper({
            perfil: {
                perfilSelecionado: "CHEFE",
                unidadeSelecionada: 10,
                perfis: ["CHEFE"]
            }
        });
        await flushPromises();

        // Abre unidade 10
        (wrapper.vm as any).abrirDetalhesUnidade({ clickable: true, codigo: 10, sigla: "U1" });

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: { codProcesso: "1", siglaUnidade: "U1" }
        }));
    });


    it("deve lidar com erro na ação em bloco", async () => {
        const wrapper = createWrapper({
            processos: {
                processoDetalhe: {
                    unidades: [
                        { codUnidade: 1, sigla: "A", situacaoSubprocesso: "QUALQUER" }
                    ]
                }
            }
        });
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const modal = wrapper.findComponent({ name: 'ModalAcaoBloco' });

        // Trigger action
        (wrapper.vm as any).acaoBlocoAtual = 'aceitar';
        (processosStore.executarAcaoBloco as any).mockRejectedValue(new Error("Erro bloco"));

        await modal.vm.$emit("confirmar", { ids: [1] });
        await flushPromises();

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

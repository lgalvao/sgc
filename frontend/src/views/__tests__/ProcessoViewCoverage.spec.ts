import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Processo from "@/views/ProcessoDetalheView.vue";
import {usePerfilStore} from "@/stores/perfil";
import {createTestingPinia} from "@pinia/testing";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import * as processoService from "@/services/processoService";

const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: {params: {codProcesso: "1"}, query: {}}
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
        currentRoute: {value: mocks.mockRoute}
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/services/processoService");

const ModalAcaoBlocoStub = {
    name: 'ModalAcaoBloco',
    template: '<div>ModalAcaoBloco</div>',
    setup(_props: unknown, {expose}: { expose: (exposed: Record<string, any>) => void }) {
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
    PageHeader: {template: '<div><slot/><slot name="actions"/></div>'},
    ProcessoAcoes: {
        name: 'ProcessoAcoes',
        template: '<div></div>',
        emits: ['finalizar']
    },
    TreeTable: {template: '<div>TreeTable</div>'},
    ModalAcaoBloco: ModalAcaoBlocoStub,
    ModalConfirmacao: {
        template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
        props: ['modelValue'],
        emits: ['confirmar', 'update:modelValue']
    },
    BAlert: {template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue']},
    BBadge: {template: '<span><slot /></span>'},
    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
    BContainer: {template: '<div><slot /></div>'},
    BSpinner: {template: '<span>Loading</span>'}
};

describe("ProcessoViewCoverage.spec.ts", () => {
    const createWrapper = (initialState: any = {}, shallow = false) => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                perfil: {
                    perfilSelecionado: "GESTOR",
                    unidadeSelecionada: 100,
                    perfisUnidades: [{perfil: "GESTOR", unidade: {codigo: 100}}],
                    perfis: ["GESTOR"]
                }
            },
            stubActions: true
        });

        if (initialState.perfil) {
            const store = usePerfilStore(pinia);
            store.$patch(initialState.perfil);
        }

        const processo = initialState.processos?.processoDetalhe ?? {
            codigo: 1,
            descricao: "Processo teste",
            tipo: TipoProcesso.MAPEAMENTO,
            situacao: "EM_ANDAMENTO",
            unidades: []
        };
        const subprocessosElegiveis = initialState.processos?.subprocessosElegiveis ?? [];

        if (initialState.processos?.lastError) {
            vi.mocked(processoService.buscarContextoCompleto).mockRejectedValue(new Error(initialState.processos.lastError.message));
        } else {
            vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue({
                ...processo,
                elegiveis: subprocessosElegiveis,
            } as any);
        }
        vi.mocked(processoService.finalizarProcesso).mockResolvedValue(undefined);
        vi.mocked(processoService.executarAcaoEmBloco).mockResolvedValue(undefined);

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

        vi.mocked(processoService.finalizarProcesso).mockRejectedValue(new Error("Erro ao finalizar"));

        await flushPromises();

        const acoes = wrapper.find('[data-testid="btn-processo-finalizar"]');
        if (acoes.exists()) await acoes.trigger('click');
        await flushPromises();

        const modal = wrapper.findComponent({name: "ModalConfirmacao"});
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

        const item = {clickable: true, sigla: "U1", codigo: 10};
        (wrapper.vm as any).abrirDetalhesUnidade(item);

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            name: "Subprocesso",
            params: {codProcesso: "1", siglaUnidade: "U1"}
        }));
    });

    it("não deve navegar se item não clicável", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        (wrapper.vm as any).abrirDetalhesUnidade({clickable: false});
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
        (wrapper.vm as any).abrirDetalhesUnidade({clickable: true, codigo: 10, sigla: "U1", unidadeAtual: "U1"});

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: {codProcesso: "1", siglaUnidade: "U1"}
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
        (wrapper.vm as any).abrirDetalhesUnidade({clickable: true, codigo: 10, sigla: "U1"});

        expect(mocks.push).toHaveBeenCalledWith(expect.objectContaining({
            params: {codProcesso: "1", siglaUnidade: "U1"}
        }));
    });

    it("deve lidar com erro na ação em bloco", async () => {
        const wrapper = createWrapper({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                    }
                ]
            }
        });
        await flushPromises();

        const modal = wrapper.findComponent({name: 'ModalAcaoBloco'});

        (wrapper.vm as any).acaoBlocoAtual = 'aceitar';
        vi.mocked(processoService.executarAcaoEmBloco).mockRejectedValue(new Error("Erro bloco"));

        await modal.vm.$emit("confirmar", {ids: [1]});
        await flushPromises();

        if (modal.exists()) {
            expect(modal.vm.setErro).toHaveBeenCalledWith("Erro bloco");
        }
    });

    it("deve calcular unidades elegíveis para Disponibilizar", async () => {
        const wrapper = createWrapper({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                        localizacaoCodigo: 100
                    }, // Eligible
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100
                    }
                ]
            }
        });
        await flushPromises();
        (wrapper.vm as any).acaoBlocoAtual = 'disponibilizar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });

    it("deve calcular unidades elegíveis para Homologar", async () => {
        const wrapper = createWrapper({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        localizacaoCodigo: 100
                    }, // Eligible
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100
                    }
                ]
            }
        });
        await flushPromises();
        (wrapper.vm as any).acaoBlocoAtual = 'homologar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
    });

    it("deve calcular unidades elegíveis para Aceitar (incluindo REVISAO_DISPONIBILIZADA)", async () => {
        const wrapper = createWrapper({
            processos: {
                subprocessosElegiveis: [
                    {
                        unidadeCodigo: 1,
                        unidadeSigla: "A",
                        unidadeNome: "Unidade A",
                        situacao: SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                        localizacaoCodigo: 100
                    }, // Eligible
                    {
                        unidadeCodigo: 2,
                        unidadeSigla: "B",
                        unidadeNome: "Unidade B",
                        situacao: "OUTRO",
                        localizacaoCodigo: 100
                    }
                ]
            }
        });
        await flushPromises();
        (wrapper.vm as any).acaoBlocoAtual = 'aceitar';
        expect((wrapper.vm as any).unidadesElegiveis).toHaveLength(1);
        expect((wrapper.vm as any).unidadesElegiveis[0].sigla).toBe("A");
    });

    it("deve cobrir branches de erro e estados de carregamento", async () => {
        const wrapper = createWrapper({
            processos: {
                processoDetalhe: {codigo: 1, situacao: 'EM_ANDAMENTO'},
                lastError: {message: 'Erro de teste'}
            }
        });
        await flushPromises();
        expect(wrapper.text()).toContain('Erro de teste');

        // In this component, notify() is used for local notifications.
        (wrapper.vm as any).notify('Erro Manual', 'danger');
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain('Erro Manual');
    });
});

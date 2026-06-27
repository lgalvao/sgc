import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {defineComponent, ref} from "vue";
import Unidades from "@/views/UnidadesView.vue";
import * as unidadeService from "@/services/unidadeService";
import {TEXTOS} from "@/constants/textos";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const mockPush = vi.fn();

vi.mock("vue-router", () => ({
    useRouter: () => ({push: mockPush}),
    RouterLink: {
        name: "RouterLink",
        props: ["to"],
        template: "<a :href=\"typeof to === 'string' ? to : to.path\"><slot /></a>"
    }
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        mostrarDiagnosticoOrganizacional: {value: true}
    })
}));

const diagnosticoQueryMock = {
    data: ref<any>(null),
    isLoading: ref(false),
    error: ref<Error | null>(null),
};

vi.mock("@/composables/useDiagnosticoOrganizacionalQuery", () => ({
    useDiagnosticoOrganizacionalQuery: () => diagnosticoQueryMock,
    useInvalidacaoDiagnosticoOrganizacional: () => ({
        invalidarDiagnostico: vi.fn(),
    }),
}));

const unidadesQueryMock = {
    data: ref<any[]>([]),
    error: ref<Error | null>(null),
    isPending: ref(false),
    isLoading: ref(false),
    refetch: vi.fn(),
    iniciado: false,
};

vi.mock("@/composables/useUnidadesQuery", () => ({
    useUnidadesQuery: () => {
        if (!unidadesQueryMock.iniciado) {
            unidadesQueryMock.iniciado = true;
            void unidadesQueryMock.refetch();
        }
        return unidadesQueryMock;
    },
}));

vi.mock("@/services/unidadeService", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/services/unidadeService")>();
    return {
        ...actual,
        buscarTodasUnidades: vi.fn(),
        buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        }),
    };
});

describe("Unidades.vue", () => {
    const context = setupComponentTest();
    const expandAllMock = vi.fn();
    const collapseAllMock = vi.fn();

    const TreeTableStub = defineComponent({
        name: "TreeTable",
        props: {
            data: {type: Array, default: () => []},
            columns: {type: Array, default: () => []},
            title: {type: String, default: ""},
            hideHeaders: {type: Boolean, default: false},
            striped: {type: Boolean, default: false},
            hideControls: {type: Boolean, default: false},
        },
        emits: ["row-click"],
        setup(_, {expose}) {
            expose({
                expandAll: expandAllMock,
                collapseAll: collapseAllMock,
            });
            return {};
        },
        template: `
          <div data-testid="tree-table">
            <button
              v-if="data && data.length > 0"
              data-testid="tree-table-row-click"
              type="button"
              @click="$emit('row-click', data[0])"
            >
              Abrir
            </button>
          </div>
        `
    });

    beforeEach(() => {
        vi.clearAllMocks();
        mockPush.mockReset();
        expandAllMock.mockReset();
        diagnosticoQueryMock.data.value = null;
        diagnosticoQueryMock.isLoading.value = false;
        diagnosticoQueryMock.error.value = null;
        collapseAllMock.mockReset();
        unidadesQueryMock.data.value = [];
        unidadesQueryMock.error.value = null;
        unidadesQueryMock.isPending.value = false;
        unidadesQueryMock.isLoading.value = false;
        unidadesQueryMock.iniciado = false;
        unidadesQueryMock.refetch.mockImplementation(async () => {
            unidadesQueryMock.isPending.value = true;
            try {
                const data = await unidadeService.buscarTodasUnidades();
                unidadesQueryMock.data.value = data as any[];
                unidadesQueryMock.error.value = null;
                return {data};
            } catch (error) {
                unidadesQueryMock.error.value = error as Error;
                throw error;
            } finally {
                unidadesQueryMock.isPending.value = false;
            }
        });
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([]);
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        } as any);
    });

    const mockUnidades = [
        {
            codigo: 1,
            sigla: "ROOT",
            nome: "Raiz",
            filhas: [{codigo: 2, sigla: "DTI", nome: "Diretoria", filhas: []}]
        }
    ];

    const createWrapper = (serviceOverride: Partial<{
        unidades: any[],
        isLoading: boolean,
        error: string | null
    }> = {}) => {
        if (serviceOverride.error) {
            vi.mocked(unidadeService.buscarTodasUnidades).mockRejectedValueOnce(
                new Error(serviceOverride.error)
            );
            unidadesQueryMock.error.value = new Error(serviceOverride.error);
        } else if (serviceOverride.unidades !== undefined) {
            vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(serviceOverride.unidades as any);
            unidadesQueryMock.data.value = serviceOverride.unidades as any[];
        }

        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {},
                {
                    PageHeader: {
                        template: "<div><h1>Page header</h1><slot name='description' /><slot name='alerta' /><slot name='actions' /></div>"
                    },
                    AppAlertaTela: {
                        template: "<div><span>{{ mensagem }}</span><button @click=\"$emit('dismissed')\">Close</button></div>",
                        props: ["mensagem"],
                        emits: ["dismissed"],
                    },
                    TreeTable: TreeTableStub,
                    BContainer: {template: "<div><slot /></div>"},
                    BSpinner: {
                        name: "BSpinner",
                        template: "<div class='spinner'></div>",
                        props: ["variant", "label"]
                    }
                }
            )
        });
        return context.wrapper;
    };

    it("deve buscar unidades ao montar", async () => {
        createWrapper();
        await flushPromises();
        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
    });

    it("não deve disparar nova carga automaticamente após a carga inicial", async () => {
        const wrapper = createWrapper({unidades: mockUnidades});
        await flushPromises();
        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();

        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).not.toHaveBeenCalled();
    });

    it("deve recarregar unidades apenas por ação explícita do usuário", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();

        await wrapper.find('[data-testid="btn-unidades-recarregar"]').trigger("click");
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
    });

    it("não deve disparar uma segunda carga durante a primeira busca pendente", async () => {
        let resolver!: (valor: any[]) => void;
        vi.mocked(unidadeService.buscarTodasUnidades).mockReturnValueOnce(new Promise((resolve) => {
            resolver = resolve;
        }) as any);

        createWrapper();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalledTimes(1);

        resolver([]);
        await flushPromises();
    });

    it("deve exibir alerta com links para todas as unidades sem responsável", async () => {
        // Controla os dados do diagnóstico via mock da query (organizacaoStore foi removido)
        const diagMock = {
            possuiViolacoes: true,
            resumo: "Foram encontradas 3 inconsistencias.",
            quantidadeTiposViolacao: 2,
            quantidadeOcorrencias: 3,
            grupos: [
                {
                    tipo: "VW_USUARIO com titulo duplicado",
                    quantidadeOcorrencias: 2,
                    ocorrencias: ["titulo=1", "titulo=2"]
                },
                {
                    tipo: "Unidade sem responsável",
                    quantidadeOcorrencias: 2,
                    ocorrencias: ["sigla=43ª Z.E.", "sigla=45ª Z.E."]
                }
            ]
        };
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce([
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 43, sigla: "43ª Z.E.", nome: "Zona 43", filhas: []},
                    {codigo: 45, sigla: "45ª Z.E.", nome: "Zona 45", filhas: []}
                ]
            }
        ] as any);

        unidadesQueryMock.data.value = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 43, sigla: "43ª Z.E.", nome: "Zona 43", filhas: []},
                    {codigo: 45, sigla: "45ª Z.E.", nome: "Zona 45", filhas: []}
                ]
            }
        ] as any;

        diagnosticoQueryMock.data.value = diagMock;

        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {},
                {
                    PageHeader: {
                        template: "<div><h1>Page header</h1><slot name='description' /><slot name='alerta' /><slot name='actions' /></div>"
                    },
                    AppAlertaTela: {
                        template: "<div><span>{{ mensagem }}</span><button @click=\"$emit('dismissed')\">Close</button></div>",
                        props: ["mensagem"],
                        emits: ["dismissed"],
                    },
                    TreeTable: TreeTableStub,
                    BContainer: {template: "<div><slot /></div>"},
                    BAlert: {
                        name: "BAlert",
                        template: "<div><slot /></div>",
                        props: ["modelValue", "variant", "dismissible"]
                    },
                }
            )
        });
        await flushPromises();
        const wrapper = context.wrapper;

        expect(wrapper.text()).toContain("As unidades");
        expect(wrapper.text()).toContain("43ª Z.E.");
        expect(wrapper.text()).toContain("45ª Z.E.");
        expect(wrapper.text()).toContain("estão atualmente sem responsável");
        expect(wrapper.text()).toContain("não poderão participar de processos");
        expect(wrapper.text()).toContain("A responsabilidade deve ser definida externamente, no SGRH, ou por atribuição temporária no próprio sistema.");
        const links = wrapper.findAll('[data-testid^="link-unidade-sem-responsavel-"]');
        expect(links).toHaveLength(2);
        expect(links[0].text()).toContain("43ª Z.E.");
        expect(links[1].text()).toContain("45ª Z.E.");
    });

    it("deve exibir spinner durante carregamento", async () => {
        // Mock que nunca resolve para manter isLoading = true
        vi.mocked(unidadeService.buscarTodasUnidades).mockReturnValueOnce(new Promise(() => {
        }));
        const wrapper = createWrapper();
        // Aguarda um tick para o DOM refletir isLoading = true
        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="pagina-carregando"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain(TEXTOS.unidades.CARREGANDO_ARVORE);
    });

    it("deve exibir erro se houver erro ao carregar", async () => {
        const wrapper = createWrapper({error: "Erro de API"});
        await flushPromises();
        expect(wrapper.text()).toContain("Erro de API");
    });

    it("deve limpar erro ao fechar o alerta", async () => {
        const wrapper = createWrapper({error: "Erro de API"});
        await flushPromises();
        expect(wrapper.text()).toContain("Erro de API");

        const closeButton = wrapper.findAll("button").find((item) => item.text() === "Close");
        expect(closeButton).toBeDefined();
        await closeButton!.trigger("click");
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).not.toContain("Erro de API");
    });

    it("deve exibir TreeTable quando houver unidades", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(mockUnidades as any);
        const wrapper = createWrapper();
        await flushPromises();
        const arvore = wrapper.findComponent({name: 'TreeTable'});
        expect(arvore.exists()).toBe(true);
        expect(arvore.props("hideControls")).toBe(true);
        expect(arvore.props("hideHeaders")).toBe(true);
        expect(arvore.props("striped")).toBe(false);
        expect(arvore.props("data")).toEqual([
            {
                codigo: 2,
                sigla: "DTI",
                unidade: "DTI - Diretoria",
                tipo: undefined,
                children: [],
                expanded: false,
                clickable: true,
            }
        ]);
    });

    it("deve filtrar unidades por sigla", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce([
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 2, sigla: "DTI", nome: "Diretoria", filhas: []},
                    {codigo: 3, sigla: "SGP", nome: "Gestao de Pessoas", filhas: []}
                ]
            }
        ] as any);

        const wrapper = createWrapper();
        await flushPromises();

        const busca = wrapper.find('[data-testid="inp-arvore-busca"]');
        await busca.setValue("dti");
        await flushPromises();

        const arvore = wrapper.findComponent({name: 'TreeTable'});
        expect(arvore.props("data")).toEqual([
            {
                codigo: 2,
                sigla: "DTI",
                unidade: "DTI - Diretoria",
                tipo: undefined,
                children: [],
                expanded: false,
                clickable: true,
            }
        ]);
    });

    it("deve desconsiderar espaços antes e depois no termo de busca", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce([
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 2, sigla: "DTI", nome: "Diretoria", filhas: []},
                    {codigo: 3, sigla: "SGP", nome: "Gestao de Pessoas", filhas: []}
                ]
            }
        ] as any);

        const wrapper = createWrapper();
        await flushPromises();

        const busca = wrapper.find('[data-testid="inp-arvore-busca"]');
        await busca.setValue("  dti  ");
        await flushPromises();

        const arvore = wrapper.findComponent({name: 'TreeTable'});
        expect(arvore.props("data")).toHaveLength(1);
        expect(arvore.props("data")[0].sigla).toBe("DTI");
    });

    it("deve mostrar toda a subárvore quando uma unidade pai coincide com a busca por sigla", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce([
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {
                        codigo: 10,
                        sigla: "PAI",
                        nome: "Unidade Pai",
                        filhas: [
                            {codigo: 11, sigla: "FILHO1", nome: "Filho 1", filhas: []},
                            {codigo: 12, sigla: "FILHO2", nome: "Filho 2", filhas: []}
                        ]
                    }
                ]
            }
        ] as any);

        const wrapper = createWrapper();
        await flushPromises();

        const busca = wrapper.find('[data-testid="inp-arvore-busca"]');
        await busca.setValue("pai");
        await flushPromises();

        const arvore = wrapper.findComponent({name: 'TreeTable'});
        const data = arvore.props("data") as any[];
        
        expect(data).toHaveLength(1);
        expect(data[0].sigla).toBe("PAI");
        // Se o bug existir, children estará vazio porque FILHO1 e FILHO2 não contêm "pai"
        expect(data[0].children).toHaveLength(2);
        expect(data[0].children[0].sigla).toBe("FILHO1");
        expect(data[0].children[1].sigla).toBe("FILHO2");
    });

    it("deve exibir mensagem quando não houver unidades", async () => {
        const wrapper = createWrapper({unidades: []});
        await flushPromises();
        expect(wrapper.text()).toContain(TEXTOS.unidades.EMPTY_TITLE);
        expect(wrapper.text()).toContain(TEXTOS.unidades.EMPTY_DESCRIPTION);
        expect(wrapper.findComponent({name: 'TreeTable'}).exists()).toBe(false);
    });

    it("deve lidar com erro sem mensagem", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockRejectedValueOnce({});
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("Erro desconhecido ou não mapeado pela aplicação.");
    });

    it("deve recarregar ao clicar no botão de recarregar", async () => {
        const wrapper = createWrapper({unidades: []});
        await flushPromises();
        const recarregarBtn = wrapper.find('[data-testid="btn-unidades-recarregar"]');

        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();
        await recarregarBtn.trigger("click");
        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
    });

    it("deve navegar para o detalhe da unidade ao clicar em uma linha", async () => {
        const unidades = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [{codigo: 2, sigla: "DTI", nome: "Diretoria", filhas: []}]
            }
        ];
        const wrapper = createWrapper({unidades});
        await flushPromises();

        await wrapper.find('[data-testid="tree-table-row-click"]').trigger("click");

        expect(mockPush).toHaveBeenCalledWith({path: "/unidade/2"});
    });

    it("deve acionar expandir e recolher pelos botões do cabeçalho", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(mockUnidades as any);
        const wrapper = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-unidades-expandir-todas"]').trigger("click");
        await wrapper.find('[data-testid="btn-unidades-recolher-todas"]').trigger("click");

        expect(expandAllMock).toHaveBeenCalled();
        expect(collapseAllMock).toHaveBeenCalled();
    });

});

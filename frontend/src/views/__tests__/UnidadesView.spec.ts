import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {defineComponent} from "vue";
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
        collapseAllMock.mockReset();
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
        } else if (serviceOverride.unidades !== undefined) {
            vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(serviceOverride.unidades as any);
        }

        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {},
                {
                    PageHeader: {
                        template: "<div><h1>Page header</h1><slot name='description' /><slot name='actions' /></div>"
                    },
                    TreeTable: TreeTableStub,
                    BContainer: {template: "<div><slot /></div>"},
                    BAlert: {
                        name: "BAlert",
                        template: "<div><slot /><button @click=\"$emit('dismissed')\">Close</button></div>",
                        props: ["modelValue", "variant", "dismissible"]
                    },
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

    it("não deve recarregar unidades ao reativar a view em keepAlive quando os dados locais ainda estão válidos", async () => {
        const wrapper = createWrapper({unidades: mockUnidades});
        await flushPromises();
        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockClear();

        const hooks = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).not.toHaveBeenCalled();
        // buscarDiagnosticoOrganizacional NÃO deve ser chamado novamente — cache de sessão ativo
        expect(unidadeService.buscarDiagnosticoOrganizacional).not.toHaveBeenCalled();
    });

    it("deve recarregar unidades ao reativar a view em keepAlive quando não houver dados locais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();

        const hooks = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
    });

    it("deve exibir alerta com links para todas as unidades sem responsável", async () => {
        // Pré-popula a organizacaoStore via initialState (evita dependência da chamada HTTP)
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

        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {
                    organizacao: {diagnostico: diagMock, erroDiagnostico: null, carregado: true}
                },
                {
                    PageHeader: {
                        template: "<div><h1>Page header</h1><slot name='description' /><slot name='actions' /></div>"
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
        const links = wrapper.findAll('a[href^="/unidade/"]');
        expect(links).toHaveLength(2);
        expect(links[0].attributes("href")).toBe("/unidade/43");
        expect(links[1].attributes("href")).toBe("/unidade/45");
    });

    it("deve exibir spinner durante carregamento", async () => {
        // Mock que nunca resolve para manter isLoading = true
        vi.mocked(unidadeService.buscarTodasUnidades).mockReturnValueOnce(new Promise(() => {
        }));
        const wrapper = createWrapper();
        // Aguarda um tick para o DOM refletir isLoading = true
        await wrapper.vm.$nextTick();
        expect(wrapper.find(".spinner").exists()).toBe(true);
        expect(wrapper.text()).toContain(TEXTOS.unidades.CARREGANDO_ARVORE);
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
                expanded: true,
                clickable: true,
            }
        ]);
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
        expect(wrapper.text()).toContain(TEXTOS.comum.ERRO_OPERACAO);
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

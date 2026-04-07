import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Unidades from "@/views/UnidadesView.vue";
import * as unidadeService from "@/services/unidadeService";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const mockPush = vi.fn();

vi.mock("vue-router", () => ({
    useRouter: () => ({ push: mockPush }),
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        isAdmin: {value: true}
    })
}));

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(),
    buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
        possuiViolacoes: false,
        resumo: '',
        quantidadeTiposViolacao: 0,
        quantidadeOcorrencias: 0,
        grupos: [],
    }),
    mapUnidadesArray: vi.fn((arr) => arr || []),
}));

describe("Unidades.vue", () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
        mockPush.mockReset();
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([]);
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        } as any);
        vi.mocked(unidadeService.mapUnidadesArray).mockImplementation((arr) => arr || []);
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
            vi.mocked(unidadeService.mapUnidadesArray).mockReturnValueOnce(serviceOverride.unidades);
            vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(serviceOverride.unidades as any);
        }

        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {},
                {
                    PageHeader: {
                        template: "<div><h1>Page header</h1><slot name='description' /></div>"
                    },
                    TreeTable: {
                        name: "TreeTable",
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
                        `,
                        props: ["data", "columns", "title"]
                    },
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

    it("deve recarregar unidades ao reativar a view em keepAlive", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        vi.mocked(unidadeService.buscarTodasUnidades).mockClear();
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockClear();

        const hooks = ((wrapper.vm.$ as {a?: Array<() => unknown>} | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
        expect(unidadeService.buscarDiagnosticoOrganizacional).toHaveBeenCalled();
    });

    it("deve exibir alerta fixo com resumo do diagnostico para ADMIN", async () => {
        vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockResolvedValueOnce({
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
                    quantidadeOcorrencias: 1,
                    ocorrencias: ["sigla=43ª Z.E."]
                }
            ]
        } as any);

        const wrapper = createWrapper({
            perfil: {perfilSelecionado: "ADMIN"},
            unidades: mockUnidades
        } as any);
        await flushPromises();

        expect(wrapper.text()).toContain("Pendências organizacionais identificadas.");
        expect(wrapper.text()).toContain("VW_USUARIO com titulo duplicado: 2 ocorrência(s)");
        expect(wrapper.text()).toContain("Unidade sem responsável: 1 ocorrência(s)");
    });

    it("deve exibir spinner durante carregamento", async () => {
        // Mock que nunca resolve para manter isLoading = true
        vi.mocked(unidadeService.buscarTodasUnidades).mockReturnValueOnce(new Promise(() => {}));
        const wrapper = createWrapper();
        // Aguarda um tick para o DOM refletir isLoading = true
        await wrapper.vm.$nextTick();
        expect(wrapper.find(".spinner").exists()).toBe(true);
        expect(wrapper.text()).toContain("Carregando árvore de unidades...");
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

        const closeButton = wrapper.find("button");
        await closeButton.trigger("click");
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).not.toContain("Erro de API");
    });

    it("deve exibir TreeTable quando houver unidades", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(mockUnidades as any);
        vi.mocked(unidadeService.mapUnidadesArray).mockReturnValueOnce(mockUnidades as any);
        const wrapper = createWrapper();
        await flushPromises();
        const arvore = wrapper.findComponent({name: 'TreeTable'});
        expect(arvore.exists()).toBe(true);
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
        expect(wrapper.text()).toContain("Nenhuma unidade encontrada.");
        expect(wrapper.findComponent({name: 'TreeTable'}).exists()).toBe(false);
    });

    it("deve lidar com erro sem mensagem", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockRejectedValueOnce({});
        const wrapper = createWrapper();
        await flushPromises();
        expect(wrapper.text()).toContain("Falha ao realizar operação. Tente novamente.");
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

});

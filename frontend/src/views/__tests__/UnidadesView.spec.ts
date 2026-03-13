import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import Unidades from "@/views/UnidadesView.vue";
import * as unidadeService from "@/services/unidadeService";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

vi.mock("vue-router", () => ({
    useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(),
    mapUnidadesArray: vi.fn((arr) => arr || []),
}));

describe("Unidades.vue", () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([]);
        vi.mocked(unidadeService.mapUnidadesArray).mockImplementation((arr) => arr || []);
    });

    const mockUnidades = [
        {codigo: 1, sigla: "ROOT", nome: "Raiz", filhas: []}
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
                        template: "<div data-testid='tree-table'></div>",
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

    it("deve exibir ArvoreUnidades quando houver unidades", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValueOnce(mockUnidades as any);
        vi.mocked(unidadeService.mapUnidadesArray).mockReturnValueOnce(mockUnidades as any);
        const wrapper = createWrapper();
        await flushPromises();
        const arvore = wrapper.findComponent({name: 'ArvoreUnidades'});
        expect(arvore.exists()).toBe(true);
        expect(arvore.props("unidades")).toBeDefined();
    });

    it("deve exibir mensagem quando não houver unidades", async () => {
        const wrapper = createWrapper({unidades: []});
        await flushPromises();
        expect(wrapper.text()).toContain("Nenhuma unidade encontrada.");
        expect(wrapper.findComponent({name: 'ArvoreUnidades'}).exists()).toBe(false);
    });

});

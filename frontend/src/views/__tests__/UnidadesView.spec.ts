import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import Unidades from "@/views/Unidades.vue";
import {useUnidadesStore} from "@/stores/unidades";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("Unidades.vue", () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mockUnidades = [
        { codigo: 1, sigla: "ROOT", nome: "Raiz", filhas: [] }
    ];

    const createWrapper = (initialStateOverride = {}) => {
        context.wrapper = mount(Unidades, {
            ...getCommonMountOptions(
                {
                    unidades: {
                        unidades: [],
                        isLoading: false,
                        error: null,
                        ...initialStateOverride
                    }
                },
                {
                    PageHeader: {
                         template: "<div><h1>Page Header</h1><slot name='description' /></div>" 
                    },
                    ArvoreUnidades: { 
                        name: "ArvoreUnidades",
                        template: "<div data-testid='arvore-unidades'></div>",
                        props: ["unidades", "modelValue", "modoSelecao", "ocultarRaiz"]
                    },
                    BContainer: { template: "<div><slot /></div>" },
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

    it("deve buscar unidades ao montar", () => {
        createWrapper();
        const store = useUnidadesStore();
        expect(store.buscarTodasAsUnidades).toHaveBeenCalled();
    });

    it("deve exibir spinner durante carregamento", () => {
        const wrapper = createWrapper({ isLoading: true });
        expect(wrapper.find(".spinner").exists()).toBe(true);
        expect(wrapper.text()).toContain("Carregando árvore de unidades...");
    });

    it("deve exibir erro se houver erro no store", () => {
        const wrapper = createWrapper({ error: "Erro de API" });
        expect(wrapper.text()).toContain("Erro de API");
    });

    it("deve chamar clearError ao fechar o alerta", async () => {
        const wrapper = createWrapper({ error: "Erro de API" });
        const store = useUnidadesStore();
        
        const closeButton = wrapper.find("button");
        await closeButton.trigger("click");
        
        expect(store.clearError).toHaveBeenCalled();
    });

    it("deve exibir ArvoreUnidades quando houver unidades", () => {
        const wrapper = createWrapper({ unidades: mockUnidades });
        const arvore = wrapper.find("[data-testid='arvore-unidades']");
        expect(arvore.exists()).toBe(true);
        
        const arvoreComponent = wrapper.findComponent({ name: "ArvoreUnidades" });
        expect(arvoreComponent.props("unidades")).toEqual(mockUnidades);
        expect(arvoreComponent.props("modoSelecao")).toBe(false);
        expect(arvoreComponent.props("ocultarRaiz")).toBe(false);
    });

    it("deve exibir mensagem quando não houver unidades", () => {
        const wrapper = createWrapper({ unidades: [] });
        expect(wrapper.text()).toContain("Nenhuma unidade encontrada.");
        expect(wrapper.find("[data-testid='arvore-unidades']").exists()).toBe(false);
    });
});

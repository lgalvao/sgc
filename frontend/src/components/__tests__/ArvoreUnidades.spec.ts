import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ArvoreUnidades from "../ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";
import {checkA11y} from "@/test-utils/a11yTestHelpers";

describe("ArvoreUnidades.vue", () => {
    // Mock data
    const mockUnidades: Unidade[] = [
        {
            codigo: 1,
            sigla: "ROOT",
            nome: "Raiz",
            filhas: [
                {
                    codigo: 10,
                    sigla: "FILHA1",
                    nome: "Filha 1",
                    isElegivel: true,
                    filhas: [],
                    tipo: "OPERACIONAL"
                },
                {
                    codigo: 20,
                    sigla: "FILHA2",
                    nome: "Filha 2",
                    isElegivel: false,
                    filhas: [
                        {
                            codigo: 21,
                            sigla: "NETA1",
                            nome: "Neta 1",
                            isElegivel: true,
                            filhas: [],
                            tipo: "OPERACIONAL"
                        }
                    ],
                    tipo: "INTERMEDIARIA"
                }
            ],
            isElegivel: false,
            tipo: "ADMINISTRATIVA"
        }
    ];

    const createWrapper = (props = {}) => {
        return mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false, // Show root to make traversal easier to test
                ...props
            },
            global: {
                stubs: {
                    // We don't stub UnidadeTreeNode completely because we want to see it calling callbacks,
                    // but since it's recursive, shallowMount might be better?
                    // Actually, let's keep it real or use a smart stub.
                    // For coverage of ArvoreUnidades, we need to trigger its methods.
                    // Let's rely on the props passed to children.
                    UnidadeTreeNode: true
                }
            }
        });
    };

    it("deve renderizar as unidades", () => {
        const wrapper = createWrapper();
        expect(wrapper.findComponent({ name: "UnidadeTreeNode" }).exists()).toBe(true);
    });

    it("deve expandir unidades iniciais", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(true);
    });

    it("deve alternar expansão", async () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        // Toggle expand via prop function
        await root.props("onToggleExpand")(mockUnidades[0]);
        // Should be false now (it started true)
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(false);

        // Toggle again
        await root.props("onToggleExpand")(mockUnidades[0]);
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(true);
    });

    it("deve calcular estado de seleção (checked)", async () => {
        const wrapper = createWrapper({ modelValue: [10] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        // 10 is checked
        expect(root.props("isChecked")(10)).toBe(true);
        // 20 is not
        expect(root.props("isChecked")(20)).toBe(false);
    });

    it("deve selecionar todas as unidades elegíveis", async () => {
        const wrapper = createWrapper({ modoSelecao: true });

        await wrapper.find('button[aria-label="Selecionar todas as unidades elegíveis"]').trigger("click");

        // Emitted value should contain 10 and 21 (elegible ones)
        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        const selection = emitted![0][0] as number[];
        expect(selection).toContain(10);
        expect(selection).toContain(21);
        expect(selection).not.toContain(1); // Root not eligible
        expect(selection).not.toContain(20); // Filha2 not eligible
    });

    it("deve limpar seleção", async () => {
        const wrapper = createWrapper({ modelValue: [10, 21], modoSelecao: true });

        await wrapper.find('button[aria-label="Desmarcar todas as unidades"]').trigger("click");

        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toEqual([]);
    });

    it("deve lidar com toggle de unidade (selecionar)", async () => {
        const wrapper = createWrapper({ modelValue: [], modoSelecao: true });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        // Select node 20 (which should select descendants 21)
        const node20 = mockUnidades[0].filhas![1];
        await root.props("onToggle")(node20, true);

        const emitted = wrapper.emitted("update:modelValue");
        const selection = emitted![emitted!.length - 1][0] as number[];

        // 21 is eligible child of 20, so it should be selected
        expect(selection).toContain(21);
        // 20 itself is not eligible but logic might add it if we passed true?
        // Logic: if (u.isElegivel) newSelection.add
        // 20 is not eligible. So it shouldn't be in selection unless updateAncestors adds it?
        // updateAncestors checks if all children selected.
        expect(selection).not.toContain(20);
    });

    it("deve lidar com toggle de unidade (deselecionar)", async () => {
        const wrapper = createWrapper({ modelValue: [10], modoSelecao: true });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        const node10 = mockUnidades[0].filhas![0];
        await root.props("onToggle")(node10, false);

        const emitted = wrapper.emitted("update:modelValue");
        const selection = emitted![emitted!.length - 1][0] as number[];
        expect(selection).not.toContain(10);
    });

    it("deve calcular estado indeterminado", () => {
        const wrapper = createWrapper({ modelValue: [21] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        // Node 20 has child 21 selected. So it should be indeterminate?
        // Logic: descendentesElegiveis = [21]. descendentesSelecionadas = 1.
        // If equal, return true.
        // So node 20 should be "true" (selected) because all its eligible descendants are selected?
        const node20 = mockUnidades[0].filhas![1];
        expect(root.props("getEstadoSelecao")(node20)).toBe(true);

        // What if we have another child of 20 that is eligible but not selected?
        // Let's modify data for this test case specifically?
        // Or check Root. Root has 10 (not selected) and 20 (fully selected).
        // Root has descendants: 10, 21. Eligible: 10, 21.
        // Selected: 21.
        // So Root should be indeterminate.
        expect(root.props("getEstadoSelecao")(mockUnidades[0])).toBe("indeterminate");
    });

    it("isHabilitado deve retornar false se não elegível e sem filhas", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        const leafNotEligible: Unidade = { codigo: 99, sigla: "X", nome: "X", isElegivel: false, filhas: [] };
        expect(root.props("isHabilitado")(leafNotEligible)).toBe(false);
    });

    it("isHabilitado deve retornar true se elegível", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        const leafEligible: Unidade = { codigo: 99, sigla: "X", nome: "X", isElegivel: true, filhas: [] };
        expect(root.props("isHabilitado")(leafEligible)).toBe(true);
    });

    it("deve atualizar ancestrais corretamente (selecionar pai se todos filhos selecionados)", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 100,
                sigla: "PAI",
                nome: "Pai",
                isElegivel: true, // Pai elegível
                filhas: [
                    { codigo: 101, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL" },
                    { codigo: 102, sigla: "F2", nome: "F2", isElegivel: true, filhas: [], tipo: "OPERACIONAL" }
                ],
                tipo: "ADMINISTRATIVA"
            }
        ];
        
        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [101] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });
        
        // Select the other child
        const child2 = unidadesTeste[0].filhas![1];
        await root.props("onToggle")(child2, true);
        
        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];
        
        // Should contain children and parent (since parent is eligible and all children selected)
        expect(lastEmission).toContain(101);
        expect(lastEmission).toContain(102);
        expect(lastEmission).toContain(100);
    });

    it("não deve deselecionar pai INTEROPERACIONAL se filhos desmarcados", async () => {
        // Logic: if (parent.tipo !== 'INTEROPERACIONAL') selectionSet.delete(parent.codigo);
        // So if it IS INTEROPERACIONAL, it should NOT delete parent from selection?
        // Wait, if allChildrenSelected is false.
        
        const unidadesTeste: Unidade[] = [
            {
                codigo: 200,
                sigla: "INTER",
                nome: "Inter",
                isElegivel: true,
                tipo: "INTEROPERACIONAL",
                filhas: [
                    { codigo: 201, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL" }
                ]
            }
        ];
        
        // Start with parent selected (and child)
        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [200, 201] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });
        
        // Deselect child
        const child = unidadesTeste[0].filhas![0];
        await root.props("onToggle")(child, false);
        
        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];
        
        // Child should be gone
        expect(lastEmission).not.toContain(201);
        // Parent should stay because it is INTEROPERACIONAL?
        // Code: if (parent.tipo !== 'INTEROPERACIONAL') { selectionSet.delete(parent.codigo); }
        expect(lastEmission).toContain(200);
    });
    
    it("deve atualizar ancestrais: não selecionar pai se não for elegível, mesmo se todos filhos selecionados", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 300,
                sigla: "PAI_INELEGIVEL",
                nome: "Pai Inelegivel",
                isElegivel: false, // Inelegível
                filhas: [
                    { codigo: 301, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL" }
                ],
                tipo: "ADMINISTRATIVA"
            }
        ];
        
        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });
        
        const child = unidadesTeste[0].filhas![0];
        await root.props("onToggle")(child, true);
        
        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];
        
        expect(lastEmission).toContain(301);
        expect(lastEmission).not.toContain(300); // Parent should NOT be selected
    });

    it("selecionarTodas deve lidar com nós sem filhas definidas", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 400,
                sigla: "SOLITARIA",
                nome: "Solitaria",
                isElegivel: true,
                // filhas undefined
                tipo: "OPERACIONAL"
            }
        ];
        
        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [], modoSelecao: true });
        
        await wrapper.find('button[aria-label="Selecionar todas as unidades elegíveis"]').trigger("click");
        
        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toContain(400);
    });

    it("deve ser acessível", async () => {
        const wrapper = createWrapper();
        await checkA11y(wrapper.element as HTMLElement);
    });

    it("deve exibir a raiz quando ocultarRaiz é false", () => {
        const wrapper = createWrapper({ ocultarRaiz: false });
        expect((wrapper.vm as any).unidadesExibidas[0].codigo).toBe(1);
    });

    it("deve filtrar unidades", () => {
        const wrapper = createWrapper({
            filtrarPor: (u: Unidade) => u.sigla === "ROOT",
            ocultarRaiz: false
        });
        expect((wrapper.vm as any).unidadesExibidas).toHaveLength(1);
        expect((wrapper.vm as any).unidadesExibidas[0].sigla).toBe("ROOT");
    });

    it("não deve permitir seleção se modoSelecao é false", async () => {
        const wrapper = createWrapper({ modoSelecao: false });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        expect(root.props("isChecked")(10)).toBe(false);
        expect(root.props("isHabilitado")(mockUnidades[0].filhas![0])).toBe(false);
        expect(root.props("getEstadoSelecao")(mockUnidades[0].filhas![0])).toBe(false);

        // toggle should do nothing
        await root.props("onToggle")(mockUnidades[0].filhas![0], true);
        expect(wrapper.emitted("update:modelValue")).toBeFalsy();
    });

    it("deve atualizar unidadesSelecionadasLocal quando modelValue muda", async () => {
        const wrapper = createWrapper({ modelValue: [10] });
        await wrapper.setProps({ modelValue: [20] });
        expect((wrapper.vm as any).unidadesSelecionadasLocal).toContain(20);
    });

    it("deve atualizar expandedUnits quando unidades muda", async () => {
        const wrapper = createWrapper();
        const novasUnidades = [{ codigo: 999, sigla: 'N', nome: 'N' }];
        await wrapper.setProps({ unidades: novasUnidades });
        expect((wrapper.vm as any).isExpanded(novasUnidades[0])).toBe(true);
    });

    it("deve calcular estado de seleção para INTEROPERACIONAL", () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 500,
                sigla: "INTER",
                nome: "Inter",
                isElegivel: true,
                tipo: "INTEROPERACIONAL",
                filhas: [
                    { codigo: 501, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL" },
                    { codigo: 502, sigla: "F2", nome: "F2", isElegivel: true, filhas: [], tipo: "OPERACIONAL" }
                ]
            }
        ];

        // INTEROPERACIONAL self selected but no children selected -> should return true
        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [500] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });
        expect(root.props("getEstadoSelecao")(unidadesTeste[0])).toBe(true);

        // INTEROPERACIONAL not selected but some children selected -> indeterminate
        const wrapper2 = createWrapper({ unidades: unidadesTeste, modelValue: [501] });
        const root2 = wrapper2.findComponent({ name: "UnidadeTreeNode" });
        expect(root2.props("getEstadoSelecao")(unidadesTeste[0])).toBe("indeterminate");
    });

    it("deve deselecionar pai não INTEROPERACIONAL se um filho for desmarcado", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 600,
                sigla: "NORMAL",
                nome: "Normal",
                isElegivel: true,
                tipo: "OPERACIONAL",
                filhas: [
                    { codigo: 601, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL" }
                ]
            }
        ];

        const wrapper = createWrapper({ unidades: unidadesTeste, modelValue: [600, 601] });
        const root = wrapper.findComponent({ name: "UnidadeTreeNode" });

        await root.props("onToggle")(unidadesTeste[0].filhas![0], false);
        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted![emitted!.length - 1][0]).not.toContain(600);
    });

    it("deve cobrir watcher de modelValue quando não há mudança real", async () => {
        const wrapper = createWrapper({ modelValue: [10] });
        await wrapper.setProps({ modelValue: [10] });
        expect((wrapper.vm as any).unidadesSelecionadasLocal).toEqual([10]);
    });

    it("deve lidar com unidades sem propriedade filhas", async () => {
        const unidadesSemFilhas: Unidade[] = [
            { codigo: 700, sigla: "SOLO", nome: "Solo", isElegivel: true } as any
        ];
        const wrapper = createWrapper({ unidades: unidadesSemFilhas, modelValue: [700] });
        expect((wrapper.vm as any).isHabilitado(unidadesSemFilhas[0])).toBe(true);
    });
});

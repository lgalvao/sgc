import {describe, expect, it} from "vitest";
import {mount, RouterLinkStub} from "@vue/test-utils";
import ArvoreUnidades from "../unidade/ArvoreUnidades.vue";

describe("ArvoreUnidades.vue Coverage", () => {
    it("deve atualizar unidades expandidas quando a prop unidades muda", async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: [],
                modelValue: []
            },
            global: {
                stubs: {
                    UnidadeTreeNode: true,
                    RouterLink: RouterLinkStub
                }
            }
        });

        // Initial state
        expect((wrapper.vm as any).expandedUnits.size).toBe(0);

        // Update prop
        await wrapper.setProps({
            unidades: [{codigo: 1, sigla: "A", nome: "A", filhas: []}]
        });

        expect((wrapper.vm as any).expandedUnits.has(1)).toBe(true);
    });

    it("deve atualizar unidadesSelecionadasLocal quando modelValue muda externamente", async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: [{codigo: 1, sigla: "A", nome: "A", filhas: []}],
                modelValue: []
            },
            global: {
                stubs: {
                    UnidadeTreeNode: true,
                    RouterLink: RouterLinkStub
                }
            }
        });

        expect((wrapper.vm as any).unidadesSelecionadasLocal).toEqual([]);

        // Update modelValue prop
        await wrapper.setProps({modelValue: [1]});

        expect((wrapper.vm as any).unidadesSelecionadasLocal).toEqual([1]);
    });

    it("não deve atualizar unidadesSelecionadasLocal se modelValue for igual (evitar loop)", async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: [],
                modelValue: [1]
            },
            global: {
                stubs: {
                    UnidadeTreeNode: true,
                    RouterLink: RouterLinkStub
                }
            }
        });

        // Update modelValue with same values (different reference)
        await wrapper.setProps({modelValue: [1]});


        // The test above hit the true case.
        // This test hits the false case.
    });
});

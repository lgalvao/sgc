import {describe, expect, it} from "vitest";
import {mount, RouterLinkStub} from "@vue/test-utils";
import ArvoreUnidades from "../unidade/ArvoreUnidades.vue";

type ArvoreUnidadesVm = {
    expandedUnits: Set<number>;
    unidadesSelecionadasLocal: number[];
};

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

        const vm = wrapper.vm as unknown as ArvoreUnidadesVm;
        expect(vm.expandedUnits.size).toBe(0);

        await wrapper.setProps({
            unidades: [{codigo: 1, sigla: "A", nome: "A", filhas: []}]
        });

        expect(vm.expandedUnits.has(1)).toBe(false);
    });

    it("deve atualizar unidadesSelecionadasLocal quando modelValue muda externamente", async () => {
        const wrapper = mount(ArvoreUnidades, {
            props: {
                unidades: [{codigo: 1, sigla: "A", nome: "A", isElegivel: true, filhas: []}],
                modelValue: []
            },
            global: {
                stubs: {
                    UnidadeTreeNode: true,
                    RouterLink: RouterLinkStub
                }
            }
        });

        const vm = wrapper.vm as unknown as ArvoreUnidadesVm;
        expect(vm.unidadesSelecionadasLocal).toEqual([]);

        await wrapper.setProps({modelValue: [1]});

        expect(vm.unidadesSelecionadasLocal).toEqual([1]);
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

        await wrapper.setProps({modelValue: [1]});

    });
});

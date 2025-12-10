import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import UnidadeTreeItem from "@/components/UnidadeTreeItem.vue";
import type {Unidade} from "@/types/tipos";

describe("UnidadeTreeItem.vue", () => {
    const mockUnidade: Unidade = {
        codigo: 1,
        sigla: "SEDE",
        nome: "Sede",
        filhas: []
    };

    const mockFilha: Unidade = {
        codigo: 2,
        sigla: "DEP",
        nome: "Departamento",
        filhas: []
    };

    const mockUnidadeComFilha: Unidade = {
        ...mockUnidade,
        filhas: [mockFilha]
    };

    const isChecked = vi.fn();
    const toggleUnidade = vi.fn();
    const isIndeterminate = vi.fn();

    const createWrapper = (unidade: Unidade) => mount(UnidadeTreeItem, {
        props: {
            unidade,
            isChecked,
            toggleUnidade,
            isIndeterminate
        }
    });

    it("deve renderizar unidade corretamente", () => {
        isChecked.mockReturnValue(false);
        isIndeterminate.mockReturnValue(false);

        const wrapper = createWrapper(mockUnidade);

        expect(wrapper.text()).toContain("SEDE");
        expect(wrapper.text()).toContain("Sede");
        expect(wrapper.find('input[type="checkbox"]').exists()).toBe(true);
    });

    it("deve chamar toggleUnidade ao clicar no checkbox", async () => {
        const wrapper = createWrapper(mockUnidade);
        const checkbox = wrapper.find('input[type="checkbox"]');

        await checkbox.trigger('change'); // Native checkbox emits change

        expect(toggleUnidade).toHaveBeenCalledWith(mockUnidade);
    });

    it("deve renderizar filhas recursivamente", () => {
        const wrapper = createWrapper(mockUnidadeComFilha);

        // Verifica se renderizou 2 checkboxes (pai e filha)
        const checkboxes = wrapper.findAll('input[type="checkbox"]');
        expect(checkboxes).toHaveLength(2);

        expect(wrapper.text()).toContain("DEP");
    });

    it("deve aplicar estado indeterminate", () => {
        isIndeterminate.mockReturnValue(true);
        const wrapper = createWrapper(mockUnidade);

        const checkbox = wrapper.find('input[type="checkbox"]');
        expect(checkbox.element.indeterminate).toBe(true);
    });
});

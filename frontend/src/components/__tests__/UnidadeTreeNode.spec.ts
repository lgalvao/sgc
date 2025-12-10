import {mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import UnidadeTreeNode from "@/components/UnidadeTreeNode.vue";
import type {Unidade} from "@/types/tipos";

describe("UnidadeTreeNode.vue", () => {
    const mockUnidade: Unidade = {
        codigo: 1,
        sigla: "TESTE",
        nome: "Unidade Teste",
        filhas: []
    };

    const BFormCheckboxStub = {
        name: 'BFormCheckbox',
        template: `
            <div class="b-form-checkbox-stub">
                <input type="checkbox" 
                       :checked="modelValue" 
                       @change="$emit('update:modelValue', $event.target.checked)" 
                />
                <slot />
            </div>
        `,
        props: ['modelValue', 'indeterminate', 'disabled'],
        emits: ['update:modelValue']
    };

    const defaultProps = {
        unidade: mockUnidade,
        isChecked: vi.fn(),
        getEstadoSelecao: vi.fn().mockReturnValue(false),
        isExpanded: vi.fn().mockReturnValue(false),
        isHabilitado: vi.fn().mockReturnValue(true),
        onToggle: vi.fn(),
        onToggleExpand: vi.fn(),
    };

    const mountOptions = {
        global: {
            stubs: {
                BFormCheckbox: BFormCheckboxStub
            }
        }
    };

    it("deve renderizar a sigla da unidade", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: defaultProps,
            ...mountOptions
        });
        expect(wrapper.text()).toContain("TESTE");
    });

    it("deve renderizar checkbox desmarcado por padrão", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: defaultProps,
            ...mountOptions
        });
        const checkbox = wrapper.find('.b-form-checkbox-stub input');
        expect((checkbox.element as HTMLInputElement).checked).toBe(false);
    });

    it("deve renderizar checkbox marcado quando getEstadoSelecao retorna true", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                getEstadoSelecao: vi.fn().mockReturnValue(true)
            },
            ...mountOptions
        });
        const checkbox = wrapper.find('.b-form-checkbox-stub input');
        expect((checkbox.element as HTMLInputElement).checked).toBe(true);
    });

    it("deve renderizar checkbox indeterminado quando getEstadoSelecao retorna 'indeterminate'", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                getEstadoSelecao: vi.fn().mockReturnValue("indeterminate")
            },
            ...mountOptions
        });
        // We can't check indeterminate property easily on the stub unless we bind it to the DOM element property
        // or check the prop passed to the stub component.
        const checkboxComponent = wrapper.findComponent(BFormCheckboxStub);
        expect(checkboxComponent.props('indeterminate')).toBe(true);
    });

    it("deve chamar onToggle ao clicar no checkbox", async () => {
        const onToggle = vi.fn();
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                onToggle
            },
            ...mountOptions
        });

        const checkbox = wrapper.find('.b-form-checkbox-stub input');
        await checkbox.setValue(true);

        expect(onToggle).toHaveBeenCalledWith(mockUnidade, true);
    });

    it("deve renderizar expansor se tiver filhas", () => {
        const unidadeComFilhas = {...mockUnidade, filhas: [{codigo: 2, sigla: "FILHA", nome: "Filha", filhas: []}]};
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                unidade: unidadeComFilhas
            },
            ...mountOptions
        });

        expect(wrapper.find('.expansor').exists()).toBe(true);
    });

    it("não deve renderizar expansor se não tiver filhas", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: defaultProps,
            ...mountOptions
        });
        expect(wrapper.find('.expansor').exists()).toBe(false);
        expect(wrapper.find('.expansor-placeholder').exists()).toBe(true);
    });

    it("deve chamar onToggleExpand ao clicar no expansor", async () => {
        const unidadeComFilhas = {...mockUnidade, filhas: [{codigo: 2, sigla: "FILHA", nome: "Filha", filhas: []}]};
        const onToggleExpand = vi.fn();
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                unidade: unidadeComFilhas,
                onToggleExpand
            },
            ...mountOptions
        });

        await wrapper.find('.expansor').trigger('click');
        expect(onToggleExpand).toHaveBeenCalledWith(unidadeComFilhas);
    });

    it("deve renderizar filhos recursivamente quando expandido", () => {
        const unidadeComFilhas = {
            ...mockUnidade,
            filhas: [{codigo: 2, sigla: "FILHA_TESTE", nome: "Filha", filhas: []}]
        };
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                unidade: unidadeComFilhas,
                isExpanded: vi.fn().mockReturnValue(true) // Expandido
            },
            ...mountOptions
        });

        expect(wrapper.text()).toContain("FILHA_TESTE");
        const children = wrapper.findAllComponents(UnidadeTreeNode);
        expect(children.length).toBe(1);
    });

    it("não deve renderizar filhos quando não expandido", () => {
        const unidadeComFilhas = {
            ...mockUnidade,
            filhas: [{codigo: 2, sigla: "FILHA_TESTE", nome: "Filha", filhas: []}]
        };
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                unidade: unidadeComFilhas,
                isExpanded: vi.fn().mockReturnValue(false) // Colapsado
            },
            ...mountOptions
        });

        expect(wrapper.text()).not.toContain("FILHA_TESTE");
    });

    it("deve estar desabilitado se isHabilitado retornar false", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                isHabilitado: vi.fn().mockReturnValue(false)
            },
            ...mountOptions
        });

        // Check prop on stub
        expect(wrapper.findComponent(BFormCheckboxStub).props('disabled')).toBe(true);
        expect(wrapper.find('.unidade-label').classes()).toContain('text-muted');
    });
});

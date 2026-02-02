import {mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import UnidadeTreeNode from "@/components/UnidadeTreeNode.vue";
import type {Unidade} from "@/types/tipos";

describe("UnidadeTreeNodeCoverage.spec.ts", () => {
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

    const vBTooltip = {
        mounted: vi.fn(),
        updated: vi.fn(),
        unmounted: vi.fn(),
        getSSRProps: () => ({})
    };

    const mountOptions = {
        global: {
            stubs: {
                BFormCheckbox: BFormCheckboxStub
            },
            directives: {
                'b-tooltip': vBTooltip
            }
        }
    };

    it("deve aplicar estilo de cursor 'help' quando desabilitado", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                isHabilitado: vi.fn().mockReturnValue(false)
            },
            ...mountOptions
        });

        const label = wrapper.find('.unidade-label');
        // Vue 3 style binding might normalize styles, so we check for presence
        expect(label.attributes('style')).toContain('cursor: help');
    });

    it("não deve aplicar estilo de cursor 'help' quando habilitado", () => {
        const wrapper = mount(UnidadeTreeNode, {
            props: {
                ...defaultProps,
                isHabilitado: vi.fn().mockReturnValue(true)
            },
            ...mountOptions
        });

        const label = wrapper.find('.unidade-label');
        // When style is empty, it might be undefined or empty string
        const style = label.attributes('style');
        if (style) {
            expect(style).not.toContain('cursor: help');
        } else {
            expect(style).toBeUndefined();
        }
    });
});

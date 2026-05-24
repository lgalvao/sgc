import {describe, expect, it, vi, beforeEach} from 'vitest';
import {mount} from '@vue/test-utils';
import EditorTextoRico from '../EditorTextoRico.vue';

const mockEditorInstance = {
    destroy: vi.fn(),
    isActive: vi.fn().mockReturnValue(false),
    getHTML: vi.fn().mockReturnValue('<p>initial</p>'),
    isEmpty: false,
    commands: {
        setContent: vi.fn(),
    },
    chain: vi.fn().mockReturnValue({
        focus: vi.fn().mockReturnValue({
            toggleBold: vi.fn().mockReturnValue({ run: vi.fn() }),
            toggleItalic: vi.fn().mockReturnValue({ run: vi.fn() }),
            toggleBulletList: vi.fn().mockReturnValue({ run: vi.fn() }),
            toggleOrderedList: vi.fn().mockReturnValue({ run: vi.fn() }),
        })
    }),
    setEditable: vi.fn(),
    on: vi.fn(),
};

vi.mock('@tiptap/vue-3', async () => {
    const actual = await vi.importActual('@tiptap/vue-3') as any;
    return {
        ...actual,
        Editor: vi.fn().mockImplementation(function() { return mockEditorInstance }),
        EditorContent: { template: '<div><slot /></div>' }
    };
});

vi.mock('@tiptap/starter-kit', () => ({
    default: {
        configure: vi.fn().mockReturnThis(),
    }
}));

describe('EditorTextoRico.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders and destroys editor', () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '<p>test</p>' }
        });
        expect(wrapper.exists()).toBe(true);
        wrapper.unmount();
        expect(mockEditorInstance.destroy).toHaveBeenCalled();
    });

    it('handles disabled state', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '', desabilitado: true }
        });

        await wrapper.setProps({ desabilitado: false });
        expect(mockEditorInstance.setEditable).toHaveBeenCalledWith(true);
    });

    it('triggers actions when buttons are clicked', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '' }
        });
        const buttons = wrapper.findAll('button');
        await buttons[0].trigger('click'); // Bold
        expect(mockEditorInstance.chain).toHaveBeenCalled();
    });

    it('sincronizarPorDom sets modelValue', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: 'initial' }
        });
        
        const div = document.createElement('div');
        div.innerHTML = '<p>new content</p>';
        
        // Access private method via any
        const vm = wrapper.vm as any;
        vm.sincronizarPorDom({ target: div } as any);
        
        expect(wrapper.emitted('update:modelValue')![0]).toEqual(['<p>new content</p>']);
    });

    it('handles attrs properly (data-testid, class, style)', () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '' },
            attrs: {
                'data-testid': 'custom-editor',
                'class': 'custom-class',
                'style': 'color: red',
                'aria-required': 'true'
            }
        });
        
        expect(wrapper.classes()).toContain('custom-class');
        expect(wrapper.attributes('style')).toContain('color: red');
        
        const vm = wrapper.vm as any;
        expect(vm.dataTestid).toBe('custom-editor');
        expect(vm.ariaRequired).toBe('true');
    });
});
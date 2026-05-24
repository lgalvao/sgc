import {describe, expect, it, vi, beforeEach} from 'vitest';
import {mount, flushPromises} from '@vue/test-utils';
import {Editor} from '@tiptap/vue-3';
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

    it('handles attrs properly (data-testid, class, style, id, aria)', () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '', id: 'custom-id' },
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
        // Check id and aria-required in editor props implicitly via constructor mock if we had access, 
        // but here we just ensure the component renders with these props.
    });

    it('onUpdate handles empty editor and exceeding limit', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '', maximoCaracteres: 5 }
        });
        await flushPromises();
        mockEditorInstance.commands.setContent.mockClear();

        // Trigger onUpdate (simulated) for empty
        mockEditorInstance.isEmpty = true;
        mockEditorInstance.getHTML = vi.fn().mockReturnValue('<p></p>');
        const onUpdate = vi.mocked(Editor).mock.calls[0][0].onUpdate;
        onUpdate({ editor: mockEditorInstance });
        expect(wrapper.emitted('update:modelValue')![0]).toEqual(['']);

        // Exceed limit
        mockEditorInstance.isEmpty = false;
        mockEditorInstance.getHTML = vi.fn().mockReturnValue('<p>123456</p>');
        onUpdate({ editor: mockEditorInstance });
        
        // Use last emitted value for invalido
        const invalidoEmits = wrapper.emitted('update:invalido');
        expect(invalidoEmits![invalidoEmits!.length - 1]).toEqual([true]);
        expect(mockEditorInstance.commands.setContent).toHaveBeenCalled();
    });
it('onUpdate handles reducing content when over limit', async () => {
    const wrapper = mount(EditorTextoRico, {
        props: { modelValue: '<p>1234567</p>', maximoCaracteres: 5 }
    });
    await flushPromises();

    // Initial call from watch happens here
    expect(mockEditorInstance.commands.setContent).toHaveBeenCalled();

    mockEditorInstance.isEmpty = false;
    mockEditorInstance.getHTML = vi.fn().mockReturnValue('<p>123456</p>');

    const onUpdate = vi.mocked(Editor).mock.calls[0][0].onUpdate;

    // Clear here to be absolutely sure we only track calls from onUpdate
    mockEditorInstance.commands.setContent.mockClear();

    onUpdate({ editor: mockEditorInstance });

    expect(wrapper.emitted('update:invalido')).toBeDefined();
    // Should not trigger setContent loop when reducing, even if still over limit
    expect(mockEditorInstance.commands.setContent).not.toHaveBeenCalled();
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['<p>123456</p>']);
});
    it('watch modelValue ignores same content', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '<p>test</p>' }
        });
        
        mockEditorInstance.getHTML = vi.fn().mockReturnValue('<p>test</p>');
        await wrapper.setProps({ modelValue: '<p>test</p>' });
        
        expect(mockEditorInstance.commands.setContent).toHaveBeenCalledTimes(1); // Only from immediate watch
    });

    it('executarAcao respects desabilitado prop', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '', desabilitado: true }
        });
        const vm = wrapper.vm as any;
        const spy = vi.fn();
        
        vm.executarAcao(spy);
        expect(spy).not.toHaveBeenCalled();

        await wrapper.setProps({ desabilitado: false });
        vm.executarAcao(spy);
        expect(spy).toHaveBeenCalled();
    });

    it('sincronizarPorDom ignores same content and non-div elements', async () => {
        const wrapper = mount(EditorTextoRico, {
            props: { modelValue: '<p>test</p>' }
        });
        const vm = wrapper.vm as any;

        // Non-div
        vm.sincronizarPorDom({ target: document.createElement('span') } as any);
        expect(wrapper.emitted('update:modelValue')).toBeUndefined();

        // Same content
        const div = document.createElement('div');
        div.innerHTML = '<p>test</p>';
        vm.sincronizarPorDom({ target: div } as any);
        expect(wrapper.emitted('update:modelValue')).toBeUndefined();
    });
});
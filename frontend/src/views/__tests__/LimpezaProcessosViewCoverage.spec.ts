import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import LimpezaProcessosView from '../LimpezaProcessosView.vue';
import {createTestingPinia} from '@pinia/testing';
import {createMemoryHistory, createRouter} from 'vue-router';
import * as processoService from '@/services/processo';

vi.mock('@/services/processo', () => ({
    excluirProcessoCompleto: vi.fn()
}));

const router = createRouter({
    history: createMemoryHistory(),
    routes: [{path: '/', component: {}}]
});

describe('LimpezaProcessosView Coverage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mountComponent = () => {
        return mount(LimpezaProcessosView, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn}), router],
                stubs: {
                    LayoutPadrao: {template: '<div><slot/></div>'},
                    PageHeader: {template: '<div><slot name="actions"/></div>', props: ['title']},
                    BButton: {template: '<button @click="$emit(\'click\')"><slot/></button>'},
                    AppAlert: true,
                    BSpinner: true,
                    BCard: true,
                    BFormGroup: true,
                    BFormInput: {
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" :data-testid="dataTestid" />',
                        props: ['modelValue', 'dataTestid', 'id']
                    },
                    BFormInvalidFeedback: {template: '<div><slot/></div>'},
                    BCol: true,
                    BRow: true,
                    LoadingButton: {template: '<button @click="$emit(\'click\')"><slot/></button>'},
                    ModalConfirmacao: {template: '<div><slot/></div>', props: ['modelValue', 'mostrarModal']},
                    EmptyState: true
                }
            }
        });
    };

    it('renders correctly', async () => {
        const wrapper = mountComponent();
        await flushPromises();
        expect(wrapper.exists()).toBe(true);
    });

    it('handles delete process permanently', async () => {
        vi.mocked(processoService.excluirProcessoCompleto).mockResolvedValueOnce({} as any);

        const wrapper = mountComponent();
        await flushPromises();

        const vm = wrapper.vm as any;
        // Simulate user entering a process id
        vm.codigoProcesso = '123';

        // Test validation bypass manually
        vm.abrirConfirmacao();
        expect(vm.mostrarConfirmacao).toBe(true);

        await vm.confirmarExclusao();
        await flushPromises();

        expect(processoService.excluirProcessoCompleto).toHaveBeenCalledWith(123);
    });

    it('covers handling api errors', async () => {
        vi.mocked(processoService.excluirProcessoCompleto).mockRejectedValueOnce(new Error('Test error'));

        const wrapper = mountComponent();
        await flushPromises();

        const vm = wrapper.vm as any;
        vm.codigoProcesso = '123';

        await vm.confirmarExclusao();
        await flushPromises();

        // The component uses notify to show error. If it didn't throw, it was handled
        expect(processoService.excluirProcessoCompleto).toHaveBeenCalled();
    });

    it('covers validation failure when opening confirmation', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        vm.codigoProcesso = ''; // Invalid

        await vm.abrirConfirmacao();

        expect(vm.mostrarConfirmacao).toBe(false);
    });

    it('covers early return in confirm deletion if code is missing', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        vm.codigoProcesso = ''; // Nullifies codigoConfirmacao

        await vm.confirmarExclusao();

        expect(processoService.excluirProcessoCompleto).not.toHaveBeenCalled();
    });
});

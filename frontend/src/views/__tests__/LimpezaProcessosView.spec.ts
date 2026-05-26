import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import LimpezaProcessosView from '../LimpezaProcessosView.vue';
import {createTestingPinia} from '@pinia/testing';
import {excluirProcessoCompleto} from '@/services/processo';

vi.mock('@/services/processo', () => ({
    excluirProcessoCompleto: vi.fn()
}));

const mockNotify = vi.fn();
const mockNotifyStructured = vi.fn();
const mockClear = vi.fn();
const mockValidarSubmissao = vi.fn((v) => v);
const mockDeveExibirErro = vi.fn((v) => v);

vi.mock('@/composables/useNotification', () => ({
    useNotification: vi.fn(() => ({
        notificacao: null,
        notify: mockNotify,
        notifyStructured: mockNotifyStructured,
        clear: mockClear
    }))
}));

vi.mock('@/composables/useValidacaoFormulario', () => ({
    useValidacaoFormulario: vi.fn(() => ({
        validarSubmissao: mockValidarSubmissao,
        deveExibirErro: mockDeveExibirErro,
        focarPrimeiroErroInvalido: vi.fn()
    }))
}));

describe('LimpezaProcessosView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mountComponent = () => {
        return mount(LimpezaProcessosView, {
            global: {
                plugins: [createTestingPinia()],
                stubs: {
                    LayoutPadrao: {template: '<div><slot/></div>'},
                    PageHeader: {template: '<div><slot/></div>', props: ['title']},
                    AppAlert: {
                        template: '<div class="app-alert-stub" @click="$emit(\'dismissed\')">Alert</div>',
                        props: ['message', 'variant']
                    },
                    LoadingButton: {
                        template: '<button class="btn-stub" @click="$emit(\'click\')"><slot/></button>',
                        props: ['loading', 'text']
                    },
                    ModalConfirmacao: {
                        template: '<div v-if="modelValue" class="modal-stub"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
                        props: ['modelValue']
                    },
                    BAlert: {template: '<div><slot/></div>'},
                    BCard: {template: '<div><slot/></div>'},
                    BFormGroup: {template: '<div><slot/><slot name="label"/></div>', props: ['state']},
                    BFormInput: {template: '<input data-testid="input-codigo-processo" type="number" @keydown.enter="$emit(\'keydown\', $event)" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['state', 'modelValue']},
                    BFormInvalidFeedback: {template: '<div><slot/></div>', props: ['state']}
                }
            }
        });
    };

    it('handles notification dismissal', async () => {
        const {useNotification} = await import('@/composables/useNotification');
        vi.mocked(useNotification).mockReturnValueOnce({
            notificacao: {message: 'msg', variant: 'info', dismissible: true} as any,
            notify: mockNotify,
            notifyStructured: mockNotifyStructured,
            clear: mockClear
        });
        const wrapper = mountComponent();
        await wrapper.find('.app-alert-stub').trigger('click');
        expect(mockClear).toHaveBeenCalled();
    });

    it('validates and opens confirmation modal', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;

        // Invalid input
        vm.codigoProcesso = 'abc';
        await vm.abrirConfirmacao();
        expect(vm.mostrarConfirmacao).toBe(false);

        // Valid input
        vm.codigoProcesso = '123';
        await vm.abrirConfirmacao();
        expect(vm.mostrarConfirmacao).toBe(true);
    });

    it('handles exclusion success', async () => {
        vi.mocked(excluirProcessoCompleto).mockResolvedValue({} as any);
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;

        vm.codigoProcesso = '123';
        vm.mostrarConfirmacao = true;
        await vm.confirmarExclusao();
        await flushPromises();

        expect(excluirProcessoCompleto).toHaveBeenCalledWith(123);
        expect(mockNotify).toHaveBeenCalledWith(expect.any(String), 'success');
        expect(vm.codigoProcesso).toBe('');
        expect(vm.mostrarConfirmacao).toBe(false);
    });

    it('handles exclusion error', async () => {
        vi.mocked(excluirProcessoCompleto).mockRejectedValue(new Error('Erro feio'));
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;

        vm.codigoProcesso = '123';
        vm.mostrarConfirmacao = true;
        await vm.confirmarExclusao();
        await flushPromises();

        expect(mockNotify).toHaveBeenCalledWith('Erro feio', 'danger');
    });

    it('skips exclusion if code is null', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        vm.codigoProcesso = '';
        await vm.confirmarExclusao();
        expect(excluirProcessoCompleto).not.toHaveBeenCalled();
    });

    it('renders form inputs correctly with and without errors', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;

        // With invalid/empty value
        vm.codigoProcesso = '';
        await wrapper.vm.$nextTick();
        // mensagemErroCodigo should be truthy because !codigoConfirmacao is true
        expect(vm.mensagemErroCodigo).toBeTruthy();
        
        // With valid value
        vm.codigoProcesso = '999';
        await wrapper.vm.$nextTick();
        // mensagemErroCodigo should be empty because !codigoConfirmacao is false
        expect(vm.mensagemErroCodigo).toBe('');
    });

    it('triggers abrirConfirmacao on enter keypress', async () => {
        const wrapper = mountComponent();
        const input = wrapper.find('[data-testid="input-codigo-processo"]');
        
        // Simulate enter press
        await input.trigger('keydown.enter');
        // Validates form
        expect(mockValidarSubmissao).toHaveBeenCalled();
    });
});

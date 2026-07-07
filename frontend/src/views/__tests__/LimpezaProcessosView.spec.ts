import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import LimpezaProcessosView from '../LimpezaProcessosView.vue';
import {createTestingPinia} from '@pinia/testing';
import {
    buscarProcessosAtivos,
    buscarProcessosFinalizados,
    excluirProcessoCompleto,
} from '@/services/processo';

vi.mock('@/services/processo', () => ({
    buscarProcessosAtivos: vi.fn(),
    buscarProcessosFinalizados: vi.fn(),
    excluirProcessoCompleto: vi.fn(),
}));

const mockNotify = vi.fn();
const mockNotifyStructured = vi.fn();
const mockClear = vi.fn();
const mockExibirSucesso = vi.fn();
const mockValidarSubmissao = vi.fn((v) => v);
const mockResetarValidacao = vi.fn();
const mockDeveExibirErro = vi.fn((v) => v);
const mockFocarPrimeiroErroInvalido = vi.fn();

vi.mock('@/composables/useNotification', () => ({
    useNotification: vi.fn(() => ({
        notificacao: null,
        notify: mockNotify,
        notifyStructured: mockNotifyStructured,
        clear: mockClear,
    })),
}));

vi.mock('@/composables/useValidacaoFormulario', () => ({
    useValidacaoFormulario: vi.fn(() => ({
        validarSubmissao: mockValidarSubmissao,
        resetarValidacao: mockResetarValidacao,
        deveExibirErro: mockDeveExibirErro,
        focarPrimeiroErroInvalido: mockFocarPrimeiroErroInvalido,
    })),
}));

vi.mock('@/composables/useToast', () => ({
    useToast: vi.fn(() => ({
        exibirSucesso: mockExibirSucesso,
        exibirErro: vi.fn(),
        exibirToast: vi.fn(),
        registrarPendente: vi.fn(),
        exibirPendente: vi.fn(),
    })),
}));

const processosAtivosMock = [
    {codigo: 123, descricao: 'Processo Alpha'},
    {codigo: 456, descricao: 'Processo Beta'},
];

const processosFinalizadosMock = [
    {codigo: 789, descricao: 'Processo Ômega'},
];

describe('LimpezaProcessosView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(buscarProcessosAtivos).mockResolvedValue([...processosAtivosMock] as any);
        vi.mocked(buscarProcessosFinalizados).mockResolvedValue([...processosFinalizadosMock] as any);
    });

    const mountComponent = () => mount(LimpezaProcessosView, {
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                LayoutPadrao: {template: '<div><slot/></div>'},
                PageHeader: {template: '<div><slot name="alerta"/><slot/></div>', props: ['title']},
                Alerta: {
                    template: '<div class="app-alert-stub" @click="$emit(\'dismissed\')">Alert</div>',
                    props: ['mensagem', 'variante'],
                },
                LoadingButton: {
                    template: '<button class="btn-stub" :disabled="disabled" v-bind="$attrs" @click="$emit(\'click\')"><slot/></button>',
                    props: ['loading', 'text', 'disabled'],
                },
                LimpezaProcessosFluxoModais: {
                    template: '<div v-if="mostrarConfirmacao" class="modal-stub"><button @click="$emit(\'confirmarExclusao\')">Confirmar</button></div>',
                    props: ['mostrarConfirmacao', 'descricaoConfirmacao'],
                },
                BCard: {template: '<div><slot/></div>'},
                BFormGroup: {template: '<div><slot name="label"/><slot/></div>', props: ['state']},
                BFormSelect: {
                    template: `
                      <select
                        data-testid="select-processo"
                        :disabled="disabled"
                        :value="modelValue ?? ''"
                        @change="$emit('update:modelValue', $event.target.value ? Number($event.target.value) : null)"
                        @keydown.enter="$emit('keydown', $event)"
                      >
                        <slot name="first" />
                        <option
                          v-for="option in options"
                          :key="option.codigo"
                          :value="option.codigo"
                        >
                          {{ option.descricao }}
                        </option>
                      </select>`,
                    props: ['state', 'modelValue', 'options', 'disabled', 'textField', 'valueField'],
                },
                BFormSelectOption: {
                    template: '<option :value="value" :disabled="disabled"><slot/></option>',
                    props: ['value', 'disabled'],
                },
                BFormInvalidFeedback: {template: '<div><slot/></div>', props: ['state']},
            },
        },
    });

    it('carrega processos ativos e finalizados para o select', async () => {
        const wrapper = mountComponent();

        await flushPromises();

        expect(buscarProcessosAtivos).toHaveBeenCalled();
        expect(buscarProcessosFinalizados).toHaveBeenCalled();
        expect((wrapper.vm as any).processos).toHaveLength(3);
        expect((wrapper.vm as any).processos.map((processo: any) => processo.descricao))
            .toEqual(['Processo Alpha', 'Processo Beta', 'Processo Ômega']);
    });

    it('notifica erro quando falha ao carregar processos', async () => {
        vi.mocked(buscarProcessosAtivos).mockRejectedValueOnce(new Error('Erro ao carregar processos'));

        mountComponent();
        await flushPromises();

        expect(mockNotify).toHaveBeenCalledWith('Erro ao carregar processos', 'danger');
    });

    it('handles notification dismissal', async () => {
        const {useNotification} = await import('@/composables/useNotification');
        vi.mocked(useNotification).mockReturnValueOnce({
            notificacao: {message: 'msg', variant: 'info', dismissible: true} as any,
            notify: mockNotify,
            notifyStructured: mockNotifyStructured,
            clear: mockClear,
        });
        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('.app-alert-stub').trigger('click');

        expect(mockClear).toHaveBeenCalled();
    });

    it('valida seleção e abre modal de confirmação', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        await flushPromises();

        vm.codigoProcessoSelecionado = null;
        await vm.abrirConfirmacao();
        expect(vm.mostrarConfirmacao).toBe(false);

        vm.codigoProcessoSelecionado = 123;
        await vm.abrirConfirmacao();
        expect(vm.mostrarConfirmacao).toBe(true);
    });

    it('exclui processo selecionado e remove da lista', async () => {
        vi.mocked(excluirProcessoCompleto).mockResolvedValue({} as any);
        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="select-processo"]').setValue('123');
        await wrapper.find('[data-testid="btn-excluir-processo-completo"]').trigger('click');
        await flushPromises();
        await wrapper.find('.modal-stub button').trigger('click');
        await flushPromises();

        expect(excluirProcessoCompleto).toHaveBeenCalledWith(123);
        expect(mockExibirSucesso).toHaveBeenCalledWith(expect.any(String));
        expect(mockResetarValidacao).toHaveBeenCalled();
        expect((wrapper.vm as any).codigoProcessoSelecionado).toBeNull();
        expect((wrapper.vm as any).processos.some((processo: any) => processo.codigo === 123)).toBe(false);
        expect((wrapper.vm as any).mostrarConfirmacao).toBe(false);
    });

    it('trata erro ao excluir processo', async () => {
        vi.mocked(excluirProcessoCompleto).mockRejectedValue(new Error('Erro feio'));
        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="select-processo"]').setValue('123');
        await wrapper.find('[data-testid="btn-excluir-processo-completo"]').trigger('click');
        await flushPromises();
        await wrapper.find('.modal-stub button').trigger('click');
        await flushPromises();

        expect(mockNotify).toHaveBeenCalledWith('Erro feio', 'danger');
    });

    it('não exclui sem processo selecionado', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        await flushPromises();

        vm.codigoProcessoSelecionado = null;
        await vm.confirmarExclusao();

        expect(excluirProcessoCompleto).not.toHaveBeenCalled();
    });

    it('mostra mensagem quando não há processos disponíveis', async () => {
        vi.mocked(buscarProcessosAtivos).mockResolvedValueOnce([] as any);
        vi.mocked(buscarProcessosFinalizados).mockResolvedValueOnce([] as any);

        const wrapper = mountComponent();
        await flushPromises();

        expect(wrapper.text()).toContain('Nenhum processo disponível para limpeza');
    });

    it('renderiza erro de validação quando nenhum processo está selecionado', async () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        await flushPromises();

        vm.codigoProcessoSelecionado = null;
        await wrapper.vm.$nextTick();

        expect(vm.mensagemErroProcesso).toBeTruthy();

        vm.codigoProcessoSelecionado = 789;
        await wrapper.vm.$nextTick();

        expect(vm.mensagemErroProcesso).toBe('');
    });

    it('aciona validação ao pressionar enter no select', async () => {
        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="select-processo"]').trigger('keydown.enter');

        expect(mockValidarSubmissao).toHaveBeenCalled();
    });
});

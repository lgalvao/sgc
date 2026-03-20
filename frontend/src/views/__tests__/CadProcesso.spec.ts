import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {nextTick, ref} from 'vue';
import ProcessoCadastroView from '@/views/ProcessoCadastroView.vue';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as unidadeService from '@/services/unidadeService';

vi.mock('@/services/processoService', () => ({
    criarProcesso: vi.fn(),
    atualizarProcesso: vi.fn(),
    excluirProcesso: vi.fn(),
}));

vi.mock('@/services/unidadeService', () => ({
    buscarArvoreComElegibilidade: vi.fn().mockResolvedValue([]),
    mapUnidadesArray: vi.fn((arr) => arr || []),
}));

const processosMock = {
    processoDetalhe: ref<any>(null),
    lastError: ref<any>(null),
    buscarProcessoDetalhe: vi.fn(),
    criarProcesso: vi.fn(),
    atualizarProcesso: vi.fn(),
    iniciarProcesso: vi.fn(),
    removerProcesso: vi.fn(),
};

vi.mock('@/composables/useProcessos', () => ({
    useProcessos: () => processosMock
}));

const {mockPush, mockRoute} = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockRoute: {query: {} as Record<string, string>}
    }
});

vi.mock('vue-router', () => {
    return {
        useRouter: () => ({push: mockPush}),
        useRoute: () => mockRoute,
        createRouter: vi.fn(() => ({
            beforeEach: vi.fn(),
            afterEach: vi.fn(),
            push: mockPush,
            replace: vi.fn(),
            resolve: vi.fn(),
            currentRoute: {value: mockRoute},
        })),
        createWebHistory: vi.fn(),
        createMemoryHistory: vi.fn(),
    };
});

const ArvoreUnidadesStub = {
    template: '<div><slot /></div>',
    props: ['unidades', 'modelValue'],
    emits: ['update:modelValue']
};

describe('ProcessoCadastroView.vue', () => {
    const context = setupComponentTest();

    const createWrapper = (initialState = {}) => {
        processosMock.processoDetalhe.value = (initialState as any).processos?.processoDetalhe ?? null;
        processosMock.lastError.value = (initialState as any).processos?.lastError ?? null;

        context.wrapper = mount(ProcessoCadastroView, {
            ...getCommonMountOptions(
                {
                    ...initialState
                },
                {
                    BContainer: {template: '<div><slot /></div>'},
                    BAlert: {template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant']},
                    BForm: {template: '<form @submit.prevent><slot /></form>'},
                    BFormGroup: {
                        template: '<div><slot name="label">{{ label }}</slot><slot /></div>',
                        props: ['label']
                    },
                    BFormInput: {
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue']
                    },
                    BFormSelect: {
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot name="first" /><option value="MAPEAMENTO">MAPEAMENTO</option></select>',
                        props: ['modelValue', 'options'],
                        inheritAttrs: false
                    },
                    BFormSelectOption: {
                        template: '<option :value="value" :disabled="disabled"><slot /></option>',
                        props: ['value', 'disabled']
                    },
                    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BModal: {
                        template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
                        props: ['modelValue']
                    },
                    BSpinner: {template: '<span>Loading...</span>'},
                    ArvoreUnidades: ArvoreUnidadesStub,
                    BFormInvalidFeedback: {template: '<div><slot /></div>'}
                }
            )
        });

        return {wrapper: context.wrapper, processosStore: processosMock};
    };

    beforeEach(async () => {
        vi.clearAllMocks();
        processosMock.processoDetalhe.value = null;
        processosMock.lastError.value = null;
        mockRoute.query = {};
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockResolvedValue([]);
        vi.mocked(unidadeService.mapUnidadesArray).mockImplementation((arr) => arr || []);

        await import('@/services/processoService');

        window.scrollTo = vi.fn();

        vi.spyOn(console, 'error').mockImplementation(() => {
        });
    });

    it('renders correctly in creation mode', async () => {
        const {wrapper} = createWrapper();
        expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
        expect(unidadeService.buscarArvoreComElegibilidade).not.toHaveBeenCalled();
        expect(wrapper.find('[data-testid="btn-processo-salvar"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-processo-remover"]').exists()).toBe(false);
    });

    it('disables action buttons when form is incomplete and enables when complete', async () => {
        const {wrapper} = createWrapper();
        const salvarBtn = wrapper.find('[data-testid="btn-processo-salvar"]');
        const iniciarBtn = wrapper.find('[data-testid="btn-processo-iniciar"]');

        // Initially disabled (empty fields)
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);
        expect((iniciarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Fill description
        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste');
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Fill date
        await wrapper.find('[data-testid="inp-processo-data-limite"]').setValue('2023-12-31');
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Select units (modelValue for ArvoreUnidades)
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);

        // Select type
        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();

        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(false);
        expect((iniciarBtn.element as HTMLButtonElement).disabled).toBe(false);

        // Clear description again
        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('');
        expect((salvarBtn.element as HTMLButtonElement).disabled).toBe(true);
    });

    it('loads process data for editing', async () => {
        mockRoute.query = {codProcesso: '123'};
        const {processosStore} = createWrapper();
        expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(123);
    });

    it('redirects if process is not editable', async () => {
        mockRoute.query = {codProcesso: '123'};
        const {processosStore} = createWrapper({
            processos: {
                processoDetalhe: {codigo: 123, situacao: 'EM_ANDAMENTO', unidades: []},
                lastError: null
            }
        });
        await flushPromises();
        expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/processo/123');
    });

    it('creates a new process', async () => {
        const {wrapper, processosStore} = createWrapper();

        const descricaoInput = wrapper.find('[data-testid="inp-processo-descricao"]');
        await descricaoInput.setValue('Novo processo');

        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();

        const dataInput = wrapper.find('[data-testid="inp-processo-data-limite"]');
        await dataInput.setValue('2023-12-31');

        wrapper.vm.unidadesSelecionadas = [1, 2];
        await nextTick();

        const salvarBtn = wrapper.find('[data-testid="btn-processo-salvar"]');
        await salvarBtn.trigger('click');

        expect(processosStore.criarProcesso).toHaveBeenCalledWith({
            descricao: 'Novo processo',
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: '2023-12-31T00:00:00',
            unidades: [1, 2]
        });

        await flushPromises();
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('handles creation error', async () => {
        const {wrapper, processosStore} = createWrapper();

        processosStore.criarProcesso.mockRejectedValue(new Error('Erro API'));
        processosStore.lastError.value = {message: 'Erro validacao', subErrors: []};

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro validacao');
    });

    it('updates an existing process', async () => {
        const {wrapper, processosStore} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.descricao = 'Editado';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');

        expect(processosStore.atualizarProcesso).toHaveBeenCalledWith(123, {
            codigo: 123,
            descricao: 'Editado',
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: '2023-12-31T00:00:00',
            unidades: [1]
        });
        await flushPromises();
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('initiates a process (confirmation flow)', async () => {
        const {wrapper, processosStore} = createWrapper();

        wrapper.vm.descricao = 'Iniciar teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);

        processosStore.criarProcesso.mockResolvedValue({codigo: 999});

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');

        expect(processosStore.criarProcesso).toHaveBeenCalled();
        await flushPromises();
        expect(processosStore.iniciarProcesso).toHaveBeenCalledWith(999, 'MAPEAMENTO', [1]);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('initiates an existing process', async () => {
        const {wrapper, processosStore} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.descricao = 'Existente';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);
        await nextTick();

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');

        expect(processosStore.criarProcesso).not.toHaveBeenCalled();
        expect(processosStore.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', [1]);
    });

    it('removes a process', async () => {
        const {wrapper, processosStore} = createWrapper();
        processosStore.removerProcesso.mockResolvedValue(undefined);

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.mostrarModalRemocao = true;

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processosStore.removerProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('updates unit list when type changes', async () => {
        const {wrapper} = createWrapper();
        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledWith('REVISAO', undefined);
    });

    it('handles error when starting a new process (creation fail)', async () => {
        const {wrapper, processosStore} = createWrapper();

        wrapper.vm.processoEditando = null;
        wrapper.vm.descricao = 'Novo processo fail';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];

        wrapper.vm.mostrarModalConfirmacao = true;
        await nextTick();

        processosStore.criarProcesso.mockRejectedValue(new Error('Erro criacao'));
        processosStore.lastError.value = {message: 'Erro ao criar', subErrors: []};

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro ao criar');
    });

    it('handles error when starting a process (initiation fail)', async () => {
        const {wrapper, processosStore} = createWrapper();

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.mostrarModalConfirmacao = true;
        await nextTick();

        processosStore.iniciarProcesso.mockRejectedValue(new Error('Erro inicio'));
        processosStore.lastError.value = {message: 'Erro ao iniciar', subErrors: []};

        await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.summary).toContain('Erro ao iniciar');
    });

    it('handles error when removing a process', async () => {
        const {wrapper, processosStore} = createWrapper();
        processosStore.removerProcesso.mockRejectedValue(new Error('Erro remocao'));

        wrapper.vm.processoEditando = {codigo: 123};
        wrapper.vm.mostrarModalRemocao = true;

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.message).toContain('Não foi possível remover o processo');
    });

    it('closes confirmation modal when cancel button is clicked', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);

        await wrapper.find('[data-testid="btn-iniciar-processo-cancelar"]').trigger('click');
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(false);
    });

    it('maps field-specific validation errors from subErrors', async () => {
        const {wrapper, processosStore} = createWrapper();
        processosStore.lastError.value = null;

        processosStore.criarProcesso.mockImplementation(async () => {
            processosStore.lastError.value = {
                message: 'Erro de validação',
                subErrors: [
                    {field: 'descricao', message: 'Descrição é obrigatória'},
                    {field: 'tipo', message: 'Tipo inválido'},
                    {field: 'dataLimiteEtapa1', message: 'Data inválida'},
                    {field: 'unidades', message: 'Selecione ao menos uma unidade'},
                    {field: null, message: 'Erro genérico'}
                ]
            };
            throw new Error('Validation error');
        });

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.fieldErrors.descricao).toBe('Descrição é obrigatória');
        expect(wrapper.vm.fieldErrors.tipo).toBe('Tipo inválido');
        expect(wrapper.vm.fieldErrors.dataLimite).toBe('Data inválida');
        expect(wrapper.vm.fieldErrors.unidades).toBe('Selecione ao menos uma unidade');
        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro genérico');
    });

    it('handles error without lastError (network/runtime error)', async () => {
        const {wrapper, processosStore} = createWrapper();

        processosStore.criarProcesso.mockRejectedValue(new Error('Network error'));
        processosStore.lastError.value = null;

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
        await flushPromises();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.message).toContain('Não foi possível salvar o processo');
    });

    it('does nothing when confirmarRemocao is called without processoEditando', async () => {
        const {wrapper} = createWrapper();
        const processoServiceModule = await import('@/services/processoService');
        vi.spyOn(processoServiceModule, 'excluirProcesso');

        wrapper.vm.processoEditando = null;
        wrapper.vm.mostrarModalRemocao = true;
        await nextTick();

        await wrapper.vm.confirmarRemocao();
        await flushPromises();

        expect(processoServiceModule.excluirProcesso).not.toHaveBeenCalled();
        expect(wrapper.vm.mostrarModalRemocao).toBe(false);
    });

    it('clears field errors when fields are updated', async () => {
        const {wrapper} = createWrapper();

        wrapper.vm.fieldErrors.descricao = 'Erro';
        wrapper.vm.fieldErrors.tipo = 'Erro';
        wrapper.vm.fieldErrors.dataLimite = 'Erro';
        wrapper.vm.fieldErrors.unidades = 'Erro';
        await nextTick();

        wrapper.vm.descricao = 'Nova descricao';
        await nextTick();
        expect(wrapper.vm.fieldErrors.descricao).toBe('');

        wrapper.vm.tipo = 'REVISAO';
        await nextTick();
        expect(wrapper.vm.fieldErrors.tipo).toBe('');

        wrapper.vm.dataLimite = '2024-01-01';
        await nextTick();
        expect(wrapper.vm.fieldErrors.dataLimite).toBe('');

        wrapper.vm.unidadesSelecionadas = [2];
        await nextTick();
        expect(wrapper.vm.fieldErrors.unidades).toBe('');
    });

    it('shows loading spinner when units are loading', async () => {
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockReturnValueOnce(new Promise(() => {}));
        const {wrapper} = createWrapper();
        wrapper.vm.tipo = 'MAPEAMENTO';
        await nextTick();
        expect(wrapper.text()).toContain('Carregando unidades...');
    });

    it('shows alert with multiple errors', async () => {
        const {wrapper} = createWrapper();
        (wrapper.vm).notifyStructured('Corpo', ['Erro 1', 'Erro 2'], 'danger');
        await nextTick();

        expect(wrapper.vm.notificacao).not.toBeNull();
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro 1');
        expect(wrapper.vm.notificacao?.notification?.details).toContain('Erro 2');
    });

    it('shows saving spinner on button', async () => {
        const {wrapper, processosStore} = createWrapper();
        processosStore.criarProcesso.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        const btn = wrapper.find('[data-testid="btn-processo-salvar"]');
        await btn.trigger('click');

        expect(btn.text()).toContain('Salvando...'); // Button content might be replaced by "Salvando..."
    });

    it('handles error when loading process details on mount', async () => {
        mockRoute.query = {codProcesso: '123'};
        processosMock.buscarProcessoDetalhe.mockRejectedValue(new Error('Load error'));

        const wrapper = mount(ProcessoCadastroView, {
            ...getCommonMountOptions({}, {
                BContainer: {template: '<div><slot /></div>'},
                BAlert: {template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant']},
                BForm: {template: '<form @submit.prevent><slot /></form>'},
                BFormGroup: {template: '<div><slot name="label">{{ label }}</slot><slot /></div>', props: ['label']},
                BFormInput: {template: '<input />', props: ['modelValue']},
                BFormSelect: {template: '<select></select>', props: ['modelValue']},
                BButton: {template: '<button></button>'},
                BModal: {template: '<div><slot /></div>'},
                BSpinner: {template: '<span></span>'},
                ArvoreUnidades: {template: '<div></div>', props: ['unidades', 'modelValue']},
                BFormInvalidFeedback: {template: '<div></div>'},
                LoadingButton: {template: '<button><slot /></button>'}
            })
        });

        await flushPromises();

        expect((wrapper.vm as any).notificacao?.message).toContain('Não foi possível carregar');
    });

    it('focuses on the first invalid field when validation errors occur', async () => {
        const {wrapper, processosStore} = createWrapper();

        const focusMock = vi.fn();
        const mockElement = {focus: focusMock} as unknown as HTMLElement;
        const querySelectorSpy = vi.spyOn(document, 'querySelector').mockReturnValue(mockElement);

        processosStore.criarProcesso.mockImplementation(async () => {
            processosStore.lastError.value = {
                message: 'Erro de validação',
                subErrors: [
                    {field: 'descricao', message: 'Descrição é obrigatória'}
                ]
            };
            throw new Error('Validation error');
        });

        wrapper.vm.descricao = 'Teste';
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.dataLimite = '2023-12-31';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');

        await flushPromises();
        await nextTick();
        await nextTick();

        expect(querySelectorSpy).toHaveBeenCalledWith('.is-invalid');
        expect(focusMock).toHaveBeenCalled();

        querySelectorSpy.mockRestore();
    });

});

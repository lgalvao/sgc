import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {nextTick} from 'vue';
import CadProcesso from '@/views/CadProcesso.vue';
import {useProcessosStore} from '@/stores/processos';
import {useUnidadesStore} from '@/stores/unidades';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mock router
const { mockPush, mockRoute } = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockRoute: { query: {} as Record<string, string> }
    }
});

vi.mock('vue-router', () => {
    return {
        useRouter: () => ({ push: mockPush }),
        useRoute: () => mockRoute,
        createRouter: vi.fn(() => ({
            beforeEach: vi.fn(),
            afterEach: vi.fn(),
            push: mockPush,
            replace: vi.fn(),
            resolve: vi.fn(),
            currentRoute: { value: mockRoute },
        })),
        createWebHistory: vi.fn(),
        createMemoryHistory: vi.fn(),
    };
});

// Components stubs
const ArvoreUnidadesStub = {
    template: '<div><slot /></div>',
    props: ['unidades', 'modelValue'],
    emits: ['update:modelValue']
};

const ModalConfirmacaoStub = {
    name: 'ModalConfirmacao',
    template: '<div class="modal-confirmacao-stub"><slot /></div>',
    props: ['modelValue', 'loading'],
    emits: ['update:modelValue', 'confirmar']
};

describe('CadProcesso.vue Coverage', () => {
    const context = setupComponentTest();
    let processosStore: any;

    const createWrapper = (initialState = {}) => {
        context.wrapper = mount(CadProcesso, {
            ...getCommonMountOptions(
                {
                    unidades: {
                        unidades: [],
                        isLoading: false
                    },
                    processos: {
                        processoDetalhe: null,
                        lastError: null
                    },
                    ...initialState
                },
                {
                    BContainer: { template: '<div><slot /></div>' },
                    BAlert: { template: '<div class="alert"><slot /></div>', props: ['modelValue', 'variant'] },
                    BForm: { template: '<form @submit.prevent><slot /></form>' },
                    BFormGroup: { template: '<div><slot /></div>', props: ['label'] },
                    BFormInput: {
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue']
                    },
                    BFormSelect: {
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option value="MAPEAMENTO">MAPEAMENTO</option></select>',
                        props: ['modelValue', 'options'],
                        inheritAttrs: false
                    },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    ModalConfirmacao: ModalConfirmacaoStub,
                    BSpinner: { template: '<span>Loading...</span>' },
                    ArvoreUnidades: ArvoreUnidadesStub,
                    BFormInvalidFeedback: { template: '<div><slot /></div>' },
                    LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' }
                }
            )
        });

        processosStore = useProcessosStore();
        const unidadesStore = useUnidadesStore();
        return { wrapper: context.wrapper, processosStore, unidadesStore };
    };

    beforeEach(() => {
        vi.clearAllMocks();
        mockRoute.query = {};
        window.scrollTo = vi.fn();
    });

    it('handles mixed errors (field + generic) correctly in handleApiErrors', async () => {
        const { wrapper, processosStore } = createWrapper();

        // Setup state for error
        processosStore.criarProcesso.mockImplementation(async () => {
             processosStore.lastError = {
                message: 'Erro misto',
                subErrors: [
                    { field: 'descricao', message: 'Descrição inválida' },
                    { field: null, message: 'Erro genérico de regra de negócio' }
                ]
            };
            throw new Error('Mixed Error');
        });

        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste');
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        // Call method directly
        await (wrapper.vm as any).salvarProcesso();
        await flushPromises();

        expect(wrapper.vm.alertState.show).toBe(true);
        expect(wrapper.vm.alertState.errors).toContain('Erro genérico de regra de negócio');
        expect(wrapper.vm.fieldErrors.descricao).toBe('Descrição inválida');
    });

    it('handles error creating process during initiation flow (without existing process)', async () => {
        const { wrapper, processosStore } = createWrapper();

        // Fill form using setValue to ensure reactivity triggers validation
        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste Inicio');
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        // Trigger initiation to open modal
        // Call the method directly to avoid issues with disabled button state in test environment
        await (wrapper.vm as any).abrirModalConfirmacao();
        expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);

        // Configure error on create
        processosStore.criarProcesso.mockRejectedValue(new Error('Create Error'));
        processosStore.lastError = { message: 'Failed to create' };

        // Confirm initiation
        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(processosStore.criarProcesso).toHaveBeenCalled();
        expect(processosStore.iniciarProcesso).not.toHaveBeenCalled();
        expect(wrapper.vm.alertState.show).toBe(true);
        expect(wrapper.vm.alertState.body).toContain('Failed to create');
        expect(wrapper.vm.isLoading).toBe(false);
    });

    it('handles error starting process during initiation flow', async () => {
        const { wrapper, processosStore } = createWrapper();

        await wrapper.find('[data-testid="inp-processo-descricao"]').setValue('Teste Inicio');
        wrapper.vm.tipo = 'MAPEAMENTO';
        wrapper.vm.unidadesSelecionadas = [1];
        await nextTick();

        await (wrapper.vm as any).abrirModalConfirmacao();

        // Configure success on create, failure on start
        processosStore.criarProcesso.mockResolvedValue({ codigo: 777 });
        processosStore.iniciarProcesso.mockRejectedValue(new Error('Start Error'));
        processosStore.lastError = { message: 'Failed to start' };

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(processosStore.criarProcesso).toHaveBeenCalled();
        expect(processosStore.iniciarProcesso).toHaveBeenCalledWith(777, 'MAPEAMENTO', [1]);
        expect(wrapper.vm.alertState.show).toBe(true);
        expect(wrapper.vm.alertState.body).toContain('Failed to start');
        expect(wrapper.vm.isLoading).toBe(false);
    });

    it('opens and confirms removal modal', async () => {
        const { wrapper, processosStore } = createWrapper();

        // Setup process being edited
        (wrapper.vm as any).processoEditando = { codigo: 123, descricao: 'Processo Teste' };
        await nextTick();

        // Open modal
        await (wrapper.vm as any).abrirModalRemocao();
        expect((wrapper.vm as any).mostrarModalRemocao).toBe(true);

        // Confirm removal
        processosStore.removerProcesso.mockResolvedValue(undefined);

        await (wrapper.vm as any).confirmarRemocao();
        await flushPromises();

        expect(processosStore.removerProcesso).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
        expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
    });

    it('handles error during removal', async () => {
        const { wrapper, processosStore } = createWrapper();

        (wrapper.vm as any).processoEditando = { codigo: 123, descricao: 'Processo Teste' };
        await nextTick();

        processosStore.removerProcesso.mockRejectedValue(new Error('Delete Error'));
        processosStore.lastError = { message: 'Failed to delete' };

        await (wrapper.vm as any).confirmarRemocao();
        await flushPromises();

        expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
        expect((wrapper.vm as any).alertState.show).toBe(true);
        expect((wrapper.vm as any).alertState.body).toContain('Failed to delete');
    });

    it('fecharModalRemocao closes the modal', async () => {
        const { wrapper } = createWrapper();
        (wrapper.vm as any).mostrarModalRemocao = true;

        (wrapper.vm as any).fecharModalRemocao();

        expect((wrapper.vm as any).mostrarModalRemocao).toBe(false);
    });

    it('confirmarRemocao does nothing if no process editing', async () => {
        const { wrapper, processosStore } = createWrapper();
        (wrapper.vm as any).processoEditando = null;

        await (wrapper.vm as any).confirmarRemocao();

        expect(processosStore.removerProcesso).not.toHaveBeenCalled();
    });

    it('triggers search for units if type changes', async () => {
        const { wrapper } = createWrapper();
        const unidadesStore = (wrapper.vm as any).unidadesStore;
        vi.spyOn(unidadesStore, 'buscarUnidadesParaProcesso').mockResolvedValue(undefined);

        (wrapper.vm as any).tipo = 'MAPEAMENTO';
        await nextTick();

        expect(unidadesStore.buscarUnidadesParaProcesso).toHaveBeenCalledWith('MAPEAMENTO', undefined);
    });

    it('populates fields when loading an existing process', async () => {
        mockRoute.query = { codProcesso: '123' };
        const mockProcesso = {
            codigo: 123,
            descricao: 'Processo Existente',
            tipo: 'MAPEAMENTO',
            dataLimite: '2023-12-31T00:00:00',
            situacao: 'CRIADO',
            unidades: [{ codUnidade: 1 }, { codUnidade: 2 }]
        };

        const { wrapper, processosStore, unidadesStore } = createWrapper({
            processos: {
                processoDetalhe: mockProcesso
            }
        });

        await flushPromises();

        expect((wrapper.vm as any).descricao).toBe('Processo Existente');
        expect((wrapper.vm as any).tipo).toBe('MAPEAMENTO');
        expect((wrapper.vm as any).dataLimite).toBe('2023-12-31');
        expect((wrapper.vm as any).unidadesSelecionadas).toEqual([1, 2]);
        expect(unidadesStore.buscarUnidadesParaProcesso).toHaveBeenCalledWith('MAPEAMENTO', 123);
    });
});

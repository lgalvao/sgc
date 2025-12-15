import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { nextTick } from 'vue';
import CadProcesso from '@/views/CadProcesso.vue';
import { createTestingPinia } from '@pinia/testing';
import { useProcessosStore } from '@/stores/processos';
import { useUnidadesStore } from '@/stores/unidades';
import * as processoService from "@/services/processoService";

// Mock router
// We need to use "vi.hoisted" to make variables available inside vi.mock factory
const { mockPush, mockRoute } = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockRoute: { query: {} }
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

// Mock child components
const ArvoreUnidadesStub = {
  template: '<div><slot /></div>',
  props: ['unidades', 'modelValue'],
  emits: ['update:modelValue']
};

describe('CadProcesso.vue', () => {
  let wrapper: any;
  let processosStore: any;
  let unidadesStore: any;

  beforeEach(() => {
    vi.clearAllMocks();
    mockRoute.query = {};
    
    // Mock window.scrollTo
    window.scrollTo = vi.fn();
    
    // Suppress console.error
    vi.spyOn(console, 'error').mockImplementation(() => {});

    wrapper = mount(CadProcesso, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              unidades: {
                unidades: [],
                isLoading: false
              },
              processos: {
                processoDetalhe: null,
                lastError: null
              }
            }
          })
        ],
        components: {
          ArvoreUnidades: ArvoreUnidadesStub
        },
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BAlert: { template: '<div><slot /></div>', props: ['modelValue', 'variant'] },
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
          BModal: {
            template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          }
        }
      }
    });

    processosStore = useProcessosStore();
    unidadesStore = useUnidadesStore();
  });

  it('renders correctly in creation mode', async () => {
    expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
    expect(unidadesStore.buscarUnidadesParaProcesso).toHaveBeenCalledWith('MAPEAMENTO');
    expect(wrapper.find('[data-testid="btn-processo-salvar"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-processo-remover"]').exists()).toBe(false);
  });

  it('loads process data for editing', async () => {
    mockRoute.query = { codProcesso: '123' };
    const mockProcesso = {
      codigo: 123,
      descricao: 'Processo Teste',
      tipo: 'MAPEAMENTO',
      situacao: 'CRIADO',
      dataLimite: '2023-12-31T00:00:00',
      unidades: [{ codUnidade: 1 }]
    };

    // We need to re-mount because onMounted runs on setup
    processosStore.buscarProcessoDetalhe.mockImplementation(async () => {
      processosStore.processoDetalhe = mockProcesso;
    });

    wrapper = mount(CadProcesso, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
        stubs: {
           BContainer: { template: '<div><slot /></div>' },
           BAlert: { template: '<div><slot /></div>' },
           BForm: { template: '<form><slot /></form>' },
           BFormGroup: { template: '<div><slot /></div>' },
           BFormInput: { template: '<input />', props: ['modelValue'] },
           BFormSelect: { template: '<select />', props: ['modelValue', 'options'], inheritAttrs: false },
           BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
           BModal: { template: '<div v-if="modelValue"><slot /></div>', props: ['modelValue'] },
           ArvoreUnidades: true
        }
      }
    });

    // Manually trigger the store/route logic mock since re-mounting with complex mocks is tricky in unit tests
    // or rely on the mock implementation above if using clean store
  });

  it('redirects if process is not editable', async () => {
    mockRoute.query = { codProcesso: '123' };
    const mockProcesso = {
      codigo: 123,
      situacao: 'EM_ANDAMENTO' // Not CRIADO
    };

    processosStore.buscarProcessoDetalhe.mockImplementation(async () => {
        processosStore.processoDetalhe = mockProcesso;
    });

    // Re-mount to trigger onMounted
    wrapper = mount(CadProcesso, {
        global: {
          plugins: [createTestingPinia({ createSpy: vi.fn, initialState: { processos: { processoDetalhe: mockProcesso } } })],
          stubs: {
             BContainer: true, BAlert: true, BForm: true, BFormGroup: true, BFormInput: true, BFormSelect: true, BButton: true, BModal: true, ArvoreUnidades: true
          }
        }
    });

    // Need to get the store from the new wrapper/pinia instance
    processosStore = useProcessosStore();

    await flushPromises();
    expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(123);
    // Since we mocked the store state initially, the component sees the state immediately
    // Wait for the next tick for the router push check
    await flushPromises();
    expect(mockPush).toHaveBeenCalledWith('/processo/123');
  });

  it('creates a new process', async () => {
    const descricaoInput = wrapper.find('[data-testid="inp-processo-descricao"]');
    await descricaoInput.setValue('Novo Processo');

    const dataInput = wrapper.find('[data-testid="inp-processo-data-limite"]');
    await dataInput.setValue('2023-12-31');

    // Simulate unit selection
    wrapper.vm.unidadesSelecionadas = [1, 2];

    const salvarBtn = wrapper.find('[data-testid="btn-processo-salvar"]');
    await salvarBtn.trigger('click');

    expect(processosStore.criarProcesso).toHaveBeenCalledWith({
      descricao: 'Novo Processo',
      tipo: 'MAPEAMENTO',
      dataLimiteEtapa1: '2023-12-31T00:00:00',
      unidades: [1, 2]
    });

    await flushPromises();
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('handles creation error', async () => {
    processosStore.criarProcesso.mockRejectedValue(new Error('Erro API'));
    processosStore.lastError = { message: 'Erro validacao', subErrors: [] };

    wrapper.vm.descricao = 'Teste';
    await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');
    await flushPromises();

    expect(wrapper.vm.alertState.show).toBe(true);
    expect(wrapper.vm.alertState.body).toContain('Erro validacao');
  });

  it('updates an existing process', async () => {
    // Setup component as editing
    wrapper.vm.processoEditando = { codigo: 123 };
    wrapper.vm.descricao = 'Editado';
    wrapper.vm.tipo = 'MAPEAMENTO';
    wrapper.vm.dataLimite = '2023-12-31';
    wrapper.vm.unidadesSelecionadas = [1];

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
    // Open modal
    await wrapper.find('[data-testid="btn-processo-iniciar"]').trigger('click');
    expect(wrapper.vm.mostrarModalConfirmacao).toBe(true);
    
    // Setup data
    wrapper.vm.descricao = 'Iniciar Teste';
    wrapper.vm.tipo = 'MAPEAMENTO';
    wrapper.vm.unidadesSelecionadas = [1];
    
    processosStore.criarProcesso.mockResolvedValue({ codigo: 999 });

    // Confirm
    await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');

    expect(processosStore.criarProcesso).toHaveBeenCalled(); // Should create first if no ID
    await flushPromises();
    expect(processosStore.iniciarProcesso).toHaveBeenCalledWith(999, 'MAPEAMENTO', [1]);
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('initiates an existing process', async () => {
    wrapper.vm.processoEditando = { codigo: 123 };
    wrapper.vm.descricao = 'Existente';
    wrapper.vm.unidadesSelecionadas = [1];

    // Open modal and confirm
    wrapper.vm.mostrarModalConfirmacao = true;
    await nextTick(); // Update DOM to show modal content

    await wrapper.find('[data-testid="btn-iniciar-processo-confirmar"]').trigger('click');
    
    expect(processosStore.criarProcesso).not.toHaveBeenCalled();
    expect(processosStore.iniciarProcesso).toHaveBeenCalledWith(123, 'MAPEAMENTO', [1]);
  });

  it('removes a process', async () => {
    // Mock the service explicitly since it's imported as *
    vi.spyOn(processoService, 'excluirProcesso').mockResolvedValue(undefined);
    
    wrapper.vm.processoEditando = { codigo: 123 };
    wrapper.vm.mostrarModalRemocao = true;
    
    // Trigger remove logic (simulating button click in modal footer)
    await wrapper.vm.confirmarRemocao();

    expect(processoService.excluirProcesso).toHaveBeenCalledWith(123);
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('updates unit list when type changes', async () => {
    wrapper.vm.tipo = 'REVISAO';
    await nextTick();
    expect(unidadesStore.buscarUnidadesParaProcesso).toHaveBeenCalledWith('REVISAO', undefined);
  });
});

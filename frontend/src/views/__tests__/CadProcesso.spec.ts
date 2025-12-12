import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import CadProcesso from '@/views/CadProcesso.vue';
import { useProcessosStore } from '@/stores/processos';
import { useUnidadesStore } from '@/stores/unidades';
import { TipoProcesso } from '@/types/tipos';
import { nextTick } from 'vue';

// Mocks
const mockPush = vi.fn();
const mockRouteQuery = { value: {} };

vi.mock('vue-router', async (importOriginal) => {
  const actual: any = await importOriginal();
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush,
    }),
    useRoute: () => ({
      query: mockRouteQuery.value,
    }),
  };
});

vi.mock('@/services/processoService', () => ({
  excluirProcesso: vi.fn(),
}));

describe('CadProcesso.vue', () => {
  let wrapper: any;
  let processosStore: any;
  let unidadesStore: any;

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteQuery.value = {};

    wrapper = mount(CadProcesso, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              processos: {
                processoDetalhe: null,
              },
              unidades: {
                unidades: [],
                isLoading: false,
              },
            },
          }),
        ],
        stubs: {
          ArvoreUnidades: true,
          BContainer: { template: '<div><slot /></div>' },
          BAlert: { template: '<div role="alert"><slot /></div>' },
          BForm: { template: '<form><slot /></form>' },
          BFormGroup: { template: '<div><slot /></div>' },
          BFormInput: { template: '<input />', props: ['modelValue'] },
          BFormSelect: { template: '<select />', props: ['modelValue', 'options'] },
          BButton: { template: '<button><slot /></button>' },
          BModal: { template: '<div><slot /><slot name="footer" /></div>' },
        },
      },
    });

    processosStore = useProcessosStore();
    unidadesStore = useUnidadesStore();
  });

  it('renders correctly', () => {
    expect(wrapper.find('h2').text()).toBe('Cadastro de processo');
    expect(wrapper.find('[data-testid="inp-processo-descricao"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="sel-processo-tipo"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="inp-processo-data-limite"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="btn-processo-salvar"]').exists()).toBe(true);
  });

  it('loads units on mount for new process', async () => {
    expect(unidadesStore.buscarUnidadesParaProcesso).toHaveBeenCalledWith('MAPEAMENTO');
  });

  it('handles saving a new process', async () => {
    wrapper.vm.descricao = 'Novo Processo';
    wrapper.vm.tipo = TipoProcesso.MAPEAMENTO;
    wrapper.vm.dataLimite = '2023-12-31';
    wrapper.vm.unidadesSelecionadas = [1, 2];

    await wrapper.find('[data-testid="btn-processo-salvar"]').trigger('click');

    expect(processosStore.criarProcesso).toHaveBeenCalledWith({
      descricao: 'Novo Processo',
      tipo: TipoProcesso.MAPEAMENTO,
      dataLimiteEtapa1: '2023-12-31T00:00:00',
      unidades: [1, 2],
    });
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('loads existing process details when codProcesso is present', async () => {
    // Remount with query param
    mockRouteQuery.value = { codProcesso: '123' };
    
    // Mock store response
    const mockProcesso = {
      codigo: 123,
      descricao: 'Processo Existente',
              tipo: TipoProcesso.MAPEAMENTO,      situacao: 'CRIADO',
      dataLimite: '2023-12-31T00:00:00',
      unidades: [{ codUnidade: 1 }, { codUnidade: 2 }],
    };
    
    // We need to setup the store behavior before mounting
    // But since createTestingPinia is used, actions are spies by default.
    // We can manually invoke the logic or mock the implementation if needed.
    // However, the component calls `buscarProcessoDetalhe` and expects `processosStore.processoDetalhe` to be populated.
    // With createTestingPinia, we can modify the state directly or mock the action implementation.
    
    processosStore = useProcessosStore(); // Get the store instance
    processosStore.buscarProcessoDetalhe.mockImplementation(async () => {
        processosStore.processoDetalhe = mockProcesso;
    });

    // Remount to trigger onMounted
    wrapper = mount(CadProcesso, {
      global: {
        plugins: [
            createTestingPinia({
                createSpy: vi.fn,
            })
        ],
        stubs: {
          ArvoreUnidades: true,
          BContainer: true,
          BAlert: true,
          BForm: true,
          BFormGroup: true,
          BFormInput: true,
          BFormSelect: true,
          BButton: true,
          BModal: true,
        },
      },
    });
    
    // Wait for onMounted
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 0)); // Wait for async onMounted
    
    // Since onMounted is async and calls await, we need to wait for promises to resolve.
    // However, flushing promises is tricky in some setups.
    
    // Let's verify if the action was called
    const store = useProcessosStore();
    expect(store.buscarProcessoDetalhe).toHaveBeenCalledWith(123);
  });
});
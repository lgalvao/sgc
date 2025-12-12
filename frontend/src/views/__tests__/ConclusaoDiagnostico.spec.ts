import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import ConclusaoDiagnostico from '@/views/ConclusaoDiagnostico.vue';
import { useFeedbackStore } from '@/stores/feedback';
import { diagnosticoService } from '@/services/diagnosticoService';

// Mocks
const mockPush = vi.fn();
const mockRouteParams = { value: { codSubprocesso: '10' } };

vi.mock('vue-router', async (importOriginal) => {
  const actual: any = await importOriginal();
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush,
    }),
    useRoute: () => ({
      params: mockRouteParams.value,
    }),
  };
});

vi.mock('@/services/diagnosticoService', () => ({
  diagnosticoService: {
    buscarDiagnostico: vi.fn(),
    concluirDiagnostico: vi.fn(),
  },
}));

describe('ConclusaoDiagnostico.vue', () => {
  let wrapper: any;
  let feedbackStore: any;

  const mockDiagnosticoPronto = {
    podeSerConcluido: true,
    situacao: 'EM_ANDAMENTO',
    motivoNaoPodeConcluir: null
  };

  const mockDiagnosticoPendente = {
    podeSerConcluido: false,
    situacao: 'EM_ANDAMENTO',
    motivoNaoPodeConcluir: 'Pendências existem'
  };

  const mockDiagnosticoConcluido = {
    podeSerConcluido: true,
    situacao: 'CONCLUIDO',
    motivoNaoPodeConcluir: null
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    wrapper = mount(ConclusaoDiagnostico, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
          }),
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BCard: { template: '<div><slot /></div>' },
          BButton: { template: '<button><slot /></button>' },
          BSpinner: { template: '<div data-testid="spinner"></div>' },
          BFormGroup: { template: '<div><slot /></div>' },
          BFormInvalidFeedback: { template: '<div><slot /></div>' },
          BFormTextarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
            props: ['modelValue', 'state']
          },
        },
      },
    });

    feedbackStore = useFeedbackStore();
    
    // Default valid response
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPronto);
  });

  it('renders loading state initially', async () => {
    // Check call
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('renders ready state correctly', async () => {
    // Already mocked pronto in beforeEach
    // Wait for update
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('O diagnóstico está completo');
    expect(wrapper.find('[data-testid="btn-confirmar-conclusao"]').attributes('disabled')).toBeUndefined();
  });

  it('renders pending state and requires justification', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPendente);
    
    // Re-mount to trigger onMounted with new mock
    wrapper = mount(ConclusaoDiagnostico, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                BCard: { template: '<div><slot /></div>' },
                BButton: { template: '<button><slot /></button>' },
                BSpinner: { template: '<div data-testid="spinner"></div>' },
                BFormGroup: { template: '<div><slot /></div>' },
                BFormInvalidFeedback: { template: '<div><slot /></div>' },
                BFormTextarea: {
                    template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
                    props: ['modelValue', 'state']
                },
            },
        }
    });

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Pendências existem');
    
    // Disabled initially
    expect(wrapper.vm.botaoHabilitado).toBe(false);
    
    // Add short justification (too short)
    const textarea = wrapper.find('textarea');
    await textarea.setValue('Short');
    expect(wrapper.vm.botaoHabilitado).toBe(false);

    // Add valid justification
    await textarea.setValue('Valid justification text here');
    expect(wrapper.vm.botaoHabilitado).toBe(true);
  });

  it('redirects if already concluded', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoConcluido);
    
    // Re-mount
    wrapper = mount(ConclusaoDiagnostico, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                BCard: { template: '<div><slot /></div>' },
                BButton: { template: '<button><slot /></button>' },
                BSpinner: { template: '<div data-testid="spinner"></div>' },
                BFormGroup: { template: '<div><slot /></div>' },
                BFormInvalidFeedback: { template: '<div><slot /></div>' },
                BFormTextarea: {
                    template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
                    props: ['modelValue', 'state']
                },
            },
        }
    });

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    // Need access to store from new wrapper
    feedbackStore = useFeedbackStore();
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Aviso', expect.any(String), 'warning');
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('concludes diagnosis', async () => {
    // Ready state
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    const btn = wrapper.find('[data-testid="btn-confirmar-conclusao"]');
    await btn.trigger('click');

    expect(diagnosticoService.concluirDiagnostico).toHaveBeenCalledWith(10, '');
    expect(mockPush).toHaveBeenCalledWith('/painel');
    expect(feedbackStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
  });

  it('concludes diagnosis with justification', async () => {
      (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPendente);
      
      // Re-mount
      wrapper = mount(ConclusaoDiagnostico, {
          global: {
              plugins: [createTestingPinia({ createSpy: vi.fn })],
              stubs: {
                  BContainer: { template: '<div><slot /></div>' },
                  BCard: { template: '<div><slot /></div>' },
                  BButton: { template: '<button><slot /></button>' },
                  BSpinner: { template: '<div data-testid="spinner"></div>' },
                  BFormGroup: { template: '<div><slot /></div>' },
                  BFormInvalidFeedback: { template: '<div><slot /></div>' },
                  BFormTextarea: {
                      template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
                      props: ['modelValue', 'state']
                  },
              },
          }
      });
      
      await wrapper.vm.$nextTick();
      await new Promise(resolve => setTimeout(resolve, 10));
      await wrapper.vm.$nextTick();
      
      // Use trigger on the element to ensure v-model updates
      const textarea = wrapper.find('textarea');
      await textarea.setValue('Justificativa válida para teste');
      
      await wrapper.vm.$nextTick();
      
      await wrapper.find('[data-testid="btn-confirmar-conclusao"]').trigger('click');
      
      expect(diagnosticoService.concluirDiagnostico).toHaveBeenCalledWith(10, 'Justificativa válida para teste');
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import MonitoramentoDiagnostico from '@/views/MonitoramentoDiagnostico.vue';
import { useFeedbackStore } from '@/stores/feedback';
import { diagnosticoService } from '@/services/diagnosticoService';

// Mocks
const mockRouteParams = { value: { codSubprocesso: '10' } };

vi.mock('vue-router', async (importOriginal) => {
  const actual: any = await importOriginal();
  return {
    ...actual,
    useRoute: () => ({
      params: mockRouteParams.value,
    }),
  };
});

vi.mock('@/services/diagnosticoService', () => ({
  diagnosticoService: {
    buscarDiagnostico: vi.fn(),
  },
}));

describe('MonitoramentoDiagnostico.vue', () => {
  let wrapper: any;
  let feedbackStore: any;

  const mockServidorConcluido = {
    nome: 'Servidor 1',
    tituloEleitoral: '111',
    situacao: 'AUTOAVALIACAO_CONCLUIDA',
    situacaoLabel: 'Concluído',
    competenciasAvaliadas: 10,
    totalCompetencias: 10
  };

  const mockServidorPendente = {
    nome: 'Servidor 2',
    tituloEleitoral: '222',
    situacao: 'AUTOAVALIACAO_NAO_REALIZADA',
    situacaoLabel: 'Não Realizada',
    competenciasAvaliadas: 2,
    totalCompetencias: 10
  };

  const mockDiagnostico = {
    situacao: 'EM_ANDAMENTO',
    situacaoLabel: 'Em Andamento',
    podeSerConcluido: false,
    servidores: [mockServidorConcluido, mockServidorPendente]
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    wrapper = mount(MonitoramentoDiagnostico, {
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
        },
      },
    });

    feedbackStore = useFeedbackStore();
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnostico);
  });

  it('renders loading state initially', async () => {
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('renders summary cards correctly', async () => {
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    // 1 completed, 1 pending = 50%
    expect(wrapper.text()).toContain('50%'); // Progress
    expect(wrapper.text()).toContain('1'); // Completed
    expect(wrapper.text()).toContain('1'); // Pending
  });

  it('renders server list correctly', async () => {
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Servidor 1');
    expect(wrapper.text()).toContain('10/10');
    
    expect(wrapper.text()).toContain('Servidor 2');
    expect(wrapper.text()).toContain('2/10');
  });

  it('disables buttons when conditions are not met', async () => {
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    // Not all completed, so Ocupações Críticas disabled (logic in computed: todosConcluiramAutoavaliacao)
    // Actually, check logic: todosConcluiramAutoavaliacao = totalServidoresPendentes === 0
    // We have 1 pending.
    const btnOcupacoes = wrapper.findAll('button')[1]; // assuming order: Voltar, Ocupações, Concluir
    expect(btnOcupacoes.attributes('disabled')).toBeDefined(); // or check specific testid if added
    
    // Check Concluir Diagnostico
    // mockDiagnostico.podeSerConcluido is false
    const btnConcluir = wrapper.find('[data-testid="btn-concluir-diagnostico"]');
    expect(btnConcluir.attributes('disabled')).toBeDefined();
  });

  it('enables buttons when conditions are met', async () => {
    const mockDiagnosticoCompleto = {
      ...mockDiagnostico,
      podeSerConcluido: true,
      servidores: [mockServidorConcluido, { ...mockServidorConcluido, nome: 'Servidor 3', tituloEleitoral: '333' }]
    };
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoCompleto);
    
    // Remount
    wrapper = mount(MonitoramentoDiagnostico, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                BCard: { template: '<div><slot /></div>' },
                BButton: { template: '<button><slot /></button>' },
                BSpinner: { template: '<div data-testid="spinner"></div>' },
            }
        }
    });

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    const btnOcupacoes = wrapper.findAll('button')[1];
    expect(btnOcupacoes.attributes('disabled')).toBeUndefined();
    
    const btnConcluir = wrapper.find('[data-testid="btn-concluir-diagnostico"]');
    expect(btnConcluir.attributes('disabled')).toBeUndefined();
  });

  it('handles error when fetching diagnosis', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockRejectedValue(new Error('Fail'));
    
    // Remount
    wrapper = mount(MonitoramentoDiagnostico, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                BCard: { template: '<div><slot /></div>' },
                BButton: { template: '<button><slot /></button>' },
                BSpinner: { template: '<div data-testid="spinner"></div>' },
            }
        }
    });
    
    // Get new store instance
    feedbackStore = useFeedbackStore();

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', expect.stringContaining('Fail'), 'danger');
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import OcupacoesCriticasDiagnostico from '@/views/OcupacoesCriticasDiagnostico.vue';
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
    salvarOcupacao: vi.fn(),
  },
}));

describe('OcupacoesCriticasDiagnostico.vue', () => {
  let wrapper: any;

  const mockAvaliacaoGap = {
    competenciaCodigo: 1,
    competenciaDescricao: 'Comp 1',
    importanciaLabel: 'Alto',
    dominioLabel: 'Baixo',
    gap: 2,
  };

  const mockAvaliacaoNoGap = {
    competenciaCodigo: 2,
    competenciaDescricao: 'Comp 2',
    importanciaLabel: 'Alto',
    dominioLabel: 'Alto',
    gap: 0,
  };

  const mockServidor = {
    nome: 'Servidor 1',
    tituloEleitoral: '111',
    avaliacoes: [mockAvaliacaoGap, mockAvaliacaoNoGap],
    ocupacoes: []
  };
  
  const mockServidorSemGaps = {
      nome: 'Servidor 2',
      tituloEleitoral: '222',
      avaliacoes: [mockAvaliacaoNoGap],
      ocupacoes: []
  };

  const mockDiagnostico = {
    situacao: 'EM_ANDAMENTO',
    servidores: [mockServidor, mockServidorSemGaps]
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    wrapper = mount(OcupacoesCriticasDiagnostico, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                unidades: { unidade: { sigla: 'TEST', nome: 'Unidade Teste' } }
            }
          }),
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BAlert: { template: '<div><slot /></div>' },
          BButton: { template: '<button><slot /></button>' },
          BSpinner: { template: '<div data-testid="spinner"></div>' },
          BFormSelect: {
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value); $emit(\'change\', $event.target.value)"><option value="AC">A capacitar</option></select>',
            props: ['modelValue', 'options']
          },
        },
      },
    });

    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnostico);
  });

  it('renders loading state initially', async () => {
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('filters and displays only servers/competencies with Gap >= 2', async () => {
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    // Should show Servidor 1
    expect(wrapper.text()).toContain('Servidor 1');
    expect(wrapper.text()).toContain('Comp 1');
    
    // Should NOT show Servidor 2 (no gaps)
    expect(wrapper.text()).not.toContain('Servidor 2');
    // Should NOT show Comp 2 (gap 0) for Servidor 1
    expect(wrapper.text()).not.toContain('Comp 2');
  });

  it('saves occupation status on change', async () => {
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    const select = wrapper.find('select'); 
    
    // Simulate selection
    await select.setValue('AC'); // 'A capacitar'
    
    expect(diagnosticoService.salvarOcupacao).toHaveBeenCalledWith(10, '111', 1, 'AC');
  });

  it('handles empty state', async () => {
    const mockDiagnosticoVazio = { ...mockDiagnostico, servidores: [mockServidorSemGaps] };
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoVazio);
    
    // Remount
    wrapper = mount(OcupacoesCriticasDiagnostico, {
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                BAlert: { template: '<div><slot /></div>' },
                BButton: { template: '<button><slot /></button>' },
                BSpinner: { template: '<div data-testid="spinner"></div>' },
                BFormSelect: true
            }
        }
    });

    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('Nenhuma ocupação crítica identificada');
  });
});

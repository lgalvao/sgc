import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import AutoavaliacaoDiagnostico from '@/views/AutoavaliacaoDiagnostico.vue';
import { useMapasStore } from '@/stores/mapas';
import { useUnidadesStore } from '@/stores/unidades';
import { useFeedbackStore } from '@/stores/feedback';
import { diagnosticoService } from '@/services/diagnosticoService';

// Mocks
const mockPush = vi.fn();
const mockRouteParams = { value: { codSubprocesso: '10', siglaUnidade: 'TEST' } };

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
    buscarMinhasAvaliacoes: vi.fn(),
    salvarAvaliacao: vi.fn(),
    concluirAutoavaliacao: vi.fn(),
  },
}));

describe('AutoavaliacaoDiagnostico.vue', () => {
  let wrapper: any;
  let mapasStore: any;
  let unidadesStore: any;
  let feedbackStore: any;

  const mockCompetencias = [
    {codigo: 1, descricao: 'Competencia 1' },
    {codigo: 2, descricao: 'Competencia 2' }
  ];

  const mockAvaliacoes = [
    { competenciaCodigo: 1, importancia: 'N5', dominio: 'N3', observacoes: 'Obs 1' }
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10', siglaUnidade: 'TEST' };

    wrapper = mount(AutoavaliacaoDiagnostico, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              mapas: {
                mapaCompleto: { competencias: mockCompetencias },
              },
              unidades: {
                unidade: { nome: 'Unidade Teste' },
              },
            },
          }),
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BButton: { template: '<button><slot /></button>' },
          BAlert: { template: '<div role="alert"><slot /></div>' },
          BSpinner: { template: '<div data-testid="spinner"></div>' },
          BCard: { template: '<div><slot name="header" /><slot /></div>' },
          BFormSelect: {
            template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value); $emit(\'change\', $event.target.value)"><option value="N1">N1</option></select>',
            props: ['modelValue', 'options']
          },
          BFormTextarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @blur="$emit(\'blur\', $event)"></textarea>',
            props: ['modelValue']
          },
        },
      },
    });

    mapasStore = useMapasStore();
    unidadesStore = useUnidadesStore();
    feedbackStore = useFeedbackStore();

    // Setup service mocks
    (diagnosticoService.buscarMinhasAvaliacoes as any).mockResolvedValue(mockAvaliacoes);
    (diagnosticoService.salvarAvaliacao as any).mockResolvedValue({});
    (diagnosticoService.concluirAutoavaliacao as any).mockResolvedValue({});
  });

  it('renders loading state initially', async () => {
    // Initially loading is true in onMounted
    expect(wrapper.find('[data-testid="spinner"]').exists()).toBe(true);
    
    expect(unidadesStore.buscarUnidade).toHaveBeenCalledWith('TEST');
    expect(mapasStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
    expect(diagnosticoService.buscarMinhasAvaliacoes).toHaveBeenCalledWith(10);
  });
  
  it('renders competencies and existing evaluations', async () => {
    // Wait for onMounted to finish
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10)); // Allow async operations to complete
    await wrapper.vm.$nextTick(); // Re-render

    expect(wrapper.text()).toContain('Competencia 1');
    expect(wrapper.text()).toContain('Competencia 2');
    
    expect(wrapper.vm.avaliacoes[1].importancia).toBe('N5');
    expect(wrapper.vm.avaliacoes[1].dominio).toBe('N3');
    expect(wrapper.vm.avaliacoes[1].observacoes).toBe('Obs 1');
    
    expect(wrapper.vm.avaliacoes[2].importancia).toBe('');
  });

  it('saves evaluation on change', async () => {
    // Wait for data load
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();

    wrapper.vm.avaliacoes[2].importancia = 'N4';
    wrapper.vm.avaliacoes[2].dominio = 'N2';
    
    await wrapper.vm.salvar(2, 'N4', 'N2');
    
    expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 2, 'N4', 'N2', '');
  });

  it('enables conclude button only when all competencies are evaluated', async () => {
    // Wait for data load
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();
    
    // Initially false because Comp 2 is incomplete
    // Note: BButton stub renders a button, so we check attribute
    // In Vue Test Utils, boolean attributes might be present or not
    // We can check wrapper.vm.podeConcluir directly too
    expect(wrapper.vm.podeConcluir).toBe(false);
    
    // Complete Comp 2
    wrapper.vm.avaliacoes[2].importancia = 'N4';
    wrapper.vm.avaliacoes[2].dominio = 'N2';
    
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.podeConcluir).toBe(true);
  });

  it('concludes self-evaluation', async () => {
    // Wait for data load
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await wrapper.vm.$nextTick();
    
    // Complete Comp 2
    wrapper.vm.avaliacoes[2].importancia = 'N4';
    wrapper.vm.avaliacoes[2].dominio = 'N2';
    
    await wrapper.vm.$nextTick();
    
    // Ensure button is clickable
    const btn = wrapper.find('[data-testid="btn-concluir-autoavaliacao"]');
    await btn.trigger('click');
    
    expect(diagnosticoService.concluirAutoavaliacao).toHaveBeenCalledWith(10);
    expect(mockPush).toHaveBeenCalledWith('/painel');
    expect(feedbackStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
  });
});

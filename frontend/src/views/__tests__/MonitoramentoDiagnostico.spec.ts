import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import MonitoramentoDiagnostico from '@/views/MonitoramentoDiagnostico.vue';
import {useFeedbackStore} from '@/stores/feedback';
import {diagnosticoService} from '@/services/diagnosticoService';
import {setupComponentTest, getCommonMountOptions} from '@/test-utils/componentTestHelpers';

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
  const ctx = setupComponentTest();
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

  const stubs = {
    BContainer: { template: '<div><slot /></div>' },
    BCard: { template: '<div><slot /></div>' },
    BButton: { template: '<button><slot /></button>' },
    BSpinner: { template: '<div data-testid="spinner"></div>' },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(MonitoramentoDiagnostico, mountOptions);

    feedbackStore = useFeedbackStore();
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnostico);
  });

  it('exibe estado de carregamento inicialmente', async () => {
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('exibe cards de resumo corretamente', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    // 1 concluído, 1 pendente = 50%
    expect(ctx.wrapper!.text()).toContain('50%');
    expect(ctx.wrapper!.text()).toContain('1');
  });

  it('exibe lista de servidores corretamente', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.text()).toContain('Servidor 1');
    expect(ctx.wrapper!.text()).toContain('10/10');
    
    expect(ctx.wrapper!.text()).toContain('Servidor 2');
    expect(ctx.wrapper!.text()).toContain('2/10');
  });

  it('desabilita botões quando condições não são atendidas', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    const btnOcupacoes = ctx.wrapper!.findAll('button')[1];
    expect(btnOcupacoes.attributes('disabled')).toBeDefined();
    
    const btnConcluir = ctx.wrapper!.find('[data-testid="btn-concluir-diagnostico"]');
    expect(btnConcluir.attributes('disabled')).toBeDefined();
  });

  it('habilita botões quando condições são atendidas', async () => {
    const mockDiagnosticoCompleto = {
      ...mockDiagnostico,
      podeSerConcluido: true,
      servidores: [mockServidorConcluido, { ...mockServidorConcluido, nome: 'Servidor 3', tituloEleitoral: '333' }]
    };
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoCompleto);
    
    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(MonitoramentoDiagnostico, mountOptions);

    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    const btnOcupacoes = ctx.wrapper!.findAll('button')[1];
    expect(btnOcupacoes.attributes('disabled')).toBeUndefined();
    
    const btnConcluir = ctx.wrapper!.find('[data-testid="btn-concluir-diagnostico"]');
    expect(btnConcluir.attributes('disabled')).toBeUndefined();
  });

  it('trata erro ao buscar diagnóstico', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockRejectedValue(new Error('Falha'));
    
    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(MonitoramentoDiagnostico, mountOptions);
    
    feedbackStore = useFeedbackStore();

    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', expect.stringContaining('Falha'), 'danger');
  });
});

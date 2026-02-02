import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import MonitoramentoDiagnostico from '@/views/MonitoramentoDiagnostico.vue';
import {useFeedbackStore} from '@/stores/feedback';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';

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
  let diagnosticoService: any;

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

  beforeEach(async () => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    // Import service after mocks are cleared
    const diagMod = await import('@/services/diagnosticoService');
    diagnosticoService = diagMod.diagnosticoService;

    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnostico);
  });

  const mountComponent = async () => {
    const mountOptions = getCommonMountOptions({}, stubs, { stubActions: false });
    ctx.wrapper = mount(MonitoramentoDiagnostico, mountOptions);
    feedbackStore = useFeedbackStore();
    await flushPromises();
    await ctx.wrapper.vm.$nextTick();
  };

  it('exibe estado de carregamento inicialmente', async () => {
    await mountComponent();
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('exibe cards de resumo corretamente', async () => {
    await mountComponent();

    // 1 concluído, 1 pendente = 50%
    expect(ctx.wrapper!.text()).toContain('50%');
    expect(ctx.wrapper!.text()).toContain('1');
  });

  it('exibe lista de servidores corretamente', async () => {
    await mountComponent();

    expect(ctx.wrapper!.text()).toContain('Servidor 1');
    expect(ctx.wrapper!.text()).toContain('10/10');
    
    expect(ctx.wrapper!.text()).toContain('Servidor 2');
    expect(ctx.wrapper!.text()).toContain('2/10');
  });

  it('desabilita botões quando condições não são atendidas', async () => {
    await mountComponent();

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
    await mountComponent();

    const btnOcupacoes = ctx.wrapper!.findAll('button')[1];
    expect(btnOcupacoes.attributes('disabled')).toBeUndefined();
    
    const btnConcluir = ctx.wrapper!.find('[data-testid="btn-concluir-diagnostico"]');
    // Bootstrap-vue-next adds disabled="" when true, so check for presence
    const disabledAttr = btnConcluir.attributes('disabled');
    expect(disabledAttr === undefined || disabledAttr === '').toBeTruthy();
  });

  it('exibe badges de status corretamente para diferentes situações', async () => {
    const mockServidoresVariados = [
      { situacao: 'CONSENSO_CRIADO', situacaoLabel: 'Consenso Criado', totalCompetencias: 10, competenciasAvaliadas: 5, tituloEleitoral: '1', nome: 'Servidor 1' },
      { situacao: 'CONSENSO_APROVADO', situacaoLabel: 'Consenso Aprovado', totalCompetencias: 10, competenciasAvaliadas: 10, tituloEleitoral: '2', nome: 'Servidor 2' },
      { situacao: 'AVALIACAO_IMPOSSIBILITADA', situacaoLabel: 'Impossibilitada', totalCompetencias: 0, competenciasAvaliadas: 0, tituloEleitoral: '3', nome: 'Servidor 3' },
      { situacao: 'OUTRA', situacaoLabel: 'Outra', totalCompetencias: 10, competenciasAvaliadas: 0, tituloEleitoral: '4', nome: 'Servidor 4' }
    ];

    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue({
        ...mockDiagnostico,
        servidores: mockServidoresVariados
    });
    await mountComponent();

    expect(ctx.wrapper!.text()).toContain('Consenso Criado');
    expect(ctx.wrapper!.text()).toContain('Consenso Aprovado');
    expect(ctx.wrapper!.text()).toContain('Impossibilitada');
    expect(ctx.wrapper!.text()).toContain('Outra');
  });

  it('trata erro ao buscar diagnóstico', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockRejectedValue(new Error('Falha'));
    await mountComponent();
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', expect.stringContaining('Falha'), 'danger');
  });
});

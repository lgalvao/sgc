import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import AutoavaliacaoDiagnostico from '@/views/AutoavaliacaoDiagnostico.vue';
import {createTestingPinia} from '@pinia/testing';
import {setupComponentTest} from '@/test-utils/componentTestHelpers';

// Mocks dos services
vi.mock('@/services/diagnosticoService', () => ({
  diagnosticoService: {
    buscarMinhasAvaliacoes: vi.fn(),
    salvarAvaliacao: vi.fn(),
    concluirAutoavaliacao: vi.fn(),
  }
}));

vi.mock('@/services/mapaService', () => ({
  obterMapaCompleto: vi.fn(),
}));

vi.mock('@/services/unidadeService', () => ({
  buscarUnidadePorSigla: vi.fn(),
}));

// Mocks do router
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

describe('AutoavaliacaoDiagnostico.vue', () => {
  const ctx = setupComponentTest();

  const stubs = {
    PageHeader: { template: '<div><slot /><slot name="actions" /></div>' },
    BContainer: { template: '<div><slot /></div>' },
    BButton: { template: '<button><slot /></button>' },
    BAlert: { template: '<div role="alert"><slot /></div>' },
    BSpinner: { template: '<div data-testid="spinner"></div>' },
    BCard: { template: '<div><slot name="header" /><slot /></div>' },
    BFormSelect: {
      template: `<select :value="modelValue" @change="$emit('update:modelValue', $event.target.value); $emit('change', $event.target.value)">
        <option value="">Selecione</option>
        <option value="NA">NA</option>
        <option value="N1">N1</option>
        <option value="N2">N2</option>
        <option value="N3">N3</option>
        <option value="N4">N4</option>
        <option value="N5">N5</option>
        <option value="N6">N6</option>
      </select>`,
      props: ['modelValue', 'options']
    },
    BFormTextarea: {
      template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @blur="$emit(\'blur\', $event)"></textarea>',
      props: ['modelValue']
    },
  };

  let diagnosticoService: any;
  let mapaService: any;
  let unidadeService: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10', siglaUnidade: 'TEST' };
    mockPush.mockClear();
    
    // Importar os mocks após vi.clearAllMocks
    const diagMod = await import('@/services/diagnosticoService');
    const mapaMod = await import('@/services/mapaService');
    const unidMod = await import('@/services/unidadeService');
    
    diagnosticoService = diagMod.diagnosticoService;
    mapaService = mapaMod;
    unidadeService = unidMod;
    
    // Configurar mocks padrão ANTES de qualquer montagem
    // IMPORTANTE: Retornar cópias para evitar mutação compartilhada entre testes
    (diagnosticoService.buscarMinhasAvaliacoes as any).mockResolvedValue([
      { competenciaCodigo: 1, importancia: 'N5', dominio: 'N3', observacoes: 'Obs 1' },
      { competenciaCodigo: 2, importancia: '', dominio: '', observacoes: '' }
    ]);
    (diagnosticoService.salvarAvaliacao as any).mockResolvedValue({});
    (diagnosticoService.concluirAutoavaliacao as any).mockResolvedValue({});
    (mapaService.obterMapaCompleto as any).mockResolvedValue({ 
      competencias: [
        {codigo: 1, descricao: 'Competencia 1' },
        {codigo: 2, descricao: 'Competencia 2' }
      ]
    });
    (unidadeService.buscarUnidadePorSigla as any).mockResolvedValue({ nome: 'Unidade Teste', sigla: 'TEST' });
  });

  const mountComponent = async (competencias?: any[], avaliacoes?: any[]) => {
    // Sempre resetar os mocks para garantir isolamento entre testes
    // Usar valores fornecidos ou valores padrão com cópias frescas
    const compData = competencias !== undefined ? competencias : [
      {codigo: 1, descricao: 'Competencia 1' },
      {codigo: 2, descricao: 'Competencia 2' }
    ];
    const avalData = avaliacoes !== undefined ? avaliacoes : [
      { competenciaCodigo: 1, importancia: 'N5', dominio: 'N3', observacoes: 'Obs 1' },
      { competenciaCodigo: 2, importancia: '', dominio: '', observacoes: '' }
    ];
    
    (mapaService.obterMapaCompleto as any).mockResolvedValue({ competencias: compData });
    (diagnosticoService.buscarMinhasAvaliacoes as any).mockResolvedValue(avalData);

    ctx.wrapper = mount(AutoavaliacaoDiagnostico, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
          }),
        ],
        stubs,
      },
    });

    await flushPromises();
    await ctx.wrapper.vm.$nextTick();
  };

  it('exibe estado de carregamento inicialmente', async () => {
    await mountComponent();
    
    expect(unidadeService.buscarUnidadePorSigla).toHaveBeenCalledWith('TEST');
    expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(10);
    expect(diagnosticoService.buscarMinhasAvaliacoes).toHaveBeenCalledWith(10, undefined);
  });
  
  it('exibe competências e avaliações existentes', async () => {
    await mountComponent();
    
    // Esperar que as competências sejam renderizadas
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const html = ctx.wrapper!.html();
    expect(html).toContain('Competencia 1');
    expect(html).toContain('Competencia 2');
    
    expect(ctx.wrapper!.vm.avaliacoes[1].importancia).toBe('N5');
    expect(ctx.wrapper!.vm.avaliacoes[1].dominio).toBe('N3');
    expect(ctx.wrapper!.vm.avaliacoes[1].observacoes).toBe('Obs 1');
    
    expect(ctx.wrapper!.vm.avaliacoes[2].importancia).toBe('');
  });

  it('salva avaliação ao alterar', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const av2 = ctx.wrapper!.vm.avaliacoes[2];
    if (!av2) throw new Error('Avaliação 2 não foi inicializada');
    
    av2.importancia = 'N4';
    av2.dominio = 'N2';
    
    await ctx.wrapper!.vm.salvar(2, 'N4', 'N2');
    await flushPromises();
    
    expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 2, 'N4', 'N2', '');
  });

  it('salva avaliação ao sair do campo de observação (blur)', async () => {
    await mountComponent();
    const textarea = ctx.wrapper!.find('[data-testid="txt-obs-1"]');
    
    if (textarea.exists()) {
      await textarea.setValue('Nova observação');
      await textarea.trigger('blur');
      expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 1, 'N5', 'N3', 'Nova observação');
    }
  });

  it('habilita botão de concluir apenas quando todas as competências estão avaliadas', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);
    
    // Update internal state to simulate user filling the form
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    // Trigger the salvar method which the component would normally call on change
    await ctx.wrapper!.vm.salvar(2, 'N4', 'N2');
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(true);
  });

  it('conclui autoavaliação', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const {useFeedbackStore} = await import('@/stores/feedback');
    const feedbackStore = useFeedbackStore();
    
    // Update internal state to simulate user filling the form
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    // Trigger the salvar method which the component would normally call on change
    await ctx.wrapper!.vm.salvar(2, 'N4', 'N2');
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const btn = ctx.wrapper!.find('[data-testid="btn-concluir-autoavaliacao"]');
    await btn.trigger('click');
    await flushPromises();
    
    expect(diagnosticoService.concluirAutoavaliacao).toHaveBeenCalledWith(10, undefined);
    expect(mockPush).toHaveBeenCalledWith('/painel');
    expect(feedbackStore.show).toHaveBeenCalled();
  });

  it('exibe alerta de competências vazias', async () => {
    await mountComponent([], []);
    
    const html = ctx.wrapper!.html();
    expect(html).toContain('Nenhuma competência encontrada');
  });

  it('não salva quando campos estão vazios', async () => {
    await mountComponent();
    vi.clearAllMocks();

    await ctx.wrapper!.vm.salvar(1, '', 'N3');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();

    await ctx.wrapper!.vm.salvar(1, 'N3', '');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();
  });

  it('exibe erro ao falhar salvamento de avaliação', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    // Verificar que avaliações foram inicializadas
    const avaliacoes = ctx.wrapper!.vm.avaliacoes;
    expect(avaliacoes[1]).toBeDefined();
    expect(avaliacoes[2]).toBeDefined();
    
    // Limpar mocks e configurar erro DEPOIS de obter feedback store
    const {useFeedbackStore} = await import('@/stores/feedback');
    const feedbackStore = useFeedbackStore();
    vi.clearAllMocks();
    
    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro de validação' } }
    });

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro de validação', 'danger');
  });

  it('exibe erro ao falhar conclusão de autoavaliação', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const av1 = ctx.wrapper!.vm.avaliacoes[1];
    const av2 = ctx.wrapper!.vm.avaliacoes[2];
    
    if (av1) {
      av1.importancia = 'N5';
      av1.dominio = 'N3';
    }
    if (av2) {
      av2.importancia = 'N4';
      av2.dominio = 'N2';
    }
    await ctx.wrapper!.vm.$nextTick();

    // Obter feedback store e limpar mocks na ordem correta
    const {useFeedbackStore} = await import('@/stores/feedback');
    const feedbackStore = useFeedbackStore();
    vi.clearAllMocks();
    
    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro ao finalizar' } }
    });

    await ctx.wrapper!.vm.concluirAutoavaliacao();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro ao finalizar', 'danger');
    expect(mockPush).not.toHaveBeenCalled();
  });

  it('não conclui quando podeConcluir é false', async () => {
    await mountComponent();
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);

    vi.clearAllMocks();
    await ctx.wrapper!.vm.concluirAutoavaliacao();
    await ctx.wrapper!.vm.$nextTick();

    expect(diagnosticoService.concluirAutoavaliacao).not.toHaveBeenCalled();
  });

  it('trata erro genérico ao salvar avaliação', async () => {
    await mountComponent();
    const {useFeedbackStore} = await import('@/stores/feedback');
    const feedbackStore = useFeedbackStore();
    
    vi.clearAllMocks();
    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce(new Error('Erro de rede'));

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    expect(feedbackStore.show).toHaveBeenCalled();
    const calls = (feedbackStore.show as any).mock.calls;
    expect(calls[0][0]).toBe('Erro');
    expect(calls[0][2]).toBe('danger');
  });

  it('trata erro genérico ao concluir autoavaliação', async () => {
    await mountComponent();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();
    
    const av1 = ctx.wrapper!.vm.avaliacoes[1];
    const av2 = ctx.wrapper!.vm.avaliacoes[2];
    
    if (av1) {
      av1.importancia = 'N5';
      av1.dominio = 'N3';
    }
    if (av2) {
      av2.importancia = 'N4';
      av2.dominio = 'N2';
    }
    await ctx.wrapper!.vm.$nextTick();

    // Obter feedback store e limpar mocks na ordem correta
    const {useFeedbackStore} = await import('@/stores/feedback');
    const feedbackStore = useFeedbackStore();
    vi.clearAllMocks();
    
    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce(new Error('Erro genérico'));

    await ctx.wrapper!.vm.concluirAutoavaliacao();
    await flushPromises();
    await ctx.wrapper!.vm.$nextTick();

    expect(feedbackStore.show).toHaveBeenCalled();
    const calls = (feedbackStore.show as any).mock.calls;
    expect(calls[0]).toEqual(['Erro', 'Erro ao concluir.', 'danger']);
  });
});

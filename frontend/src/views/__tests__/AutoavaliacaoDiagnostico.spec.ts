import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import AutoavaliacaoDiagnostico from '@/views/AutoavaliacaoDiagnostico.vue';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useFeedbackStore} from '@/stores/feedback';
import {diagnosticoService} from '@/services/diagnosticoService';
import {setupComponentTest, getCommonMountOptions} from '@/test-utils/componentTestHelpers';

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
  const ctx = setupComponentTest();
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

  const stubs = {
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
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10', siglaUnidade: 'TEST' };

    const mountOptions = getCommonMountOptions({
      mapas: {
        mapaCompleto: { competencias: mockCompetencias },
      },
      unidades: {
        unidade: { nome: 'Unidade Teste' },
      },
    }, stubs);

    ctx.wrapper = mount(AutoavaliacaoDiagnostico, mountOptions);

    mapasStore = useMapasStore();
    unidadesStore = useUnidadesStore();
    feedbackStore = useFeedbackStore();

    // Setup service mocks
    (diagnosticoService.buscarMinhasAvaliacoes as any).mockResolvedValue(mockAvaliacoes);
    (diagnosticoService.salvarAvaliacao as any).mockResolvedValue({});
    (diagnosticoService.concluirAutoavaliacao as any).mockResolvedValue({});
  });

  it('exibe estado de carregamento inicialmente', async () => {
    expect(ctx.wrapper!.find('[data-testid="spinner"]').exists()).toBe(true);
    
    expect(unidadesStore.buscarUnidade).toHaveBeenCalledWith('TEST');
    expect(mapasStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
    expect(diagnosticoService.buscarMinhasAvaliacoes).toHaveBeenCalledWith(10);
  });
  
  it('exibe competências e avaliações existentes', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.text()).toContain('Competencia 1');
    expect(ctx.wrapper!.text()).toContain('Competencia 2');
    
    expect(ctx.wrapper!.vm.avaliacoes[1].importancia).toBe('N5');
    expect(ctx.wrapper!.vm.avaliacoes[1].dominio).toBe('N3');
    expect(ctx.wrapper!.vm.avaliacoes[1].observacoes).toBe('Obs 1');
    
    expect(ctx.wrapper!.vm.avaliacoes[2].importancia).toBe('');
  });

  it('salva avaliação ao alterar', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    await ctx.wrapper!.vm.salvar(2, 'N4', 'N2');
    
    expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 2, 'N4', 'N2', '');
  });

  it('habilita botão de concluir apenas quando todas as competências estão avaliadas', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);
    
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    await ctx.wrapper!.vm.$nextTick();
    expect(ctx.wrapper!.vm.podeConcluir).toBe(true);
  });

  it('conclui autoavaliação', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();
    
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    await ctx.wrapper!.vm.$nextTick();
    
    const btn = ctx.wrapper!.find('[data-testid="btn-concluir-autoavaliacao"]');
    await btn.trigger('click');
    
    expect(diagnosticoService.concluirAutoavaliacao).toHaveBeenCalledWith(10);
    expect(mockPush).toHaveBeenCalledWith('/painel');
    expect(feedbackStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
  });

  it('exibe alerta de competências vazias', async () => {
    // Mount with empty competencias
    const emptyMountOptions = getCommonMountOptions({
      mapas: {
        mapaCompleto: { competencias: [] },
      },
      unidades: {
        unidade: { nome: 'Unidade Teste' },
      },
    }, stubs);

    ctx.wrapper = mount(AutoavaliacaoDiagnostico, emptyMountOptions);
    
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    // Set loading to false manually to see the "empty" state
    ctx.wrapper!.vm.loading = false;
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.text()).toContain('Nenhuma competência encontrada');
  });

  it('não salva quando campos estão vazios', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    // Call salvar with empty values - should return early
    await ctx.wrapper!.vm.salvar(1, '', 'N3');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();

    await ctx.wrapper!.vm.salvar(1, 'N3', '');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();
  });

  it('exibe erro ao falhar salvamento de avaliação', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro de validação' } }
    });

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro de validação', 'danger');
  });

  it('exibe erro ao falhar conclusão de autoavaliação', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    ctx.wrapper!.vm.avaliacoes[1].importancia = 'N5';
    ctx.wrapper!.vm.avaliacoes[1].dominio = 'N3';
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    await ctx.wrapper!.vm.$nextTick();

    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro ao finalizar' } }
    });

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro ao finalizar', 'danger');
    expect(mockPush).not.toHaveBeenCalled();
  });

  it('não conclui quando podeConcluir é false', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    // Ensure not all ratings are filled
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(diagnosticoService.concluirAutoavaliacao).not.toHaveBeenCalled();
  });

  it('trata erro genérico ao salvar avaliação', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce(new Error('Erro de rede'));

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro de rede', 'danger');
  });

  it('trata erro genérico ao concluir autoavaliação', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    ctx.wrapper!.vm.avaliacoes[1].importancia = 'N5';
    ctx.wrapper!.vm.avaliacoes[1].dominio = 'N3';
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    await ctx.wrapper!.vm.$nextTick();

    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce(new Error('Erro genérico'));

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro ao concluir.', 'danger');
  });
});

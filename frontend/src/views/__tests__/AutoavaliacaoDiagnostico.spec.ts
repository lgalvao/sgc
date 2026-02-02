import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import AutoavaliacaoDiagnostico from '@/views/AutoavaliacaoDiagnostico.vue';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useFeedbackStore} from '@/stores/feedback';
import {diagnosticoService} from '@/services/diagnosticoService';
import * as mapaService from '@/services/mapaService';
import * as unidadeService from '@/services/unidadeService';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';

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

vi.mock('@/services/mapaService', () => ({
  obterMapaCompleto: vi.fn(),
}));

vi.mock('@/services/unidadeService', () => ({
  buscarUnidade: vi.fn(),
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
    { competenciaCodigo: 1, importancia: 'N5', dominio: 'N3', observacoes: 'Obs 1' },
    { competenciaCodigo: 2, importancia: '', dominio: '', observacoes: '' }
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

  beforeEach(async () => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10', siglaUnidade: 'TEST' };

    // Setup service mocks BEFORE mounting
    (diagnosticoService.buscarMinhasAvaliacoes as any).mockResolvedValue(mockAvaliacoes);
    (diagnosticoService.salvarAvaliacao as any).mockResolvedValue({});
    (diagnosticoService.concluirAutoavaliacao as any).mockResolvedValue({});
    (mapaService.obterMapaCompleto as any).mockResolvedValue({ competencias: mockCompetencias });
    (unidadeService.buscarUnidade as any).mockResolvedValue({ nome: 'Unidade Teste' });

    const mountOptions = getCommonMountOptions({}, stubs, { stubActions: false });

    ctx.wrapper = mount(AutoavaliacaoDiagnostico, mountOptions);

    mapasStore = useMapasStore();
    unidadesStore = useUnidadesStore();
    feedbackStore = useFeedbackStore();
    
    // Wait for all async operations to complete
    await ctx.wrapper.vm.$nextTick();
    await ctx.wrapper.vm.$nextTick();
  });

  it('exibe estado de carregamento inicialmente', async () => {
    expect(unidadeService.buscarUnidade).toHaveBeenCalledWith('TEST');
    expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(10);
    expect(diagnosticoService.buscarMinhasAvaliacoes).toHaveBeenCalledWith(10, undefined);
  });
  
  it('exibe competências e avaliações existentes', async () => {
    expect(ctx.wrapper!.text()).toContain('Competencia 1');
    expect(ctx.wrapper!.text()).toContain('Competencia 2');
    
    expect(ctx.wrapper!.vm.avaliacoes[1].importancia).toBe('N5');
    expect(ctx.wrapper!.vm.avaliacoes[1].dominio).toBe('N3');
    expect(ctx.wrapper!.vm.avaliacoes[1].observacoes).toBe('Obs 1');
    
    expect(ctx.wrapper!.vm.avaliacoes[2].importancia).toBe('');
  });

  it('salva avaliação ao alterar', async () => {
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    await ctx.wrapper!.vm.salvar(2, 'N4', 'N2');
    
    expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 2, 'N4', 'N2', '');
  });

  it('salva avaliação ao sair do campo de observação (blur)', async () => {
    const textarea = ctx.wrapper!.find('[data-testid="txt-obs-1"]');

    // Set value triggers input -> v-model update
    await textarea.setValue('Nova observação');

    // Trigger blur
    await textarea.trigger('blur');

    // Expect salvarAvaliacao to be called with existing importance/domain (N5, N3) and new observation
    expect(diagnosticoService.salvarAvaliacao).toHaveBeenCalledWith(10, 1, 'N5', 'N3', 'Nova observação');
  });

  it('habilita botão de concluir apenas quando todas as competências estão avaliadas', async () => {
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);
    
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    expect(ctx.wrapper!.vm.podeConcluir).toBe(true);
  });

  it('conclui autoavaliação', async () => {
    
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';
    
    
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
    
    
    // Set loading to false manually to see the "empty" state
    ctx.wrapper!.vm.loading = false;

    expect(ctx.wrapper!.text()).toContain('Nenhuma competência encontrada');
  });

  it('não salva quando campos estão vazios', async () => {

    // Call salvar with empty values - should return early
    await ctx.wrapper!.vm.salvar(1, '', 'N3');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();

    await ctx.wrapper!.vm.salvar(1, 'N3', '');
    expect(diagnosticoService.salvarAvaliacao).not.toHaveBeenCalled();
  });

  it('exibe erro ao falhar salvamento de avaliação', async () => {

    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro de validação' } }
    });

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro de validação', 'danger');
  });

  it('exibe erro ao falhar conclusão de autoavaliação', async () => {

    ctx.wrapper!.vm.avaliacoes[1].importancia = 'N5';
    ctx.wrapper!.vm.avaliacoes[1].dominio = 'N3';
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';

    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce({
      response: { data: { message: 'Erro ao finalizar' } }
    });

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro ao finalizar', 'danger');
    expect(mockPush).not.toHaveBeenCalled();
  });

  it('não conclui quando podeConcluir é false', async () => {

    // Ensure not all ratings are filled
    expect(ctx.wrapper!.vm.podeConcluir).toBe(false);

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(diagnosticoService.concluirAutoavaliacao).not.toHaveBeenCalled();
  });

  it('trata erro genérico ao salvar avaliação', async () => {

    (diagnosticoService.salvarAvaliacao as any).mockRejectedValueOnce(new Error('Erro de rede'));

    await ctx.wrapper!.vm.salvar(1, 'N4', 'N2');
    
    // Error handler normalizes network errors
    expect(feedbackStore.show).toHaveBeenCalledWith('Erro de Rede', 'Não foi possível conectar ao servidor. Verifique sua conexão.', 'danger');
  });

  it('trata erro genérico ao concluir autoavaliação', async () => {

    ctx.wrapper!.vm.avaliacoes[1].importancia = 'N5';
    ctx.wrapper!.vm.avaliacoes[1].dominio = 'N3';
    ctx.wrapper!.vm.avaliacoes[2].importancia = 'N4';
    ctx.wrapper!.vm.avaliacoes[2].dominio = 'N2';

    (diagnosticoService.concluirAutoavaliacao as any).mockRejectedValueOnce(new Error('Erro genérico'));

    await ctx.wrapper!.vm.concluirAutoavaliacao();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', 'Erro ao concluir.', 'danger');
  });
});

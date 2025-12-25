import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import ConclusaoDiagnostico from '@/views/ConclusaoDiagnostico.vue';
import {useFeedbackStore} from '@/stores/feedback';
import {diagnosticoService} from '@/services/diagnosticoService';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';

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
  const ctx = setupComponentTest();
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

  const stubs = {
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
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockRouteParams.value = { codSubprocesso: '10' };

    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(ConclusaoDiagnostico, mountOptions);

    feedbackStore = useFeedbackStore();
    
    // Resposta válida padrão
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPronto);
  });

  it('exibe estado de carregamento inicialmente', async () => {
    expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(10);
  });

  it('exibe estado pronto corretamente', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.text()).toContain('O diagnóstico está completo');
    expect(ctx.wrapper!.find('[data-testid="btn-confirmar-conclusao"]').attributes('disabled')).toBeUndefined();
  });

  it('exibe estado pendente e requer justificativa', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPendente);
    
    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(ConclusaoDiagnostico, mountOptions);

    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    expect(ctx.wrapper!.text()).toContain('Pendências existem');
    
    // Desabilitado inicialmente
    expect(ctx.wrapper!.vm.botaoHabilitado).toBe(false);
    
    // Justificativa curta demais
    const textarea = ctx.wrapper!.find('textarea');
    await textarea.setValue('Curto');
    expect(ctx.wrapper!.vm.botaoHabilitado).toBe(false);

    // Justificativa válida
    await textarea.setValue('Texto de justificativa válido aqui');
    expect(ctx.wrapper!.vm.botaoHabilitado).toBe(true);
  });

  it('redireciona se já concluído', async () => {
    (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoConcluido);
    
    const mountOptions = getCommonMountOptions({}, stubs);
    ctx.wrapper = mount(ConclusaoDiagnostico, mountOptions);

    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    feedbackStore = useFeedbackStore();
    
    expect(feedbackStore.show).toHaveBeenCalledWith('Aviso', expect.any(String), 'warning');
    expect(mockPush).toHaveBeenCalledWith('/painel');
  });

  it('conclui diagnóstico', async () => {
    await ctx.wrapper!.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    await ctx.wrapper!.vm.$nextTick();

    const btn = ctx.wrapper!.find('[data-testid="btn-confirmar-conclusao"]');
    await btn.trigger('click');

    expect(diagnosticoService.concluirDiagnostico).toHaveBeenCalledWith(10, '');
    expect(mockPush).toHaveBeenCalledWith('/painel');
    expect(feedbackStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
  });

  it('conclui diagnóstico com justificativa', async () => {
      (diagnosticoService.buscarDiagnostico as any).mockResolvedValue(mockDiagnosticoPendente);
      
      const mountOptions = getCommonMountOptions({}, stubs);
      ctx.wrapper = mount(ConclusaoDiagnostico, mountOptions);
      
      await ctx.wrapper!.vm.$nextTick();
      await new Promise(resolve => setTimeout(resolve, 10));
      await ctx.wrapper!.vm.$nextTick();
      
      const textarea = ctx.wrapper!.find('textarea');
      await textarea.setValue('Justificativa válida para teste');
      
      await ctx.wrapper!.vm.$nextTick();
      
      await ctx.wrapper!.find('[data-testid="btn-confirmar-conclusao"]').trigger('click');
      
      expect(diagnosticoService.concluirDiagnostico).toHaveBeenCalledWith(10, 'Justificativa válida para teste');
  });
});

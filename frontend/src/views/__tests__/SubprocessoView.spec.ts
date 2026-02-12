import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import Subprocesso from '@/views/Subprocesso.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useFeedbackStore} from '@/stores/feedback';
import {useProcessosStore} from '@/stores/processos';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import * as processoService from '@/services/processoService';
import {checkA11y} from "@/test-utils/a11yTestHelpers";

// Mock child components
const SubprocessoHeaderStub = {
  template: '<div data-testid="subprocesso-header"></div>',
  props: ['podeAlterarDataLimite', 'processoDescricao', 'situacao', 'unidadeAtual'],
  emits: ['alterar-data-limite', 'reabrir-cadastro', 'reabrir-revisao', 'enviar-lembrete']
};
const SubprocessoCardsStub = {
  template: '<div data-testid="subprocesso-cards"></div>',
  props: ['situacao', 'tipoProcesso']
};
const SubprocessoModalStub = {
  template: '<div data-testid="subprocesso-modal"></div>',
  props: ['mostrarModal'],
  emits: ['confirmar-alteracao', 'fechar-modal']
};
const TabelaMovimentacoesStub = {
  template: '<div data-testid="tabela-movimentacoes"></div>',
  props: ['movimentacoes']
};

// Mock Services
vi.mock('@/services/processoService', () => ({
  reabrirCadastro: vi.fn(),
  reabrirRevisaoCadastro: vi.fn(),
  enviarLembrete: vi.fn(),
}));

describe('Subprocesso.vue', () => {
  const mockSubprocesso = {
    codigo: 10,
    situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
    situacaoLabel: 'Em Andamento',
    processoDescricao: 'Processo Teste',
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    unidade: {
      codigo: 1,
      sigla: 'TEST',
      nome: 'Unidade Teste'
    },
    responsavel: {
      codigo: 1,
      nome: 'Resp',
      tituloEleitoral: '123456789012',
      unidade: { codigo: 1, sigla: 'TEST', nome: 'Unidade Teste' },
      email: 'resp@test.com',
      ramal: '123'
    },
    titular: {
      codigo: 2,
      nome: 'Titular',
      tituloEleitoral: '987654321012',
      unidade: { codigo: 1, sigla: 'TEST', nome: 'Unidade Teste' },
      email: 'titular@test.com',
      ramal: '456'
    },
    etapaAtual: 1,
    prazoEtapaAtual: '2023-12-31T00:00:00',
    localizacaoAtual: 'Unidade Teste',
    isEmAndamento: true,
    elementosProcesso: [],
    permissoes: {
      podeAlterarDataLimite: true,
      podeEditarMapa: true,
      podeVisualizarMapa: true,
      podeVisualizarDiagnostico: true,
      podeDisponibilizarCadastro: false,
      podeDevolverCadastro: false,
      podeAceitarCadastro: false,
      podeVisualizarImpacto: false,
      podeVerPagina: true,
      podeRealizarAutoavaliacao: false,
      podeReabrirCadastro: true,
      podeReabrirRevisao: true,
      podeEnviarLembrete: true
    },
    movimentacoes: [] as any[]
  };

  const additionalStubs = {
      BContainer: { template: '<div><slot /></div>' },
      SubprocessoHeader: SubprocessoHeaderStub,
      SubprocessoCards: SubprocessoCardsStub,
      SubprocessoModal: SubprocessoModalStub,
      TabelaMovimentacoes: TabelaMovimentacoesStub,
      BModal: { template: '<div><slot /><slot name="footer" /></div>', props: ['modelValue', 'title'], emits: ['update:modelValue', 'ok'] },
      BFormTextarea: { template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue'], emits: ['update:modelValue'] },
      BButton: { template: '<button :disabled="disabled"><slot /></button>', props: ['disabled'] }
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Helper to mount component with specific setup
  const mountComponent = (overrideMockSubprocesso?: Partial<typeof mockSubprocesso>) => {
    const subprocessoToUse = overrideMockSubprocesso ? { ...mockSubprocesso, ...overrideMockSubprocesso } : mockSubprocesso;
    
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        subprocessos: {
          subprocessoDetalhe: null,
        },
        mapas: {
          mapaCompleto: null,
        },
      },
      stubActions: true,
    });

    const store = useSubprocessosStore(pinia);
    const mapaStore = useMapasStore(pinia);
    const feedbackStore = useFeedbackStore(pinia);
    const processosStore = useProcessosStore(pinia);

    // Mock implementations
    (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
    (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
        store.subprocessoDetalhe = subprocessoToUse as any;
        return subprocessoToUse;
    });
    (mapaStore.buscarMapaCompleto as any).mockResolvedValue({});
    (store.alterarDataLimiteSubprocesso as any).mockResolvedValue({});
    
    // Mock store actions that call services
    (store.reabrirCadastro as any).mockImplementation(async (cod: number, just: string) => {
      try {
        await (processoService.reabrirCadastro as any)(cod, just);
        feedbackStore.show('Cadastro reaberto', 'O cadastro foi reaberto com sucesso', 'success');
        return true;
      } catch {
        feedbackStore.show('Erro', 'Não foi possível reabrir o cadastro', 'danger');
        return false;
      }
    });
    (store.reabrirRevisaoCadastro as any).mockImplementation(async (cod: number, just: string) => {
      try {
        await (processoService.reabrirRevisaoCadastro as any)(cod, just);
        feedbackStore.show('Revisão reaberta', 'A revisão foi reaberta com sucesso', 'success');
        return true;
      } catch {
        feedbackStore.show('Erro', 'Não foi possível reabrir a revisão', 'danger');
        return false;
      }
    });
    
    // Mock processos store action that calls service  
    (processosStore.enviarLembrete as any).mockImplementation(async (codProcesso: number, codUnidade: number) => {
      try {
        await (processoService.enviarLembrete as any)(codProcesso, codUnidade);
        feedbackStore.show('Lembrete enviado', 'O lembrete foi enviado com sucesso', 'success');
        return true;
      } catch {
        feedbackStore.show('Erro', 'Não foi possível enviar o lembrete', 'danger');
        return false;
      }
    });

    const wrapper = mount(Subprocesso, {
      global: {
        plugins: [pinia],
        stubs: {
          RouterLink: RouterLinkStub,
          RouterView: true,
          ...additionalStubs,
        },
      },
      props: {
        codProcesso: 1,
        siglaUnidade: 'TEST'
      }
    });

    return { wrapper, store, mapaStore, feedbackStore, processosStore };
  };

  it('fetches data on mount', async () => {
    const { store, mapaStore } = mountComponent();
    await flushPromises();

    expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
    expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
    expect(mapaStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
  });

  it('renders components when data is available', async () => {
    const { wrapper } = mountComponent();
    await flushPromises();
    await (wrapper.vm as any).$nextTick();

    expect(wrapper.findComponent(SubprocessoHeaderStub).exists()).toBe(true);
    expect(wrapper.findComponent(SubprocessoCardsStub).exists()).toBe(true);
    expect(wrapper.findComponent(TabelaMovimentacoesStub).exists()).toBe(true);
  });

  it('opens date limit modal when allowed', async () => {
    const { wrapper } = mountComponent();
    await flushPromises();
    await (wrapper.vm as any).$nextTick();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');
    await (wrapper.vm as any).$nextTick();

    expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(true);
  });

  it('shows error when opening date limit modal is not allowed', async () => {
    const subprocessoSemPermissao = {
      permissoes: { ...mockSubprocesso.permissoes, podeAlterarDataLimite: false }
    };
    
    const { wrapper, feedbackStore } = mountComponent(subprocessoSemPermissao);
    await flushPromises();
    await (wrapper.vm as any).$nextTick();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');
    await (wrapper.vm as any).$nextTick();

    expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('não tem permissão'), 'danger');
  });

  it('handles date limit update confirmation', async () => {
    const { wrapper, store, feedbackStore } = mountComponent();
    await flushPromises();
    await (wrapper.vm as any).$nextTick();

    // Open modal
    (wrapper.vm as any).modals.open('alterarDataLimite');
    await (wrapper.vm as any).$nextTick();

    // Confirm
    const modal = wrapper.findComponent(SubprocessoModalStub);
    await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

    await flushPromises();

    expect(store.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, { novaData: '2024-01-01' });
    expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('sucesso'), 'success');
  });

  it('trata erro ao alterar data limite', async () => {
    const { wrapper, store, feedbackStore } = mountComponent();
    await flushPromises();
    (store.alterarDataLimiteSubprocesso as any).mockRejectedValue(new Error('Falha'));

    (wrapper.vm as any).mostrarModalAlterarDataLimite = true;
    const modal = wrapper.findComponent(SubprocessoModalStub);
    await modal.vm.$emit('confirmar-alteracao', '2024-01-01');
    await flushPromises();

    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('Não foi possível alterar'), 'danger');
  });

  it('reabre cadastro com sucesso', async () => {
    const { wrapper, feedbackStore, store } = mountComponent();
    await flushPromises();

    // Trigger Reabertura
    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('reabrir-cadastro');
    await (wrapper.vm as any).$nextTick();
    
    expect((wrapper.vm as any).tipoReabertura).toBe('cadastro');
    expect((wrapper.vm as any).modals.modals.reabrir.value.isOpen).toBe(true);

    // Preencher justificativa
    const textarea = wrapper.find('textarea');
    await textarea.setValue('Erro no preenchimento');

    // Confirmar
    const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
    await btn.trigger('click');
    await flushPromises();

    expect(processoService.reabrirCadastro).toHaveBeenCalledWith(10, 'Erro no preenchimento');
    expect(feedbackStore.show).toHaveBeenCalledWith('Cadastro reaberto', expect.any(String), 'success');
    expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledTimes(2); // Initial + Reload
  });

  it('reabre revisão com sucesso', async () => {
    const { wrapper, feedbackStore } = mountComponent();
    await flushPromises();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('reabrir-revisao');
    expect((wrapper.vm as any).tipoReabertura).toBe('revisao');

    const textarea = wrapper.find('textarea');
    await textarea.setValue('Revisão incompleta');

    const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
    await btn.trigger('click');
    await flushPromises();

    expect(processoService.reabrirRevisaoCadastro).toHaveBeenCalledWith(10, 'Revisão incompleta');
    expect(feedbackStore.show).toHaveBeenCalledWith('Revisão reaberta', expect.any(String), 'success');
  });

  it('impede reabertura se justificativa vazia (botão desabilitado)', async () => {
    const { wrapper } = mountComponent();
    await flushPromises();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('reabrir-cadastro');

    // O botão deve estar desabilitado se a justificativa for vazia
    const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
    expect(btn.attributes('disabled')).toBeDefined();
    expect(processoService.reabrirCadastro).not.toHaveBeenCalled();
  });

  it('trata erro na API ao reabrir', async () => {
    const { wrapper, feedbackStore } = mountComponent();
    await flushPromises();
    vi.mocked(processoService.reabrirCadastro).mockRejectedValue(new Error('API Error'));

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('reabrir-cadastro');

    const textarea = wrapper.find('textarea');
    await textarea.setValue('Justificativa');

    const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
    await btn.trigger('click');
    await flushPromises();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', expect.stringContaining('Não foi possível reabrir'), 'danger');
  });

  it('envia lembrete com sucesso', async () => {
    const { wrapper, feedbackStore } = mountComponent();
    await flushPromises();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('enviar-lembrete');
    await flushPromises();

    expect(processoService.enviarLembrete).toHaveBeenCalledWith(1, 1);
    expect(feedbackStore.show).toHaveBeenCalledWith('Lembrete enviado', expect.any(String), 'success');
  });

  it('trata erro ao enviar lembrete', async () => {
    const { wrapper, feedbackStore } = mountComponent();
    await flushPromises();
    vi.mocked(processoService.enviarLembrete).mockRejectedValue(new Error('Erro'));

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('enviar-lembrete');
    await flushPromises();

    expect(feedbackStore.show).toHaveBeenCalledWith('Erro', expect.stringContaining('Não foi possível enviar'), 'danger');
  });

  it('deve ser acessível', async () => {
    const { wrapper } = mountComponent();
    await flushPromises();
    await checkA11y(wrapper.element as HTMLElement);
  });
});

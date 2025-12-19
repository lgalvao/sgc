import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useFeedbackStore} from '@/stores/feedback';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import { setupComponentTest } from '@/test-utils/componentTestHelpers';

// Mock child components
const SubprocessoHeaderStub = {
  template: '<div data-testid="subprocesso-header"></div>',
  props: ['podeAlterarDataLimite', 'processoDescricao', 'situacao', 'unidadeAtual'],
  emits: ['alterar-data-limite']
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

describe('SubprocessoView.vue', () => {
  const context = setupComponentTest();

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
      podeRealizarAutoavaliacao: false
    },
    movimentacoes: [] as any[]
  };

  const additionalStubs = {
      BContainer: { template: '<div><slot /></div>' },
      SubprocessoHeader: SubprocessoHeaderStub,
      SubprocessoCards: SubprocessoCardsStub,
      SubprocessoModal: SubprocessoModalStub,
      TabelaMovimentacoes: TabelaMovimentacoesStub,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Helper to mount component with specific setup
  const mountComponent = (overrideMockSubprocesso?: typeof mockSubprocesso) => {
    const subprocessoToUse = overrideMockSubprocesso || mockSubprocesso;
    
    // Criar pinia ANTES do mount para configurar mocks
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

    // Mock implementations ANTES do mount para que onMounted encontre os mocks
    (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
    
    (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
        store.subprocessoDetalhe = subprocessoToUse;
        return subprocessoToUse;
    });

    (mapaStore.buscarMapaCompleto as any).mockResolvedValue({});

    (store.alterarDataLimiteSubprocesso as any).mockResolvedValue({});

    context.wrapper = mount(SubprocessoView, {
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

    return { store, mapaStore, feedbackStore };
  };

  it('fetches data on mount', async () => {
    const { store, mapaStore } = mountComponent();
    await flushPromises();

    expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
    expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
    expect(mapaStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
  });

  it('renders components when data is available', async () => {
    mountComponent();
    await flushPromises();
    await context.wrapper.vm.$nextTick();

    expect(context.wrapper.findComponent(SubprocessoHeaderStub).exists()).toBe(true);
    expect(context.wrapper.findComponent(SubprocessoCardsStub).exists()).toBe(true);
    expect(context.wrapper.findComponent(TabelaMovimentacoesStub).exists()).toBe(true);
  });

  it('opens date limit modal when allowed', async () => {
    mountComponent();
    await flushPromises();
    await context.wrapper.vm.$nextTick();

    const header = context.wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');

    expect(context.wrapper.vm.mostrarModalAlterarDataLimite).toBe(true);
  });

  it('shows error when opening date limit modal is not allowed', async () => {
    // Subprocesso com permissão de alterar data limite = false
    const subprocessoSemPermissao = {
      ...mockSubprocesso,
      permissoes: { ...mockSubprocesso.permissoes, podeAlterarDataLimite: false }
    };
    
    const { feedbackStore } = mountComponent(subprocessoSemPermissao as typeof mockSubprocesso);
    await flushPromises();
    await context.wrapper.vm.$nextTick();

    const header = context.wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');

    expect(context.wrapper.vm.mostrarModalAlterarDataLimite).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('não tem permissão'), 'danger');
  });

  it('handles date limit update confirmation', async () => {
    const { store, feedbackStore } = mountComponent();
    await flushPromises();
    await context.wrapper.vm.$nextTick();

    // Open modal
    context.wrapper.vm.mostrarModalAlterarDataLimite = true;
    await context.wrapper.vm.$nextTick();

    // Confirm
    const modal = context.wrapper.findComponent(SubprocessoModalStub);
    await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

    await flushPromises();

    expect(store.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(1, { novaData: '2024-01-01' });
    expect(context.wrapper.vm.mostrarModalAlterarDataLimite).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('sucesso'), 'success');
  });
});

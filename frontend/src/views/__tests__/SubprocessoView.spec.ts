import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useFeedbackStore} from '@/stores/feedback';
import {TipoProcesso} from '@/types/tipos';
import { setupComponentTest, getCommonMountOptions } from '@/test-utils/componentTestHelpers';

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
    situacao: 'EM_ANDAMENTO',
    situacaoLabel: 'Em Andamento',
    processoDescricao: 'Processo Teste',
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    unidade: {
      codigo: 1,
      sigla: 'TEST',
      nome: 'Unidade Teste'
    },
    responsavel: { nome: 'Resp', email: 'resp@test.com', ramal: '123' },
    titular: { nome: 'Titular', email: 'titular@test.com', ramal: '456' },
    etapaAtual: 1,
    prazoEtapaAtual: '2023-12-31T00:00:00',
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
    movimentacoes: [{ id: 1, descricao: 'Mov 1' }]
  };

  const additionalStubs = {
      BContainer: { template: '<div><slot /></div>' },
      SubprocessoHeader: SubprocessoHeaderStub,
      SubprocessoCards: SubprocessoCardsStub,
      SubprocessoModal: SubprocessoModalStub,
      TabelaMovimentacoes: TabelaMovimentacoesStub,
  };

  const piniaOptions = {
      initialState: {
          subprocessos: {
              subprocessoDetalhe: null,
          },
          mapas: {
              mapaCompleto: null,
          },
      },
      stubActions: true // Use spies for actions
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Helper to mount component with specific setup
  const mountComponent = () => {
    context.wrapper = mount(SubprocessoView, {
      ...getCommonMountOptions({}, additionalStubs, piniaOptions),
      props: {
        codProcesso: 1,
        siglaUnidade: 'TEST'
      }
    });

    const store = useSubprocessosStore();
    const mapaStore = useMapasStore();
    const feedbackStore = useFeedbackStore();

    // Mock implementations to simulate side effects of actions
    (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
    
    (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
        store.subprocessoDetalhe = mockSubprocesso;
        return mockSubprocesso;
    });

    (mapaStore.buscarMapaCompleto as any).mockResolvedValue({});

    (store.alterarDataLimiteSubprocesso as any).mockResolvedValue({});

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
    context.wrapper = mount(SubprocessoView, {
      ...getCommonMountOptions({}, additionalStubs, piniaOptions),
      props: {
        codProcesso: 1,
        siglaUnidade: 'TEST'
      }
    });

    const store = useSubprocessosStore();
    const feedbackStore = useFeedbackStore();

    (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
    // Return subprocesso with different permissions
    (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
        const forbidden = {
            ...mockSubprocesso,
            permissoes: { ...mockSubprocesso.permissoes, podeAlterarDataLimite: false }
        };
        store.subprocessoDetalhe = forbidden;
        return forbidden;
    });

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

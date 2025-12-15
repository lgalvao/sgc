import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useFeedbackStore} from '@/stores/feedback';
import {TipoProcesso} from '@/types/tipos';

// Mock child components
const SubprocessoHeaderStub = {
  template: '<div data-testid="subprocesso-header"></div>',
  props: ['podeAlterarDataLimite', 'processoDescricao', 'situacao', 'unidadeAtual']
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
  let wrapper: any;
  let subprocessosStore: any;
  let mapaStore: any;
  let feedbackStore: any;

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

  beforeEach(() => {
    vi.clearAllMocks();

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState: {
        subprocessos: {
          // Set subprocessoDetalhe directly in initial state for most tests
          subprocessoDetalhe: mockSubprocesso,
        },
        mapas: {
          mapaCompleto: null,
        },
      },
    });

    subprocessosStore = useSubprocessosStore(pinia);
    mapaStore = useMapasStore(pinia);
    feedbackStore = useFeedbackStore(pinia);

    // Set mock resolved values for async actions
    subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(10);
    subprocessosStore.buscarSubprocessoDetalhe.mockResolvedValue(mockSubprocesso);
    mapaStore.buscarMapaCompleto.mockResolvedValue({});
    subprocessosStore.alterarDataLimiteSubprocesso.mockResolvedValue({});

    wrapper = mount(SubprocessoView, {
      props: {
        codProcesso: 1,
        siglaUnidade: 'TEST'
      },
      global: {
        plugins: [
          pinia,
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          SubprocessoHeader: SubprocessoHeaderStub,
          SubprocessoCards: SubprocessoCardsStub,
          SubprocessoModal: SubprocessoModalStub,
          TabelaMovimentacoes: TabelaMovimentacoesStub,
        },
      },
    });
  });

  it.skip('fetches data on mount', async () => {
    // Create a fresh pinia for this test to ensure clean state and correct store usage
    const testingPinia = createTestingPinia({ createSpy: vi.fn });
    
    // Pre-configure the store mocks before mount
    const store = useSubprocessosStore(testingPinia);
    const mapa = useMapasStore(testingPinia);

    (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(10);
    (store.buscarSubprocessoDetalhe as any).mockResolvedValue(mockSubprocesso);
    (mapa.buscarMapaCompleto as any).mockResolvedValue({});

    wrapper = mount(SubprocessoView, {
        props: { codProcesso: 1, siglaUnidade: 'TEST' },
        global: {
            plugins: [testingPinia],
            stubs: {
                BContainer: { template: '<div><slot /></div>' },
                SubprocessoHeader: SubprocessoHeaderStub,
                SubprocessoCards: SubprocessoCardsStub,
                SubprocessoModal: SubprocessoModalStub,
                TabelaMovimentacoes: TabelaMovimentacoesStub,
            }
        }
    });

    await flushPromises();

    expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
    expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
    expect(mapa.buscarMapaCompleto).toHaveBeenCalledWith(10);
  });

  it('renders components when data is available', async () => {
    await flushPromises(); // Let onMounted finish clearing/fetching

    // Manually set store state for this test case
    subprocessosStore.subprocessoDetalhe = mockSubprocesso;
    await wrapper.vm.$nextTick(); 

    expect(wrapper.find('[data-testid="subprocesso-header"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="subprocesso-cards"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="tabela-movimentacoes"]').exists()).toBe(true);
  });

  it('opens date limit modal when allowed', async () => {
    await flushPromises();
    subprocessosStore.subprocessoDetalhe = mockSubprocesso;
    await wrapper.vm.$nextTick();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');

    expect(wrapper.vm.mostrarModalAlterarDataLimite).toBe(true);
  });

  it('shows error when opening date limit modal is not allowed', async () => {
    await flushPromises();
    subprocessosStore.subprocessoDetalhe = {
      ...mockSubprocesso,
      permissoes: { ...mockSubprocesso.permissoes, podeAlterarDataLimite: false }
    };
    await wrapper.vm.$nextTick();

    const header = wrapper.findComponent(SubprocessoHeaderStub);
    await header.vm.$emit('alterar-data-limite');

    expect(wrapper.vm.mostrarModalAlterarDataLimite).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('não tem permissão'), 'danger');
  });

  it('handles date limit update confirmation', async () => {
    await flushPromises();
    subprocessosStore.subprocessoDetalhe = mockSubprocesso;
    await wrapper.vm.$nextTick();

    // Open modal
    wrapper.vm.mostrarModalAlterarDataLimite = true;
    await wrapper.vm.$nextTick(); 

    // Confirm
    const modal = wrapper.findComponent(SubprocessoModalStub);
    await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

    expect(subprocessosStore.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(1, { novaData: '2024-01-01' });
    expect(wrapper.vm.mostrarModalAlterarDataLimite).toBe(false);
    expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.stringContaining('sucesso'), 'success');
  });
});

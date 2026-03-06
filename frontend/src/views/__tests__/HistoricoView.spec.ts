import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import HistoricoView from '../HistoricoView.vue';
import { useRouter } from 'vue-router';
import { useProcessosStore } from '@/stores/processos';

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}));

const LayoutPadraoStub = {
  template: '<div><slot /></div>'
};

const PageHeaderStub = {
  template: '<div data-testid="page-header">{{ title }}</div>',
  props: ['title']
};

const TabelaProcessosStub = {
  template: '<div data-testid="tabela-processos" @click="$emit(\'selecionar-processo\', { codigo: 1, linkDestino: \'/test/1\' })" @dblclick="$emit(\'ordenar\', \'descricao\')"></div>',
  props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc', 'compacto', 'showDataFinalizacao']
};

const mockProcessosFinalizados = [
  { codigo: 1, dataFinalizacao: '2024-01-02', descricao: 'Proc B' },
  { codigo: 2, dataFinalizacao: '2024-01-01', descricao: 'Proc A' },
  { codigo: 3, dataFinalizacao: null, descricao: 'Proc C' }
];

describe('HistoricoView.vue', () => {
  let wrapper: any;
  let mockRouter: any;
  let processosStore: any;

  beforeEach(() => {
    mockRouter = { push: vi.fn() };
    (useRouter as any).mockReturnValue(mockRouter);
  });

  const createWrapper = () => {
    return mount(HistoricoView, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              processos: {
                processosFinalizados: mockProcessosFinalizados
              }
            }
          })
        ],
        stubs: {
          LayoutPadrao: LayoutPadraoStub,
          PageHeader: PageHeaderStub,
          TabelaProcessos: TabelaProcessosStub,
          BSpinner: { template: '<div data-testid="spinner"></div>' }
        }
      }
    });
  };

  it('deve carregar historico onMounted', async () => {
    wrapper = createWrapper();
    processosStore = useProcessosStore();
    
    // initially loading
    // then loaded
    await flushPromises();

    expect(processosStore.buscarProcessosFinalizados).toHaveBeenCalled();
  });

  it('deve lidar com erro ao carregar historico', async () => {
    wrapper = createWrapper();
    processosStore = useProcessosStore();
    processosStore.buscarProcessosFinalizados.mockRejectedValueOnce(new Error('error'));
    
    await flushPromises();
    // shouldn't crash, error caught in try/catch block
    expect(wrapper.find('[data-testid="tabela-processos"]').exists()).toBe(true);
  });

  it('deve ordenar processos corretamente pela data', async () => {
    wrapper = createWrapper();
    await flushPromises();

    const processos = wrapper.vm.processosOrdenados;
    // default desc by dataFinalizacao
    expect(processos[0].codigo).toBe(1); // 2024-01-02
    expect(processos[1].codigo).toBe(2); // 2024-01-01
    expect(processos[2].codigo).toBe(3); // null
  });

  it('deve ordenar alterando o criterio e direcao', async () => {
    wrapper = createWrapper();
    await flushPromises();

    wrapper.vm.ordenarPor('descricao');
    expect(wrapper.vm.criterio).toBe('descricao');
    expect(wrapper.vm.asc).toBe(true); // first click sets to asc

    let processos = wrapper.vm.processosOrdenados;
    expect(processos[0].descricao).toBe('Proc A');

    wrapper.vm.ordenarPor('descricao');
    expect(wrapper.vm.asc).toBe(false); // second click toggles asc

    processos = wrapper.vm.processosOrdenados;
    expect(processos[0].descricao).toBe('Proc C');
  });

  it('deve navegar para ver detalhes', async () => {
    wrapper = createWrapper();
    await flushPromises();

    wrapper.vm.verDetalhes({ codigo: 1, linkDestino: '/custom/link' });
    expect(mockRouter.push).toHaveBeenCalledWith('/custom/link');

    wrapper.vm.verDetalhes({ codigo: 2 }); // without linkDestino
    expect(mockRouter.push).toHaveBeenCalledWith('/processo/2');
    
    wrapper.vm.verDetalhes(undefined);
    // should not throw error or call push
    expect(mockRouter.push).toHaveBeenCalledTimes(2);
  });
});

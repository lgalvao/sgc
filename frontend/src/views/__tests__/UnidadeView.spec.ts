import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import UnidadeView from '@/views/UnidadeView.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes';
import {usePerfilStore} from '@/stores/perfil';
import {useUsuariosStore} from '@/stores/usuarios';
import {useMapasStore} from '@/stores/mapas';
import {buscarUsuarioPorTitulo} from '@/services/usuarioService';

// Mocks
const mockPush = vi.fn();
vi.mock('vue-router', async (importOriginal) => {
  const actual: any = await importOriginal();
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush,
    }),
  };
});

vi.mock('@/services/usuarioService', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        buscarUsuarioPorTitulo: vi.fn(),
    }
});

const TreeTableStub = {
  template: '<div data-testid="tree-table"></div>',
  props: ['data', 'columns', 'title'],
  emits: ['row-click']
};

describe('UnidadeView.vue', () => {
  let wrapper: any;
  let unidadesStore: any;
  let atribuicaoStore: any;
  let perfilStore: any;
  let usuariosStore: any;
  let mapasStore: any;

  const mockUnidade = {
    codigo: 1,
    sigla: 'TEST',
    nome: 'Unidade Teste',
    idServidorTitular: 10,
    tituloTitular: '123456',
    filhas: [
      { codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: [] },
      { codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: [] }
    ]
  };

  const mockUsuario = {
    codigo: 10,
    nome: 'Titular Teste',
    email: 'titular@test.com',
    ramal: '123'
  };
  
  const mockUsuarioResponsavel = {
      codigo: 20,
      nome: 'Responsavel Teste',
      email: 'resp@test.com',
      ramal: '456'
  }

  const setupWrapper = (initialStateOverride = {}) => {
    wrapper = mount(UnidadeView, {
      props: {
        codUnidade: 1
      },
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              unidades: {
                unidade: null,
              },
              atribuicoes: {
                atribuicoes: [],
              },
              perfil: {
                perfilSelecionado: 'USER',
              },
              usuarios: {
                usuarios: [],
              },
              mapas: {
                mapaCompleto: null,
              },
              ...initialStateOverride
            },
          }),
        ],
        stubs: {
          BContainer: { template: '<div><slot /></div>' },
          BCard: { template: '<div><slot /></div>' },
          BCardBody: { template: '<div><slot /></div>' },
          BButton: { template: '<button><slot /></button>' },
          TreeTable: TreeTableStub,
        },
      },
    });

    unidadesStore = useUnidadesStore();
    atribuicaoStore = useAtribuicaoTemporariaStore();
    perfilStore = usePerfilStore();
    usuariosStore = useUsuariosStore();
    mapasStore = useMapasStore();
    
    // Setup default mock behaviors if not already set by initialState or need specific returns
    if (!unidadesStore.buscarArvoreUnidade.mock) {
        // If it was already mocked by pinia testing, we can adjust it. 
        // But createTestingPinia with stubActions: true (default) mocks all actions.
    }
    
    unidadesStore.buscarArvoreUnidade.mockResolvedValue(null);
    atribuicaoStore.buscarAtribuicoes.mockResolvedValue(null);
    atribuicaoStore.obterAtribuicoesPorUnidade = vi.fn().mockReturnValue([]);
    usuariosStore.obterUsuarioPorId = vi.fn().mockImplementation((id: number) => {
        if (id === 10) return mockUsuario;
        if (id === 20) return mockUsuarioResponsavel;
        return null;
    });
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (buscarUsuarioPorTitulo as any).mockResolvedValue(mockUsuario);
  });

  it('fetches data on mount', async () => {
    setupWrapper();
    expect(unidadesStore.buscarArvoreUnidade).toHaveBeenCalledWith(1);
    expect(atribuicaoStore.buscarAtribuicoes).toHaveBeenCalled();
  });

  it('renders unit details correctly', async () => {
    setupWrapper({
        unidades: {
            unidade: mockUnidade
        }
    });
    
    await wrapper.vm.$nextTick();
    await flushPromises();

    expect(wrapper.text()).toContain('TEST - Unidade Teste');
    expect(wrapper.text()).toContain('Titular: Titular Teste');
  });

  it('renders "Criar atribuição" button only for ADMIN', async () => {
    setupWrapper();
    perfilStore.perfilSelecionado = 'ADMIN';
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(true);

    perfilStore.perfilSelecionado = 'USER';
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(false);
  });
  
  it('navigates to create assignment', async () => {
    setupWrapper();
    perfilStore.perfilSelecionado = 'ADMIN';
    await wrapper.vm.$nextTick();
    
    await wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').trigger('click');
    expect(mockPush).toHaveBeenCalledWith({ path: '/unidade/1/atribuicao' });
  });

  it('calculates dynamic responsible person correctly', async () => {
    setupWrapper({
        unidades: {
            unidade: mockUnidade
        }
    });
    
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    const mockAtribuicao = {
      servidor: { ...mockUsuarioResponsavel, unidade: { codigo: 1 } },
      unidade: { codigo: 1 },
      dataInicio: yesterday.toISOString(),
      dataTermino: tomorrow.toISOString(),
    };

    atribuicaoStore.obterAtribuicoesPorUnidade.mockReturnValue([mockAtribuicao]);
    
    // Force re-computation
    await wrapper.vm.$nextTick();
    await flushPromises();

    expect(wrapper.text()).toContain('Responsável: Responsavel Teste');
  });

  it('renders subordinate units tree table', async () => {
    setupWrapper({
        unidades: {
            unidade: mockUnidade
        }
    });
    await wrapper.vm.$nextTick();

    const treeTable = wrapper.findComponent(TreeTableStub);
    expect(treeTable.exists()).toBe(true);
  });

  it('navigates to subordinate unit on row click', async () => {
    setupWrapper({
        unidades: {
            unidade: mockUnidade
        }
    });
    await wrapper.vm.$nextTick();

    const treeTable = wrapper.findComponent(TreeTableStub);
    treeTable.vm.$emit('row-click', { id: 2 });
    
    expect(mockPush).toHaveBeenCalledWith({ path: '/unidade/2' });
  });

  it('renders and clicks "Mapa vigente" button when map exists', async () => {
    setupWrapper({
        unidades: {
            unidade: mockUnidade
        }
    });
    mapasStore.mapaCompleto = { subprocessoCodigo: 99 };
    await wrapper.vm.$nextTick();

    const btn = wrapper.find('[data-testid="btn-mapa-vigente"]');
    expect(btn.exists()).toBe(true);
    
    await btn.trigger('click');
    expect(mockPush).toHaveBeenCalledWith({
      name: 'SubprocessoVisMapa',
      params: { codProcesso: 99, siglaUnidade: 'TEST' }
    });
  });
});

// Helper to flush promises
async function flushPromises() {
  return new Promise(resolve => setTimeout(resolve, 0));
}
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import PainelView from '../PainelView.vue';
import { createTestingPinia } from '@pinia/testing';
import { usePerfilStore } from '@/stores/perfil';
import { useToastStore } from '@/stores/toast';
import { useProcessos } from '@/composables/useProcessos';
import * as painelService from '@/services/painelService';
import { createRouter, createMemoryHistory } from 'vue-router';

vi.mock('@/services/painelService', () => ({
  listarAlertas: vi.fn(),
}));

const mockBuscarProcessosPainel = vi.fn();
vi.mock('@/composables/useProcessos', () => ({
  useProcessos: () => ({
    processosPainel: { value: [] },
    buscarProcessosPainel: mockBuscarProcessosPainel,
  }),
}));

const mockRouterPush = vi.fn();
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div></div>' } },
    { path: '/detalhes', component: { template: '<div></div>' } },
    { path: '/cadastro', name: 'CadProcesso', component: { template: '<div></div>' } }
  ],
});
router.push = mockRouterPush as any;

const mockToastCreate = vi.fn();
vi.mock('bootstrap-vue-next', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    useToast: () => ({
      create: mockToastCreate,
    }),
  };
});

function createMountOptions(initialStateOverrides = {}) {
  return {
    global: {
      plugins: [
        router,
        createTestingPinia({
          initialState: {
            perfil: {
              perfilSelecionado: 'ADMIN',
              unidadeSelecionada: 1,
              usuarioCodigo: 'U123',
              permissoesAcesso: { 'ADMIN': true },
              ...initialStateOverrides
            }
          },
          stubActions: false,
        }),
      ],
      stubs: {
        LayoutPadrao: { template: '<div><slot></slot></div>' },
        PageHeader: { template: '<div><slot></slot><slot name="actions"></slot></div>', props: ['title'] },
        TabelaProcessos: { template: '<div></div>' },
        BTable: { template: '<div><slot name="cell(mensagem)" :item="{}" :value="123"></slot></div>', props: ['items', 'fields'] },
        EmptyState: { template: '<div></div>' },
      },
    },
  };
}

describe('PainelView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(painelService.listarAlertas).mockResolvedValue({
      content: [{codigo: 1, mensagem: 'Alerta 1'}],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
    } as any);
  });

  it('deve carregar os dados e exibir toast pendente no onMounted', async () => {
    const options = createMountOptions();
    const pinia = options.global.plugins[1] as any;
    const toastStore = useToastStore(pinia);
    toastStore.consumePending = vi.fn().mockReturnValue({ body: 'Sucesso' });

    const wrapper = mount(PainelView, options);
    await wrapper.vm.$nextTick();

    expect(mockBuscarProcessosPainel).toHaveBeenCalledWith('ADMIN', 1, 0, 10);
    expect(painelService.listarAlertas).toHaveBeenCalledWith('U123', 1, 0, 10, undefined, undefined);
    expect(mockToastCreate).toHaveBeenCalledWith(expect.objectContaining({ props: expect.objectContaining({ body: 'Sucesso' }) }));
  });

  it('nao deve carregar alertas se usuarioCodigo for nulo', async () => {
    const options = createMountOptions({ usuarioCodigo: null });
    mount(PainelView, options);
    expect(painelService.listarAlertas).not.toHaveBeenCalled();
  });

  it('deve ordenar processos corretamente', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    await wrapper.vm.$nextTick();

    const vm = wrapper.vm as any;
    
    // Inverter direção no mesmo critério (default é "descricao" e asc=true)
    vm.ordenarPor('descricao');
    expect(vm.asc).toBe(false);
    expect(mockBuscarProcessosPainel).toHaveBeenCalledWith('ADMIN', 1, 0, 10, 'descricao', 'desc');

    // Mudar critério
    vm.ordenarPor('dataCriacao');
    expect(vm.criterio).toBe('dataCriacao');
    expect(vm.asc).toBe(true);
    expect(mockBuscarProcessosPainel).toHaveBeenCalledWith('ADMIN', 1, 0, 10, 'dataCriacao', 'asc');
  });

  it('deve abrir detalhes do processo se linkDestino existir', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    const vm = wrapper.vm as any;

    vm.abrirDetalhesProcesso(undefined); // nao deve falhar nem chamar router
    expect(mockRouterPush).not.toHaveBeenCalled();

    vm.abrirDetalhesProcesso({codigo: 1}); // sem linkDestino
    expect(mockRouterPush).not.toHaveBeenCalled();

    vm.abrirDetalhesProcesso({codigo: 1, linkDestino: '/detalhes'});
    expect(mockRouterPush).toHaveBeenCalledWith('/detalhes');
  });

  it('deve ordenar alertas corretamente', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    await wrapper.vm.$nextTick();
    const vm = wrapper.vm as any;

    // Default alertaCriterio é 'data' e alertaAsc é false
    // Sort by data novamente inverte a ordem
    vm.handleSortChangeAlertas({ sortBy: [{ key: 'dataHora' }] });
    expect(vm.alertaCriterio).toBe('data');
    expect(vm.alertaAsc).toBe(true);
    expect(painelService.listarAlertas).toHaveBeenCalledWith('U123', 1, 0, 10, 'data', 'asc');

    // Sort by processo muda o critério e asc passa a ser true
    vm.handleSortChangeAlertas({ sortBy: [{ key: 'processo' }] });
    expect(vm.alertaCriterio).toBe('processo');
    expect(vm.alertaAsc).toBe(true);
    expect(painelService.listarAlertas).toHaveBeenCalledWith('U123', 1, 0, 10, 'processo', 'asc');

    // Invertendo no processo
    vm.handleSortChangeAlertas([{ key: 'processo' }]);
    expect(vm.alertaCriterio).toBe('processo');
    expect(vm.alertaAsc).toBeDefined();
  });

  it('deve retornar classes e atributos da linha de alertas corretamente', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    const vm = wrapper.vm as any;

    // null checks
    expect(vm.rowClassAlerta(null)).toBe('');
    expect(vm.rowAttrAlerta(null)).toEqual({});

    // item sem leitura (negrito)
    expect(vm.rowClassAlerta({ dataHoraLeitura: null })).toBe('fw-bold');
    
    // item com leitura (normal)
    expect(vm.rowClassAlerta({ dataHoraLeitura: '2025-01-01' })).toBe('');

    // attr id
    expect(vm.rowAttrAlerta({codigo: 99})).toEqual({ 'data-testid': 'row-alerta-99' });
  });
});

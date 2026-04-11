import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import PainelView from '../PainelView.vue';
import {createTestingPinia} from '@pinia/testing';
import {useToastStore} from '@/stores/toast';
import * as painelService from '@/services/painelService';
import {createMemoryHistory, createRouter} from 'vue-router';

vi.mock('@/services/painelService', () => ({
  obterBootstrap: vi.fn(),
  listarProcessos: vi.fn(),
  listarAlertas: vi.fn(),
  marcarAlertasLidos: vi.fn().mockResolvedValue(undefined),
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
  const permissoesAdmin = {
    mostrarCriarProcesso: true,
    mostrarArvoreCompletaUnidades: true,
    mostrarCtaPainelVazio: true,
    mostrarDiagnosticoOrganizacional: true,
    mostrarMenuConfiguracoes: true,
    mostrarMenuAdministradores: true,
    mostrarCriarAtribuicaoTemporaria: true,
  };
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
              permissoesSessao: permissoesAdmin,
              ...initialStateOverrides
            }
          },
          stubActions: false,
        }),
      ],
      stubs: {
        LayoutPadrao: { template: '<div><slot></slot></div>' },
        PageHeader: { template: '<div><slot></slot><slot name="actions"></slot></div>', props: ['title'] },
        TabelaProcessos: { template: '<div data-testid="tbl-processos"></div>' },
        BTable: { template: '<div><slot name="cell(mensagem)" :item="{}" :value="123"></slot></div>', props: ['items', 'fields'] },
        EmptyState: { template: '<div data-testid="empty-state-alertas"></div>' },
        BSpinner: { template: '<div data-testid="spinner-painel"></div>' },
      },
    },
  };
}

function criarPromessaPendente<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((res) => {
    resolve = res;
  });
  return {promise, resolve};
}

describe('PainelView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(painelService.obterBootstrap).mockResolvedValue({
      processos: [{codigo: 1, descricao: 'Proc 1'}],
      alertas: [{codigo: 1, mensagem: 'Alerta 1'}],
    } as any);
  });

  it('deve carregar os dados e exibir toast pendente no onMounted', async () => {
    const options = createMountOptions();
    const pinia = options.global.plugins[1] as any;
    const toastStore = useToastStore(pinia);
    toastStore.consumePending = vi.fn().mockReturnValue({ body: 'Sucesso' });

    mount(PainelView, options);
    await flushPromises();

    expect(painelService.obterBootstrap).toHaveBeenCalled();
    expect(mockToastCreate).toHaveBeenCalledWith(expect.objectContaining({ props: expect.objectContaining({ body: 'Sucesso' }) }));
  });

  it('deve manter o carregando ate o bootstrap concluir', async () => {
    const bootstrapPromise = criarPromessaPendente<any>();
    vi.mocked(painelService.obterBootstrap).mockReturnValueOnce(bootstrapPromise.promise);

    const wrapper = mount(PainelView, createMountOptions());
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="painel-carregando"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="tbl-processos"]').exists()).toBe(false);

    bootstrapPromise.resolve({
      processos: [{codigo: 1, descricao: 'Proc 1'}],
      alertas: [{codigo: 1, mensagem: 'Alerta 1'}],
    });
    await flushPromises();

    expect(wrapper.find('[data-testid="painel-carregando"]').exists()).toBe(false);
    expect(wrapper.find('[data-testid="tbl-processos"]').exists()).toBe(true);
  });

  it('nao deve carregar dados se unidadeSelecionada for nula', async () => {
    const options = createMountOptions({ unidadeSelecionada: null });
    mount(PainelView, options);
    expect(painelService.obterBootstrap).not.toHaveBeenCalled();
  });

  it('deve ordenar processos corretamente sem chamar o backend', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    await flushPromises();

    const vm = wrapper.vm as any;

    // Inverter direção no mesmo critério (default é "descricao" e asc=true)
    vm.ordenarPor('descricao');
    expect(vm.asc).toBe(false);
    // Ordenação é local — não chama o backend
    expect(painelService.obterBootstrap).toHaveBeenCalledTimes(1); // apenas no onMounted

    // Mudar critério
    vm.ordenarPor('dataCriacao');
    expect(vm.criterio).toBe('dataCriacao');
    expect(vm.asc).toBe(true);
    expect(painelService.obterBootstrap).toHaveBeenCalledTimes(1); // ainda apenas o onMounted
  });

  it('deve abrir detalhes do processo se linkDestino existir', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    await flushPromises();
    const vm = wrapper.vm as any;

    vm.abrirDetalhesProcesso(undefined); // nao deve falhar nem chamar router
    expect(mockRouterPush).not.toHaveBeenCalled();

    vm.abrirDetalhesProcesso({codigo: 1}); // sem linkDestino
    expect(mockRouterPush).not.toHaveBeenCalled();

    vm.abrirDetalhesProcesso({codigo: 1, linkDestino: '/detalhes'});
    expect(mockRouterPush).toHaveBeenCalledWith('/detalhes');

    mockRouterPush.mockClear();
    vm.abrirDetalhesProcesso({codigo: 1, linkDestino: '/detalhes', codSubprocesso: 99});
    expect(mockRouterPush).toHaveBeenCalledWith({
      path: '/detalhes',
      query: {codSubprocesso: '99'},
    });
  });

  it('deve retornar classes e atributos da linha de alertas corretamente', async () => {
    const wrapper = mount(PainelView, createMountOptions());
    await flushPromises();
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

import { describe, it, expect, vi, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import HistoricoView from '../HistoricoView.vue';
import { getCommonMountOptions } from '@/test-utils/componentTestHelpers';
import { apiClient } from '@/axios-setup';

// Mock do axios-setup
vi.mock('@/axios-setup', () => ({
  apiClient: {
    get: vi.fn()
  }
}));

// Mock do vue-router
const mockPush = vi.fn();
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  }),
  useRoute: () => ({})
}));

describe('HistoricoView.vue', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  const mountOptions = getCommonMountOptions();
  // Stubbing BCard to ensure it renders slots immediately
  mountOptions.global.stubs = {
    ...mountOptions.global.stubs,
    BCard: { template: '<div><slot /></div>' },
    PageHeader: true, // Shallow render PageHeader
    BButton: true // Shallow render BButton
  };

  it('exibe o spinner de carregamento durante a busca', async () => {
    // Simula uma promessa pendente para manter o estado "loading"
    let resolvePromise: any;
    const pendingPromise = new Promise((resolve) => {
      resolvePromise = resolve;
    });

    (apiClient.get as any).mockReturnValue(pendingPromise);

    const wrapper = mount(HistoricoView, mountOptions);

    // loading starts at false, but onMounted sets it to true immediately.
    // However, if we await nextTick, the template should update.
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.spinner-border').exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
    expect(wrapper.findComponent({ name: 'EmptyState' }).exists()).toBe(false);

    // Resolve para limpar
    resolvePromise({ data: [] });
  });

  it('exibe o EmptyState quando não há processos', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [] });

    const wrapper = mount(HistoricoView, mountOptions);
    await flushPromises();

    expect(wrapper.findComponent({ name: 'EmptyState' }).exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
    expect(wrapper.text()).toContain('Nenhum processo finalizado encontrado');
  });

  it('exibe a tabela quando há processos', async () => {
    const mockProcessos = [
      {
        codigo: 1,
        descricao: 'Processo Teste 1',
        tipo: 'MAPEAMENTO',
        dataFinalizacao: '2023-01-01'
      },
      {
        codigo: 2,
        descricao: 'Processo Teste 2',
        tipo: 'REVISAO',
        dataFinalizacao: '2023-02-01'
      }
    ];

    (apiClient.get as any).mockResolvedValue({ data: mockProcessos });

    const wrapper = mount(HistoricoView, mountOptions);
    await flushPromises();

    expect(wrapper.find('table').exists()).toBe(true);
    expect(wrapper.findAll('tbody tr')).toHaveLength(2);
    expect(wrapper.text()).toContain('Processo Teste 1');
    expect(wrapper.text()).toContain('Mapeamento');
    expect(wrapper.findComponent({ name: 'EmptyState' }).exists()).toBe(false);
  });
});

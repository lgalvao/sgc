import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import NotificacoesAdminView from '../NotificacoesAdminView.vue';
import { createTestingPinia } from '@pinia/testing';
import { createRouter, createWebHistory } from 'vue-router';
import { listarResumoSubprocessosAtivos, reenviarFalhasDefinitivas } from '@/services/notificacaoService';

vi.mock('@/services/notificacaoService', () => ({
  listarResumoSubprocessosAtivos: vi.fn(),
  reenviarFalhasDefinitivas: vi.fn()
}));

const mockNotify = vi.fn();
const mockClear = vi.fn();

vi.mock('@/composables/useNotification', () => ({
  useNotification: vi.fn(() => ({
    notificacao: null,
    notify: mockNotify,
    clear: mockClear
  }))
}));

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: {} }, { path: '/subprocesso/:codProcesso/:siglaUnidade', name: 'Subprocesso', component: {} }]
});

describe('NotificacoesAdminView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountComponent = () => {
    return mount(NotificacoesAdminView, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn }), router],
        stubs: {
          LayoutPadrao: { template: '<div><slot/></div>' },
          PageHeader: { template: '<div><slot name="actions"/></div>', props: ['title'] },
          ModalConfirmacao: { template: '<div><slot/></div>', props: ['modelValue'] },
          AppAlert: true,
          BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>', props: ['disabled'] },
          BAlert: { template: '<div><slot/></div>', props: ['modelValue', 'variant'] },
          BTable: {
            template: '<table><tr v-for="item in items" :key="item.unidadeSigla"><slot name="cell(unidadeSigla)" :item="item" /><slot name="cell(processoDescricao)" :item="item" /><slot name="cell(statusGeral)" :item="item" /><slot name="cell(ultimoErro)" :item="item" /><slot name="cell(proximaTentativaEm)" :item="item" /><slot name="cell(acoes)" :item="item" /><slot name="cell(dataHoraEnvio)" :item="item" /></tr></table>',
            props: ['fields', 'items']
          },
          BSpinner: true,
          BBadge: true,
          RouterLink: { template: '<a :href="to.name"><slot/></a>', props: ['to'] },
          UnidadeLink: { template: '<div>{{ item.unidadeSigla }} - {{ item.situacaoSubprocesso }}</div>', props: ['item'] }
        }
      }
    });
  };

  it('renders loading state initially', async () => {
    vi.mocked(listarResumoSubprocessosAtivos).mockImplementation(() => new Promise(() => {}));
    const wrapper = mountComponent();
    expect(wrapper.find('[data-testid="notificacoes-carregando"]').exists()).toBe(true);
  });

  it('renders error state', async () => {
    vi.mocked(listarResumoSubprocessosAtivos).mockRejectedValue(new Error('Network error'));
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.find('[data-testid="notificacoes-carregando"]').exists()).toBe(false);
    expect(wrapper.text()).toContain('Network error');
  });

  it('renders empty states', async () => {
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue([]);
    const wrapper = mountComponent();
    await flushPromises();

    expect(wrapper.find('[data-testid="alert-notificacoes-sem-pendencias"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="alert-notificacoes-sem-concluidas"]').exists()).toBe(true);
  });

  it('renders list properly and sorts correctly', async () => {
    const mockData = [
      {
        subprocessoCodigo: 1,
        processoCodigo: 10,
        unidadeSigla: 'U1',
        situacaoSubprocesso: 'AGUARDANDO_ENVIO',
        processoDescricao: 'Proc A',
        statusGeral: 'PENDENTE',
        ultimoErro: null,
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 0,
        podeReenviar: false,
      },
      {
        subprocessoCodigo: 2,
        processoCodigo: 10,
        unidadeSigla: 'U2',
        situacaoSubprocesso: 'ERRO_ENVIO',
        processoDescricao: 'Proc B',
        statusGeral: 'FALHA_DEFINITIVA',
        ultimoErro: 'Erro fatal',
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 5,
        podeReenviar: true,
      },
      {
        subprocessoCodigo: 3,
        processoCodigo: 11,
        unidadeSigla: 'U3',
        situacaoSubprocesso: 'CONCLUIDO',
        processoDescricao: 'Proc C',
        statusGeral: 'OK',
        ultimoErro: null,
        proximaTentativaEm: null,
        ultimaNotificacaoEm: '2023-01-01T12:00:00Z',
        maiorTentativas: 0,
        podeReenviar: false,
      },
      {
        subprocessoCodigo: 4,
        processoCodigo: 10,
        unidadeSigla: 'U4',
        situacaoSubprocesso: 'ERRO_ENVIO',
        processoDescricao: 'Proc D',
        statusGeral: 'INCONSISTENTE',
        ultimoErro: 'Erro fatal 2',
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 5,
        podeReenviar: true,
      },
      {
        subprocessoCodigo: 5,
        processoCodigo: 10,
        unidadeSigla: 'U5',
        situacaoSubprocesso: 'ERRO_ENVIO',
        processoDescricao: 'Proc E',
        statusGeral: 'FALHA_TEMPORARIA',
        ultimoErro: 'Erro fatal 3',
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 5,
        podeReenviar: true,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);
    const wrapper = mountComponent();
    await flushPromises();

    expect(wrapper.find('[data-testid="notificacoes-carregando"]').exists()).toBe(false);

    // Check pending table
    const pendingTable = wrapper.find('[data-testid="sec-notificacoes-pendentes"]');
    expect(pendingTable.exists()).toBe(true);
    // Should contain U2 (FALHA_DEFINITIVA - highest prio) then U1 (PENDENTE)
    expect(pendingTable.text()).toContain('U2');
    expect(pendingTable.text()).toContain('U1');
    expect(pendingTable.text()).toContain('U4');
    expect(pendingTable.text()).toContain('U5');
    expect(pendingTable.text()).not.toContain('U3');

    // Check completed table
    const completedTable = wrapper.find('[data-testid="sec-notificacoes-concluidas"]');
    expect(completedTable.exists()).toBe(true);
    expect(completedTable.text()).toContain('U3');
  });

  it('handles re-send action successfully', async () => {
    const mockData = [
      {
        subprocessoCodigo: 2,
        processoCodigo: 10,
        unidadeSigla: 'U2',
        situacaoSubprocesso: 'ERRO_ENVIO',
        processoDescricao: 'Proc B',
        statusGeral: 'FALHA_DEFINITIVA',
        ultimoErro: 'Erro fatal',
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 5,
        podeReenviar: true,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);
    vi.mocked(reenviarFalhasDefinitivas).mockResolvedValue({ reenfileiradas: 1 });

    const wrapper = mountComponent();
    await flushPromises();

    // Click resend button
    await wrapper.find('[data-testid="btn-notificacoes-reenviar-U2"]').trigger('click');

    // Check if confirm text is visible
    expect(wrapper.find('[data-testid="txt-notificacoes-reenviar-confirmacao"]').exists()).toBe(true);

    // Simulate confirm modal
    // @ts-ignore
    wrapper.vm.reenviar();
    await flushPromises();

    expect(reenviarFalhasDefinitivas).toHaveBeenCalledWith(2);
    expect(mockNotify).toHaveBeenCalledWith(expect.stringContaining('recolocada(s)'), 'success');
  });

  it('handles re-send action failure', async () => {
    const mockData = [
      {
        subprocessoCodigo: 2,
        processoCodigo: 10,
        unidadeSigla: 'U2',
        situacaoSubprocesso: 'ERRO_ENVIO',
        processoDescricao: 'Proc B',
        statusGeral: 'FALHA_DEFINITIVA',
        ultimoErro: 'Erro fatal',
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 5,
        podeReenviar: true,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);
    vi.mocked(reenviarFalhasDefinitivas).mockRejectedValue(new Error('Resend error'));

    const wrapper = mountComponent();
    await flushPromises();

    // Click resend button
    await wrapper.find('[data-testid="btn-notificacoes-reenviar-U2"]').trigger('click');

    // Simulate confirm modal
    // @ts-ignore
    wrapper.vm.reenviar();
    await flushPromises();

    expect(reenviarFalhasDefinitivas).toHaveBeenCalledWith(2);
    expect(mockNotify).toHaveBeenCalledWith('Resend error', 'danger');
  });

  it('handles ignoring re-send if nothing selected', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    // @ts-ignore
    wrapper.vm.linhaSelecionada = null;
    // @ts-ignore
    wrapper.vm.reenviar();

    expect(reenviarFalhasDefinitivas).not.toHaveBeenCalled();
  });

  it('covers manual refresh action', async () => {
    const mockData = [
      {
        subprocessoCodigo: 1,
        processoCodigo: 10,
        unidadeSigla: 'U1',
        situacaoSubprocesso: 'AGUARDANDO_ENVIO',
        processoDescricao: 'Proc A',
        statusGeral: 'PENDENTE',
        ultimoErro: null,
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 0,
        podeReenviar: false,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);

    const wrapper = mountComponent();
    await flushPromises();

    vi.clearAllMocks();

    await wrapper.find('[data-testid="btn-notificacoes-atualizar"]').trigger('click');
    await flushPromises();

    expect(vi.mocked(listarResumoSubprocessosAtivos)).toHaveBeenCalled();
  });

  it('covers formatters', async () => {
    const mockData = [
      {
        subprocessoCodigo: 1,
        processoCodigo: 10,
        unidadeSigla: 'U1',
        situacaoSubprocesso: 'AGUARDANDO_ENVIO',
        processoDescricao: 'Proc A',
        statusGeral: 'PENDENTE',
        ultimoErro: null,
        proximaTentativaEm: '2023-01-01T12:00:00Z',
        ultimaNotificacaoEm: 'invalid date',
        maiorTentativas: 0,
        podeReenviar: false,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);
    const wrapper = mountComponent();
    await flushPromises();

    expect(wrapper.text()).toContain('01/01/2023');
  });

  it('renders correctly without stubs to cover UnidadeLink', async () => {
    const mockData = [
      {
        subprocessoCodigo: 1,
        processoCodigo: 10,
        unidadeSigla: 'U1',
        situacaoSubprocesso: 'AGUARDANDO_ENVIO',
        processoDescricao: 'Proc A',
        statusGeral: 'PENDENTE',
        ultimoErro: null,
        proximaTentativaEm: null,
        ultimaNotificacaoEm: null,
        maiorTentativas: 0,
        podeReenviar: false,
      }
    ];
    // @ts-ignore
    vi.mocked(listarResumoSubprocessosAtivos).mockResolvedValue(mockData);

    const wrapper = mount(NotificacoesAdminView, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn }), router],
        stubs: {
          LayoutPadrao: { template: '<div><slot/></div>' },
          PageHeader: { template: '<div><slot name="actions"/></div>', props: ['title'] },
          ModalConfirmacao: { template: '<div><slot/></div>', props: ['modelValue'] },
          AppAlert: true,
          BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>', props: ['disabled'] },
          BAlert: { template: '<div><slot/></div>', props: ['modelValue', 'variant'] },
          BSpinner: true,
          BBadge: true,
        }
      }
    });

    await flushPromises();
    expect(wrapper.find('[data-testid="notificacao-unidade-U1"]').exists()).toBe(true);
  });
});

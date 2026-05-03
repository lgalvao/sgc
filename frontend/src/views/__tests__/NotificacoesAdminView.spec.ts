import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import NotificacoesAdminView from '../NotificacoesAdminView.vue';
import {createTestingPinia} from '@pinia/testing';
import {createMemoryHistory, createRouter} from 'vue-router';
import {listarNotificacoesAdmin, reenviarNotificacao} from '@/services/notificacaoService';
import {formatarDestinatario} from "@/utils/notificacaoFormatters";

vi.mock('@/services/notificacaoService', async (importActual) => {
  const actual = await importActual<typeof import('@/services/notificacaoService')>();
  return {
    ...actual,
    listarNotificacoesAdmin: vi.fn(),
    reenviarNotificacao: vi.fn()
  };
});

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
  history: createMemoryHistory(),
  routes: [{ path: '/', component: {} }]
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
          EmptyState: { template: '<div class="empty-state-stub"><slot/></div>', props: ['title', 'description', 'icon'] },
          BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>', props: ['disabled', 'variant'] },
          BAlert: { template: '<div><slot/></div>', props: ['modelValue', 'variant'] },
          BTable: {
            template: '<table><tr v-for="item in items" :key="item.codigo"><slot name="cell(destinatario)" :item="item" /><slot name="cell(tipoNotificacao)" :item="item" /><slot name="cell(assunto)" :item="item" /><slot name="cell(situacao)" :item="item" /><slot name="cell(quando)" :item="item" /><slot name="cell(acoes)" :item="item" /></tr></table>',
            props: ['fields', 'items']
          },
          BSpinner: true,
          BBadge: true,
          BModal: { template: '<div v-if="modelValue" class="modal-stub" v-bind="$attrs"><slot/></div>', props: ['modelValue', 'title'] },
          NotificacaoTabela: {
            template: '<div data-testid="tbl-notificacoes"><div v-for="item in items" :key="item.codigo" class="row-stub"><span>{{ item.assunto }}</span><span>{{ item.tipoNotificacao }}</span><button :data-testid="\'btn-detalhes-\' + item.codigo" @click="$emit(\'detalhes\', item)"></button><button :data-testid="\'btn-preview-\' + item.codigo" @click="$emit(\'preview\', item)"></button><button :data-testid="\'btn-notificacoes-reenviar-\' + item.codigo" @click="$emit(\'reenviar\', item)"></button></div></div>',
            props: ['items']
          }
        }
      }
    });
  };

  it('renders loading state initially', async () => {
    vi.mocked(listarNotificacoesAdmin).mockImplementation(() => new Promise(() => {}));
    const wrapper = mountComponent();
    expect(wrapper.find('[data-testid="notificacoes-carregando"]').exists()).toBe(true);
  });

  it('renders unified list of notifications', async () => {
    const mockData = [
      {
        codigo: 1,
        unidadeSigla: 'U1',
        processoDescricao: 'Processo Alfa',
        tipoNotificacao: 'PROCESSO_INICIADO',
        destinatario: 'u1@tre-pe.jus.br',
        assunto: 'SGC: Assunto Enviado',
        situacao: 'ENVIADO',
        tentativas: 0,
        dataHoraCriacao: '2023-01-01T10:00:00Z',
        dataHoraEnvio: '2023-01-01T10:05:00Z',
        corpoHtml: '<p>corpo</p>'
      },
      {
        codigo: 2,
        unidadeSigla: 'U2',
        processoDescricao: 'Processo Beta',
        tipoNotificacao: 'MAPA_HOMOLOGADO',
        destinatario: 'u2@tre-pe.jus.br',
        assunto: 'SGC: Assunto Pendente',
        situacao: 'FALHA_DEFINITIVA',
        tentativas: 2,
        dataHoraCriacao: '2023-01-01T11:00:00Z',
        ultimoErro: 'Erro fatal'
      }
    ];
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue(mockData as any);
    const wrapper = mountComponent();
    await flushPromises();

    expect(wrapper.find('[data-testid="tbl-notificacoes"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="tbl-notificacoes"]').text()).toContain('Assunto Enviado');
    expect(wrapper.find('[data-testid="tbl-notificacoes"]').text()).toContain('Assunto Pendente');
    expect(wrapper.text()).toContain('PROCESSO_INICIADO');
    expect(wrapper.text()).toContain('MAPA_HOMOLOGADO');
    expect(wrapper.text()).toContain('SGC: Assunto Enviado');
  });

  it('opens preview modal', async () => {
    const mockData = [
      {
        codigo: 1,
        assunto: 'Assunto Preview',
        situacao: 'ENVIADO',
        tentativas: 0,
        dataHoraCriacao: '2023-01-01T10:00:00Z',
        corpoHtml: '<p>conteudo do email</p>',
        destinatario: 'destino@teste.com'
      }
    ];
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue(mockData as any);
    const wrapper = mountComponent();
    await flushPromises();

    await wrapper.find('[data-testid="btn-preview-1"]').trigger('click');

    expect(wrapper.find('[data-testid="modal-preview-email"]').exists()).toBe(true);
    const iframe = wrapper.find('[data-testid="iframe-preview-email"]');
    expect(iframe.exists()).toBe(true);
    expect((iframe.element as HTMLIFrameElement).getAttribute('srcdoc')).toContain('conteudo do email');
  });

  it('opens details modal', async () => {
    const mockData = [
      {
        codigo: 2,
        situacao: 'FALHA_DEFINITIVA',
        tentativas: 5,
        destinatario: 'destino@teste.com',
        dataHoraCriacao: '2023-01-01T11:00:00Z',
        ultimoErro: 'SMTP indisponivel'
      }
    ];
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue(mockData as any);
    const wrapper = mountComponent();
    await flushPromises();

    await wrapper.find('[data-testid="btn-detalhes-2"]').trigger('click');
    expect(wrapper.find('[data-testid="modal-detalhes-notificacao"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('SMTP indisponivel');
  });

  it('handles re-send action', async () => {
    const mockData = [
      {
        codigo: 2,
        situacao: 'FALHA_DEFINITIVA',
        tentativas: 5,
        destinatario: 'destino@teste.com'
      }
    ];
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue(mockData as any);
    vi.mocked(reenviarNotificacao).mockResolvedValue({ codigo: 2, reenfileiradas: 1 });

    const wrapper = mountComponent();
    await flushPromises();

    await wrapper.find('[data-testid="btn-notificacoes-reenviar-2"]').trigger('click');
    expect(wrapper.find('[data-testid="txt-notificacoes-reenviar-confirmacao"]').text()).toContain('destino@teste.com');

    await (wrapper.vm as any).reenviar();
    await flushPromises();

    expect(reenviarNotificacao).toHaveBeenCalledWith(2);
    expect(mockNotify).toHaveBeenCalledWith('E-mail recolocado na fila de envio', 'success');
  });

  it('covers manual refresh action', async () => {
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue([] as any);
    const wrapper = mountComponent();
    await flushPromises();

    vi.clearAllMocks();
    await wrapper.find('[data-testid="btn-notificacoes-atualizar"]').trigger('click');
    expect(vi.mocked(listarNotificacoesAdmin)).toHaveBeenCalled();
  });

  it('handles re-send failure', async () => {
    const mockData = [{ codigo: 1, situacao: 'FALHA_DEFINITIVA', destinatario: 'a@a.com' }];
    vi.mocked(listarNotificacoesAdmin).mockResolvedValue(mockData as any);
    vi.mocked(reenviarNotificacao).mockRejectedValue(new Error('Erro Reenvio'));

    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.itemSelecionado = mockData[0];

    await vm.reenviar();
    await flushPromises();

    expect(mockNotify).toHaveBeenCalledWith('Erro Reenvio', 'danger');
  });

  it('handles load failure', async () => {
    vi.mocked(listarNotificacoesAdmin).mockRejectedValue(new Error('Erro Lista'));
    const wrapper = mountComponent();
    await flushPromises();

    expect(wrapper.text()).toContain('Erro Lista');
  });

  it('covers formatarDestinatario institutional email branch', () => {
    // 1. Institutional email
    const item1 = { destinatario: 'leonardo@tre-pe.jus.br' };
    expect(formatarDestinatario(item1)).toBe('LEONARDO');

    // 2. Already has title
    const item2 = { destinatario: 'a@a.com', usuarioDestinoTitulo: 'TITULO' };
    expect(formatarDestinatario(item2)).toBe('a@a.com');
  });
});

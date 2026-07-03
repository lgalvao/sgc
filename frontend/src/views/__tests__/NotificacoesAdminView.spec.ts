import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {PiniaColada} from '@pinia/colada';
import NotificacoesAdminView from '../NotificacoesAdminView.vue';
import {createPinia} from 'pinia';
import {createMemoryHistory, createRouter} from 'vue-router';
import {buscarUrlLeitorEmailTestes, listarNotificacoesAdmin, reenviarNotificacao} from '@/services/notificacaoService';
import {formatarDestinatario} from "@/utils/notificacaoFormatters";
import {ehModoProducao} from "@/utils/ambiente";
import {usePerfilStore} from "@/stores/perfil";

vi.mock('@/services/notificacaoService', async (importActual) => {
    const actual = await importActual<typeof import('@/services/notificacaoService')>();
    return {
        ...actual,
        listarNotificacoesAdmin: vi.fn(),
        reenviarNotificacao: vi.fn(),
        buscarUrlLeitorEmailTestes: vi.fn()
    };
});

vi.mock('@/utils/ambiente', () => ({
    ehModoProducao: vi.fn(() => false)
}));

const mockNotify = vi.fn();
const mockClear = vi.fn();
const exibirSucessoMock = vi.fn();

vi.mock('@/composables/useNotification', () => ({
    useNotification: vi.fn(() => ({
        notificacao: null,
        notify: mockNotify,
        clear: mockClear
    }))
}));

vi.mock('@/composables/useToast', () => ({
    useToast: vi.fn(() => ({
        exibirSucesso: exibirSucessoMock,
        exibirErro: vi.fn(),
        exibirToast: vi.fn(),
        registrarPendente: vi.fn(),
        exibirPendente: vi.fn(),
    }))
}));

const router = createRouter({
    history: createMemoryHistory(),
    routes: [{path: '/', component: {}}]
});

describe('NotificacoesAdminView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(buscarUrlLeitorEmailTestes).mockResolvedValue(null);
        vi.mocked(ehModoProducao).mockReturnValue(false);
    });

    const mountComponent = () => {
        const pinia = createPinia();
        const perfilStore = usePerfilStore(pinia);
        perfilStore.perfilSelecionado = 'ADMIN' as any;

        return mount(NotificacoesAdminView, {
            global: {
                plugins: [pinia, [PiniaColada, {}], router],
                stubs: {
                    LayoutPadrao: {template: '<div><slot/></div>'},
                    CarregamentoPagina: {template: '<div data-testid="pagina-carregando"></div>'},
                    PageHeader: {template: '<div><slot name="actions"/></div>', props: ['title']},
                    NotificacoesAdminFluxoModais: {
                        props: ['mostrarPreview', 'mostrarDetalhes', 'mostrarModalReenvio', 'itemSelecionado', 'itemParaPreview', 'itemParaDetalhes'],
                        template: `
                          <div>
                            <div v-if="mostrarPreview" data-testid="modal-preview-email">
                              <iframe data-testid="iframe-preview-email" :srcdoc="itemParaPreview?.corpoHtml || ''"></iframe>
                            </div>
                            <div v-if="mostrarDetalhes" data-testid="modal-detalhes-notificacao">
                              {{ itemParaDetalhes?.ultimoErro }}
                            </div>
                            <div v-if="mostrarModalReenvio">
                              <p data-testid="txt-notificacoes-reenviar-confirmacao">
                                Confirma o reenvio deste e-mail específico para {{ itemSelecionado?.destinatario }}?
                              </p>
                            </div>
                          </div>
                        `
                    },
                    Alerta: {
                        template: '<div><slot />{{ mensagem }}{{ notificacao?.resumo }}</div>',
                        props: ['mensagem', 'notificacao']
                    },
                    EmptyState: {
                        template: '<div class="empty-state-stub"><slot/></div>',
                        props: ['title', 'description', 'icon']
                    },
                    BButton: {
                        template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot/></button>',
                        props: ['disabled', 'variant']
                    },
                    BAlert: {template: '<div><slot/></div>', props: ['modelValue', 'variant']},
                    BTable: {
                        template: '<table><tr v-for="item in items" :key="item.codigo"><slot name="cell(destinatario)" :item="item" /><slot name="cell(tipoNotificacao)" :item="item" /><slot name="cell(assunto)" :item="item" /><slot name="cell(situacao)" :item="item" /><slot name="cell(quando)" :item="item" /><slot name="cell(acoes)" :item="item" /></tr></table>',
                        props: ['fields', 'items']
                    },
                    BSpinner: true,
                    BBadge: true,
                    NotificacaoTabela: {
                        template: '<div data-testid="tbl-notificacoes"><div v-for="item in items" :key="item.codigo" class="row-stub"><span>{{ item.assunto }}</span><span>{{ item.tipoNotificacao }}</span><button :data-testid="\'btn-detalhes-\' + item.codigo" @click="$emit(\'detalhes\', item)"></button><button :data-testid="\'btn-preview-\' + item.codigo" @click="$emit(\'preview\', item)"></button><button :data-testid="\'btn-notificacoes-reenviar-\' + item.codigo" @click="$emit(\'reenviar\', item)"></button></div></div>',
                        props: ['items']
                    }
                }
            }
        });
    };

    it('renders loading state initially', async () => {
        vi.mocked(listarNotificacoesAdmin).mockImplementation(() => new Promise(() => {
        }));
        const wrapper = mountComponent();
        expect(wrapper.find('[data-testid="pagina-carregando"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('Notificações');
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

    it('exibe link do leitor de e-mail de testes quando configurado', async () => {
        vi.mocked(listarNotificacoesAdmin).mockResolvedValue([] as any);
        vi.mocked(buscarUrlLeitorEmailTestes).mockResolvedValue('https://seseldev06.tre-pe.gov.br/');

        const wrapper = mountComponent();
        await flushPromises();

        const link = wrapper.find('[data-testid="link-leitor-email-testes"]');
        expect(link.exists()).toBe(true);
        expect(link.attributes('href')).toBe('https://seseldev06.tre-pe.gov.br/');
        expect(link.text()).toContain('Leitor de e-mail de testes');
    });

    it('nao exibe link do leitor em producao', async () => {
        vi.mocked(listarNotificacoesAdmin).mockResolvedValue([] as any);
        vi.mocked(buscarUrlLeitorEmailTestes).mockResolvedValue('https://seseldev06.tre-pe.gov.br/');
        vi.mocked(ehModoProducao).mockReturnValue(true);

        const wrapper = mountComponent();
        await flushPromises();

        expect(wrapper.find('[data-testid="link-leitor-email-testes"]').exists()).toBe(false);
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
        vi.mocked(reenviarNotificacao).mockResolvedValue({codigo: 2, reenfileiradas: 1});

        const wrapper = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-notificacoes-reenviar-2"]').trigger('click');
        expect(wrapper.find('[data-testid="txt-notificacoes-reenviar-confirmacao"]').text()).toContain('destino@teste.com');

        await (wrapper.vm as any).reenviar();
        await flushPromises();

        expect(reenviarNotificacao).toHaveBeenCalledWith(2);
        expect(exibirSucessoMock).toHaveBeenCalledWith('E-mail recolocado na fila de envio');
    });

    it('handles re-send failure', async () => {
        const mockData = [{codigo: 1, situacao: 'FALHA_DEFINITIVA', destinatario: 'a@a.com'}];
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
        const item1 = {destinatario: 'leonardo@tre-pe.jus.br'};
        expect(formatarDestinatario(item1)).toBe('LEONARDO');

        // 2. Already has title
        const item2 = {destinatario: 'a@a.com', usuarioDestinoTitulo: 'TITULO'};
        expect(formatarDestinatario(item2)).toBe('a@a.com');
    });

    it('formata data com hifen para valores invalidos', () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        expect(vm.formatarDataOuHifen(null)).toBe('-');
        expect(vm.formatarDataOuHifen('')).toBe('-');
        expect(vm.formatarDataOuHifen('data-invalida')).toBe('-');
    });

    it('limpa html para preview removendo scripts e handlers', () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        const sujo = '<p>Olá</p><script>alert(1)</script><div onclick="alert(2)"></div>';
        const limpo = vm.limparHtmlPreview(sujo);
        expect(limpo).not.toContain('<script>');
        expect(limpo).not.toContain('onclick');
        expect(limpo).toContain('<p>Olá</p>');
    });

    it('montarPreviewHtml lida com corpo vazio', () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        const html = vm.montarPreviewHtml('');
        expect(html).toContain('Conteúdo indisponível');
    });

    it('montarPreviewHtml remove a largura fixa do layout de e-mail no preview', () => {
        const wrapper = mountComponent();
        const vm = wrapper.vm as any;
        const html = vm.montarPreviewHtml('<div class="container"><div class="content">Teste</div></div>');
        expect(html).toContain('max-width: none !important;');
        expect(html).toContain('width: 100% !important;');
    });
});

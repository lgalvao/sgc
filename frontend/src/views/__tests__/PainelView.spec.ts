import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import PainelView from '../PainelView.vue';
import {createTestingPinia} from '@pinia/testing';
import {useToastStore} from '@/stores/toast';
import {usePainelStore} from '@/stores/painel';
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
        {path: '/', component: {template: '<div></div>'}},
        {path: '/detalhes', component: {template: '<div></div>'}},
        {path: '/cadastro', name: 'CadProcesso', component: {template: '<div></div>'}}
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
                LayoutPadrao: {template: '<div><slot></slot></div>'},
                PageHeader: {template: '<div><slot></slot><slot name="actions"></slot></div>', props: ['title']},
                TabelaProcessos: {template: '<div data-testid="tbl-processos"></div>'},
                BTable: {
                    template: '<div><slot name="cell(mensagem)" :item="{}" :value="123"></slot></div>',
                    props: ['items', 'fields']
                },
                EmptyState: {template: '<div data-testid="empty-state-alertas"></div>'},
                BSpinner: {template: '<div data-testid="spinner-painel"></div>'},
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
        toastStore.consumePending = vi.fn().mockReturnValue({body: 'Sucesso'});

        mount(PainelView, options);
        await flushPromises();

        expect(painelService.obterBootstrap).toHaveBeenCalled();
        expect(mockToastCreate).toHaveBeenCalledWith(expect.objectContaining({props: expect.objectContaining({body: 'Sucesso'})}));
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
        const options = createMountOptions({unidadeSelecionada: null});
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
        expect(mockRouterPush).toHaveBeenCalledWith('/detalhes');
    });

    it('deve retornar classes e atributos da linha de alertas corretamente', async () => {
        const wrapper = mount(PainelView, createMountOptions());
        await flushPromises();
        const vm = wrapper.vm as any;

        // null checks
        expect(vm.rowClassAlerta(null)).toBe('');
        expect(vm.rowAttrAlerta(null)).toEqual({});

        // item sem leitura (negrito)
        expect(vm.rowClassAlerta({dataHoraLeitura: null})).toBe('fw-bold');

        // item com leitura (normal)
        expect(vm.rowClassAlerta({dataHoraLeitura: '2025-01-01'})).toBe('');

        // attr id
        expect(vm.rowAttrAlerta({codigo: 99})).toEqual({'data-testid': 'row-alerta-99'});
    });

    it('cobre ordenacao computed localmente', async () => {
        const options = createMountOptions();
        const wrapper = mount(PainelView, options);
        await flushPromises();

        const painelStore = (wrapper.vm as any).painelStore;

        painelStore.processos = [
            {codigo: 1, descricao: 'B'},
            {codigo: 2, descricao: 'A'},
            {codigo: 3, descricao: 'B'},
            {codigo: 4}, // undefined descricao
        ];

        const vm = wrapper.vm as any;
        vm.criterio = 'descricao';
        vm.asc = true;

        const ordenadosAsc = vm.processosOrdenados;
        expect(ordenadosAsc[0].codigo).toBe(4);
        expect(ordenadosAsc[1].codigo).toBe(2);

        vm.asc = false;
        const ordenadosDesc = vm.processosOrdenados;
        expect(ordenadosDesc[0].descricao).toBe('B');
    });

    it('cobre onActivated e comportamento de cache', async () => {
        const options = createMountOptions();

        // Mock dadosValidos BEFORE mounting so onActivated doesn't trigger a second call
        const pinia = options.global.plugins[1] as any;
        const painelStore = usePainelStore(pinia);
        painelStore.dadosValidos = vi.fn().mockReturnValue(true);

        const KeepAliveWrapper = {
            template: `<keep-alive><PainelView v-if="show" /></keep-alive>`,
            components: {PainelView},
            data() {
                return {show: true}
            }
        };
        const wrapper = mount(KeepAliveWrapper, options);
        const vm = wrapper.vm as unknown as { show: boolean };
        await flushPromises();

        expect(painelService.obterBootstrap).toHaveBeenCalledTimes(1);

        // Deactivate
        vm.show = false;
        await flushPromises();

        // Activate - cache is INVALID
        painelStore.dadosValidos = vi.fn().mockReturnValue(false);
        vm.show = true;
        await flushPromises();
        expect(painelService.obterBootstrap).toHaveBeenCalledTimes(2);

        // Deactivate
        vm.show = false;
        await flushPromises();

        // Activate - cache is VALID
        painelStore.dadosValidos = vi.fn().mockReturnValue(true);
        vm.show = true;
        await flushPromises();
        expect(painelService.obterBootstrap).toHaveBeenCalledTimes(2); // Should not increase
    });

    it('deve mostrar estado vazio de alertas sem renderizar a tabela', async () => {
        vi.mocked(painelService.obterBootstrap).mockResolvedValueOnce({
            processos: [],
            alertas: [],
        } as any);

        const wrapper = mount(PainelView, createMountOptions());
        await flushPromises();

        expect(wrapper.find('[data-testid="empty-state-alertas"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-alertas"]').exists()).toBe(false);
    });

    it('deve renderizar a tabela de alertas quando houver itens', async () => {
        const alertaMock = {
            codigo: 1,
            codProcesso: 10,
            unidadeOrigem: 'SEC',
            unidadeDestino: 'CGP',
            descricao: 'Alerta',
            dataHora: '2026-03-18T10:00:00',
            dataHoraLeitura: null,
            mensagem: 'Processo pendente',
            origem: 'Secretaria',
            processo: 'Processo 10',
        };
        vi.mocked(painelService.obterBootstrap).mockResolvedValueOnce({
            processos: [],
            alertas: [alertaMock],
        } as any);

        const wrapper = mount(PainelView, createMountOptions());
        await flushPromises();

        expect(wrapper.find('[data-testid="tbl-alertas"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="empty-state-alertas"]').exists()).toBe(false);
    });

    it('deve mostrar botão de criar processo quando mostrarCriarProcesso=true', async () => {
        const wrapper = mount(PainelView, createMountOptions());
        await flushPromises();
        expect(wrapper.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(true);
    });

    it('não deve mostrar botão de criar processo quando mostrarCriarProcesso=false', async () => {
        const wrapper = mount(PainelView, createMountOptions({permissoesSessao: {mostrarCriarProcesso: false}} as any));
        await flushPromises();
        expect(wrapper.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(false);
    });

    it('deve redirecionar para CadProcesso ao emitir evento cta-vazio', async () => {
        const opcoesCtaVazio = {
            ...createMountOptions(),
            global: {
                ...createMountOptions().global,
                stubs: {
                    ...createMountOptions().global.stubs,
                    TabelaProcessos: {
                        name: 'TabelaProcessos',
                        template: '<div data-testid="tbl-processos"></div>',
                        emits: ['cta-vazio'],
                    },
                },
            },
        };
        const wrapper = mount(PainelView, opcoesCtaVazio);
        await flushPromises();

        mockRouterPush.mockClear();
        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('cta-vazio');
        expect(mockRouterPush).toHaveBeenCalledWith({name: 'CadProcesso'});
    });
});

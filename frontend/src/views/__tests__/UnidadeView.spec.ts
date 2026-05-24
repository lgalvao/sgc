import {describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import UnidadeView from '@/views/UnidadeView.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {BAlert} from 'bootstrap-vue-next';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {nextTick, ref} from "vue";

const {
    mockPush,
    mockUnidadeData,
    mockMapaVigente,
    mockObterUnidade,
    mockObterReferenciaMapaVigente,
    mockUnidadeStore,
    notify,
    downloadRelatorioMapaVigenteUnidadePdf,
    downloadRelatorioMapaVigenteUnidadeCsv
} = vi.hoisted(() => {
    const u = {
        codigo: 10,
        nome: 'Titular teste',
        tituloEleitoral: '123456',
        matricula: 'M10',
        email: 't@t',
        ramal: '1',
        unidade: {codigo: 1, sigla: 'TEST'}
    };
    const ur = {
        codigo: 20,
        nome: 'Responsavel teste',
        tituloEleitoral: '654321',
        matricula: 'M20',
        email: 'r@r',
        ramal: '2',
        unidade: {codigo: 1, sigla: 'TEST'}
    };
    return {
        mockPush: vi.fn(),
        mockUnidadeData: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'UnidadeView Teste',
            titular: u,
            responsavel: ur,
            tipoResponsabilidade: 'SUBSTITUTO',
            dataFimResponsabilidade: '2026-05-30T23:59:59',
            filhas: [
                {codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: []},
                {codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: []}
            ]
        },
        mockMapaVigente: {codProcesso: 99, codSubprocesso: 77},
        mockObterUnidade: vi.fn(),
        mockObterReferenciaMapaVigente: vi.fn(),
        mockUnidadeStore: {
            cacheUnidades: new Map<number, unknown>(),
            cacheMapasVigentes: new Map<number, unknown>(),
            obterUnidade: vi.fn(),
            obterReferenciaMapaVigente: vi.fn(),
        },
        notify: vi.fn(),
        downloadRelatorioMapaVigenteUnidadePdf: vi.fn(),
        downloadRelatorioMapaVigenteUnidadeCsv: vi.fn(),
    };
});

vi.mock('vue-router', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        useRouter: () => ({
            push: mockPush,
        }),
    };
});

vi.mock('@/stores/unidade', () => {
    return {
        useUnidadeStore: () => mockUnidadeStore,
    };
});
vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({notify}),
}));
vi.mock('@/services/relatoriosService', () => ({
    relatoriosService: {
        downloadRelatorioMapaVigenteUnidadePdf,
        downloadRelatorioMapaVigenteUnidadeCsv,
    },
}));
vi.mock('@/utils/apiError', () => ({
    normalizarErro: vi.fn((e) => ({
        mensagem: e?.response?.data?.message || e?.message || 'Erro padrão'
    })),
}));

const TreeTableStub = {
    template: '<div data-testid="tree-table"></div>',
    props: ['data', 'columns', 'title'],
    emits: ['row-click']
};

describe('UnidadeView.vue', () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
        mockUnidadeStore.cacheUnidades.clear();
        mockUnidadeStore.cacheMapasVigentes.clear();
        mockUnidadeStore.obterUnidade = mockObterUnidade;
        mockUnidadeStore.obterReferenciaMapaVigente = mockObterReferenciaMapaVigente;
        mockObterUnidade.mockResolvedValue(mockUnidadeData);
        mockObterReferenciaMapaVigente.mockResolvedValue(null);
        downloadRelatorioMapaVigenteUnidadePdf.mockResolvedValue(undefined);
        downloadRelatorioMapaVigenteUnidadeCsv.mockResolvedValue(undefined);
    });

    const createWrapper = (initialStateOverride = {}) => {
        context.wrapper = mount(UnidadeView, {
            ...getCommonMountOptions(
                {
                    perfil: {
                        perfilSelecionado: 'USER',
                        permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
                    },
                    ...initialStateOverride
                },
                {
                    BContainer: {template: '<div><slot /></div>'},
                    BCard: {template: '<div><slot /></div>'},
                    BCardBody: {template: '<div><slot /></div>'},
                    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BDropdown: {template: '<div><slot /></div>'},
                    BDropdownItemButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BAlert: {template: '<div><slot /></div>', emits: ['dismissed']},
                    TreeTable: TreeTableStub,
                },
                {stubActions: false}
            ),
            props: {
                codUnidade: 1
            },
        });
        return {wrapper: context.wrapper};
    };

    const createKeepAliveWrapper = (manterMontado: { value: boolean }, initialStateOverride = {}) => mount({
        components: {UnidadeView},
        setup() {
            return {manterMontado};
        },
        template: "<keep-alive><UnidadeView v-if='manterMontado' :cod-unidade='1' /></keep-alive>",
    }, {
        ...getCommonMountOptions(
            {
                perfil: {
                    perfilSelecionado: 'USER',
                    permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
                },
                ...initialStateOverride
            },
            {
                BContainer: {template: '<div><slot /></div>'},
                BCard: {template: '<div><slot /></div>'},
                BCardBody: {template: '<div><slot /></div>'},
                BButton: {template: `<button @click="$emit('click')"><slot /></button>`},
                BDropdown: {template: '<div><slot /></div>'},
                BDropdownItemButton: {template: `<button @click="$emit('click')"><slot /></button>`},
                BAlert: {template: '<div><slot /></div>', emits: ['dismissed']},
                TreeTable: TreeTableStub,
            },
            {stubActions: false}
        )
    });

    it('fetches data on mount', async () => {
        createWrapper();
        expect(mockObterUnidade).toHaveBeenCalledWith(1, false);
    });

    it('renders unit details correctly', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain('TEST');
        expect(wrapper.text()).toContain('UnidadeView Teste');
        expect(wrapper.text()).toContain('Titular teste');
        expect(wrapper.text()).toContain('Substituição');
    });

    it('não exibe responsável quando for o titular', async () => {
        const unidadeTitular = {
            ...mockUnidadeData,
            responsavel: mockUnidadeData.titular,
            tipoResponsabilidade: 'TITULAR'
        };
        mockObterUnidade.mockResolvedValueOnce(unidadeTitular);

        const {wrapper} = createWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain('Titular teste');
        expect(wrapper.find('[data-testid="unidade-titular-info"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="unidade-responsavel-info"]').exists()).toBe(true);
        expect(wrapper.text()).toContain('Titular');
        expect(wrapper.text()).not.toContain('Responsável');
    });

    it('renders subordinate units tree table', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        expect(treeTable.exists()).toBe(true);
    });

    it('navigates to subordinate unit on row click', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const treeTable = wrapper.findComponent(TreeTableStub);
        treeTable.vm.$emit('row-click', {codigo: 2});

        expect(mockPush).toHaveBeenCalledWith({path: '/unidade/2'});
    });

    it('não exibe botão de visualização do mapa vigente na tela da unidade', async () => {
        mockObterReferenciaMapaVigente.mockResolvedValueOnce(mockMapaVigente);
        const {wrapper} = createWrapper();
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-mapa-vigente"]').exists()).toBe(false);
    });

    it('exporta PDF do mapa vigente para CHEFE', async () => {
        mockObterReferenciaMapaVigente.mockResolvedValueOnce(mockMapaVigente);
        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'CHEFE',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        await wrapper.find('[data-testid="btn-exportar-mapa-vigente-pdf"]').trigger('click');

        expect(downloadRelatorioMapaVigenteUnidadePdf).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();
    });

    it('exporta CSV do mapa vigente para CHEFE', async () => {
        mockObterReferenciaMapaVigente.mockResolvedValueOnce(mockMapaVigente);
        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'CHEFE',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        await wrapper.find('[data-testid="btn-exportar-mapa-vigente-csv"]').trigger('click');

        expect(downloadRelatorioMapaVigenteUnidadeCsv).toHaveBeenCalledWith(1);
        expect(notify).not.toHaveBeenCalled();
    });

    it('displays error alert when fetching unit fails', async () => {
        mockObterUnidade.mockRejectedValueOnce(new Error('Erro ao carregar unidade'));
        const {wrapper} = createWrapper();
        await flushPromises();

        const alert = wrapper.findComponent(BAlert);
        expect(alert.exists()).toBe(true);
        expect(wrapper.text()).toContain('Erro ao carregar unidade');
    });

    it('renderiza contatos do titular com links', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        const emailLink = wrapper.find('a[href="mailto:t@t"]');
        expect(emailLink.exists()).toBe(true);
        expect(emailLink.text()).toBe('t@t');
    });

    it('handles null unit gracefully', async () => {
        mockObterUnidade.mockResolvedValue(null);
        const {wrapper} = createWrapper();
        await flushPromises();

        expect(wrapper.findComponent(EmptyState).exists()).toBe(true);
    });

    it('não deve duplicar carregamento quando mudança de unidade e onActivated coincidirem', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();

        let resolver!: (valor: unknown) => void;
        mockObterUnidade.mockImplementationOnce(() => new Promise((resolve) => {
            resolver = resolve;
        }) as any);
        mockObterReferenciaMapaVigente.mockResolvedValueOnce(null);

        const trocaProps = wrapper.setProps({codUnidade: 2});
        const hook = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a)?.[0];
        const ativacao = hook?.call(wrapper.vm);

        expect(mockObterUnidade).toHaveBeenCalledTimes(2);
        expect(mockObterReferenciaMapaVigente).toHaveBeenCalledTimes(2);

        resolver({
            ...mockUnidadeData,
            codigo: 2,
            sigla: 'TEST2',
            nome: 'Unidade 2'
        });
        await trocaProps;
        await ativacao;
        await flushPromises();
    });

    it('exibe botão de editar atribuição quando responsável atual veio de AT', async () => {
        mockObterUnidade.mockResolvedValueOnce({
            ...mockUnidadeData,
            tipoResponsabilidade: 'ATRIBUICAO_TEMPORARIA',
            dataFimResponsabilidade: '2026-05-30T23:59:59'
        });

        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'ADMIN',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: true},
            },
        });
        await flushPromises();

        expect(wrapper.text()).toContain('Editar atribuição');
        expect(wrapper.text()).toContain('Atrib. temporária');
    });

    it('reaplica o snapshot atualizado da store ao reativar a view', async () => {
        const unidadeComAtribuicao = {
            ...mockUnidadeData,
            tipoResponsabilidade: 'ATRIBUICAO_TEMPORARIA',
            dataFimResponsabilidade: '2026-05-30T23:59:59'
        };
        const unidadeSemAtribuicao = {
            ...mockUnidadeData,
            responsavel: mockUnidadeData.titular,
            tipoResponsabilidade: 'TITULAR',
            dataFimResponsabilidade: null
        };

        mockObterUnidade
            .mockResolvedValueOnce(unidadeComAtribuicao)
            .mockResolvedValue(unidadeSemAtribuicao);

        const manterMontado = ref(true);
        const wrapper = createKeepAliveWrapper(manterMontado, {
            perfil: {
                perfilSelecionado: 'ADMIN',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: true},
            },
        });
        await flushPromises();

        expect(wrapper.find('[data-testid="unidade-view__btn-atribuicao-texto"]').text()).toBe('Editar atribuição');

        mockUnidadeStore.cacheUnidades.set(1, unidadeSemAtribuicao);
        mockUnidadeStore.cacheMapasVigentes.set(1, null);

        manterMontado.value = false;
        await nextTick();
        manterMontado.value = true;
        await flushPromises();

        expect(mockObterUnidade).toHaveBeenCalledTimes(1);
        expect(wrapper.find('[data-testid="unidade-view__btn-atribuicao-texto"]').text()).toBe('Criar atribuição');
        expect(wrapper.text()).toContain('Titular');
        expect(wrapper.text()).not.toContain('Responsável');
        wrapper.unmount();
    });

    it('exportarMapaVigentePdf lida com erro', async () => {
        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'CHEFE',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        mockObterReferenciaMapaVigente.mockResolvedValueOnce(mockMapaVigente);
        downloadRelatorioMapaVigenteUnidadePdf.mockRejectedValueOnce(new Error('Erro PDF'));

        const vm = wrapper.vm as any;
        vm.mapaVigente = mockMapaVigente;

        await vm.exportarMapaVigentePdf();
        expect(downloadRelatorioMapaVigenteUnidadePdf).toHaveBeenCalledWith(1);
        expect(vm.loadingExportacaoPdf).toBe(false);
    });

    it('calcula corretamente as propriedades computadas de responsabilidade e titular', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Caso Substituição com data
        vm.unidade = {
            ...mockUnidadeData,
            tipoResponsabilidade: 'SUBSTITUTO',
            dataFimResponsabilidade: '2026-12-31T23:59:59'
        };
        expect(vm.descricaoResponsabilidade).toContain('Substituição');
        expect(vm.descricaoResponsabilidade).toContain('31/12/2026');

        // Caso Atribuição Temporária sem data
        vm.unidade = {
            ...mockUnidadeData,
            tipoResponsabilidade: 'ATRIBUICAO_TEMPORARIA',
            dataFimResponsabilidade: null
        };
        expect(vm.descricaoResponsabilidade).toBe('Atrib. temporária');

        // Caso Responsável não é Titular
        vm.unidade = {
            ...mockUnidadeData,
            titular: {tituloEleitoral: '123'},
            responsavel: {tituloEleitoral: '456'}
        };
        expect(vm.responsavelEhTitular).toBe(false);
        expect(vm.titularExibivel).toBe(true);
        expect(vm.labelContatoPrincipal).toContain('Responsável');

        // Caso Responsável É Titular
        vm.unidade = {
            ...mockUnidadeData,
            titular: {tituloEleitoral: '123'},
            responsavel: {tituloEleitoral: '123'}
        };
        expect(vm.responsavelEhTitular).toBe(true);
        expect(vm.titularExibivel).toBe(false);
        expect(vm.labelContatoPrincipal).toContain('Titular');
    });

    it('exportarMapaVigenteCsv lida com erro', async () => {
        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'CHEFE',
                permissoesSessao: {mostrarCriarAtribuicaoTemporaria: false},
            },
        });
        await flushPromises();

        downloadRelatorioMapaVigenteUnidadeCsv.mockRejectedValueOnce(new Error('Erro CSV'));

        const vm = wrapper.vm as any;
        vm.mapaVigente = mockMapaVigente;

        await vm.exportarMapaVigenteCsv();
        expect(downloadRelatorioMapaVigenteUnidadeCsv).toHaveBeenCalledWith(1);
        expect(vm.loadingExportacaoCsv).toBe(false);
    });

    it('carregarDados evita re-entrada', async () => {
        const {wrapper} = createWrapper();
        const vm = wrapper.vm as any;
        
        // Account for initial call during mount
        await flushPromises();
        mockObterUnidade.mockClear();
        
        let resolve1!: (val: any) => void;
        mockObterUnidade.mockReturnValueOnce(new Promise(r => resolve1 = r));
        
        const p1 = vm.carregarDados(true); // forcar=true to bypass some checks
        const p2 = vm.carregarDados(true);
        
        resolve1(mockUnidadeData);
        await p1;
        await p2;
        
        // Should only have been called once despite two calls to vm.carregarDados()
        expect(mockObterUnidade).toHaveBeenCalledTimes(1); 
    });

    it('possuiDadosLocaisValidos retorna falso quando dados divergem do cache', () => {
        const {wrapper} = createWrapper();
        const vm = wrapper.vm as any;
        
        vm.unidade = { codigo: 1 };
        mockUnidadeStore.cacheUnidades.set(1, { codigo: 1 });
        mockUnidadeStore.cacheMapasVigentes.set(1, { codigo: 100 });
        
        // Case: unidade.value !== unidadeEmCache
        mockUnidadeStore.cacheUnidades.set(1, { codigo: 1, diff: true });
        expect(vm.possuiDadosLocaisValidos()).toBe(false);

        // Case: mapaVigente.value !== mapaVigenteEmCache
        vm.unidade = mockUnidadeStore.cacheUnidades.get(1);
        vm.mapaVigente = { codigo: 100 };
        mockUnidadeStore.cacheMapasVigentes.set(1, { codigo: 100, diff: true });
        expect(vm.possuiDadosLocaisValidos()).toBe(false);
    });

    it('reaplicarDadosDoCache retorna falso quando cache incompleto', () => {
        const {wrapper} = createWrapper();
        const vm = wrapper.vm as any;
        
        mockUnidadeStore.cacheUnidades.delete(1);
        expect(vm.reaplicarDadosDoCache()).toBe(false);
        
        mockUnidadeStore.cacheUnidades.set(1, {});
        mockUnidadeStore.cacheMapasVigentes.delete(1);
        expect(vm.reaplicarDadosDoCache()).toBe(false);
    });

    it('deveExibirCarregamento depende de forcar e cache', () => {
        const {wrapper} = createWrapper();
        const vm = wrapper.vm as any;
        
        mockUnidadeStore.cacheUnidades.set(1, {});
        expect(vm.deveExibirCarregamento(false)).toBe(false);
        expect(vm.deveExibirCarregamento(true)).toBe(true);
        
        mockUnidadeStore.cacheUnidades.delete(1);
        expect(vm.deveExibirCarregamento(false)).toBe(true);
    });

    it('carregarDados lida com erro e define ultimoErro', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        mockObterUnidade.mockRejectedValueOnce({ response: { data: { message: 'Erro Customizado' } } });
        await vm.carregarDados(true);
        
        expect(vm.ultimoErro).toBe('Erro Customizado');
    });

    it('podeExportarMapaVigente computed property branches', async () => {
        const {wrapper} = createWrapper({
            perfil: {
                perfilSelecionado: 'USER',
            },
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        // CASE 1: USER profile, has mapaVigente -> false
        vm.mapaVigente = { id: 1 };
        expect(vm.podeExportarMapaVigente).toBe(false);

        // CASE 2: CHEFE profile, NO mapaVigente -> false
        vm.perfilStore.perfilSelecionado = 'CHEFE';
        vm.mapaVigente = null;
        expect(vm.podeExportarMapaVigente).toBe(false);

        // CASE 3: CHEFE profile, has mapaVigente -> true
        vm.mapaVigente = { id: 1 };
        expect(vm.podeExportarMapaVigente).toBe(true);
    });

    it('onActivated branches', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        
        const hook = ((wrapper.vm.$ as any).a)?.[0];
        
        // CASE: !carregamentoInicialConcluido -> returns early
        vm.carregamentoInicialConcluido = false;
        mockObterUnidade.mockClear();
        await hook.call(vm);
        expect(mockObterUnidade).not.toHaveBeenCalled();

        // CASE: possuiDadosLocaisValidos -> returns early
        vm.carregamentoInicialConcluido = true;
        // Mock data to satisfy possuiDadosLocaisValidos
        vm.unidade = { codigo: 1 };
        vm.mapaVigente = { id: 100 };
        mockUnidadeStore.cacheUnidades.set(1, vm.unidade);
        mockUnidadeStore.cacheMapasVigentes.set(1, vm.mapaVigente);
        vm.ultimoErro = null;
        
        mockObterUnidade.mockClear();
        await hook.call(vm);
        expect(mockObterUnidade).not.toHaveBeenCalled();

        // CASE: reaplicarDadosDoCache -> returns early
        // Make possuiDadosLocaisValidos false (by changing unit)
        vm.unidade = { codigo: 99 }; 
        // But make reaplicarDadosDoCache true (satisfy its condition)
        mockUnidadeStore.cacheUnidades.set(1, { codigo: 1 });
        mockUnidadeStore.cacheMapasVigentes.set(1, { id: 100 });
        
        mockObterUnidade.mockClear();
        await hook.call(vm);
        expect(mockObterUnidade).not.toHaveBeenCalled();

        // CASE: none of above -> calls carregarDados
        mockUnidadeStore.cacheUnidades.clear();
        mockObterUnidade.mockClear();
        await hook.call(vm);
        expect(mockObterUnidade).toHaveBeenCalled();
    });
});

import {describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import UnidadeView from '@/views/UnidadeView.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {BAlert} from 'bootstrap-vue-next';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

const {
    mockPush,
    mockUnidadeData,
    mockMapaVigente,
    mockObterUnidade,
    mockObterReferenciaMapaVigente
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
            filhas: [
                {codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: []},
                {codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: []}
            ]
        },
        mockMapaVigente: {codProcesso: 99, codSubprocesso: 77},
        mockObterUnidade: vi.fn(),
        mockObterReferenciaMapaVigente: vi.fn()
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
        useUnidadeStore: () => ({
            cacheUnidades: new Map<number, unknown>(),
            cacheMapasVigentes: new Map<number, unknown>(),
            obterUnidade: mockObterUnidade,
            obterReferenciaMapaVigente: mockObterReferenciaMapaVigente,
        }),
    };
});

const TreeTableStub = {
    template: '<div data-testid="tree-table"></div>',
    props: ['data', 'columns', 'title'],
    emits: ['row-click']
};

describe('UnidadeView.vue', () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
        mockObterUnidade.mockResolvedValue(mockUnidadeData);
        mockObterReferenciaMapaVigente.mockResolvedValue(null);
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
    });

    it('não exibe responsável quando for o titular', async () => {
        const unidadeTitular = {
            ...mockUnidadeData,
            tipoResponsabilidade: 'TITULAR'
        };
        mockObterUnidade.mockResolvedValueOnce(unidadeTitular);

        const {wrapper} = createWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain('Titular teste');
        expect(wrapper.find('[data-testid="unidade-responsavel-info"]').exists()).toBe(false);
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

    it('renders and clicks "Mapa vigente" button when map exists', async () => {
        mockObterReferenciaMapaVigente.mockResolvedValueOnce(mockMapaVigente);
        const {wrapper} = createWrapper();
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-mapa-vigente"]');
        expect(btn.exists()).toBe(true);

        await btn.trigger('click');
        expect(mockPush).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {codProcesso: 99, siglaUnidade: 'TEST'}
        });
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
});

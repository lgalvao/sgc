import {describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import UnidadeView from '@/views/UnidadeView.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {BAlert} from 'bootstrap-vue-next';
import {usePerfilStore} from '@/stores/perfil';
import {useMapasStore} from '@/stores/mapas';
import {buscarUsuarioPorTitulo} from '@/services/usuarioService';
import {buscarArvoreUnidade} from '@/services/unidadeService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {logger} from "@/utils";

const {mockPush, mockUnidadeData, mockUsuario, mockUsuarioResponsavel} = vi.hoisted(() => {
    const u = {
        codigo: 10,
        nome: 'Titular teste',
        unidade: {codigo: 1, sigla: 'TEST'}
    };
    const ur = {
        codigo: 20,
        nome: 'Responsavel teste',
        unidade: {codigo: 1, sigla: 'TEST'}
    };
    return {
        mockPush: vi.fn(),
        mockUnidadeData: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'UnidadeView Teste',
            usuarioCodigo: 10,
            tituloTitular: '123456',
            responsavel: ur,
            filhas: [
                {codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: []},
                {codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: []}
            ]
        },
        mockUsuario: u,
        mockUsuarioResponsavel: ur
    };
});

vi.mock("@/utils", () => ({
    logger: {
        error: vi.fn(),
    }
}));

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
        buscarUsuarioPorTitulo: vi.fn().mockResolvedValue(mockUsuario),
    }
});

vi.mock('@/services/unidadeService', () => ({
    buscarArvoreUnidade: vi.fn().mockResolvedValue(mockUnidadeData),
    buscarUnidadePorSigla: vi.fn().mockResolvedValue(mockUnidadeData),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    buscarTodasAtribuicoes: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/services/mapaService', () => ({
    obterMapaCompleto: vi.fn().mockResolvedValue(null),
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
        (buscarArvoreUnidade as any).mockResolvedValue(mockUnidadeData);
    });

    let perfilStore: any;
    let mapasStore: any;

    const mockUnidade = mockUnidadeData;

    const createWrapper = (initialStateOverride = {}) => {
        context.wrapper = mount(UnidadeView, {
            ...getCommonMountOptions(
                {
                    perfil: {
                        perfilSelecionado: 'USER',
                    },
                    mapas: {
                        mapaCompleto: null,
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

        perfilStore = usePerfilStore();
        mapasStore = useMapasStore();

        return {wrapper: context.wrapper, perfilStore, mapasStore};
    };

    it('fetches data on mount', async () => {
        createWrapper();
        expect(buscarArvoreUnidade).toHaveBeenCalledWith(1);
    });

    it('renders unit details correctly', async () => {
        const {wrapper} = createWrapper();

        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(wrapper.text()).toContain('TEST - UnidadeView Teste');
        expect(wrapper.text()).toContain('Titular: Titular teste');
    });

    it('renders "Criar atribuição" button only for ADMIN', async () => {
        const {wrapper, perfilStore} = createWrapper();
        await flushPromises();
        perfilStore.perfilSelecionado = 'ADMIN';
        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(true);

        perfilStore.perfilSelecionado = 'USER';
        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(false);
    });

    it('navigates to create assignment', async () => {
        const {wrapper, perfilStore} = createWrapper();
        await flushPromises();
        perfilStore.perfilSelecionado = 'ADMIN';
        await wrapper.vm.$nextTick();

        await wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').trigger('click');
        expect(mockPush).toHaveBeenCalledWith({path: '/unidade/1/atribuicao'});
    });

    it('renders responsible person correctly from API payload', async () => {
        const unidadeComResponsavel = {
            ...mockUnidade,
            responsavel: mockUsuarioResponsavel
        };
        (buscarArvoreUnidade as any).mockResolvedValueOnce(unidadeComResponsavel);

        const {wrapper} = createWrapper();

        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(wrapper.text()).toContain('Responsável: Responsavel teste');
    });

    it('renders subordinate units tree table', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();
        await wrapper.vm.$nextTick();

        const treeTable = wrapper.findComponent(TreeTableStub);
        expect(treeTable.exists()).toBe(true);
    });

    it('navigates to subordinate unit on row click', async () => {
        const {wrapper} = createWrapper();
        await flushPromises();
        await wrapper.vm.$nextTick();

        const treeTable = wrapper.findComponent(TreeTableStub);
        treeTable.vm.$emit('row-click', {codigo: 2});

        expect(mockPush).toHaveBeenCalledWith({path: '/unidade/2'});
    });

    it('renders and clicks "Mapa vigente" button when map exists', async () => {
        const {wrapper, mapasStore} = createWrapper();
        await flushPromises();
        mapasStore.mapaCompleto = {subprocessoCodigo: 99};
        await wrapper.vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-mapa-vigente"]');
        expect(btn.exists()).toBe(true);

        await btn.trigger('click');
        expect(mockPush).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: {codProcesso: 99, siglaUnidade: 'TEST'}
        });
    });

    it('displays error alert when fetching unit fails', async () => {
        (buscarArvoreUnidade as any).mockRejectedValueOnce(new Error('Erro ao carregar unidade'));
        const {wrapper} = createWrapper();
        await flushPromises();

        const alert = wrapper.findComponent(BAlert);
        expect(alert.exists()).toBe(true);
        expect(wrapper.text()).toContain('Erro ao carregar unidade');
    });

    it('logs error when fetching titular fails', async () => {
        (buscarUsuarioPorTitulo as any).mockRejectedValue(new Error('Fetch error'));

        createWrapper();
        await flushPromises();

        expect(logger.error).toHaveBeenCalledWith('Erro ao carregar dados da unidade:', expect.any(Error));
    });

    it('renders clickable contact links for titular', async () => {
        const mockUserWithContact = {
            ...mockUsuario,
            ramal: '1234',
            email: 'test@example.com'
        };
        (buscarUsuarioPorTitulo as any).mockResolvedValue(mockUserWithContact);

        const {wrapper} = createWrapper();

        await wrapper.vm.$nextTick();
        await flushPromises();

        const ramalLink = wrapper.find('a[href="tel:1234"]');
        expect(ramalLink.exists()).toBe(true);
        expect(ramalLink.text()).toBe('1234');
        expect(ramalLink.attributes('aria-label')).toBe('Ligar para 1234');

        const emailLink = wrapper.find('a[href="mailto:test@example.com"]');
        expect(emailLink.exists()).toBe(true);
        expect(emailLink.text()).toBe('test@example.com');
        expect(emailLink.attributes('aria-label')).toBe('Enviar e-mail para test@example.com');
    });

    it('handles unit with no children safely', async () => {
        const mockUnidadeSemFilhas = {
            ...mockUnidade,
            filhas: []
        };
        (buscarArvoreUnidade as any).mockResolvedValue(mockUnidadeSemFilhas);

        const {wrapper} = createWrapper();
        await wrapper.vm.$nextTick();
        await flushPromises();

        expect((wrapper.vm as any).dadosFormatadosSubordinadas).toEqual([]);

        const treeTable = wrapper.findComponent(TreeTableStub);
        expect(treeTable.exists()).toBe(false);
    });

    it('handles null unit gracefully', async () => {
        (buscarArvoreUnidade as any).mockResolvedValue(null);
        const {wrapper} = createWrapper();
        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(wrapper.findComponent(EmptyState).exists()).toBe(true);
    });
});

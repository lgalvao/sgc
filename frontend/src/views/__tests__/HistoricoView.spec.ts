import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import HistoricoView from '../HistoricoView.vue';
import {useRouter} from 'vue-router';
import * as processoService from '@/services/processo';
import * as configuracaoService from '@/services/configuracaoService';

vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        push: vi.fn()
    }))
}));

vi.mock('@/services/processo', () => ({
    buscarProcessosFinalizados: vi.fn(),
}));

vi.mock('@/services/configuracaoService', () => ({
    buscarConfiguracoes: vi.fn(),
    salvarConfiguracoes: vi.fn(),
}));

const LayoutPadraoStub = {
    template: '<div><slot /></div>'
};

const PageHeaderStub = {
    template: '<div data-testid="page-header">{{ title }}</div>',
    props: ['title']
};

const TabelaProcessosStub = {
    template: '<div data-testid="tabela-processos" @click="$emit(\'selecionar-processo\', { codigo: 1, linkDestino: \'/test/1\' })" @dblclick="$emit(\'ordenar\', \'descricao\')"></div>',
    props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc', 'compacto', 'showDataFinalizacao', 'showSituacao']
};

const mockProcessosFinalizados = [
    {codigo: 1, dataFinalizacao: '2024-01-02', descricao: 'Proc B'},
    {codigo: 2, dataFinalizacao: '2024-01-01', descricao: 'Proc A'},
    {codigo: 3, dataFinalizacao: null, descricao: 'Proc C'}
];

describe('HistoricoView.vue', () => {
    let wrapper: any;
    let mockRouter: any;

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(processoService.buscarProcessosFinalizados).mockResolvedValue([...mockProcessosFinalizados] as any);
        vi.mocked(configuracaoService.buscarConfiguracoes).mockResolvedValue([]);
        mockRouter = {push: vi.fn()};
        (useRouter as any).mockReturnValue(mockRouter);
    });

    const createWrapper = (initialState?: Record<string, unknown>) => {
        return mount(HistoricoView, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState,
                        stubActions: false,
                    })
                ],
                stubs: {
                    LayoutPadrao: LayoutPadraoStub,
                    PageHeader: PageHeaderStub,
                    TabelaProcessos: TabelaProcessosStub,
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina"></div>'}
                }
            }
        });
    };

    it('deve carregar historico onMounted', async () => {
        wrapper = createWrapper();

        // initially loading
        // then loaded
        await flushPromises();

        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalled();
    });

    it('nao deve carregar configuracoes sem permissao', async () => {
        wrapper = createWrapper();

        await flushPromises();

        expect(configuracaoService.buscarConfiguracoes).not.toHaveBeenCalled();
    });

    it('deve carregar configuracoes quando a permissao existir', async () => {
        wrapper = createWrapper({
            perfil: {
                permissoesSessao: {
                    mostrarMenuConfiguracoes: true
                }
            }
        });

        await flushPromises();

        expect(configuracaoService.buscarConfiguracoes).toHaveBeenCalledTimes(1);
    });

    it('deve lidar com erro ao carregar historico', async () => {
        vi.mocked(processoService.buscarProcessosFinalizados).mockRejectedValueOnce(new Error('error'));
        wrapper = createWrapper();

        await flushPromises();
        // erro propaga ao error handler do Vue, componente permanece funcional com lista vazia
        expect(wrapper.find('[data-testid="tabela-processos"]').exists()).toBe(true);
    });

    it('deve ordenar processos corretamente pela data', async () => {
        wrapper = createWrapper();
        await flushPromises();

        const processos = wrapper.vm.processosOrdenados;
        // default desc by dataFinalizacao
        expect(processos[0].codigo).toBe(1); // 2024-01-02
        expect(processos[1].codigo).toBe(2); // 2024-01-01
        expect(processos[2].codigo).toBe(3); // null
    });

    it('deve configurar a tabela sem situação no histórico', async () => {
        wrapper = createWrapper();
        await flushPromises();

        const tabela = wrapper.findComponent(TabelaProcessosStub);
        expect(tabela.props('showSituacao')).toBe(false);
        expect(tabela.props('showDataFinalizacao')).toBe(true);
    });

    it('deve ordenar alterando o criterio e direcao', async () => {
        wrapper = createWrapper();
        await flushPromises();

        wrapper.vm.ordenarPor('descricao');
        expect(wrapper.vm.criterio).toBe('descricao');
        expect(wrapper.vm.asc).toBe(true); // first click sets to asc

        let processos = wrapper.vm.processosOrdenados;
        expect(processos[0].descricao).toBe('Proc A');

        wrapper.vm.ordenarPor('descricao');
        expect(wrapper.vm.asc).toBe(false); // second click toggles asc

        processos = wrapper.vm.processosOrdenados;
        expect(processos[0].descricao).toBe('Proc C');
    });

    it('deve navegar para ver detalhes', async () => {
        wrapper = createWrapper();
        await flushPromises();

        wrapper.vm.verDetalhes({codigo: 1, linkDestino: '/custom/link'});
        expect(mockRouter.push).toHaveBeenCalledWith('/custom/link');

        wrapper.vm.verDetalhes({codigo: 2}); // without linkDestino
        expect(mockRouter.push).toHaveBeenCalledWith('/processo/2');

        wrapper.vm.verDetalhes(undefined);
        // should not throw error or call push
        expect(mockRouter.push).toHaveBeenCalledTimes(2);
    });
});

import {beforeEach, describe, expect, it, vi} from 'vitest';
import {usePerfilStore} from '@/stores/perfil';
import type {RouteLocationNormalized, Router} from 'vue-router';
import {createMemoryHistory, createRouter} from 'vue-router';
import mainRoutes from '../main.routes';
import processoRoutes from '../processo.routes';
import unidadeRoutes from '../unidade.routes';

vi.mock('@/views/ProcessoCadastroView.vue', () => ({default: {name: 'ProcessoCadastroView'}}));
vi.mock('@/views/ProcessoDetalheView.vue', () => ({default: {name: 'ProcessoDetalheView'}}));
vi.mock('@/views/SubprocessoView.vue', () => ({default: {name: 'SubprocessoView'}}));
vi.mock('@/views/MapaView.vue', () => ({default: {name: 'MapaView'}}));
vi.mock('@/views/CadastroView.vue', () => ({default: {name: 'CadastroView'}}));
vi.mock('@/views/LoginView.vue', () => ({default: {name: 'LoginView'}}));
vi.mock('@/views/PainelView.vue', () => ({default: {name: 'PainelView'}}));
vi.mock('@/views/HistoricoView.vue', () => ({default: {name: 'HistoricoView'}}));
vi.mock('@/views/RelatoriosView.vue', () => ({default: {name: 'RelatoriosView'}}));
vi.mock('@/views/ConfiguracaoView.vue', () => ({default: {name: 'ConfiguracaoView'}}));
vi.mock('@/views/AdministradoresView.vue', () => ({default: {name: 'AdministradoresView'}}));
vi.mock('@/views/UnidadesView.vue', () => ({default: {name: 'UnidadesView'}}));
vi.mock('@/views/UnidadeView.vue', () => ({default: {name: 'UnidadeView'}}));
vi.mock('@/views/AtribuicaoTemporariaView.vue', () => ({default: {name: 'AtribuicaoTemporariaView'}}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: vi.fn(),
}));

type RotaComProps = {
    params: Record<string, string>;
    query?: Record<string, string>;
};

const rotas = [...mainRoutes, ...processoRoutes, ...unidadeRoutes];

describe('Router', () => {
    let router: Router;
    let perfilStoreMock: {
        usuarioCodigo: number | null;
        perfisUnidades: never[];
    };

    beforeEach(() => {
        vi.clearAllMocks();
        perfilStoreMock = {usuarioCodigo: null, perfisUnidades: []};
        vi.mocked(usePerfilStore).mockReturnValue(perfilStoreMock as never);

        router = createRouter({
            history: createMemoryHistory(),
            routes: rotas,
        });

        router.beforeEach((to: RouteLocationNormalized) => {
            const perfilStore = usePerfilStore();
            const autenticado = Boolean(perfilStore.usuarioCodigo);
            const paginaPublica = ['/login'].includes(to.path);

            if (!paginaPublica && !autenticado) {
                return '/login';
            }

            return true;
        });

        router.afterEach((to: RouteLocationNormalized) => {
            const tituloBase = typeof to.meta.title === 'string' ? to.meta.title : (to.name as string) || 'SGC';
            document.title = `${tituloBase} - SGC`;
        });
    });

    it('redireciona para login quando usuário não autenticado acessa rota privada', async () => {
        await router.push('/painel');
        expect(router.currentRoute.value.path).toBe('/login');
    });

    it('permite acesso à rota privada quando usuário autenticado', async () => {
        perfilStoreMock.usuarioCodigo = 123;
        await router.push('/painel');
        expect(router.currentRoute.value.path).toBe('/painel');
    });

    it('atualiza document.title com meta.title e com fallback para nome da rota', async () => {
        perfilStoreMock.usuarioCodigo = 123;

        await router.push('/processo/cadastro');
        expect(document.title).toBe('Novo processo - SGC');

        router.addRoute({
            path: '/teste-sem-titulo',
            name: 'TesteSemTitulo',
            component: {template: '<div>Teste</div>'},
        });

        await router.push('/teste-sem-titulo');
        expect(document.title).toBe('TesteSemTitulo - SGC');
    });

    it('transforma params e query em props para rotas de processo e unidade', () => {
        const subprocesso = processoRoutes.find((rota) => rota.name === 'Subprocesso');
        expect(subprocesso).toBeDefined();
        const propsSubprocesso = subprocesso?.props as (rota: RotaComProps) => unknown;
        expect(propsSubprocesso({
            params: {codProcesso: '10', siglaUnidade: 'TIC'},
            query: {codSubprocesso: '55'}
        })).toEqual({
            codProcesso: 10,
            siglaUnidade: 'TIC',
            codSubprocesso: 55,
        });

        const mapa = processoRoutes.find((rota) => rota.name === 'SubprocessoMapa');
        expect(mapa).toBeDefined();
        const propsMapa = mapa?.props as (rota: RotaComProps) => unknown;
        expect(propsMapa({params: {codProcesso: '11', siglaUnidade: 'DIP'}, query: {codSubprocesso: '66'}})).toEqual({
            codProcesso: 11,
            sigla: 'DIP',
            codSubprocesso: 66,
        });

        const cadastro = processoRoutes.find((rota) => rota.name === 'SubprocessoCadastro');
        expect(cadastro).toBeDefined();
        const propsCadastro = cadastro?.props as (rota: RotaComProps) => unknown;
        expect(propsCadastro({
            params: {codProcesso: '13', siglaUnidade: 'XYZ'},
            query: {codSubprocesso: '77'}
        })).toEqual({
            codProcesso: 13,
            sigla: 'XYZ',
            codSubprocesso: 77,
        });

        const mapaUnidade = unidadeRoutes.find((rota) => rota.name === 'Mapa');
        expect(mapaUnidade).toBeDefined();
        const propsMapaUnidade = mapaUnidade?.props as (rota: RotaComProps) => unknown;
        expect(propsMapaUnidade({params: {codUnidade: '10'}, query: {codProcesso: '99'}})).toEqual({
            codUnidade: 10,
            codProcesso: 99
        });

        const atribuicao = unidadeRoutes.find((rota) => rota.name === 'AtribuicaoTemporariaForm');
        expect(atribuicao).toBeDefined();
        const propsAtribuicao = atribuicao?.props as (rota: RotaComProps) => unknown;
        expect(propsAtribuicao({params: {codUnidade: '20'}})).toEqual({codUnidade: 20});
    });
});

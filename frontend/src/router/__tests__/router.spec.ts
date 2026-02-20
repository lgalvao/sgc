import {beforeEach, describe, expect, it, vi} from 'vitest';
import {usePerfilStore} from '@/stores/perfil';
import {createMemoryHistory, createRouter} from 'vue-router';
import mainRoutes from '../main.routes';
import processoRoutes from '../processo.routes';
import unidadeRoutes from '../unidade.routes';

vi.mock('@/stores/perfil', () => ({
  usePerfilStore: vi.fn(),
}));

const routes = [
    ...mainRoutes,
    ...processoRoutes,
    ...unidadeRoutes,
    { path: '/login', component: { template: '<div>Login</div>' } }, // Mock login component
    { path: '/painel', component: { template: '<div>Painel</div>' } } // Mock painel component if needed
];

describe('Router Guards', () => {
  let router: any;
  let perfilStoreMock: any;

  beforeEach(() => {
    vi.clearAllMocks();

    // Setup Store Mock
    perfilStoreMock = {
      usuarioCodigo: null,
    };
    (usePerfilStore as any).mockReturnValue(perfilStoreMock);

    // Create fresh router
    router = createRouter({
        history: createMemoryHistory(),
        routes: routes,
    });

    // Copying logic from router/index.ts for testing
    router.beforeEach((to: any) => {
        const perfilStore = usePerfilStore();
        const isAuthenticated = perfilStore.usuarioCodigo;
        const publicPages = ["/login"];
        const authRequired = !publicPages.includes(to.path);

        if (authRequired && !isAuthenticated) {
            return "/login";
        }

        return true;
    });

    router.afterEach((to: any) => {
        const meta = to.meta || {};
        const titleBase = typeof meta.title === "string" ? meta.title : (to.name as string) || "SGC";
        document.title = `${titleBase} - SGC`;
    });
  });

  it('redirects to login if not authenticated and trying to access private route', async () => {
    perfilStoreMock.usuarioCodigo = null;
    await router.push('/painel');
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('allows access to login if not authenticated', async () => {
    perfilStoreMock.usuarioCodigo = null;
    await router.push('/login');
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('allows access to private route if authenticated', async () => {
    perfilStoreMock.usuarioCodigo = 123;
    await router.push('/painel');
    expect(router.currentRoute.value.path).toBe('/painel');
  });

   it('updates document title', async () => {
    perfilStoreMock.usuarioCodigo = 123;
    await router.push('/processo/cadastro');
    expect(document.title.toLowerCase()).toContain('novo processo - sgc');
  });
});

describe('Route Props Logic', () => {
  it('Processo routes props transformation', () => {
    const subprocessoRoute = processoRoutes.find(r => r.name === 'Subprocesso');
    const propsFn = subprocessoRoute?.props as (...args: any[]) => any;
    expect(propsFn({ params: { codProcesso: '10', siglaUnidade: 'TIC' } }))
      .toEqual({ codProcesso: 10, siglaUnidade: 'TIC' });

    const mapaRoute = processoRoutes.find(r => r.name === 'SubprocessoMapa');
    const mapaProps = (mapaRoute?.props as (...args: any[]) => any)({ params: { codProcesso: '11', siglaUnidade: 'DIP' } });
    expect(mapaProps).toEqual({ codProcesso: 11, sigla: 'DIP' });

    const visMapaRoute = processoRoutes.find(r => r.name === 'SubprocessoVisMapa');
    const visMapaProps = (visMapaRoute?.props as (...args: any[]) => any)({ params: { codProcesso: '12', siglaUnidade: 'ABC' } });
    expect(visMapaProps).toEqual({ codProcesso: 12, sigla: 'ABC' });

    const cadastroRoute = processoRoutes.find(r => r.name === 'SubprocessoCadastro');
    const cadastroProps = (cadastroRoute?.props as (...args: any[]) => any)({ params: { codProcesso: '13', siglaUnidade: 'XYZ' } });
    expect(cadastroProps).toEqual({ codProcesso: 13, sigla: 'XYZ' });

    const visCadastroRoute = processoRoutes.find(r => r.name === 'SubprocessoVisCadastro');
    const visCadastroProps = (visCadastroRoute?.props as (...args: any[]) => any)({ params: { codProcesso: '14', siglaUnidade: 'TEST' } });
    expect(visCadastroProps).toEqual({ codProcesso: 14, sigla: 'TEST' });
  });

  it('Unidade routes props transformation', () => {
    const mapaRoute = unidadeRoutes.find(r => r.name === 'Mapa');
    const propsFn = mapaRoute?.props as (...args: any[]) => any;
    // Mapa route uses query param for codProcesso and params for codUnidade
    expect(propsFn({ params: { codUnidade: '10' }, query: { codProcesso: '99' } }))
      .toEqual({ codUnidade: 10, codProcesso: 99 });

    const atribuicaoRoute = unidadeRoutes.find(r => r.name === 'AtribuicaoTemporariaForm');
    const atribProps = (atribuicaoRoute?.props as (...args: any[]) => any)({ params: { codUnidade: '20' } });
    expect(atribProps).toEqual({ codUnidade: 20 });
  });
});

import {beforeEach, describe, expect, it, vi} from 'vitest';
import {usePerfilStore} from '@/stores/perfil';
import {createMemoryHistory, createRouter} from 'vue-router';
import mainRoutes from '../main.routes';
import processoRoutes from '../processo.routes';
import unidadeRoutes from '../unidade.routes';
import diagnosticoRoutes from '../diagnostico.routes';

vi.mock('@/stores/perfil', () => ({
  usePerfilStore: vi.fn(),
}));

const routes = [
    ...mainRoutes,
    ...processoRoutes,
    ...unidadeRoutes,
    ...diagnosticoRoutes,
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
      servidorId: null,
    };
    (usePerfilStore as any).mockReturnValue(perfilStoreMock);

    // Create fresh router
    router = createRouter({
        history: createMemoryHistory(),
        routes: routes,
    });

    // Copying logic from router/index.ts for testing
    router.beforeEach((to: any, from: any, next: any) => {
        const perfilStore = usePerfilStore();
        const isAuthenticated = perfilStore.servidorId;
        const publicPages = ["/login"];
        const authRequired = !publicPages.includes(to.path);

        if (authRequired && !isAuthenticated) {
            return next("/login");
        }

        next();
    });

    router.afterEach((to: any) => {
        const meta = to.meta || {};
        const titleBase = typeof meta.title === "string" ? meta.title : (to.name as string) || "SGC";
        document.title = `${titleBase} - SGC`;
    });
  });

  it('redirects to login if not authenticated and trying to access private route', async () => {
    perfilStoreMock.servidorId = null;
    await router.push('/painel');
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('allows access to login if not authenticated', async () => {
    perfilStoreMock.servidorId = null;
    await router.push('/login');
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('allows access to private route if authenticated', async () => {
    perfilStoreMock.servidorId = 123;
    await router.push('/painel');
    expect(router.currentRoute.value.path).toBe('/painel');
  });

   it('updates document title', async () => {
    perfilStoreMock.servidorId = 123;
    await router.push('/processo/cadastro');
    expect(document.title.toLowerCase()).toContain('novo processo - sgc');
  });
});

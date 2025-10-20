import {mount, RouterLinkStub} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {type Pinia} from 'pinia';
import {initPinia} from '@/test-utils/helpers';
import {navigateAndAssertBreadcrumbs} from '@/test-utils/uiHelpers';
import {
    createRouter,
    createWebHistory,
    type RouteLocationNormalized,
    type Router,
    type RouteRecordRaw,
} from 'vue-router';
import BarraNavegacao from '../BarraNavegacao.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {Perfil, type Unidade} from '@/types/tipos';
import {usePerfilStore} from '@/stores/perfil';

const routes: RouteRecordRaw[] = [
  { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
  { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
  {
    path: '/alguma-pagina',
    name: 'AlgumaPagina',
    component: { template: '<div>Alguma Página</div>' },
    meta: { breadcrumb: 'Alguma Página' },
  },
  {
    path: '/processo/:idProcesso',
    name: 'Processo',
    component: { template: '<div>Processo</div>' },
    meta: { breadcrumb: 'Processo' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade',
    name: 'Subprocesso',
    component: { template: '<div>Subprocesso</div>' },
    meta: { breadcrumb: (route: RouteLocationNormalized) => route.params.siglaUnidade as string },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/mapa',
    name: 'SubprocessoMapa',
    component: { template: '<div>Mapa</div>' },
    meta: { breadcrumb: 'Mapa' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/vis-mapa',
    name: 'SubprocessoVisMapa',
    component: { template: '<div>Visualização de Mapa</div>' },
    meta: { breadcrumb: 'Visualização de Mapa' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/cadastro',
    name: 'SubprocessoCadastro',
    component: { template: '<div>Cadastro</div>' },
    meta: { breadcrumb: 'Cadastro' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/vis-cadastro',
    name: 'SubprocessoVisCadastro',
    component: { template: '<div>Visualização de Atividades</div>' },
    meta: { breadcrumb: 'Visualização de Atividades' },
  },
  {
    path: '/unidade/:siglaUnidade',
    name: 'Unidade',
    component: { template: '<div>Unidade</div>' },
    meta: { breadcrumb: (route: RouteLocationNormalized) => route.params.siglaUnidade as string },
  },
  {
    path: '/unidade/:siglaUnidade/atribuicao',
    name: 'AtribuicaoTemporariaForm',
    component: { template: '<div>Atribuição Temporária</div>' },
    meta: { breadcrumb: 'Atribuição' },
  },
  {
    path: '/unidade/:siglaUnidade/mapa',
    name: 'Mapa',
    component: { template: '<div>Mapa da Unidade</div>' },
    meta: { breadcrumb: 'Mapa' },
  },
  {
    path: '/relatorios',
    name: 'Relatorios',
    component: { template: '<div>Relatórios</div>' },
    meta: { breadcrumb: 'Relatórios' },
  },
  { path: '/custom', name: 'Custom', component: { template: '<div>Custom</div>' } },
];

describe('BarraNavegacao.vue', () => {
  let router: Router;
  let pinia: Pinia;

  beforeEach(() => {
    pinia = initPinia();

    router = createRouter({
      history: createWebHistory(),
      routes,
    });

    const unidadesStore = useUnidadesStore();
    vi.spyOn(unidadesStore, 'pesquisarUnidade').mockImplementation((sigla: string): Unidade | undefined => ({
      codigo: 1,
      sigla,
      nome: `Unidade ${sigla}`,
      tipo: 'Tipo',
      idServidorTitular: 1,
      responsavel: {
        codigo: 1,
        nome: 'Fulano',
        tituloEleitoral: '123',
        unidade: {} as Unidade,
        email: 'a@a.com',
        ramal: '123',
      },
      filhas: [],
    }));

    vi.spyOn(sessionStorage, 'getItem').mockReturnValue(null);
    vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {});
    vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {});
  });

  const mountComponent = async () => {
    const wrapper = mount(BarraNavegacao, {
      global: {
        plugins: [router, pinia],
        stubs: {
          RouterLink: RouterLinkStub,
        },
      },
    });
    await router.isReady();
    await wrapper.vm.$nextTick();
    return wrapper;
  };

  it('deve montar o componente corretamente', async () => {
    await router.push('/painel');
    const wrapper = await mountComponent();
    expect(wrapper.exists()).toBe(true);
  });

  describe('Botão Voltar', () => {
    it('não deve exibir o botão Voltar na página de login', async () => {
      await router.push('/login');
      const wrapper = await mountComponent();
      expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(false);
    });

    it('não deve exibir o botão Voltar na página de painel', async () => {
      await router.push('/painel');
      const wrapper = await mountComponent();
      expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(false);
    });

    it('deve exibir o botão Voltar em outras páginas', async () => {
      await router.push('/alguma-pagina');
      const wrapper = await mountComponent();
      expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(true);
    });

    it('deve chamar router.back() ao clicar no botão Voltar', async () => {
      await router.push('/alguma-pagina');
      const wrapper = await mountComponent();
      const routerBackSpy = vi.spyOn(router, 'back');
      await wrapper.find('button.btn-outline-secondary').trigger('click');
      expect(routerBackSpy).toHaveBeenCalled();
    });
  });

  describe('Breadcrumbs', () => {
    it('não deve exibir breadcrumbs na página de login', async () => {
      await router.push('/login');
      const wrapper = await mountComponent();
      expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(false);
    });

    it('não deve exibir breadcrumbs na página de painel', async () => {
      await router.push('/painel');
      const wrapper = await mountComponent();
      expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(false);
    });

    it('deve exibir breadcrumbs em outras páginas', async () => {
      await router.push('/alguma-pagina');
      const wrapper = await mountComponent();
      expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(true);
    });
  });

  describe('Lógica de Breadcrumbs (crumbs)', () => {
    let perfilStore: ReturnType<typeof usePerfilStore>;

    beforeEach(() => {
      perfilStore = usePerfilStore();
    });

    it('deve exibir o breadcrumb home e o da página atual para uma rota simples', async () => {
      await navigateAndAssertBreadcrumbs(router, mountComponent, '/alguma-pagina', ['Alguma Página']);
    });

    it('deve popular a trilha para uma rota de processo', async () => {
      await navigateAndAssertBreadcrumbs(router, mountComponent, '/processo/123', ['Processo']);
    });

    it('deve popular a trilha para uma rota de subprocesso para ADMIN', async () => {
      perfilStore.perfilSelecionado = Perfil.ADMIN;
      await navigateAndAssertBreadcrumbs(router, mountComponent, '/processo/123/ABC', ['Processo', 'ABC']);
    });

    it('deve popular a trilha para uma rota de subprocesso para CHEFE sem o crumb Processo', async () => {
      perfilStore.perfilSelecionado = Perfil.CHEFE;
      await router.push('/processo/123/ABC');
      const wrapper = await mountComponent();
      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(2);
      expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
      expect(breadcrumbItems[1].text()).toBe('ABC');
    });

    it('deve popular a trilha para uma rota de subprocesso com sub-página (mapa) para GESTOR', async () => {
      perfilStore.perfilSelecionado = Perfil.GESTOR;
      await navigateAndAssertBreadcrumbs(router, mountComponent, '/processo/123/ABC/mapa', ['Processo', 'ABC', 'Mapa']);
    });

    it('deve popular a trilha para uma rota de subprocesso com sub-página (mapa) para SERVIDOR sem o crumb Processo', async () => {
      perfilStore.perfilSelecionado = Perfil.SERVIDOR;
      await router.push('/processo/123/ABC/mapa');
      const wrapper = await mountComponent();
      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(3);
      expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
      expect(breadcrumbItems[1].text()).toBe('ABC');
      expect(breadcrumbItems[2].text()).toBe('Mapa');
    });

    it('deve popular a trilha para uma rota de unidade', async () => {
      await navigateAndAssertBreadcrumbs(router, mountComponent, '/unidade/XYZ', ['XYZ']);
    });

    it('deve popular a trilha para uma rota de unidade com sub-página (atribuicao)', async () => {
      await router.push('/unidade/XYZ/atribuicao');
      const wrapper = await mountComponent();
      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(3);
      expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
      expect(breadcrumbItems[1].text()).toBe('XYZ');
      const routerLink = wrapper.findComponent(RouterLinkStub);
      expect(routerLink.props().to).toEqual({ name: 'Unidade', params: { siglaUnidade: 'XYZ' } });
      expect(breadcrumbItems[2].text()).toBe('Atribuição');
      expect(breadcrumbItems[2].find('a').exists()).toBe(false); // Last crumb is not a link
    });

    it('deve popular a trilha para uma rota genérica com meta breadcrumb', async () => {
      await router.push('/relatorios');
      const wrapper = await mountComponent();
      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(2);
      expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
      expect(breadcrumbItems[1].text()).toBe('Relatórios');
      expect(breadcrumbItems[1].find('a').exists()).toBe(false); // Last crumb is not a link
    });

    it('não deve mostrar breadcrumbs quando a rota é painel', async () => {
      await router.push('/painel');
      const wrapper = await mountComponent();
      // Breadcrumbs should not be rendered for painel route
      expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(false);
    });

    it('deve lidar com função breadcrumb que retorna string', async () => {
      const customRoute = {
        path: '/custom',
        name: 'Custom',
        component: { template: '<div>Custom</div>' },
        meta: { breadcrumb: (_route: RouteLocationNormalized) => 'Custom Page' },
      };

      router.addRoute(customRoute);
      await router.push('/custom');
      const wrapper = await mountComponent();

      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(2);
      expect(breadcrumbItems[1].text()).toBe('Custom Page');

      router.removeRoute('Custom');
    });

    it('deve evitar duplicação de breadcrumbs quando currentPageLabel é igual ao último crumb', async () => {
      // Criar uma rota onde o breadcrumb da meta seria igual ao último crumb
      const duplicateRoute = {
        path: '/processo/123',
        name: 'Processo',
        component: { template: '<div>Processo</div>' },
        meta: { breadcrumb: 'Processo' }, // Isso seria duplicado com o crumb de processo
      };

      router.addRoute('Processo', duplicateRoute);
      await router.push('/processo/123');
      const wrapper = await mountComponent();

      // Verificar que não há duplicação
      const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
      expect(breadcrumbItems.length).toBe(2); // Home + Processo (não duplicado)

      router.removeRoute('Processo');
    });
  });
});
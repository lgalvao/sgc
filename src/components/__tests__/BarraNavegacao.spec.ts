import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router';
import BarraNavegacao from '../BarraNavegacao.vue';
import {useNavigationTrail} from '@/stores/navigationTrail';
import {useUnidadesStore} from '@/stores/unidades';

const routes: RouteRecordRaw[] = [
    {path: '/painel', name: 'Painel', component: {template: '<div>Painel</div>'}},
    {path: '/login', name: 'Login', component: {template: '<div>Login</div>'}},
    {
        path: '/alguma-pagina',
        name: 'AlgumaPagina',
        component: {template: '<div>Alguma Página</div>'},
        meta: {breadcrumb: 'Alguma Página'}
    },
    {
        path: '/processo/:idProcesso',
        name: 'Processo',
        component: {template: '<div>Processo</div>'},
        meta: {breadcrumb: 'Processo'}
    },
    {
        path: '/unidade/:siglaUnidade',
        name: 'Unidade',
        component: {template: '<div>Unidade</div>'},
        meta: {breadcrumb: 'Unidade'}
    },
    {
        path: '/relatorios',
        name: 'Relatorios',
        component: {template: '<div>Relatórios</div>'},
        meta: {breadcrumb: 'Relatórios'}
    },
    {path: '/custom', name: 'Custom', component: {template: '<div>Custom</div>'}},
];

describe('BarraNavegacao.vue', () => {
    let router: ReturnType<typeof createRouter>;
    let pinia: ReturnType<typeof createPinia>;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);

        router = createRouter({
            history: createWebHistory(),
            routes,
        });

        // Mock das unidades store para não depender de dados reais
        const unidadesStore = useUnidadesStore();
        vi.spyOn(unidadesStore, 'pesquisarUnidade').mockImplementation((sigla: string) => ({
            id: 1,
            sigla,
            nome: `Unidade ${sigla}`,
            tipo: 'Tipo',
            titular: 1,
            responsavel: null,
            filhas: []
        }));

        // Mock sessionStorage
        vi.spyOn(sessionStorage, 'getItem').mockReturnValue(null);

        vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
        });

        vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {
        });
    });

    const mountComponent = async () => {
        const wrapper = mount(BarraNavegacao, {
            global: {
                plugins: [router, pinia],
                stubs: {
                    RouterLink: {template: '<a><slot /></a>'},
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

        it('deve exibir o botão Voltar em outras páginas', async () => {
            await router.push('/painel');
            const wrapper = await mountComponent();
            expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(true);
        });

        it('deve navegar para o painel se não houver crumbs anteriores', async () => {
            await router.push('/alguma-pagina');
            const wrapper = await mountComponent();
            const pushSpy = vi.spyOn(router, 'push');
            await wrapper.find('button.btn-outline-secondary').trigger('click');
            expect(pushSpy).toHaveBeenCalledWith({path: '/painel'});
        });

        it('deve navegar para o crumb pai se houver', async () => {
            const trailStore = useNavigationTrail();
            trailStore.set([
                {label: '__home__', to: '/painel'},
                {label: 'Página Atual', to: '/alguma-pagina'}
            ]);
            await router.push('/alguma-pagina');
            const pushSpy = vi.spyOn(router, 'push'); // Move spy creation here
            const wrapper = await mountComponent();
            await wrapper.find('button.btn-outline-secondary').trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
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
        it('deve iniciar com o crumb home por padrão', async () => {
            await router.push('/alguma-pagina');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2); // Home + Página Atual
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Alguma Página');
        });

        it('deve resetar a trilha se veio da navbar', async () => {
            const trailStore = useNavigationTrail();
            const resetSpy = vi.spyOn(trailStore, 'reset');
            vi.spyOn(sessionStorage, 'getItem').mockReturnValue('1');
            trailStore.set([{label: 'Old Crumb', to: '/old'}]);
            await router.push('/alguma-pagina');
            await mountComponent();
            expect(resetSpy).toHaveBeenCalled();
            expect(sessionStorage.removeItem).toHaveBeenCalledWith('cameFromNavbar');
        });

        it('deve popular a trilha para uma rota de processo', async () => {
            const trailStore = useNavigationTrail();
            const ensureBaseSpy = vi.spyOn(trailStore, 'ensureBase');
            const pushSpy = vi.spyOn(trailStore, 'push');
            await router.push('/processo/123');
            const wrapper = await mountComponent();
            expect(ensureBaseSpy).toHaveBeenCalled();
            expect(pushSpy).toHaveBeenCalledWith({
                label: 'Processo',
                to: {name: 'Processo', params: {idProcesso: 123}}
            });
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Processo');
        });

        it('deve popular a trilha para uma rota de unidade', async () => {
            const trailStore = useNavigationTrail();
            const ensureBaseSpy = vi.spyOn(trailStore, 'ensureBase');
            const pushSpy = vi.spyOn(trailStore, 'push');
            await router.push('/unidade/ABC');
            const wrapper = await mountComponent();
            expect(ensureBaseSpy).toHaveBeenCalled();
            expect(pushSpy).toHaveBeenCalledWith({
                label: 'ABC',
                to: {path: '/unidade/ABC'},
                title: 'Unidade ABC'
            });
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('ABC');
        });

        it('deve popular a trilha para uma rota genérica com breadcrumb meta', async () => {
            const trailStore = useNavigationTrail();
            const ensureBaseSpy = vi.spyOn(trailStore, 'ensureBase');
            const pushSpy = vi.spyOn(trailStore, 'push');
            await router.push('/relatorios');
            const wrapper = await mountComponent();
            expect(ensureBaseSpy).toHaveBeenCalled();
            expect(pushSpy).toHaveBeenCalledWith({label: 'Relatórios', to: '/relatorios'});

            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Relatórios');
        });

        it('deve usar a trilha da store se ela já estiver populada', async () => {
            const trailStore = useNavigationTrail();
            trailStore.set([
                {label: '__home__', to: '/painel'},
                {label: 'Custom Crumb', to: '/custom'}
            ]);
            await router.push('/custom');

            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Custom Crumb');
        });
    });
});
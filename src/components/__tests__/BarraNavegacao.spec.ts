import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {
    createRouter,
    createWebHistory,
    RouteLocationNormalized,
    type RouteRecordRaw,
    RouterLink,
    useRouter
} from 'vue-router';
import BarraNavegacao from '../BarraNavegacao.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {Perfil, TipoResponsabilidade} from '@/types/tipos';
import {computed} from "vue";
import {usePerfilStore} from "@/stores/perfil";

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
        path: '/processo/:idProcesso/:siglaUnidade',
        name: 'Subprocesso',
        component: {template: '<div>Subprocesso</div>'},
        meta: {breadcrumb: (route: RouteLocationNormalized) => route.params.siglaUnidade as string}
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/mapa',
        name: 'SubprocessoMapa',
        component: {template: '<div>Mapa</div>'},
        meta: {breadcrumb: 'Mapa'}
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/vis-mapa',
        name: 'SubprocessoVisMapa',
        component: {template: '<div>Visualização de Mapa</div>'},
        meta: {breadcrumb: 'Visualização de Mapa'}
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/cadastro',
        name: 'SubprocessoCadastro',
        component: {template: '<div>Cadastro</div>'},
        meta: {breadcrumb: 'Cadastro'}
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/vis-cadastro',
        name: 'SubprocessoVisCadastro',
        component: {template: '<div>Visualização de Atividades</div>'},
        meta: {breadcrumb: 'Visualização de Atividades'}
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/impacto-mapa',
        name: 'SubprocessoImpactoMapa',
        component: {template: '<div>Impacto no Mapa</div>'},
        meta: {breadcrumb: 'Impacto no Mapa'}
    },
    {
        path: '/unidade/:siglaUnidade',
        name: 'Unidade',
        component: {template: '<div>Unidade</div>'},
        meta: {breadcrumb: (route: RouteLocationNormalized) => route.params.siglaUnidade as string}
    },
    {
        path: '/unidade/:siglaUnidade/atribuicao',
        name: 'AtribuicaoTemporariaForm',
        component: {template: '<div>Atribuição Temporária</div>'},
        meta: {breadcrumb: 'Atribuição'}
    },
    {
        path: '/unidade/:siglaUnidade/mapa',
        name: 'Mapa',
        component: {template: '<div>Mapa da Unidade</div>'},
        meta: {breadcrumb: 'Mapa'}
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
            idServidorTitular: 1,
            responsavel: {
                idServidor: 1,
                tipo: TipoResponsabilidade.ATRIBUICAO,
                dataInicio: new Date('2025-01-01'),
                dataFim: new Date('2025-12-31')
            },
            filhas: []
        }));

        // Mock sessionStorage - not strictly needed for this component anymore, but good practice if other parts rely on it
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
                    RouterLink: {
                        template: '<a :href="resolvedTo"><slot /></a>',
                        props: ['to'],
                        setup(props) {
                            const router = useRouter();
                            const resolvedTo = computed(() => {
                                if (typeof props.to === 'string') {
                                    return props.to;
                                }
                                const resolved = router.resolve(props.to);
                                return resolved.fullPath;
                            });
                            return {resolvedTo};
                        },
                    },
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
        })

        it('deve exibir o breadcrumb home e o da página atual para uma rota simples', async () => {
            await router.push('/alguma-pagina');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Alguma Página');
        });

        it('deve popular a trilha para uma rota de processo', async () => {
            await router.push('/processo/123');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Processo');
            expect(breadcrumbItems[1].find('a').exists()).toBe(false); // Last crumb is not a link
        });

        it('deve popular a trilha para uma rota de subprocesso para ADMIN', async () => {
            perfilStore.perfilSelecionado = Perfil.ADMIN;
            await router.push('/processo/123/ABC');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(3);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Processo');
            expect(breadcrumbItems[1].findComponent(RouterLink).props().to).toEqual({
                name: 'Processo',
                params: {idProcesso: 123}
            });
            expect(breadcrumbItems[2].text()).toBe('ABC');
            expect(breadcrumbItems[2].find('a').exists()).toBe(false); // Last crumb is not a link
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
            await router.push('/processo/123/ABC/mapa');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(4);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Processo');
            expect(breadcrumbItems[1].findComponent(RouterLink).props().to).toEqual({
                name: 'Processo',
                params: {idProcesso: 123}
            });
            expect(breadcrumbItems[2].text()).toBe('ABC');
            expect(breadcrumbItems[2].findComponent(RouterLink).props().to).toEqual({
                name: 'Subprocesso',
                params: {idProcesso: 123, siglaUnidade: 'ABC'}
            });
            expect(breadcrumbItems[3].text()).toBe('Mapa');
            expect(breadcrumbItems[3].find('a').exists()).toBe(false); // Last crumb is not a link
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
            await router.push('/unidade/XYZ');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('XYZ');
            expect(breadcrumbItems[1].find('a').exists()).toBe(false); // Last crumb is not a link
        });

        it('deve popular a trilha para uma rota de unidade com sub-página (atribuicao)', async () => {
            await router.push('/unidade/XYZ/atribuicao');
            const wrapper = await mountComponent();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(3);
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('XYZ');
            expect(breadcrumbItems[1].findComponent(RouterLink).props().to).toEqual({
                name: 'Unidade',
                params: {siglaUnidade: 'XYZ'}
            });
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
    });
});

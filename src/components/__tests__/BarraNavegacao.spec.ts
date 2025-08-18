import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {createRouter, createWebHistory} from 'vue-router'; // Importar do vue-router real
import BarraNavegacao from '../BarraNavegacao.vue';

// Mocks das stores Pinia (manter como estão)
let mockPesquisarUnidade = vi.fn((sigla: string) => ({sigla, nome: `Unidade ${sigla}`}));
vi.mock('@/stores/unidades', () => ({
    useUnidadesStore: () => ({
        pesquisarUnidade: mockPesquisarUnidade,
    }),
}));

let mockTrailCrumbs: any[] = [];
let mockTrailReset = vi.fn();
let mockTrailPopTo = vi.fn();
let mockTrailEnsureBase = vi.fn(() => { // Modificar este mock
    if (mockTrailCrumbs.length === 0) {
        mockTrailCrumbs.push({label: '__home__', to: {path: '/painel'}, title: 'Painel'});
    }
});
let mockTrailPush = vi.fn((crumb: any) => mockTrailCrumbs.push(crumb));

describe('BarraNavegacao.vue', () => {
    let router: any; // Declarar o router aqui
    let pushSpy: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        vi.useFakeTimers();

        // Resetar os mocks das stores Pinia
        mockPesquisarUnidade.mockClear();
        mockPesquisarUnidade.mockImplementation((sigla: string) => ({sigla, nome: `Unidade ${sigla}`}));

        mockTrailCrumbs.length = 0; // Limpar o array de crumbs
        mockTrailReset.mockClear();
        mockTrailPopTo.mockClear();
        mockTrailEnsureBase.mockClear();
        mockTrailPush.mockClear();

        // Mock sessionStorage (manter como está, pois é global)
        vi.spyOn(sessionStorage, 'getItem').mockReturnValue(null);
        vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
        });

        // Criar uma instância do vue-router real
        router = createRouter({
            history: createWebHistory(),
            routes: [
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
                // Adicionar outras rotas que o componente possa usar
            ],
        });

        // Espiar os métodos do router
        pushSpy = vi.spyOn(router, 'push');
    });

    it('deve montar o componente corretamente', async () => {
        // Navegar para uma rota inicial para que o router tenha um estado
        await router.push('/painel');
        const wrapper = mount(BarraNavegacao, {
            global: {
                plugins: [router, createPinia()], // Passar o router real como plugin
                stubs: {
                    RouterLink: {template: '<a><slot /></a>'}, // Renderiza o slot dentro de uma tag <a>
                },
            },
        });
        expect(wrapper.exists()).toBe(true);
    });

    // Testes para shouldShowBackButton e goBack
    describe('Botão Voltar', () => {
        it('não deve exibir o botão Voltar na página de login', async () => {
            await router.push('/login');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(false);
        });

        it('deve exibir o botão Voltar em outras páginas', async () => {
            await router.push('/painel');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            expect(wrapper.find('button.btn-outline-secondary').exists()).toBe(true);
        });

        it('deve navegar para o painel se não houver crumbs anteriores', async () => {
            await router.push('/alguma-pagina'); // Define a rota inicial
            pushSpy.mockClear(); // Limpa as chamadas anteriores do pushSpy
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            await wrapper.find('button.btn-outline-secondary').trigger('click');
            expect(pushSpy).toHaveBeenCalledWith({path: '/painel'}); // Mudar para objeto
        });

        it('deve navegar para o crumb pai se houver', async () => {
            await router.push('/painel'); // Rota inicial
            await router.push('/alguma-pagina'); // Rota atual
            mockTrailCrumbs.push({label: '__home__', to: '/painel'}, {label: 'Página Atual', to: '/alguma-pagina'});
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            await wrapper.find('button.btn-outline-secondary').trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/painel'); // Verificar a navegação real
        });
    });

    // Testes para shouldShowBreadcrumbs
    describe('Breadcrumbs', () => {
        it('não deve exibir breadcrumbs na página de login', async () => {
            await router.push('/login');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(false);
        });

        it('não deve exibir breadcrumbs na página de painel', async () => {
            await router.push('/painel');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(false);
        });

        it('deve exibir breadcrumbs em outras páginas', async () => {
            await router.push('/alguma-pagina');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            expect(wrapper.find('[data-testid="breadcrumbs"]').exists()).toBe(true);
        });
    });

    // Testes para a lógica de crumbs (updateTrail e computed crumbs)
    describe('Lógica de Breadcrumbs (crumbs)', () => {
        it('deve iniciar com o crumb home por padrão', async () => {
            await router.push('/alguma-pagina');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2); // Home + Página Atual
            expect(breadcrumbItems[0].find('[data-testid="breadcrumb-home-icon"]').exists()).toBe(true);
            expect(breadcrumbItems[1].text()).toBe('Alguma Página');
        });

        it('deve resetar a trilha se veio da navbar', async () => {
            vi.spyOn(sessionStorage, 'getItem').mockReturnValue('1');
            mockTrailCrumbs.push({label: 'Old Crumb', to: '/old'}); // Popula antes do mount
            await router.push('/alguma-pagina');
            mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: {template: '<a><slot /></a>'}}
                }
            });
            await vi.runOnlyPendingTimers(); // Aguardar o watcher
            expect(mockTrailReset).toHaveBeenCalled();
            expect(sessionStorage.removeItem).toHaveBeenCalledWith('cameFromNavbar');
        });

        it('deve popular a trilha para uma rota de processo', async () => {
            await router.push('/processo/123');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            expect(mockTrailEnsureBase).toHaveBeenCalled();
            expect(mockTrailPush).toHaveBeenCalledWith({
                label: 'Processo',
                to: {name: 'Processo', params: {idProcesso: 123}}
            });
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Processo');
        });

        it('deve popular a trilha para uma rota de unidade', async () => {
            await router.push('/unidade/ABC');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            expect(mockTrailEnsureBase).toHaveBeenCalled();
            expect(mockTrailPush).toHaveBeenCalledWith({
                label: 'ABC',
                to: {path: '/unidade/ABC'},
                title: 'Unidade ABC'
            });
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('ABC');
        });

        it('deve popular a trilha para uma rota genérica com breadcrumb meta', async () => {
            await router.push('/relatorios');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            expect(mockTrailEnsureBase).toHaveBeenCalled();
            expect(mockTrailPush).toHaveBeenCalledWith({label: 'Relatórios', to: '/relatorios'});
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Relatórios');
        });

        it('deve usar a trilha da store se ela já estiver populada', async () => {
            mockTrailCrumbs.push({label: '__home__', to: '/painel'}, {label: 'Custom Crumb', to: '/custom'});
            await router.push('/custom');
            const wrapper = mount(BarraNavegacao, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: {template: '<a><slot /></a>'},
                    },
                },
            });
            await wrapper.vm.$nextTick();
            const breadcrumbItems = wrapper.findAll('[data-testid="breadcrumb-item"]');
            expect(breadcrumbItems.length).toBe(2);
            expect(breadcrumbItems[1].text()).toBe('Custom Crumb');
        });
    });
});

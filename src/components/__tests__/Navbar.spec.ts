import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {createRouter, createWebHistory} from 'vue-router';
import Navbar from '../Navbar.vue';
import {ref} from 'vue'; // Importar ref

// Mock do composable usePerfil
const mockServidorLogado = vi.fn();
const mockServidoresComPerfil = vi.fn();
const mockPerfilSelecionado = vi.fn();
const mockUnidadeSelecionada = vi.fn();

vi.mock('@/composables/usePerfil', () => ({
    usePerfil: () => ({
        servidorLogado: mockServidorLogado(),
        servidoresComPerfil: mockServidoresComPerfil(),
        perfilSelecionado: mockPerfilSelecionado(),
        unidadeSelecionada: mockUnidadeSelecionada(),
    }),
}));

// Mock da store perfilStore
const mockSetServidorId = vi.fn();
const mockSetPerfilUnidade = vi.fn();

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        setServidorId: mockSetServidorId,
        setPerfilUnidade: mockSetPerfilUnidade,
    }),
}));

describe('Navbar.vue', () => {
    let router: any;
    let pushSpy: any;
    let routerLinkStub: any; // Declarar o stub aqui

    beforeEach(() => {
        setActivePinia(createPinia());
        vi.useFakeTimers();

        // Resetar mocks do composable usePerfil
        mockServidorLogado.mockReturnValue(ref({id: 1, nome: 'Teste', perfil: 'ADMIN', unidade: 'ABC'}));
        mockServidoresComPerfil.mockReturnValue(ref([
            {id: 1, nome: 'Teste', perfil: 'ADMIN', unidade: 'ABC'},
            {id: 2, nome: 'Outro', perfil: 'USER', unidade: 'XYZ'},
        ]));
        mockPerfilSelecionado.mockReturnValue(ref('ADMIN'));
        mockUnidadeSelecionada.mockReturnValue(ref('ABC'));

        // Resetar mocks da perfilStore
        mockSetServidorId.mockClear();
        mockSetPerfilUnidade.mockClear();

        // Criar uma instância do vue-router real
        router = createRouter({
            history: createWebHistory(),
            routes: [
                {path: '/painel', name: 'Painel', component: {template: '<div>Painel</div>'}},
                {path: '/unidade/:sigla', name: 'Unidade', component: {template: '<div>Unidade</div>'}},
                {path: '/relatorios', name: 'Relatorios', component: {template: '<div>Relatórios</div>'}},
                {path: '/historico', name: 'Historico', component: {template: '<div>Histórico</div>'}},
                {path: '/configuracoes', name: 'Configuracoes', component: {template: '<div>Configurações</div>'}},
                {path: '/login', name: 'Login', component: {template: '<div>Login</div>'}},
            ],
        });

        // Espiar os métodos do router
        pushSpy = vi.spyOn(router, 'push');

        // Mock sessionStorage
        vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {
        });
        vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
        });
    });

    it('deve montar o componente corretamente', async () => {
        await router.push('/painel');
        const wrapper = mount(Navbar, {
            global: {
                plugins: [router, createPinia()],
                stubs: {
                    RouterLink: routerLinkStub, // Usar o stub definido
                },
            },
        });
        expect(wrapper.exists()).toBe(true);
    });

    describe('Navegação', () => {
        it('deve navegar para o painel', async () => {
            await router.push('/login'); // Rota inicial diferente
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            pushSpy.mockClear(); // Limpar chamadas anteriores
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Painel'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para minha unidade', async () => {
            await router.push('/login');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            pushSpy.mockClear();
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Minha unidade'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/unidade/ABC');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para relatórios', async () => {
            await router.push('/login');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            pushSpy.mockClear();
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Relatórios'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/relatorios');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para histórico', async () => {
            await router.push('/login');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            pushSpy.mockClear();
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Histórico'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/historico');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para configurações', async () => {
            await router.push('/login');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            pushSpy.mockClear();
            // O link de configurações não tem href="#", mas sim um router-link direto
            await wrapper.find('a[title="Configurações do sistema"]').trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/configuracoes');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para login ao sair', async () => {
            await router.push('/painel');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {
                        RouterLink: routerLinkStub,
                    },
                },
            });
            pushSpy.mockClear();

            const logoutLink = wrapper.find('a[title="Sair"]');
            expect(logoutLink.exists()).toBe(true);

            await logoutLink.trigger('click');
            await wrapper.vm.$nextTick(); // Adicionar esta linha

            expect(pushSpy).toHaveBeenCalledWith('/login');
            expect(sessionStorage.setItem).not.toHaveBeenCalledWith('cameFromNavbar', '1');
        });
    });

    describe('Seleção de Perfil', () => {
        it('deve exibir o perfil e unidade selecionados', async () => {
            await router.push('/painel');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            expect(wrapper.text()).toContain('ADMIN - ABC');
            expect(wrapper.find('select').exists()).toBe(false);
        });

        it('deve ativar o modo de edição ao clicar no perfil', async () => {
            await router.push('/painel');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            await wrapper.vm.$nextTick(); // Usar wrapper.vm.$nextTick()
            expect(wrapper.find('select').exists()).toBe(true);
            expect(wrapper.find('span[style="cursor: pointer;"]').exists()).toBe(false);
        });

        it('deve desativar o modo de edição ao perder o foco', async () => {
            await router.push('/painel');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            await wrapper.vm.$nextTick(); // Usar wrapper.vm.$nextTick()
            await wrapper.find('select').trigger('blur');
            await wrapper.vm.$nextTick(); // Usar wrapper.vm.$nextTick()
            expect(wrapper.find('select').exists()).toBe(false);
            expect(wrapper.find('span[style="cursor: pointer;"]').exists()).toBe(true);
        });

        it('deve atualizar o perfil e navegar para o painel ao selecionar um novo perfil', async () => {
            await router.push('/painel');
            const wrapper = mount(Navbar, {
                global: {
                    plugins: [router, createPinia()],
                    stubs: {RouterLink: routerLinkStub}
                }
            });
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            await wrapper.vm.$nextTick(); // Usar wrapper.vm.$nextTick()

            const select = wrapper.find('select');
            await select.setValue('2'); // Seleciona o servidor com id 2 (USER - XYZ)
            await select.trigger('change');
            await wrapper.vm.$nextTick(); // Usar wrapper.vm.$nextTick()

            expect(mockSetServidorId).toHaveBeenCalledWith(2);
            expect(mockSetPerfilUnidade).toHaveBeenCalledWith('USER', 'XYZ');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
            expect(wrapper.find('select').exists()).toBe(false);
        });
    });
});

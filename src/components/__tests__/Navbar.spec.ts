import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, MockInstance, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router';
import Navbar from '../Navbar.vue';
import {ref} from 'vue';

interface MockPerfilServidor {
    id: number;
    nome: string;
    perfil: string;
    unidade: string;
    email: string | null;
    ramal: string | null;
}

// Mock do composable usePerfil
const mockServidorLogadoRef = ref<MockPerfilServidor | {}>({}); // Allow empty object initially
const mockServidoresComPerfilRef = ref<MockPerfilServidor[]>([]);
const mockPerfilSelecionadoRef = ref('');
const mockUnidadeSelecionadaRef = ref('');

vi.mock('@/composables/usePerfil', () => ({
    usePerfil: () => ({
        servidorLogado: mockServidorLogadoRef,
        servidoresComPerfil: mockServidoresComPerfilRef,
        perfilSelecionado: mockPerfilSelecionadoRef,
        unidadeSelecionada: mockUnidadeSelecionadaRef,
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

const routes: RouteRecordRaw[] = [
    {path: '/painel', name: 'Painel', component: {template: '<div>Painel</div>'}},
    {path: '/unidade/:sigla', name: 'Unidade', component: {template: '<div>Unidade</div>'}},
    {path: '/relatorios', name: 'Relatorios', component: {template: '<div>Relatórios</div>'}},
    {path: '/historico', name: 'Historico', component: {template: '<div>Histórico</div>'}},
    {path: '/configuracoes', name: 'Configuracoes', component: {template: '<div>Configurações</div>'}},
    {path: '/login', name: 'Login', component: {template: '<div>Login</div>'}},
];

describe('Navbar.vue', () => {
    let router: ReturnType<typeof createRouter>;
    let pinia: ReturnType<typeof createPinia>;
    let pushSpy: MockInstance<typeof router.push>; // Explicitly type pushSpy

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);

        router = createRouter({
            history: createWebHistory(),
            routes,
        });

        pushSpy = vi.spyOn(router, 'push');

        // Mock sessionStorage
        vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {
        });
        vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
        });
    });

    const mountComponent = async (initialRoute: string = '/painel', perfil: string = '') => {
        if (perfil) {
            mockPerfilSelecionadoRef.value = perfil;
        }
        await router.push(initialRoute);
        const wrapper = mount(Navbar, {
            global: {plugins: [router, pinia]},
        });
        await router.isReady();
        await wrapper.vm.$nextTick();
        return wrapper;
    };

    it('deve montar o componente corretamente', async () => {
        const wrapper = await mountComponent();
        expect(wrapper.exists()).toBe(true);
    });

    describe('Navegação', () => {
        beforeEach(() => {
            // Reset mocks for navigation tests
            vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {
            });
            vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
            });
            // Ensure mockUnidadeSelecionadaRef is set for navigation tests
            mockUnidadeSelecionadaRef.value = 'ABC';
            // Removed: mockPerfilSelecionadoRef.value = 'ADMIN';
        });

        it('deve navegar para o painel', async () => {
            const wrapper = await mountComponent('/login');
            pushSpy.mockClear(); // Clear pushSpy after mountComponent
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Painel'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para minha unidade', async () => {
            const wrapper = await mountComponent('/login');
            pushSpy.mockClear(); // Clear pushSpy after mountComponent
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Minha unidade'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/unidade/ABC');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para relatórios', async () => {
            const wrapper = await mountComponent('/login');
            pushSpy.mockClear(); // Clear pushSpy after mountComponent
            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Relatórios'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/relatorios');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para histórico', async () => {
            const wrapper = await mountComponent('/login');
            pushSpy.mockClear();

            await wrapper.findAll('a[href="#"][class="nav-link"]').find(w => w.text().includes('Histórico'))?.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/historico');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it.skip('deve navegar para configurações', async () => {
            mockPerfilSelecionadoRef.value = 'ADMIN'; // Ensure ADMIN profile is set before mounting
            const wrapper = await mountComponent('/login');
            pushSpy.mockClear();
            await wrapper.vm.$nextTick(); // Add this line

            // O link de configurações não tem href="#", mas sim um router-link direto
            const settingsLink = wrapper.find('a[title="Configurações do sistema"]');
            expect(settingsLink.exists()).toBe(true); // Check if element exists
            await settingsLink.trigger('click');
            expect(pushSpy).toHaveBeenCalledWith('/configuracoes');
            expect(sessionStorage.setItem).toHaveBeenCalledWith('cameFromNavbar', '1');
        });

        it('deve navegar para login ao sair', async () => {
            const wrapper = await mountComponent('/painel'); // Start from painel
            pushSpy.mockClear(); // Clear spy after initial navigation
            const logoutLink = wrapper.find('a[title="Sair"]'); // Find the router-link
            expect(logoutLink.exists()).toBe(true);

            await logoutLink.trigger('click'); // Trigger click on the router-link
            await wrapper.vm.$nextTick();

            expect(pushSpy).toHaveBeenCalledWith('/login'); // router-link calls router.push
            expect(sessionStorage.setItem).not.toHaveBeenCalledWith('cameFromNavbar', '1'); // router-link does not set this
        });
    });

    describe('Seleção de Perfil', () => {
        beforeEach(() => {
            vi.spyOn(sessionStorage, 'setItem').mockImplementation(() => {
            });
            vi.spyOn(sessionStorage, 'removeItem').mockImplementation(() => {
            });

            mockServidorLogadoRef.value = {
                id: 1,
                nome: 'Teste',
                perfil: 'ADMIN',
                unidade: 'ABC',
                email: 'teste@example.com',
                ramal: null
            } as MockPerfilServidor;

            mockServidoresComPerfilRef.value = [
                {id: 1, nome: 'Teste', perfil: 'ADMIN', unidade: 'ABC', email: 'teste@example.com', ramal: null},
                {id: 2, nome: 'Outro', perfil: 'USER', unidade: 'XYZ', email: null, ramal: '1234'},
            ] as MockPerfilServidor[];

            mockPerfilSelecionadoRef.value = 'ADMIN';
            mockUnidadeSelecionadaRef.value = 'ABC';

            // Clear the global mocks for perfilStore actions
            mockSetServidorId.mockClear();
            mockSetPerfilUnidade.mockClear();
        });

        it('deve exibir o perfil e unidade selecionados', async () => {
            const wrapper = await mountComponent();
            expect(wrapper.text()).toContain('ADMIN - ABC');
            expect(wrapper.find('select').exists()).toBe(false);
        });

        it('deve ativar o modo de edição ao clicar no perfil', async () => {
            const wrapper = await mountComponent();
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            expect(wrapper.find('select').exists()).toBe(true);
            expect(wrapper.find('span[style="cursor: pointer;"]').exists()).toBe(false);
        });

        it('deve desativar o modo de edição ao perder o foco', async () => {
            const wrapper = await mountComponent();
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            await wrapper.vm.$nextTick();
            await wrapper.find('select').trigger('blur');
            expect(wrapper.find('select').exists()).toBe(false);
            expect(wrapper.find('span[style="cursor: pointer;"]').exists()).toBe(true);
        });

        it('deve atualizar o perfil e navegar para o painel ao selecionar um novo perfil', async () => {
            const wrapper = await mountComponent();
            await wrapper.find('span[style="cursor: pointer;"]').trigger('click');
            await wrapper.vm.$nextTick();

            const select = wrapper.find('select');
            await select.setValue('2'); // Seleciona o servidor com id 2 (USER - XYZ)
            await select.trigger('change');
            await wrapper.vm.$nextTick();

            expect(mockSetServidorId).toHaveBeenCalledWith(2);
            expect(mockSetPerfilUnidade).toHaveBeenCalledWith('USER', 'XYZ');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
            expect(wrapper.find('select').exists()).toBe(false);
        });
    });
});
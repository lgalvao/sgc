import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, MockInstance, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router';
import Navbar from '../Navbar.vue';
import {ref} from 'vue';
import {useServidoresStore} from '@/stores/servidores';
import {Perfil, Servidor} from "@/types/tipos";

// Mock do composable usePerfil
const mockServidorLogadoRef = ref<Servidor | Record<string, unknown>>({});
const mockPerfilSelecionadoRef = ref('');
const mockUnidadeSelecionadaRef = ref('');
const mockGetPerfisDoServidor = vi.fn();

vi.mock('@/composables/usePerfil', () => ({
    usePerfil: () => ({
        servidorLogado: mockServidorLogadoRef,
        perfilSelecionado: mockPerfilSelecionadoRef,
        unidadeSelecionada: mockUnidadeSelecionadaRef,
        getPerfisDoServidor: mockGetPerfisDoServidor,
    }),
}));

// Mock da store perfilStore
const mockSetServidorId = vi.fn();
const mockSetPerfilUnidade = vi.fn();

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        setServidorId: mockSetServidorId,
        setPerfilUnidade: mockSetPerfilUnidade,
        servidorId: 1, // Mock a default serverId
        perfilSelecionado: mockPerfilSelecionadoRef,
        unidadeSelecionada: mockUnidadeSelecionadaRef,
    }),
}));

const routes: RouteRecordRaw[] = [
    {path: '/painel', name: 'Painel', component: {template: '<div>Painel</div>'}},
    {path: '/login', name: 'Login', component: {template: '<div>Login</div>'}},
];

describe('Navbar.vue', () => {
    let router: ReturnType<typeof createRouter>;
    let pinia: ReturnType<typeof createPinia>;
    let pushSpy: MockInstance; // Remover os argumentos de tipo

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);

        router = createRouter({
            history: createWebHistory(),
            routes,
        });

        pushSpy = vi.spyOn(router, 'push');

        // Reset mocks
        mockGetPerfisDoServidor.mockClear();
        mockSetServidorId.mockClear();
        mockSetPerfilUnidade.mockClear();
        mockServidorLogadoRef.value = {};
        mockPerfilSelecionadoRef.value = '';
        mockUnidadeSelecionadaRef.value = '';
    });

    const mountComponent = async (initialRoute: string = '/painel') => {
        await router.push(initialRoute);
        const wrapper = mount(Navbar, {
            global: {plugins: [router, pinia]},
        });
        await router.isReady();
        await wrapper.vm.$nextTick();
        return wrapper;
    };

    describe('Seleção de Perfil', () => {
        beforeEach(() => {
            // Setup mock data for profile selection tests
            const servidoresStore = useServidoresStore();
            servidoresStore.servidores = [
                {id: 1, nome: 'Teste Admin', unidade: 'ABC', email: 'teste@example.com', ramal: null},
                {id: 2, nome: 'Teste User', unidade: 'XYZ', email: null, ramal: '1234'},
            ];

            mockGetPerfisDoServidor.mockImplementation((id) => {
                if (id === 1) return [{perfil: Perfil.ADMIN, unidade: 'ABC'}];
                if (id === 2) return [{perfil: 'USER', unidade: 'XYZ'}];
                return [];
            });

            mockServidorLogadoRef.value = {id: 1, nome: 'Teste Admin'};
            mockPerfilSelecionadoRef.value = Perfil.ADMIN;
            mockUnidadeSelecionadaRef.value = 'ABC';
        });

        it('deve exibir o perfil e unidade selecionados', async () => {
            const wrapper = await mountComponent();
            expect(wrapper.text()).toContain(`${Perfil.ADMIN} - ABC`);
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

            await wrapper.vm.$nextTick(); // Garante que o select e as options estão renderizados

            const select = wrapper.find('select');
            await select.setValue('2-USER-XYZ'); // Simula a seleção da opção
            await wrapper.vm.$nextTick(); // Aguarda a atualização do Vue

            expect(mockSetServidorId).toHaveBeenCalledWith(2);
            expect(mockSetPerfilUnidade).toHaveBeenCalledWith('USER', 'XYZ');
            expect(pushSpy).toHaveBeenCalledWith('/painel');
            expect(wrapper.find('select').exists()).toBe(false);
        });
    });

    describe('Navegação', () => {
        beforeEach(() => {
            // Setup mock data for navigation tests
            mockServidorLogadoRef.value = {id: 1, nome: 'Teste User'};
            mockPerfilSelecionadoRef.value = 'USER';
            mockUnidadeSelecionadaRef.value = 'ABC';
        });

        it('deve definir sessionStorage e navegar corretamente ao usar navigateFromNavbar', async () => {
            const wrapper = await mountComponent();

            // Mock sessionStorage
            const sessionStorageSpy = vi.spyOn(sessionStorage, 'setItem');

            const vm = wrapper.vm as unknown as { navigateFromNavbar: (route: string) => void };
            vm.navigateFromNavbar('/test-route');

            // Verifica se sessionStorage foi definido corretamente
            expect(sessionStorageSpy).toHaveBeenCalledWith('cameFromNavbar', '1');

            // Verifica se o router.push foi chamado com a rota correta
            expect(pushSpy).toHaveBeenCalledWith('/test-route');

            // Limpa o spy
            sessionStorageSpy.mockRestore();
        });

        it('deve navegar para diferentes rotas usando navigateFromNavbar', async () => {
            const wrapper = await mountComponent();

            const vm = wrapper.vm as unknown as { navigateFromNavbar: (route: string) => void };

            // Testa navegação para painel
            vm.navigateFromNavbar('/painel');
            expect(pushSpy).toHaveBeenCalledWith('/painel');

            // Testa navegação para relatórios
            vm.navigateFromNavbar('/relatorios');
            expect(pushSpy).toHaveBeenCalledWith('/relatorios');

            // Testa navegação para histórico
            vm.navigateFromNavbar('/historico');
            expect(pushSpy).toHaveBeenCalledWith('/historico');
        });
    });
});
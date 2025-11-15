import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, type MockInstance, vi} from 'vitest';
import {createRouter, createWebHistory} from 'vue-router';
import {ref} from 'vue';
import NavBar from '../Navbar.vue';
import {initPinia} from '@/test-utils/helpers';
import {usePerfil} from '@/composables/usePerfil';
import {usePerfilStore} from '@/stores/perfil';

// Mocks
vi.mock('@/composables/usePerfil');
vi.mock('@/stores/perfil');

const routes = [
    {path: '/', component: {template: '<div></div>'}},
    {path: '/painel', component: {template: '<div></div>'}},
    {path: '/login', component: {template: '<div></div>'}},
    {path: '/teste', component: {template: '<div></div>'}},
    {path: '/configuracoes', component: {template: '<div></div>'}},
];

describe('Navbar.vue', () => {
    let router: ReturnType<typeof createRouter>;
    let pushSpy: MockInstance;

    beforeEach(() => {
        initPinia();
        router = createRouter({history: createWebHistory(), routes});
        pushSpy = vi.spyOn(router, 'push');
        vi.clearAllMocks();

        vi.mocked(usePerfil).mockReturnValue({
            servidorLogado: ref({nome: 'Usuario Teste'}),
            perfilSelecionado: ref('GESTOR'),
            unidadeSelecionada: ref('Unidade Teste'),
        } as any);

        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: 'ADMIN',
            unidadeSelecionada: 456,
        } as any);
    });

    it('deve navegar para a rota correta ao chamar navigateFromNavbar', async () => {
        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();
        const vm = wrapper.vm as any;
        vm.navigateFromNavbar('/teste');
        expect(pushSpy).toHaveBeenCalledWith('/teste');
    });

    it('deve exibir o perfil e a unidade do usuário', async () => {
        vi.mocked(usePerfil).mockReturnValue({
            perfilSelecionado: ref('CHEFE'),
            unidadeSelecionada: ref('TRE-PR'),
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        const userInfo = wrapper.find('span.nav-link');
        expect(userInfo.text()).toContain('CHEFE - TRE-PR');
    });

    it('deve exibir o ícone de configurações para o perfil ADMIN', async () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: 'ADMIN',
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        const settingsIcon = wrapper.find('[title="Configurações do sistema"]');
        expect(settingsIcon.exists()).toBe(true);
    });

    it('NÃO deve exibir o ícone de configurações para perfis diferentes de ADMIN', async () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: 'GESTOR',
        } as any);

        const wrapper = mount(NavBar, {global: {plugins: [router]}});
        await router.isReady();

        const settingsIcon = wrapper.find('[title="Configurações do sistema"]');
        expect(settingsIcon.exists()).toBe(false);
    });
});

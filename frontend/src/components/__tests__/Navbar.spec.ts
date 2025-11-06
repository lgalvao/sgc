import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, type MockInstance, vi} from 'vitest';
import {createRouter, createWebHistory} from 'vue-router';
import Navbar from '@/components/Navbar.vue';
import {initPinia} from '@/test-utils/helpers';

// Mocks
vi.mock('@/composables/usePerfil', () => ({
    usePerfil: () => ({
        servidorLogado: { value: { nome: 'Teste' } },
        perfilSelecionado: { value: 'ADMIN' },
        unidadeSelecionada: { value: 123 },
    }),
}));
vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({}),
}));

const routes = [
    { path: '/', component: { template: '<div></div>' } },
    { path: '/painel', component: { template: '<div></div>' } },
    { path: '/login', component: { template: '<div></div>' } },
    { path: '/teste', component: { template: '<div></div>' } },
];

describe('Navbar.vue', () => {
    let router: ReturnType<typeof createRouter>;
    let pushSpy: MockInstance;

    beforeEach(() => {
        initPinia();
        router = createRouter({ history: createWebHistory(), routes });
        pushSpy = vi.spyOn(router, 'push');
    });

    it('deve navegar para a rota correta ao chamar navigateFromNavbar', async () => {
        const wrapper = mount(Navbar, { global: { plugins: [router] } });
        await router.isReady();
        const vm = wrapper.vm as any;
        vm.navigateFromNavbar('/teste');
        expect(pushSpy).toHaveBeenCalledWith('/teste');
    });
});

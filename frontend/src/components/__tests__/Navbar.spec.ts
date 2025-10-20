import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, type MockInstance, vi} from 'vitest';
import {type Pinia} from 'pinia';
import {initPinia} from '@/test-utils/helpers';
import {createRouter, createWebHistory, type Router, type RouteRecordRaw} from 'vue-router';
import Navbar from '../Navbar.vue';
import {ref} from 'vue';
import {useServidoresStore} from '@/stores/servidores';
import type {PerfilUnidade} from '@/types/tipos';
import {Perfil, type Servidor, type Unidade} from '@/types/tipos';

// Mock do composable usePerfil
const mockServidorLogadoRef = ref<Servidor | null>(null);
const mockPerfilSelecionadoRef = ref<Perfil | '' >('');
const mockUnidadeSelecionadaRef = ref<Unidade | null>(null);
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
  { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
  { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
];

describe('Navbar.vue', () => {
  let router: Router;
  let pinia: Pinia;
  let pushSpy: MockInstance;

  beforeEach(() => {
    pinia = initPinia();

    router = createRouter({
      history: createWebHistory(),
      routes,
    });

    pushSpy = vi.spyOn(router, 'push');

    // Reset mocks
    mockGetPerfisDoServidor.mockClear();
    mockSetServidorId.mockClear();
    mockSetPerfilUnidade.mockClear();
    mockServidorLogadoRef.value = null;
    mockPerfilSelecionadoRef.value = '';
    mockUnidadeSelecionadaRef.value = null;
  });

  const mountComponent = async (initialRoute: string = '/painel') => {
    await router.push(initialRoute);
    const wrapper = mount(Navbar, {
      global: { plugins: [router, pinia] },
    });
    await router.isReady();
    await wrapper.vm.$nextTick();
    return wrapper;
  };

  describe('Seleção de Perfil', () => {
    beforeEach(() => {
      const servidoresStore = useServidoresStore();
      servidoresStore.servidores = [
        { codigo: 1, nome: 'Teste Admin', unidade: { sigla: 'ABC' } } as Servidor,
        { codigo: 2, nome: 'Teste User', unidade: { sigla: 'XYZ' } } as Servidor,
      ];

      mockGetPerfisDoServidor.mockImplementation((id: number): PerfilUnidade[] => {
        if (id === 1) return [{ perfil: Perfil.ADMIN, unidade: { sigla: 'ABC' } } as PerfilUnidade];
        if (id === 2) return [{ perfil: Perfil.SERVIDOR, unidade: { sigla: 'XYZ' } } as PerfilUnidade];
        return [];
      });

      mockServidorLogadoRef.value = { codigo: 1, nome: 'Teste Admin' } as Servidor;
      mockPerfilSelecionadoRef.value = Perfil.ADMIN;
      mockUnidadeSelecionadaRef.value = 'ABC' as any;
    });

    it('deve exibir o perfil e unidade selecionados', async () => {
      const wrapper = await mountComponent();
      expect(wrapper.find('span.nav-link[style="cursor: pointer;"]').text()).toContain(`${Perfil.ADMIN} - ABC`);
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
      await select.setValue('2-SERVIDOR-XYZ');
      await wrapper.vm.$nextTick();

      expect(mockSetServidorId).toHaveBeenCalledWith(2);
      expect(mockSetPerfilUnidade).toHaveBeenCalledWith(Perfil.SERVIDOR, 'XYZ');
      expect(pushSpy).toHaveBeenCalledWith('/painel');
      expect(wrapper.find('select').exists()).toBe(false);
    });
  });

  describe('Navegação', () => {
    beforeEach(() => {
      mockServidorLogadoRef.value = { codigo: 1, nome: 'Teste User' } as Servidor;
      mockPerfilSelecionadoRef.value = Perfil.SERVIDOR;
      mockUnidadeSelecionadaRef.value = 'ABC' as any;
    });

    it('deve definir sessionStorage e navegar corretamente ao usar navigateFromNavbar', async () => {
      const wrapper = await mountComponent();
      const sessionStorageSpy = vi.spyOn(sessionStorage, 'setItem');
      const vm = wrapper.vm as any;
      vm.navigateFromNavbar('/test-route');
      expect(sessionStorageSpy).toHaveBeenCalledWith('cameFromNavbar', '1');
      expect(pushSpy).toHaveBeenCalledWith('/test-route');
      sessionStorageSpy.mockRestore();
    });

    it('deve navegar para diferentes rotas usando navigateFromNavbar', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;

      vm.navigateFromNavbar('/painel');
      expect(pushSpy).toHaveBeenCalledWith('/painel');

      vm.navigateFromNavbar('/relatorios');
      expect(pushSpy).toHaveBeenCalledWith('/relatorios');

      vm.navigateFromNavbar('/historico');
      expect(pushSpy).toHaveBeenCalledWith('/historico');
    });
  });
});
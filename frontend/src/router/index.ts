import type {RouteRecordRaw} from 'vue-router';
import {createMemoryHistory, createRouter, createWebHistory} from 'vue-router';
import processoRoutes from './processo.routes';
import unidadeRoutes from './unidade.routes';
import mainRoutes from './main.routes';
import {usePerfilStore} from '@/stores/perfil';

const routes: RouteRecordRaw[] = [
  ...mainRoutes,
  ...processoRoutes,
  ...unidadeRoutes,
];

const router = createRouter({
  history: typeof window === 'undefined' ? createMemoryHistory() : createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const perfilStore = usePerfilStore();
  const isAuthenticated = perfilStore.servidorId;
  const publicPages = ['/login'];
  const authRequired = !publicPages.includes(to.path);

  if (authRequired && !isAuthenticated) {
    return next('/login');
  }

  next();
});

router.afterEach((to) => {
  const meta = to.meta || {};
  const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC';
  document.title = `${titleBase} - SGC`;
});

export default router;

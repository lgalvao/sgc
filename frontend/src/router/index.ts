import { createRouter, createWebHistory, createMemoryHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import processoRoutes from './processo.routes';
import unidadeRoutes from './unidade.routes';
import mainRoutes from './main.routes';

const routes: RouteRecordRaw[] = [
  ...mainRoutes,
  ...processoRoutes,
  ...unidadeRoutes,
];

const router = createRouter({
  history: typeof window === 'undefined' ? createMemoryHistory() : createWebHistory(),
  routes,
});

router.afterEach((to) => {
  const meta = to.meta || {};
  const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC';
  document.title = `${titleBase} - SGC`;
});

export default router;

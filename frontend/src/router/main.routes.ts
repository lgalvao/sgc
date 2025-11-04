import type {RouteRecordRaw} from 'vue-router';

const mainRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: 'Login' },
  },
  {
    path: '/painel',
    name: 'Painel',
    component: () => import('@/views/Painel.vue'),
    meta: { title: 'Painel' },
  },
  {
    path: '/historico',
    name: 'Historico',
    component: () => import('@/views/Historico.vue'),
    meta: { title: 'Histórico' },
  },
  {
    path: '/relatorios',
    name: 'Relatorios',
    component: () => import('@/views/Relatorios.vue'),
    meta: { title: 'Relatórios' },
  },
  {
    path: '/configuracoes',
    name: 'Configuracoes',
    component: () => import('@/views/Configuracoes.vue'),
    meta: { title: 'Configurações' },
  },
];

export default mainRoutes;

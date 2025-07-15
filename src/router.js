// src/router.js
// Configuração do Vue Router para navegação entre páginas
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/Login.vue'),
  },
  {
    path: '/painel',
    name: 'Painel',
    component: () => import('./views/Painel.vue'),
    props: true,
  },
  {
    path: '/processos',
    name: 'Processos',
    component: () => import('./views/Processos.vue'),
  },
  {
    path: '/processos/novo',
    name: 'NovoProcesso',
    component: () => import('./views/FormProcesso.vue'),
  },
  {
    path: '/mapas',
    name: 'Mapas',
    component: () => import('./views/Mapas.vue'),
  },
  {
    path: '/diagnostico',
    name: 'Diagnóstico',
    component: () => import('./views/Diagnostico.vue'),
  },
  {
    path: '/atribuicoes',
    name: 'Atribuições',
    component: () => import('./views/Atribuicoes.vue'),
  },
  {
    path: '/processos/:id/unidade/:unidadeId/atividades',
    name: 'AtividadesConhecimentos',
    component: () => import('./views/AtividadesConhecimentos.vue'),
  },
  {
    path: '/processos/:id/unidades',
    name: 'UnidadesProcesso',
    component: () => import('./views/UnidadesProcesso.vue'),
  },
  {
    path: '/unidade/:sigla',
    name: 'DetalheUnidade',
    component: () => import('./views/DetalheUnidade.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router 
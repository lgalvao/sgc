import {createRouter, createWebHistory, RouteLocationNormalized} from 'vue-router';

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
    path: '/processos/novo',
    name: 'CadProcesso',
    component: () => import('./views/CadProcesso.vue'),
  },
  {
    path: '/processos/:id/unidade/:unidadeId/atividades',
    name: 'CadAtividades',
    component: () => import('./views/CadAtividades.vue'),
    props: true, // Adicionado para passar id e unidadeId como props
  },
  {
    path: '/processos/:id/unidades',
    name: 'ProcessoUnidades',
    component: () => import('./views/Processo.vue'),
  },
  {
    path: '/unidade/:sigla',
    name: 'Unidade',
    component: () => import('./views/Unidade.vue'),
  },
  {
    path: '/processo-unidade/:idProcessoUnidade',
    name: 'ProcessoUnidade',
    component: () => import('./views/ProcessoUnidade.vue'),
    props: true,
  },
  {
    path: '/processos/:processoId/unidades/:sigla',
    name: 'ProcessosSubordinadas',
    component: () => import('./views/ProcessosSubordinadas.vue'),
    props: true,
  },
  {
    path: '/unidade/:sigla/mapa',
    name: 'Mapa',
    component: () => import('./views/CadMapa.vue'),
    props: (route: RouteLocationNormalized) => ({ sigla: route.params.sigla, processoId: route.query.processoId }),
  },
  {
    path: '/unidade/:sigla/mapa/visualizar',
    name: 'VisualizacaoMapa',
    component: () => import('./views/CadMapaVisualizacao.vue'),
    props: (route: RouteLocationNormalized) => ({ sigla: route.params.sigla, processoId: route.query.processoId }),
  },
  {
    path: '/finalizacao-mapa',
    name: 'FinalizacaoMapa',
    component: () => import('./views/CadMapaFinalizacao.vue'),
    props: (route: RouteLocationNormalized) => ({ sigla: route.query.sigla, processoId: route.query.processoId }),
  },
  {
    path: '/unidade/:sigla/atribuicao',
    name: 'AtribuicaoTemporariaForm',
    component: () => import('./views/CadAtribuicao.vue'),
  },
  {
    path: '/historico',
    name: 'Historico',
    component: () => import('./views/HistoricoProcessos.vue'),
  },
  {
    path: '/relatorios',
    name: 'Relatorios',
    component: () => import('./views/Relatorios.vue'),
  },
  {
    path: '/configuracoes',
    name: 'Configuracoes',
    component: () => import('./views/Configuracoes.vue'),
  },

];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
import type {RouteLocationNormalized, RouteRecordRaw} from 'vue-router';

const unidadeRoutes: RouteRecordRaw[] = [
  {
    path: '/unidade/:siglaUnidade',
    name: 'Unidade',
    component: () => import('@/views/Unidade.vue'),
    props: true,
    meta: {
      title: 'Unidade',
      breadcrumb: (route: RouteLocationNormalized) => `${route.params.siglaUnidade ?? ''}`,
    },
  },
  {
    path: '/unidade/:siglaUnidade/mapa',
    name: 'Mapa',
    component: () => import('@/views/CadMapa.vue'),
    props: (route: RouteLocationNormalized) => ({
      sigla: route.params.siglaUnidade,
      codProcesso: Number(route.query.codProcesso),
    }),
    meta: { title: 'Mapa' },
  },
  {
    path: '/unidade/:siglaUnidade/atribuicao',
    name: 'AtribuicaoTemporariaForm',
    component: () => import('@/views/CadAtribuicao.vue'),
    props: (route: RouteLocationNormalized) => ({ sigla: route.params.siglaUnidade }),
    meta: { title: 'Atribuição Temporária' },
  },
];

export default unidadeRoutes;

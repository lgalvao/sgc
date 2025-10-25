import type { RouteRecordRaw, RouteLocationNormalized } from 'vue-router';

const processoRoutes: RouteRecordRaw[] = [
  {
    path: '/processo/cadastro',
    name: 'CadProcesso',
    component: () => import('@/views/CadProcesso.vue'),
    meta: { title: 'Novo Processo' },
  },
  {
    path: '/processo/:idProcesso',
    name: 'Processo',
    component: () => import('@/views/Processo.vue'),
    props: true,
    meta: { title: 'Unidades do Processo' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade',
    name: 'Subprocesso',
    component: () => import('@/views/Subprocesso.vue'),
    props: (route: RouteLocationNormalized) => ({
      idProcesso: Number(route.params.idProcesso),
      siglaUnidade: route.params.siglaUnidade,
    }),
    meta: { title: 'Processos da Unidade' },
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/mapa',
    name: 'SubprocessoMapa',
    component: () => import('@/views/CadMapa.vue'),
    props: (route: RouteLocationNormalized) => ({
      sigla: route.params.siglaUnidade,
      idProcesso: Number(route.params.idProcesso),
    }),
    meta: { title: 'Mapa' },
  },
  {
        path: '/processo/:idProcesso/:siglaUnidade/vis-mapa',
        name: 'SubprocessoVisMapa',
        component: () => import('@/views/VisMapa.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Visualização de Mapa'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/cadastro',
        name: 'SubprocessoCadastro',
        component: () => import('@/views/CadAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Cadastro'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/vis-cadastro',
        name: 'SubprocessoVisCadastro',
        component: () => import('@/views/VisAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Visualização de Atividades'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/diagnostico-equipe',
        name: 'DiagnosticoEquipe',
        component: () => import('@/views/DiagnosticoEquipe.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            siglaUnidade: route.params.siglaUnidade
        }),
        meta: {title: 'Diagnóstico da Equipe'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/ocupacoes-criticas',
        name: 'OcupacoesCriticas',
        component: () => import('@/views/OcupacoesCriticas.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            siglaUnidade: route.params.siglaUnidade
        }),
        meta: {title: 'Ocupações Críticas'},
    },
];

export default processoRoutes;

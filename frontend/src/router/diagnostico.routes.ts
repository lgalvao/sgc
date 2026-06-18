import type {RouteLocationNormalized, RouteRecordRaw} from 'vue-router';

const diagnosticoRoutes: RouteRecordRaw[] = [
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao',
        name: 'AutoavaliacaoDiagnostico',
        component: () => import('@/views/AutoavaliacaoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: String(route.params.siglaUnidade),
        }),
        meta: {title: 'Autoavaliação - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/situacao-capacitacao',
        name: 'SituacaoCapacitacaoDiagnostico',
        component: () => import('@/views/SituacaoCapacitacaoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: String(route.params.siglaUnidade),
        }),
        meta: {title: 'Situação de Capacitação - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/consenso/:servidorTitulo',
        name: 'ConsensoDiagnostico',
        component: () => import('@/views/ConsensoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: String(route.params.siglaUnidade),
            servidorTitulo: String(route.params.servidorTitulo),
        }),
        meta: {title: 'Avaliação de consenso - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/unidade',
        name: 'DiagnosticoUnidade',
        component: () => import('@/views/DiagnosticoUnidadeView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: String(route.params.siglaUnidade),
        }),
        meta: {title: 'Análise da Unidade - Diagnóstico'},
    },
];

export default diagnosticoRoutes;

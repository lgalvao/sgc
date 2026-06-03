import type {RouteLocationNormalized, RouteRecordRaw} from 'vue-router';

const diagnosticoRoutes: RouteRecordRaw[] = [
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao',
        name: 'AutoavaliacaoDiagnostico',
        component: () => import('@/views/AutoavaliacaoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: route.params.siglaUnidade as string,
        }),
        meta: {title: 'Autoavaliação - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/situacao-capacitacao',
        name: 'OcupacoesCriticasDiagnostico',
        component: () => import('@/views/OcupacoesCriticasDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: route.params.siglaUnidade as string,
        }),
        meta: {title: 'Situação de Capacitação - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/monitoramento',
        name: 'MonitoramentoDiagnostico',
        component: () => import('@/views/MonitoramentoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: route.params.siglaUnidade as string,
        }),
        meta: {title: 'Monitoramento - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/consenso/:servidorTitulo',
        name: 'ConsensoDiagnostico',
        component: () => import('@/views/ConsensoDiagnosticoView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: route.params.siglaUnidade as string,
            servidorTitulo: route.params.servidorTitulo as string,
        }),
        meta: {title: 'Avaliação de Consenso - Diagnóstico'},
    },
    {
        path: '/diagnostico/:codSubprocesso/:siglaUnidade/unidade',
        name: 'DiagnosticoUnidade',
        component: () => import('@/views/DiagnosticoUnidadeView.vue'),
        props: (route: RouteLocationNormalized) => ({
            codSubprocesso: Number(route.params.codSubprocesso),
            siglaUnidade: route.params.siglaUnidade as string,
        }),
        meta: {title: 'Análise da Unidade - Diagnóstico'},
    },
];

export default diagnosticoRoutes;

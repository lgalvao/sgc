import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

const diagnosticoRoutes: RouteRecordRaw[] = [
    {
        path: "/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao",
        name: "AutoavaliacaoDiagnostico",
        component: () => import("@/views/AutoavaliacaoDiagnostico.vue"),
        props: true,
        meta: {title: "Autoavaliação"},
    },
    {
        path: "/diagnostico/:codSubprocesso/monitoramento",
        name: "MonitoramentoDiagnostico",
        component: () => import("@/views/MonitoramentoDiagnostico.vue"),
        props: true,
        meta: {title: "Monitoramento de Diagnóstico"},
    },
    {
        path: "/diagnostico/:codSubprocesso/ocupacoes",
        name: "OcupacoesCriticasDiagnostico",
        component: () => import("@/views/OcupacoesCriticasDiagnostico.vue"),
        props: true,
        meta: {title: "Ocupações Críticas"},
    },
    {
        path: "/diagnostico/:codSubprocesso/conclusao",
        name: "ConclusaoDiagnostico",
        component: () => import("@/views/ConclusaoDiagnostico.vue"),
        props: true,
        meta: {title: "Conclusão de Diagnóstico"},
    },
];

export default diagnosticoRoutes;

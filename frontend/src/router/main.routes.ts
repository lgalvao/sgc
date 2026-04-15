import type {RouteRecordRaw} from "vue-router";

const mainRoutes: RouteRecordRaw[] = [
    {
        path: "/",
        redirect: "/login",
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/LoginView.vue"),
        meta: {title: "Login"},
    },
    {
        path: "/erro",
        name: "ErroGeral",
        component: () => import("@/views/ErroGeralView.vue"),
        meta: {title: "Erro"},
    },
    {
        path: "/painel",
        name: "Painel",
        component: () => import("@/views/PainelView.vue"),
        meta: {title: "Painel", keepAlive: true},
    },
    {
        path: "/historico",
        name: "Historico",
        component: () => import("@/views/HistoricoView.vue"),
        meta: {title: "Histórico", keepAlive: true},
    },
    {
        path: "/relatorios",
        name: "Relatorios",
        component: () => import("@/views/RelatoriosView.vue"),
        meta: {title: "Relatórios", keepAlive: true},
    },
    {
        path: "/relatorios/andamento",
        name: "RelatorioAndamento",
        component: () => import("@/views/RelatorioAndamentoView.vue"),
        meta: {title: "Relatório de Andamento"},
    },
    {
        path: "/relatorios/mapas-vigentes",
        name: "RelatorioMapas",
        component: () => import("@/views/RelatorioMapasView.vue"),
        meta: {title: "Relatório de Mapas Vigentes"},
    },
    {
        path: "/configuracoes",
        name: "Parametros",
        component: () => import("@/views/ParametrosView.vue"),
        meta: {title: "Configurações"},
    },
    {
        path: "/administradores",
        name: "Administradores",
        component: () => import("@/views/AdministradoresView.vue"),
        meta: {title: "Administradores"},
    },
    {
        path: "/administracao/limpeza-processos",
        name: "LimpezaProcessos",
        component: () => import("@/views/LimpezaProcessosView.vue"),
        meta: {title: "Limpeza de processos"},
    },
];

export default mainRoutes;

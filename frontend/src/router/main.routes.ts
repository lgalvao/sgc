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
        path: "/painel",
        name: "Painel",
        component: () => import("@/views/PainelView.vue"),
        meta: {title: "Painel"},
    },
    {
        path: "/historico",
        name: "Historico",
        component: () => import("@/views/HistoricoView.vue"),
        meta: {title: "Histórico"},
    },
    {
        path: "/relatorios",
        name: "Relatorios",
        component: () => import("@/views/RelatoriosView.vue"),
        meta: {title: "Relatórios"},
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
];

export default mainRoutes;

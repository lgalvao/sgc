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
        path: "/relatorios/diagnostico/gaps",
        name: "RelatorioDiagnosticoGaps",
        component: () => import("@/views/RelatorioDiagnosticoGapsView.vue"),
        meta: {title: "Relatório de Gaps de Diagnóstico"},
    },
    {
        path: "/relatorios/diagnostico/situacao-capacitacao",
        name: "RelatorioDiagnosticoSituacaoCapacitacao",
        component: () => import("@/views/RelatorioDiagnosticoSituacaoCapacitacaoView.vue"),
        meta: {title: "Relatório de situações de capacitação"},
    },
    {
        path: "/relatorios/unidades-sem-mapas-vigentes",
        name: "RelatorioUnidadesSemMapasVigentes",
        component: () => import("@/views/RelatorioUnidadesSemMapasVigentesView.vue"),
        meta: {title: "Relatório de unidades sem mapas vigentes"},
    },
    {
        path: "/configuracoes",
        name: "Configuracoes",
        component: () => import("@/views/ConfiguracaoView.vue"),
        meta: {title: "Configurações"},
    },
    {
        path: "/administradores",
        name: "Administradores",
        component: () => import("@/views/AdministradoresView.vue"),
        meta: {title: "Administradores"},
    },
    {
        path: "/administracao/notificacoes",
        name: "NotificacoesAdmin",
        component: () => import("@/views/NotificacoesAdminView.vue"),
        meta: {title: "Notificações"},
    },
    {
        path: "/administracao/feedbacks",
        name: "FeedbacksAdmin",
        component: () => import("@/views/FeedbacksAdminView.vue"),
        meta: {title: "Feedbacks"},
    },
    {
        path: "/administracao/limpeza-processos",
        name: "LimpezaProcessos",
        component: () => import("@/views/LimpezaProcessosView.vue"),
        meta: {title: "Limpeza de processos"},
    },
];

export default mainRoutes;

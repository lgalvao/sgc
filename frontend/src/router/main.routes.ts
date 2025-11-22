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
      name: "Configuracoes",
      component: () => import("@/views/ConfiguracoesView.vue"),
      meta: {title: "Configurações"},
  },
];

export default mainRoutes;

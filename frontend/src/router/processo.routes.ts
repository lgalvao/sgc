import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

const processoRoutes: RouteRecordRaw[] = [
  {
      path: "/processo/cadastro",
      name: "CadProcesso",
      component: () => import("@/views/CadProcesso.vue"),
      meta: {title: "Novo Processo"},
  },
  {
      path: "/processo/:codProcesso",
      name: "Processo",
      component: () => import("@/views/ProcessoView.vue"),
    props: true,
      meta: {title: "Unidades do Processo"},
  },
  {
      path: "/processo/:codProcesso/:siglaUnidade",
      name: "Subprocesso",
      component: () => import("@/views/SubprocessoView.vue"),
    props: (route: RouteLocationNormalized) => ({
      codProcesso: Number(route.params.codProcesso),
      siglaUnidade: route.params.siglaUnidade,
    }),
      meta: {title: "Processos da Unidade"},
  },
  {
      path: "/processo/:codProcesso/:siglaUnidade/mapa",
      name: "SubprocessoMapa",
      component: () => import("@/views/CadMapa.vue"),
    props: (route: RouteLocationNormalized) => ({
      sigla: route.params.siglaUnidade,
      codProcesso: Number(route.params.codProcesso),
    }),
      meta: {title: "Mapa"},
  },
  {
      path: "/processo/:codProcesso/:siglaUnidade/vis-mapa",
      name: "SubprocessoVisMapa",
      component: () => import("@/views/VisMapa.vue"),
      props: (route: RouteLocationNormalized) => ({
          codProcesso: Number(route.params.codProcesso),
          sigla: route.params.siglaUnidade,
      }),
      meta: {title: "Visualização de Mapa"},
  },
    {
        path: "/processo/:codProcesso/:siglaUnidade/cadastro",
        name: "SubprocessoCadastro",
        component: () => import("@/views/CadAtividades.vue"),
        props: (route: RouteLocationNormalized) => ({
            codProcesso: Number(route.params.codProcesso),
            sigla: route.params.siglaUnidade,
        }),
        meta: {title: "Cadastro"},
    },
    {
        path: "/processo/:codProcesso/:siglaUnidade/vis-cadastro",
        name: "SubprocessoVisCadastro",
        component: () => import("@/views/VisAtividades.vue"),
        props: (route: RouteLocationNormalized) => ({
            codProcesso: Number(route.params.codProcesso),
            sigla: route.params.siglaUnidade,
        }),
        meta: {title: "Visualização de Atividades"},
    },
    {
        path: "/subprocesso/:codSubprocesso/:siglaUnidade/autoavaliacao",
        name: "AutoavaliacaoDiagnostico",
        component: () => import("@/views/AutoavaliacaoDiagnostico.vue"),
        props: true,
        meta: {title: "Autoavaliação"},
    },
    {
        path: "/subprocesso/:codSubprocesso/:siglaUnidade/ocupacoes",
        name: "OcupacoesCriticasDiagnostico",
        component: () => import("@/views/OcupacoesCriticasDiagnostico.vue"),
        props: true,
        meta: {title: "Ocupações Críticas"},
    },
    {
        path: "/subprocesso/:codSubprocesso/:siglaUnidade/monitoramento",
        name: "MonitoramentoDiagnostico",
        component: () => import("@/views/MonitoramentoDiagnostico.vue"),
        props: true,
        meta: {title: "Monitoramento"},
    },

];

export default processoRoutes;

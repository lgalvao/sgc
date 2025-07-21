import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("./views/Login.vue"),
  },
  {
    path: "/painel",
    name: "Painel",
    component: () => import("./views/Painel.vue"),
    props: true,
  },
  {
    path: "/processos/novo",
    name: "NovoProcesso",
    component: () => import("./views/FormProcesso.vue"),
  },
  {
    path: "/mapas",
    name: "Mapas",
    component: () => import("./views/Mapas.vue"),
  },
  {
    path: "/diagnostico",
    name: "Diagnóstico",
    component: () => import("./views/Diagnostico.vue"),
  },
  {
    path: "/processos/:id/unidade/:unidadeId/atividades",
    name: "AtividadesConhecimentos",
    component: () => import("./views/AtividadesConhecimentos.vue"),
  },
  {
    path: "/processos/:id/unidades",
    name: "UnidadesProcesso",
    component: () => import("./views/UnidadesProcesso.vue"),
  },
  {
    path: "/unidade/:sigla",
    name: "DetalheUnidade",
    component: () => import("./views/DetalheUnidade.vue"),
  },
  {
    path: "/unidade/:sigla/mapa",
    name: "MapaCompetencias",
    component: () => import("./views/MapaCompetencias.vue"),
  },
  {
    path: "/unidade/:sigla/mapa/visualizar",
    name: "VisualizacaoMapa",
    component: () => import("./views/VisualizacaoMapa.vue"),
  },
  {
    path: "/finalizacao-mapa",
    name: "FinalizacaoMapa",
    component: () => import("./views/FinalizacaoMapa.vue"),
  },
  {
    path: "/unidade/:sigla/atribuir",
    name: "AtribuicaoTemporariaForm",
    component: () => import("./views/AtribuicaoTemporariaForm.vue"),
  },
  {
    path: "/historico",
    name: "Historico",
    component: () => import("./views/Historico.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

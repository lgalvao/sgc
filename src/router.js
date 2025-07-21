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
    component: () => import("./views/CadProcesso.vue"),
  },
  {
    path: "/mapas",
    name: "Mapas",
    component: () => import("./views/Mapas.vue"),
  },
  {
    path: "/processos/:id/unidade/:unidadeId/atividades",
    name: "AtividadesConhecimentos",
    component: () => import("./views/CadAtividadesConhecimentos.vue"),
  },
  {
    path: "/processos/:id/unidades",
    name: "UnidadesProcesso",
    component: () => import("./views/DetalhesProcesso.vue"),
  },
  {
    path: "/unidade/:sigla",
    name: "DetalheUnidade",
    component: () => import("./views/DetalhesUnidade.vue"),
  },
  {
    path: "/unidade/:sigla/mapa",
    name: "MapaCompetencias",
    component: () => import("./views/CadastroMapa.vue"),
  },
  {
    path: "/unidade/:sigla/mapa/visualizar",
    name: "VisualizacaoMapa",
    component: () => import("./views/MapaVisualizacao.vue"),
  },
  {
    path: "/finalizacao-mapa",
    name: "FinalizacaoMapa",
    component: () => import("./views/MapaFinalizacao.vue"),
  },
  {
    path: "/unidade/:sigla/atribuir",
    name: "AtribuicaoTemporariaForm",
    component: () => import("./views/CadastroAtribuicao.vue"),
  },
  {
    path: "/historico",
    name: "Historico",
    component: () => import("./views/HistoricoProcessos.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

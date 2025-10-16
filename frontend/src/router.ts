import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { usePerfilStore } from '@/stores/perfil'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/Login.vue'),
    meta: { title: 'Login', public: true } // Rota pública
  },
  {
    path: '/',
    redirect: '/painel'
  },
  {
    path: '/painel',
    name: 'Painel',
    component: () => import('./views/Painel.vue'),
    meta: { title: 'Painel' }
  },
  // Adicione outras rotas aqui, elas serão protegidas por padrão
  {
    path: '/processo/cadastro',
    name: 'CadProcesso',
    component: () => import('./views/CadProcesso.vue'),
    meta: { title: 'Novo processo' }
  },
  {
    path: '/processo/:idProcesso',
    name: 'Processo',
    component: () => import('./views/Processo.vue'),
    props: true,
    meta: { title: 'Unidades do Processo' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade',
    name: 'Subprocesso',
    component: () => import('./views/Subprocesso.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      siglaUnidade: route.params.siglaUnidade
    }),
    meta: { title: 'Processos da Unidade' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/mapa',
    name: 'SubprocessoMapa',
    component: () => import('./views/CadMapa.vue'),
    props: (route) => ({
      sigla: route.params.siglaUnidade,
      idProcesso: Number(route.params.idProcesso)
    }),
    meta: { title: 'Mapa' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/vis-mapa',
    name: 'SubprocessoVisMapa',
    component: () => import('./views/VisMapa.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      sigla: route.params.siglaUnidade
    }),
    meta: { title: 'Visualização de Mapa' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/cadastro',
    name: 'SubprocessoCadastro',
    component: () => import('./views/CadAtividades.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      sigla: route.params.siglaUnidade
    }),
    meta: { title: 'Cadastro' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/vis-cadastro',
    name: 'SubprocessoVisCadastro',
    component: () => import('./views/VisAtividades.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      sigla: route.params.siglaUnidade
    }),
    meta: { title: 'Visualização de Atividades' }
  },

  {
    path: '/processo/:idProcesso/:siglaUnidade/diagnostico-equipe',
    name: 'DiagnosticoEquipe',
    component: () => import('./views/DiagnosticoEquipe.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      siglaUnidade: route.params.siglaUnidade
    }),
    meta: { title: 'Diagnóstico da Equipe' }
  },
  {
    path: '/processo/:idProcesso/:siglaUnidade/ocupacoes-criticas',
    name: 'OcupacoesCriticas',
    component: () => import('./views/OcupacoesCriticas.vue'),
    props: (route) => ({
      idProcesso: Number(route.params.idProcesso),
      siglaUnidade: route.params.siglaUnidade
    }),
    meta: { title: 'Ocupações Críticas' }
  },
  {
    path: '/unidade/:siglaUnidade',
    name: 'Unidade',
    component: () => import('./views/Unidade.vue'),
    props: true,
    meta: { title: 'Unidade' }
  },
  {
    path: '/unidade/:siglaUnidade/mapa',
    name: 'Mapa',
    component: () => import('./views/CadMapa.vue'),
    props: (route) => ({
      sigla: route.params.siglaUnidade,
      idProcesso: Number(route.query.idProcesso)
    }),
    meta: { title: 'Mapa' }
  },
  {
    path: '/unidade/:siglaUnidade/atribuicao',
    name: 'AtribuicaoTemporariaForm',
    component: () => import('./views/CadAtribuicao.vue'),
    props: (route) => ({ sigla: route.params.siglaUnidade }),
    meta: { title: 'Atribuição Temporária' }
  },
  {
    path: '/historico',
    name: 'Historico',
    component: () => import('./views/Historico.vue'),
    meta: { title: 'Histórico' }
  },
  {
    path: '/relatorios',
    name: 'Relatorios',
    component: () => import('./views/Relatorios.vue'),
    meta: { title: 'Relatórios' }
  },
  {
    path: '/configuracoes',
    name: 'Configuracoes',
    component: () => import('./views/Configuracoes.vue'),
    meta: { title: 'Configurações' }
  },
   // Rota de fallback para páginas não encontradas
   {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/painel'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guarda de navegação global
router.beforeEach((to, from, next) => {
  const perfilStore = usePerfilStore()
  const isPublic = to.matched.some((record) => record.meta.public)
  const isAuthenticated = perfilStore.estaAutenticado

  if (!isPublic && !isAuthenticated) {
    // Se a rota não é pública e o usuário não está autenticado,
    // redireciona para a página de login.
    return next({ name: 'Login' })
  }

  // Se o usuário está autenticado e tenta acessar a página de login,
  // redireciona para o painel.
  if (isAuthenticated && to.name === 'Login') {
    return next({ name: 'Painel' })
  }

  next()
})

router.afterEach((to) => {
  const titleBase = (to.meta.title as string) || 'SGC'
  document.title = `${titleBase} - SGC`
})

export default router
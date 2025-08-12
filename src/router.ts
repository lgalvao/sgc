import {createRouter, createWebHistory, RouteLocationNormalized} from 'vue-router';

const routes = [
    {
        path: '/',
        redirect: '/login',
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('./views/Login.vue'),
        meta: { title: 'Login', breadcrumb: false },
    },
    {
        path: '/painel',
        name: 'Painel',
        component: () => import('./views/Painel.vue'),
        props: true,
        meta: { title: 'Painel', breadcrumb: 'Painel' },
    },
    // Processo - Cadastro (novo padrão)
    {
        path: '/processo/cadastro',
        name: 'CadProcesso',
        component: () => import('./views/CadProcesso.vue'),
        meta: { title: 'Novo Processo', breadcrumb: 'Novo Processo' },
    },
    

    // NOVO PADRÃO: processo/:processoId[/:sigla/(mapa|cadastro)]
    {
        path: '/processo/:processoId',
        name: 'Processo',
        component: () => import('./views/Processo.vue'),
        props: true,
        meta: { title: 'Unidades do Processo', breadcrumb: 'Processo' },
    },
    {
        path: '/processo/:processoId/:sigla',
        name: 'ProcessoUnidade',
        component: () => import('./views/ProcessoUnidade.vue'),
        props: true,
        meta: { title: 'Processos da Unidade', breadcrumb: (route: RouteLocationNormalized) => `${route.params.sigla ?? ''}` },
    },

    // Páginas completas (não aninhadas) para subfuncionalidades da unidade no processo
    {
        path: '/processo/:processoId/:sigla/mapa',
        name: 'ProcessoUnidadeMapa',
        component: () => import('./views/CadMapa.vue'),
        props: (route: RouteLocationNormalized) => ({ sigla: route.params.sigla, processoId: route.params.processoId }),
        meta: { title: 'Mapa', breadcrumb: 'Mapa' },
    },
    {
        path: '/processo/:processoId/:sigla/cadastro',
        name: 'ProcessoUnidadeCadastro',
        component: () => import('./views/CadAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({ processoId: route.params.processoId, sigla: route.params.sigla }),
        meta: { title: 'Cadastro', breadcrumb: 'Cadastro' },
    },

    

    
    
    {
        path: '/unidade/:sigla',
        name: 'Unidade',
        component: () => import('./views/Unidade.vue'),
        meta: {
            title: 'Unidade',
            breadcrumb: (route: RouteLocationNormalized) => `${route.params.sigla ?? ''}`,
        }
    },
    
    
    {
        path: '/unidade/:sigla/mapa',
        name: 'Mapa',
        component: () => import('./views/CadMapa.vue'),
        props: (route: RouteLocationNormalized) => ({sigla: route.params.sigla, processoId: route.query.processoId}),
        meta: { title: 'Mapa', breadcrumb: 'Mapa' },
    },
    {
        path: '/unidade/:sigla/atribuicao',
        name: 'AtribuicaoTemporariaForm',
        component: () => import('./views/CadAtribuicao.vue'),
        meta: { title: 'Atribuição Temporária', breadcrumb: 'Atribuição' },
    },
    {
        path: '/historico',
        name: 'Historico',
        component: () => import('./views/HistoricoProcessos.vue'),
        meta: { title: 'Histórico', breadcrumb: 'Histórico' },
    },
    {
        path: '/relatorios',
        name: 'Relatorios',
        component: () => import('./views/Relatorios.vue'),
        meta: { title: 'Relatórios', breadcrumb: 'Relatórios' },
    },
    {
        path: '/configuracoes',
        name: 'Configuracoes',
        component: () => import('./views/Configuracoes.vue'),
        meta: { title: 'Configurações', breadcrumb: 'Configurações' },
    },

];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// Marcar navegação iniciada pela navbar e definir título da página
router.beforeEach((to, from, next) => {
    // Se veio da navbar, marca na sessão
    if (to.query && typeof to.query.fromNavbar !== 'undefined') {
        try { sessionStorage.setItem('cameFromNavbar', '1') } catch {}
    }
    // Ao sair do contexto de unidade (ou ir ao painel), limpa a marcação
    const leavingUnidadeContext = !(to.path.startsWith('/unidade/')) || to.path === '/painel'
    if (leavingUnidadeContext) {
        try { sessionStorage.removeItem('cameFromNavbar') } catch {}
    }
    next()
})

router.afterEach((to) => {
    const meta: any = to.meta || {}
    const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC'
    document.title = `${titleBase} - SGC`
})

export default router;
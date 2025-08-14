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
        meta: {title: 'Login', breadcrumb: false},
    },
    {
        path: '/painel',
        name: 'Painel',
        component: () => import('./views/Painel.vue'),
        props: true,
        meta: {title: 'Painel', breadcrumb: 'Painel'},
    },
    {
        path: '/processo/cadastro',
        name: 'CadProcesso',
        component: () => import('./views/CadProcesso.vue'),
        meta: {title: 'Novo Processo', breadcrumb: 'Novo Processo'},
    },
    {
        path: '/processo/:processoId',
        name: 'Processo',
        component: () => import('./views/Processo.vue'),
        props: true,
        meta: {title: 'Unidades do Processo', breadcrumb: 'Processo'},
    },
    {
        path: '/processo/:processoId/:sigla',
        name: 'ProcessoUnidade',
        component: () => import('./views/Subprocesso.vue'),
        props: true,
        meta: {
            title: 'Processos da Unidade',
            breadcrumb: (route: RouteLocationNormalized) => `${route.params.sigla ?? ''}`
        },
    },
    {
        path: '/processo/:processoId/:sigla/mapa',
        name: 'ProcessoUnidadeMapa',
        component: () => import('./views/CadMapa.vue'),
        props: (route: RouteLocationNormalized) => ({sigla: route.params.sigla, processoId: route.params.processoId}),
        meta: {title: 'Mapa', breadcrumb: 'Mapa'},
    },
    {
        path: '/processo/:processoId/:sigla/vis-mapa',
        name: 'ProcessoUnidadeVisMapa',
        component: () => import('./views/VisMapa.vue'),
        props: (route: RouteLocationNormalized) => ({processoId: route.params.processoId, sigla: route.params.sigla}),
        meta: {title: 'Visualização de Mapa', breadcrumb: 'Visualização de Mapa'},
    },
    {
        path: '/processo/:processoId/:sigla/cadastro',
        name: 'ProcessoUnidadeCadastro',
        component: () => import('./views/CadAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({processoId: route.params.processoId, sigla: route.params.sigla}),
        meta: {title: 'Cadastro', breadcrumb: 'Cadastro'},
    },
    {
        path: '/processo/:processoId/:sigla/vis-cadastro',
        name: 'ProcessoUnidadeVisCadastro',
        component: () => import('./views/VisAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({processoId: route.params.processoId, sigla: route.params.sigla}),
        meta: {title: 'Visualização de Atividades', breadcrumb: 'Visualização de Atividades'},
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
        meta: {title: 'Mapa', breadcrumb: 'Mapa'},
    },
    {
        path: '/unidade/:sigla/atribuicao',
        name: 'AtribuicaoTemporariaForm',
        component: () => import('./views/CadAtribuicao.vue'),
        meta: {title: 'Atribuição Temporária', breadcrumb: 'Atribuição'},
    },
    {
        path: '/historico',
        name: 'Historico',
        component: () => import('./views/Historico.vue'),
        meta: {title: 'Histórico', breadcrumb: 'Histórico'},
    },
    {
        path: '/relatorios',
        name: 'Relatorios',
        component: () => import('./views/Relatorios.vue'),
        meta: {title: 'Relatórios', breadcrumb: 'Relatórios'},
    },
    {
        path: '/configuracoes',
        name: 'Configuracoes',
        component: () => import('./views/Configuracoes.vue'),
        meta: {title: 'Configurações', breadcrumb: 'Configurações'},
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.afterEach((to) => {
    const meta: any = to.meta || {}
    const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC'
    document.title = `${titleBase} - SGC`
})

export default router;
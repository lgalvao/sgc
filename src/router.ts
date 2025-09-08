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
        path: '/processo/:idProcesso',
        name: 'Processo',
        component: () => import('./views/Processo.vue'),
        props: true,
        meta: {title: 'Unidades do Processo', breadcrumb: 'Processo'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade',
        name: 'Subprocesso',
        component: () => import('./views/Subprocesso.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            siglaUnidade: route.params.siglaUnidade
        }),
        meta: {
            title: 'Processos da Unidade',
            breadcrumb: (route: RouteLocationNormalized) => `${route.params.siglaUnidade ?? ''}`
        },
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/mapa',
        name: 'SubprocessoMapa',
        component: () => import('./views/CadMapa.vue'),
        props: (route: RouteLocationNormalized) => ({
            sigla: route.params.siglaUnidade,
            idProcesso: Number(route.params.idProcesso)
        }),
        meta: {title: 'Mapa', breadcrumb: 'Mapa'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/vis-mapa',
        name: 'SubprocessoVisMapa',
        component: () => import('./views/VisMapa.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Visualização de Mapa', breadcrumb: 'Visualização de Mapa'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/cadastro',
        name: 'SubprocessoCadastro',
        component: () => import('./views/CadAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Cadastro', breadcrumb: 'Cadastro'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/vis-cadastro',
        name: 'SubprocessoVisCadastro',
        component: () => import('./views/VisAtividades.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            sigla: route.params.siglaUnidade
        }),
        meta: {title: 'Visualização de Atividades', breadcrumb: 'Visualização de Atividades'},
    },

    {
        path: '/processo/:idProcesso/:siglaUnidade/diagnostico-equipe',
        name: 'DiagnosticoEquipe',
        component: () => import('./views/DiagnosticoEquipe.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            siglaUnidade: route.params.siglaUnidade
        }),
        meta: {title: 'Diagnóstico da Equipe', breadcrumb: 'Diagnóstico da Equipe'},
    },
    {
        path: '/processo/:idProcesso/:siglaUnidade/ocupacoes-criticas',
        name: 'OcupacoesCriticas',
        component: () => import('./views/OcupacoesCriticas.vue'),
        props: (route: RouteLocationNormalized) => ({
            idProcesso: Number(route.params.idProcesso),
            siglaUnidade: route.params.siglaUnidade
        }),
        meta: {title: 'Ocupações Críticas', breadcrumb: 'Ocupações Críticas'},
    },
    {
        path: '/unidade/:siglaUnidade',
        name: 'Unidade',
        component: () => import('./views/Unidade.vue'),
        props: true,
        meta: {
            title: 'Unidade',
            breadcrumb: (route: RouteLocationNormalized) => `${route.params.siglaUnidade ?? ''}`,
        }
    },
    {
        path: '/unidade/:siglaUnidade/mapa',
        name: 'Mapa',
        component: () => import('./views/CadMapa.vue'),
        props: (route: RouteLocationNormalized) => ({
            sigla: route.params.siglaUnidade,
            idProcesso: Number(route.query.idProcesso)
        }),
        meta: {title: 'Mapa', breadcrumb: 'Mapa'},
    },
    {
        path: '/unidade/:siglaUnidade/atribuicao',
        name: 'AtribuicaoTemporariaForm',
        component: () => import('./views/CadAtribuicao.vue'),
        props: (route: RouteLocationNormalized) => ({sigla: route.params.siglaUnidade}),
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
    const meta = to.meta || {}
    const titleBase = typeof meta.title === 'string' ? meta.title : (to.name as string) || 'SGC'
    document.title = `${titleBase} - SGC`
})

export default router;
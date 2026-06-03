import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

function lerCodSubprocesso(route: RouteLocationNormalized): number | undefined {
    const valor = route.query?.codSubprocesso;
    if (typeof valor !== "string") {
        return undefined;
    }

    const codigo = Number(valor);
    return Number.isFinite(codigo) ? codigo : undefined;
}

const processoRoutes: RouteRecordRaw[] = [
    {
        path: "/processo/cadastro",
        name: "CadProcesso",
        component: () => import("@/views/ProcessoCadastroView.vue"),
        meta: {title: "Novo processo"},
    },
    {
        path: "/processo/:codProcesso",
        name: "Processo",
        component: () => import("@/views/ProcessoDetalheView.vue"),
        props: true,
        meta: {
            title: "Unidades do Processo",
            breadcrumb: "Detalhes do processo",
            keepAlive: true,
        },
    },
    {
        path: "/processo/:codProcesso/:siglaUnidade",
        name: "Subprocesso",
        component: () => import("@/views/SubprocessoView.vue"),
        props: (route: RouteLocationNormalized) => ({
            codProcesso: Number(route.params.codProcesso),
            siglaUnidade: route.params.siglaUnidade,
            codSubprocesso: lerCodSubprocesso(route),
        }),
        meta: {
            title: "Processos da Unidade",
            breadcrumb: (route: RouteLocationNormalized) => String(route.params.siglaUnidade),
            keepAlive: true,
        },
    },
    {
        path: "/processo/:codProcesso/:siglaUnidade/mapa",
        name: "SubprocessoMapa",
        component: () => import("@/views/MapaView.vue"),
        props: (route: RouteLocationNormalized) => ({
            sigla: route.params.siglaUnidade,
            codProcesso: Number(route.params.codProcesso),
            codSubprocesso: lerCodSubprocesso(route),
        }),
        meta: {title: "Mapa"},
    },
    {
        path: "/processo/:codProcesso/:siglaUnidade/cadastro",
        name: "SubprocessoCadastro",
        component: () => import("@/views/CadastroView.vue"),
        props: (route: RouteLocationNormalized) => ({
            codProcesso: Number(route.params.codProcesso),
            sigla: route.params.siglaUnidade,
            codSubprocesso: lerCodSubprocesso(route),
        }),
        meta: {title: "Cadastro"},
    },
];

export default processoRoutes;

import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

const unidadeRoutes: RouteRecordRaw[] = [
    {
        path: "/unidade/:codUnidade",
        name: "Unidade",
        component: () => import("@/views/UnidadeView.vue"),
        props: (route: RouteLocationNormalized) => ({
            codUnidade: Number(route.params.codUnidade),
        }),
        meta: {
            title: "Unidade",
            breadcrumb: (route: RouteLocationNormalized) =>
                `${route.params.codUnidade ?? ""}`,
        },
    },
    {
        path: "/unidade/:codUnidade/mapa",
        name: "Mapa",
        component: () => import("@/views/CadMapa.vue"),
        props: (route: RouteLocationNormalized) => ({
            codUnidade: Number(route.params.codUnidade),
            codProcesso: Number(route.query.codProcesso),
        }),
        meta: {title: "Mapa"},
    },
    {
        path: "/unidade/:codUnidade/atribuicao",
        name: "AtribuicaoTemporariaForm",
        component: () => import("@/views/CadAtribuicao.vue"),
        props: (route: RouteLocationNormalized) => ({
            codUnidade: Number(route.params.codUnidade),
        }),
        meta: {title: "Atribuição Temporária"},
    },
];

export default unidadeRoutes;

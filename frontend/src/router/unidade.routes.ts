import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

const unidadeRoutes: RouteRecordRaw[] = [
    {
        path: "/unidades",
        name: "Unidades",
        component: () => import("@/views/UnidadesView.vue"),
        meta: {
            title: "Unidades",
            breadcrumb: "Unidades",
        },
    },
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
        component: () => import("@/views/MapaView.vue"),
        props: (route: RouteLocationNormalized) => ({
            codUnidade: Number(route.params.codUnidade),
            codProcesso: Number(route.query.codProcesso),
        }),
        meta: {title: "Mapa"},
    },
    {
        path: "/unidade/:codUnidade/atribuicao",
        name: "AtribuicaoTemporariaForm",
        component: () => import("@/views/AtribuicaoTemporariaView.vue"),
        props: (route: RouteLocationNormalized) => ({
            codUnidade: Number(route.params.codUnidade),
        }),
        meta: {title: "Atribuição Temporária"},
    },
];

export default unidadeRoutes;

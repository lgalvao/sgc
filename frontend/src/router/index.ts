import type {RouteRecordRaw} from "vue-router";
import {createMemoryHistory, createRouter, createWebHistory,} from "vue-router";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";
import mainRoutes from "./main.routes";
import processoRoutes from "./processo.routes";
import unidadeRoutes from "./unidade.routes";
import diagnosticoRoutes from "./diagnostico.routes";

const routes: RouteRecordRaw[] = [
    ...mainRoutes,
    ...processoRoutes,
    ...unidadeRoutes,
    ...diagnosticoRoutes,
];

const isTest = import.meta.env?.VITEST || (typeof process !== "undefined" && process.env?.VITEST);

const router = createRouter({
    history: isTest
        ? createMemoryHistory()
        : createWebHistory(),
    routes,
});

router.beforeEach((to) => {
    const perfilStore = usePerfilStore();
    const isAuthenticated = perfilStore.usuarioCodigo;
    const publicPages = ["/login", "/erro"];
    const authRequired = !publicPages.includes(to.path);

    if (authRequired && !isAuthenticated) {
        return "/login";
    }

    if (to.path === "/relatorios/unidades-sem-mapas-vigentes" && perfilStore.perfilSelecionado !== Perfil.ADMIN) {
        return "/painel";
    }

    if (to.path.startsWith("/relatorios") && perfilStore.perfilSelecionado !== Perfil.ADMIN && perfilStore.perfilSelecionado !== Perfil.GESTOR) {
        return "/painel";
    }

    if (to.path.startsWith("/administracao") && perfilStore.perfilSelecionado !== Perfil.ADMIN) {
        return "/painel";
    }

    return true;
});

router.afterEach((to) => {
    const meta = to.meta || {};
    const titleBase = typeof meta.title === "string" ? meta.title : String(to.name || "SGC");
    document.title = `${titleBase} - SGC`;
});

export default router;

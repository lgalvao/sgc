import {beforeEach, describe, expect, it} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import router from "../index";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";

describe("Router", () => {
    let perfilStore: any;

    beforeEach(async () => {
        setActivePinia(createPinia());
        perfilStore = usePerfilStore();
        perfilStore.usuarioCodigo = null;
        // Garantir que o router comece em um estado conhecido
        await router.push("/");
    });

    it("redireciona para login se não autenticado", async () => {
        const to = { path: "/painel" };
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("permite acesso se autenticado", async () => {
        perfilStore.usuarioCodigo = "123";
        const to = { path: "/painel" };
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/painel");
    });

    it("redireciona para painel se autenticado sem perfil ADMIN em rota administrativa", async () => {
        perfilStore.usuarioCodigo = "123";
        perfilStore.perfilSelecionado = Perfil.GESTOR;
        await router.push("/administracao/limpeza-processos");
        expect(router.currentRoute.value.path).toBe("/painel");
    });

    it("permite rota administrativa para ADMIN autenticado", async () => {
        perfilStore.usuarioCodigo = "123";
        perfilStore.perfilSelecionado = Perfil.ADMIN;
        await router.push("/administracao/limpeza-processos");
        expect(router.currentRoute.value.path).toBe("/administracao/limpeza-processos");
    });

    it("permite acesso a páginas públicas sem autenticação", async () => {
        const to = { path: "/login" };
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("atualiza o título da página no afterEach", async () => {
        perfilStore.usuarioCodigo = "123";
        await router.push({ name: "Painel" });
        expect(document.title).toBe("Painel - SGC");
    });
});

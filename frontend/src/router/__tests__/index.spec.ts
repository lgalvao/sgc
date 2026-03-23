import {beforeEach, describe, expect, it, vi} from "vitest";
import router from "../index";
import {usePerfilStore} from "@/stores/perfil";

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: vi.fn(),
}));

describe("Router", () => {
    let perfilStore: any;

    beforeEach(() => {
        vi.clearAllMocks();
        perfilStore = {
            usuarioCodigo: null,
        };
        vi.mocked(usePerfilStore).mockReturnValue(perfilStore);
    });

    it("redireciona para login se não autenticado", async () => {
        const to = { path: "/painel", meta: {} };
        await router.push("/login"); // Reset state
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("permite acesso se autenticado", async () => {
        perfilStore.usuarioCodigo = 123;
        const to = { path: "/painel", meta: {} };
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/painel");
    });

    it("permite acesso a páginas públicas sem autenticação", async () => {
        const to = { path: "/login", meta: {} };
        await router.push(to as any);
        expect(router.currentRoute.value.path).toBe("/login");
    });

    it("atualiza o título da página no afterEach", async () => {
        perfilStore.usuarioCodigo = 123;
        await router.push({ name: "Painel" });
        expect(document.title).toBe("Painel - SGC");

        // Simulating afterEach directly since we want to check title logic specifically
        // but it's better to just use router and mock routes if possible.
        // For title, we already verified one case.
    });
});

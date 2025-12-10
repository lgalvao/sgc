import {describe, expect, it} from "vitest";
import mainRoutes from "../main.routes";

describe("main.routes", () => {
    it("deve exportar um array de rotas", () => {
        expect(Array.isArray(mainRoutes)).toBe(true);
        expect(mainRoutes).toHaveLength(6);
    });

    it("deve redirecionar da raiz para /login", () => {
        const route = mainRoutes.find((r) => r.path === "/");
        expect(route).toBeDefined();
        expect(route?.redirect).toBe("/login");
    });

    it("deve conter a rota Login", async () => {
        const route = mainRoutes.find((r) => r.name === "Login");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/login");
        expect(route?.meta?.title).toBe("Login");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Painel", async () => {
        const route = mainRoutes.find((r) => r.name === "Painel");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/painel");
        expect(route?.meta?.title).toBe("Painel");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Historico", async () => {
        const route = mainRoutes.find((r) => r.name === "Historico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/historico");
        expect(route?.meta?.title).toBe("Histórico");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Relatorios", async () => {
        const route = mainRoutes.find((r) => r.name === "Relatorios");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/relatorios");
        expect(route?.meta?.title).toBe("Relatórios");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Configuracoes", async () => {
        const route = mainRoutes.find((r) => r.name === "Configuracoes");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/configuracoes");
        expect(route?.meta?.title).toBe("Configurações");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });
});

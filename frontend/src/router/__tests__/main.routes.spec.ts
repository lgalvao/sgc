import {describe, expect, it, vi} from "vitest";
import mainRoutes from "../main.routes";

vi.mock('@/views/LoginView.vue', () => ({ default: { name: 'LoginView' } }));
vi.mock('@/views/PainelView.vue', () => ({ default: { name: 'PainelView' } }));
vi.mock('@/views/HistoricoView.vue', () => ({ default: { name: 'HistoricoView' } }));
vi.mock('@/views/RelatoriosView.vue', () => ({ default: { name: 'RelatoriosView' } }));
vi.mock('@/views/ParametrosView.vue', () => ({ default: { name: 'ParametrosView' } }));
vi.mock('@/views/AdministradoresView.vue', () => ({ default: { name: 'AdministradoresView' } }));
describe("main.routes", () => {
    it("deve exportar um array de rotas", () => {
        expect(Array.isArray(mainRoutes)).toBe(true);
        expect(mainRoutes).toHaveLength(7);
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
    }, 30000);

    it("deve conter a rota Painel", async () => {
        const route = mainRoutes.find((r) => r.name === "Painel");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/painel");
        expect(route?.meta?.title).toBe("Painel");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    }, 30000);

    it("deve conter a rota Historico", async () => {
        const route = mainRoutes.find((r) => r.name === "Historico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/historico");
        expect(route?.meta?.title).toBe("Histórico");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    }, 30000);

    it("deve conter a rota Relatorios", async () => {
        const route = mainRoutes.find((r) => r.name === "Relatorios");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/relatorios");
        expect(route?.meta?.title).toBe("Relatórios");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    }, 30000);

    it("deve conter a rota Parametros", async () => {
        const route = mainRoutes.find((r) => r.name === "Parametros");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/parametros");
        expect(route?.meta?.title).toBe("Parâmetros");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    }, 30000);

    it("deve conter a rota Administradores", async () => {
        const route = mainRoutes.find((r) => r.name === "Administradores");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/administradores");
        expect(route?.meta?.title).toBe("Administradores");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    }, 30000);
});

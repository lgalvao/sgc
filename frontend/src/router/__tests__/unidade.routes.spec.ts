import {describe, expect, it, vi} from "vitest";
import unidadeRoutes from "../unidade.routes";
import type {RouteLocationNormalized} from "vue-router";

// Mock views to avoid loading real components which might have dependencies causing timeouts
vi.mock("@/views/unidade/UnidadesView.vue", () => ({default: {name: 'UnidadesView'}}));
vi.mock("@/views/unidade/UnidadeDetalheView.vue", () => ({default: {name: 'UnidadeDetalheView'}}));
vi.mock("@/views/processo/MapaCadastroView.vue", () => ({default: {name: 'MapaCadastroView'}}));
vi.mock("@/views/unidade/AtribuicaoTemporariaView.vue", () => ({default: {name: 'AtribuicaoTemporariaView'}}));

describe("unidade.routes", () => {
    it("deve exportar um array de rotas", () => {
        expect(Array.isArray(unidadeRoutes)).toBe(true);
        expect(unidadeRoutes).toHaveLength(4);
    });

    it("deve conter a rota Unidades", async () => {
        const route = unidadeRoutes.find((r) => r.name === "Unidades");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/unidades");
        expect(route?.meta?.title).toBe("Unidades");
        expect(route?.meta?.breadcrumb).toBe("Unidades");

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Unidade e mapear props/meta corretamente", async () => {
        const route = unidadeRoutes.find((r) => r.name === "Unidade");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/unidade/:codUnidade");

        const mockRoute = {
            params: {codUnidade: "123"}
        } as unknown as RouteLocationNormalized;

        if (typeof route?.props === "function") {
            const props = route.props(mockRoute);
            expect(props).toEqual({codUnidade: 123});
        } else {
            expect.fail("props deve ser uma função");
        }

        if (typeof route?.meta?.breadcrumb === "function") {
            expect(route.meta.breadcrumb(mockRoute)).toBe("123");
        } else {
            expect.fail("meta.breadcrumb deve ser uma função");
        }

        // Testar breadcrumb com valor nulo
        const mockRouteNull = {
            params: {}
        } as unknown as RouteLocationNormalized;
        if (typeof route?.meta?.breadcrumb === "function") {
            expect(route.meta.breadcrumb(mockRouteNull)).toBe("");
        }

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota Mapa e mapear props corretamente (query params)", async () => {
        const route = unidadeRoutes.find((r) => r.name === "Mapa");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/unidade/:codUnidade/mapa");

        const mockRoute = {
            params: {codUnidade: "123"},
            query: {codProcesso: "456"}
        } as unknown as RouteLocationNormalized;

        if (typeof route?.props === "function") {
            const props = route.props(mockRoute);
            expect(props).toEqual({codUnidade: 123, codProcesso: 456});
        } else {
            expect.fail("props deve ser uma função");
        }

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota AtribuicaoTemporariaForm e mapear props corretamente", async () => {
        const route = unidadeRoutes.find((r) => r.name === "AtribuicaoTemporariaForm");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/unidade/:codUnidade/atribuicao");

        const mockRoute = {
            params: {codUnidade: "123"}
        } as unknown as RouteLocationNormalized;

        if (typeof route?.props === "function") {
            const props = route.props(mockRoute);
            expect(props).toEqual({codUnidade: 123});
        } else {
            expect.fail("props deve ser uma função");
        }

        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });
});

import { describe, it, expect } from "vitest";
import unidadeRoutes from "../unidade.routes";
import type { RouteLocationNormalized } from "vue-router";

describe("unidade.routes", () => {
  it("deve exportar um array de rotas", () => {
    expect(Array.isArray(unidadeRoutes)).toBe(true);
    expect(unidadeRoutes).toHaveLength(3);
  });

  it("deve conter a rota Unidade e mapear props/meta corretamente", async () => {
    const route = unidadeRoutes.find((r) => r.name === "Unidade");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/unidade/:codUnidade");

    const mockRoute = {
      params: { codUnidade: "123" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ codUnidade: 123 });
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
      const component = await route.component();
      expect(component.default).toBeDefined();
    }
  });

  it("deve conter a rota Mapa e mapear props corretamente (query params)", async () => {
    const route = unidadeRoutes.find((r) => r.name === "Mapa");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/unidade/:codUnidade/mapa");

    const mockRoute = {
      params: { codUnidade: "123" },
      query: { codProcesso: "456" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ codUnidade: 123, codProcesso: 456 });
    } else {
        expect.fail("props deve ser uma função");
    }

    if (typeof route?.component === "function") {
      const component = await route.component();
      expect(component.default).toBeDefined();
    }
  });

  it("deve conter a rota AtribuicaoTemporariaForm e mapear props corretamente", async () => {
    const route = unidadeRoutes.find((r) => r.name === "AtribuicaoTemporariaForm");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/unidade/:codUnidade/atribuicao");

    const mockRoute = {
      params: { codUnidade: "123" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ codUnidade: 123 });
    } else {
        expect.fail("props deve ser uma função");
    }

    if (typeof route?.component === "function") {
      const component = await route.component();
      expect(component.default).toBeDefined();
    }
  });
});

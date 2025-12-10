import { describe, it, expect } from "vitest";
import processoRoutes from "../processo.routes";
import type { RouteLocationNormalized } from "vue-router";

describe("processo.routes", () => {
  it("deve exportar um array de rotas", () => {
    expect(Array.isArray(processoRoutes)).toBe(true);
    expect(processoRoutes).toHaveLength(7);
  });

  it("deve conter a rota CadProcesso", () => {
    const route = processoRoutes.find((r) => r.name === "CadProcesso");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/processo/cadastro");
    expect(route?.meta?.title).toBe("Novo Processo");
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota Processo", () => {
    const route = processoRoutes.find((r) => r.name === "Processo");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/processo/:codProcesso");
    expect(route?.props).toBe(true);
    expect(route?.meta?.title).toBe("Unidades do Processo");
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota Subprocesso e mapear props corretamente", () => {
    const route = processoRoutes.find((r) => r.name === "Subprocesso");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/processo/:codProcesso/:siglaUnidade");
    
    // Testar props function
    const mockRoute = {
      params: { codProcesso: "123", siglaUnidade: "DTI" }
    } as unknown as RouteLocationNormalized;
    
    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ codProcesso: 123, siglaUnidade: "DTI" });
    } else {
        expect.fail("props deve ser uma função");
    }
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota SubprocessoMapa e mapear props corretamente", () => {
    const route = processoRoutes.find((r) => r.name === "SubprocessoMapa");
    expect(route).toBeDefined();
    expect(route?.path).toBe("/processo/:codProcesso/:siglaUnidade/mapa");

    const mockRoute = {
      params: { codProcesso: "123", siglaUnidade: "DTI" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ sigla: "DTI", codProcesso: 123 });
    } else {
        expect.fail("props deve ser uma função");
    }
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota SubprocessoVisMapa e mapear props corretamente", () => {
    const route = processoRoutes.find((r) => r.name === "SubprocessoVisMapa");
    expect(route).toBeDefined();
    
    const mockRoute = {
      params: { codProcesso: "123", siglaUnidade: "DTI" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
      const props = route.props(mockRoute);
      expect(props).toEqual({ codProcesso: 123, sigla: "DTI" });
    } else {
        expect.fail("props deve ser uma função");
    }
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota SubprocessoCadastro e mapear props corretamente", () => {
    const route = processoRoutes.find((r) => r.name === "SubprocessoCadastro");
    expect(route).toBeDefined();

    const mockRoute = {
      params: { codProcesso: "123", siglaUnidade: "DTI" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
        const props = route.props(mockRoute);
        expect(props).toEqual({ codProcesso: 123, sigla: "DTI" });
    } else {
        expect.fail("props deve ser uma função");
    }
    expect(typeof route?.component).toBe("function");
  });

  it("deve conter a rota SubprocessoVisCadastro e mapear props corretamente", () => {
    const route = processoRoutes.find((r) => r.name === "SubprocessoVisCadastro");
    expect(route).toBeDefined();

    const mockRoute = {
        params: { codProcesso: "123", siglaUnidade: "DTI" }
    } as unknown as RouteLocationNormalized;

    if (typeof route?.props === "function") {
        const props = route.props(mockRoute);
        expect(props).toEqual({ codProcesso: 123, sigla: "DTI" });
    } else {
        expect.fail("props deve ser uma função");
    }
    expect(typeof route?.component).toBe("function");
  });
});
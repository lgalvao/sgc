import {describe, expect, it} from "vitest";
import diagnosticoRoutes from "../diagnostico.routes";

describe("diagnostico.routes", () => {
    it("deve exportar um array de rotas", () => {
        expect(Array.isArray(diagnosticoRoutes)).toBe(true);
        expect(diagnosticoRoutes).toHaveLength(4);
    });

    it("deve conter a rota AutoavaliacaoDiagnostico", async () => {
        const route = diagnosticoRoutes.find((r) => r.name === "AutoavaliacaoDiagnostico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao");
        expect(route?.props).toBe(true);
        expect(route?.meta?.title).toBe("Autoavaliação");

        // Verificar componente lazy loading
        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota MonitoramentoDiagnostico", async () => {
        const route = diagnosticoRoutes.find((r) => r.name === "MonitoramentoDiagnostico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/diagnostico/:codSubprocesso/monitoramento");
        expect(route?.props).toBe(true);
        expect(route?.meta?.title).toBe("Monitoramento de Diagnóstico");

        // Verificar componente lazy loading
        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota OcupacoesCriticasDiagnostico", async () => {
        const route = diagnosticoRoutes.find((r) => r.name === "OcupacoesCriticasDiagnostico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/diagnostico/:codSubprocesso/ocupacoes");
        expect(route?.props).toBe(true);
        expect(route?.meta?.title).toBe("Ocupações Críticas");

        // Verificar componente lazy loading
        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });

    it("deve conter a rota ConclusaoDiagnostico", async () => {
        const route = diagnosticoRoutes.find((r) => r.name === "ConclusaoDiagnostico");
        expect(route).toBeDefined();
        expect(route?.path).toBe("/diagnostico/:codSubprocesso/conclusao");
        expect(route?.props).toBe(true);
        expect(route?.meta?.title).toBe("Conclusão de Diagnóstico");

        // Verificar componente lazy loading
        if (typeof route?.component === "function") {
            const component = await (route.component as () => Promise<any>)();
            expect(component.default).toBeDefined();
        }
    });
});

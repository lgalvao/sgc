import {describe, expect, it, vi} from "vitest";
import {useMapasStore} from "../mapas";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import * as subprocessoService from "@/services/subprocessoService";

vi.mock("@/services/subprocessoService", () => ({
    obterMapaCompleto: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

describe("mapas store", () => {
    const context = setupStoreTest(useMapasStore);

    it("invalidar deve preservar o snapshot principal, mas marcar o mapa como inválido", () => {
        context.store.definirMapaCompleto(10, {
            codigo: 1,
            subprocessoCodigo: 10,
            observacoes: "snapshot",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        } as any);

        context.store.invalidar(10);

        expect(context.store.obterMapaCompletoCache(10)).toEqual(expect.objectContaining({
            subprocessoCodigo: 10,
            observacoes: "snapshot",
        }));
        expect(context.store.dadosMapaValidos(10)).toBe(false);
        expect(context.store.mapaCompleto).toEqual(expect.objectContaining({
            subprocessoCodigo: 10,
        }));
    });

    it("garantirMapaCompleto deve voltar a buscar após invalidação explícita", async () => {
        vi.mocked(subprocessoService.obterMapaCompleto)
            .mockResolvedValueOnce({
                codigo: 1,
                subprocessoCodigo: 10,
                observacoes: "antes",
                competencias: [],
                atividades: [],
                situacao: "EM_ANDAMENTO",
            } as any)
            .mockResolvedValueOnce({
                codigo: 1,
                subprocessoCodigo: 10,
                observacoes: "depois",
                competencias: [],
                atividades: [],
                situacao: "EM_ANDAMENTO",
            } as any);

        await context.store.garantirMapaCompleto(10);
        context.store.invalidar(10);
        const resultado = await context.store.garantirMapaCompleto(10);

        expect(subprocessoService.obterMapaCompleto).toHaveBeenCalledTimes(2);
        expect(resultado.observacoes).toBe("depois");
        expect(context.store.dadosMapaValidos(10)).toBe(true);
    });

    it("invalidarImpacto deve limpar apenas o impacto atual", () => {
        context.store.definirImpactoMapa(10, {
            temImpactos: true,
            totalAtividadesInseridas: 1,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        } as any);

        context.store.invalidarImpacto(10);

        expect(context.store.impactoMapa).toBeNull();
        expect(context.store.dadosImpactoValidos(10)).toBe(false);
    });

    it("resetar deve limpar completamente o estado", () => {
        context.store.definirMapaCompleto(10, {
            codigo: 1,
            subprocessoCodigo: 10,
            observacoes: "snapshot",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        } as any);
        context.store.definirImpactoMapa(10, {
            temImpactos: true,
            totalAtividadesInseridas: 1,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        } as any);

        context.store.resetar();

        expect(context.store.mapaCompleto).toBeNull();
        expect(context.store.impactoMapa).toBeNull();
        expect(context.store.obterMapaCompletoCache(10)).toBeNull();
        expect(context.store.obterImpactoMapaCache(10)).toBeNull();
        expect(context.store.dadosMapaValidos(10)).toBe(false);
        expect(context.store.dadosImpactoValidos(10)).toBe(false);
    });
});

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

    it("invalidar deve preservar o último mapa carregado, mas marcar o dado para atualização", () => {
        context.store.sincronizarMapa(10, {
            codigo: 1,
            subprocessoCodigo: 10,
            observacoes: "mapa local",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        } as any);

        context.store.invalidar(10);

        expect(context.store.obterMapa(10)).toEqual(expect.objectContaining({
            subprocessoCodigo: 10,
            observacoes: "mapa local",
        }));
        expect(context.store.mapaDisponivel(10)).toBe(false);
        expect(context.store.mapaCompleto).toEqual(expect.objectContaining({
            subprocessoCodigo: 10,
        }));
    });

    it("carregarMapa deve voltar a buscar após invalidação explícita", async () => {
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

        await context.store.carregarMapa(10);
        context.store.invalidar(10);
        const resultado = await context.store.carregarMapa(10);

        expect(subprocessoService.obterMapaCompleto).toHaveBeenCalledTimes(2);
        expect(resultado.observacoes).toBe("depois");
        expect(context.store.mapaDisponivel(10)).toBe(true);
    });

    it("invalidarImpacto deve limpar apenas o impacto atual", () => {
        context.store.sincronizarImpacto(10, {
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
        expect(context.store.impactoDisponivel(10)).toBe(false);
    });

    it("carregarImpacto deve reutilizar o resultado atual sem re-fetch quando ainda estiver disponível", async () => {
        const impacto = {
            temImpactos: false,
            totalAtividadesInseridas: 0,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        } as any;
        vi.mocked(subprocessoService.verificarImpactosMapa).mockResolvedValue(impacto);

        await context.store.carregarImpacto(10);
        await context.store.carregarImpacto(10);

        expect(subprocessoService.verificarImpactosMapa).toHaveBeenCalledTimes(1);
    });

    it("carregarImpacto deve re-buscar após invalidação", async () => {
        const impactoA = {temImpactos: false, totalAtividadesInseridas: 0, totalAtividadesRemovidas: 0, totalAtividadesAlteradas: 0, totalCompetenciasImpactadas: 0, atividadesInseridas: [], atividadesRemovidas: [], atividadesAlteradas: [], competenciasImpactadas: []} as any;
        const impactoB = {temImpactos: true, totalAtividadesInseridas: 1, totalAtividadesRemovidas: 0, totalAtividadesAlteradas: 0, totalCompetenciasImpactadas: 0, atividadesInseridas: [], atividadesRemovidas: [], atividadesAlteradas: [], competenciasImpactadas: []} as any;
        vi.mocked(subprocessoService.verificarImpactosMapa)
            .mockResolvedValueOnce(impactoA)
            .mockResolvedValueOnce(impactoB);

        await context.store.carregarImpacto(10);
        context.store.invalidarImpacto(10);
        const resultado = await context.store.carregarImpacto(10);

        expect(subprocessoService.verificarImpactosMapa).toHaveBeenCalledTimes(2);
        expect(resultado.temImpactos).toBe(true);
    });

    it("resetar deve limpar completamente o estado", () => {
        context.store.sincronizarMapa(10, {
            codigo: 1,
            subprocessoCodigo: 10,
            observacoes: "mapa local",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        } as any);
        context.store.sincronizarImpacto(10, {
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
        expect(context.store.obterMapa(10)).toBeNull();
        expect(context.store.obterImpacto(10)).toBeNull();
        expect(context.store.mapaDisponivel(10)).toBe(false);
        expect(context.store.impactoDisponivel(10)).toBe(false);
    });
});

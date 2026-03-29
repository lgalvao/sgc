import {beforeEach, describe, expect, it, vi} from "vitest";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";

vi.mock("@/services/subprocessoService", () => ({
    obterMapaCompleto: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

describe("useMapas", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve inicializar com valores nulos", async () => {
        const {useMapas} = await import("../useMapas");
        const mapas = useMapas();

        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapas.impactoMapa.value).toBeNull();
    });

    it("deve buscar mapa completo com sucesso", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "teste",
            competencias: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.buscarMapaCompleto(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledWith(1);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
    });

    it("deve definir erro em caso de falha ao buscar mapa completo", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        vi.mocked(service.obterMapaCompleto).mockRejectedValue(new Error("Failed"));

        await mapas.buscarMapaCompleto(1);

        expect(mapas.erro.value).toBe("Failed");
    });

    it("deve buscar impacto do mapa com sucesso", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        const mockImpacto: ImpactoMapa = {
            temImpactos: true,
            totalAtividadesInseridas: 0,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };
        vi.mocked(service.verificarImpactosMapa).mockResolvedValue(mockImpacto);

        await mapas.buscarImpactoMapa(1);

        expect(service.verificarImpactosMapa).toHaveBeenCalledWith(1);
        expect(mapas.impactoMapa.value).toEqual(mockImpacto);
    });

    it("não deve buscar impacto se codSubprocesso for zero", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();

        await mapas.buscarImpactoMapa(0);

        expect(service.verificarImpactosMapa).not.toHaveBeenCalled();
    });

});

import {beforeEach, describe, expect, it, vi} from "vitest";
import type {ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from "@/types/tipos";

vi.mock("@/services/subprocessoService", () => ({
    obterMapaCompleto: vi.fn(),
    obterMapaAjuste: vi.fn(),
    verificarImpactosMapa: vi.fn(),
    obterMapaVisualizacao: vi.fn(),
    verificarMapaVigente: vi.fn(),
}));

describe("useMapas", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve inicializar com valores nulos", async () => {
        const {useMapas} = await import("../useMapas");
        const mapas = useMapas();

        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapas.mapaAjuste.value).toBeNull();
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

    it("deve buscar mapa ajuste com sucesso", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        const mockMapa: MapaAjuste = {
            codigo: 1,
            descricao: "teste",
            competencias: [],
        };
        vi.mocked(service.obterMapaAjuste).mockResolvedValue(mockMapa);

        await mapas.buscarMapaAjuste(1);

        expect(service.obterMapaAjuste).toHaveBeenCalledWith(1);
        expect(mapas.mapaAjuste.value).toEqual(mockMapa);
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

    it("deve buscar mapa de visualizacao com sucesso", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        const mockMapa: MapaVisualizacao = {
            codigo: 1,
            descricao: "Teste",
            competencias: [],
        };
        vi.mocked(service.obterMapaVisualizacao).mockResolvedValue(mockMapa);

        await mapas.buscarMapaVisualizacao(1);

        expect(service.obterMapaVisualizacao).toHaveBeenCalledWith(1);
        expect(mapas.mapaVisualizacao.value).toEqual(mockMapa);
    });

    it("deve retornar false quando verificar mapa vigente falhar", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        vi.mocked(service.verificarMapaVigente).mockRejectedValue(new Error("Falha"));

        await expect(mapas.temMapaVigente(10)).resolves.toBe(false);
    });

    it("não deve buscar mapa visualizacao se codSubprocesso for zero", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();

        await mapas.buscarMapaVisualizacao(0);

        expect(service.obterMapaVisualizacao).not.toHaveBeenCalled();
    });

    it("não deve buscar impacto se codSubprocesso for zero", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();

        await mapas.buscarImpactoMapa(0);

        expect(service.verificarImpactosMapa).not.toHaveBeenCalled();
    });

    it("deve lidar com falha silenciosa no buscarMapaVisualizacao", async () => {
        const {useMapas} = await import("../useMapas");
        const service = await import("@/services/subprocessoService");
        const mapas = useMapas();
        vi.mocked(service.obterMapaVisualizacao).mockRejectedValue(new Error("Erro silencioso"));

        await mapas.buscarMapaVisualizacao(1);

        expect(mapas.erro.value).toBe("Erro silencioso");
    });
});

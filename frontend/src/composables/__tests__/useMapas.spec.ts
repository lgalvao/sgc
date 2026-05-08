import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import * as service from "@/services/subprocessoService";
import {useMapas} from "../useMapas";
import {useMapasStore} from "@/stores/mapas";

vi.mock("@/services/subprocessoService", () => ({
    obterMapaCompleto: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

describe("useMapas", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        setActivePinia(createPinia());
    });

    it("deve inicializar com valores nulos", async () => {
        const mapas = useMapas();

        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapas.impactoMapa.value).toBeNull();
    });

    it("deve buscar mapa completo com sucesso", async () => {
        const mapas = useMapas();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "teste",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.buscarMapaCompleto(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledWith(1);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
    });

    it("deve reaproveitar o cache do mapa por subprocesso ao reativar a view", async () => {
        const mapas = useMapas();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "teste",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.buscarMapaCompleto(1);
        await mapas.buscarMapaCompleto(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledTimes(1);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
    });

    it("deve definir erro em caso de falha ao buscar mapa completo", async () => {
        const mapas = useMapas();
        vi.mocked(service.obterMapaCompleto).mockRejectedValue(new Error("Failed"));

        await mapas.buscarMapaCompleto(1);

        expect(mapas.erro.value).toBe("Failed");
    });

    it("deve buscar impacto do mapa com sucesso", async () => {
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

    it("deve recalcular o impacto do mapa a cada nova busca", async () => {
        const mapas = useMapas();
        const primeiroImpacto: ImpactoMapa = {
            temImpactos: true,
            totalAtividadesInseridas: 1,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };
        const segundoImpacto: ImpactoMapa = {
            ...primeiroImpacto,
            totalAtividadesInseridas: 2,
        };
        vi.mocked(service.verificarImpactosMapa)
            .mockResolvedValueOnce(primeiroImpacto)
            .mockResolvedValueOnce(segundoImpacto);

        await mapas.buscarImpactoMapa(1);
        await mapas.buscarImpactoMapa(1);

        expect(service.verificarImpactosMapa).toHaveBeenCalledTimes(2);
        expect(mapas.impactoMapa.value).toEqual(segundoImpacto);
    });

    it("deve manter snapshots separados por subprocesso em views keepAlive", async () => {
        const codigoAtual = ref<number | null>(1);
        const mapas = useMapas(codigoAtual);
        const mapaUm: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "mapa 1",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        const mapaDois: MapaCompleto = {
            codigo: 2,
            subprocessoCodigo: 2,
            observacoes: "mapa 2",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto)
            .mockResolvedValueOnce(mapaUm)
            .mockResolvedValueOnce(mapaDois);

        await mapas.buscarMapaCompleto(1);
        codigoAtual.value = 2;
        await mapas.buscarMapaCompleto(2);
        expect(mapas.mapaCompleto.value).toEqual(mapaDois);

        codigoAtual.value = 1;
        expect(mapas.mapaCompleto.value).toEqual(mapaUm);
    });

    it("não deve buscar impacto se codSubprocesso for zero", async () => {
        const mapas = useMapas();

        await mapas.buscarImpactoMapa(0);

        expect(service.verificarImpactosMapa).not.toHaveBeenCalled();
    });

    it("deve invalidar o impacto quando o mapa do mesmo subprocesso é atualizado", async () => {
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
        vi.mocked(service.obterMapaCompleto).mockResolvedValue({
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "novo",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        });

        await mapas.buscarImpactoMapa(1);
        expect(mapas.impactoMapa.value).toEqual(mockImpacto);

        await mapas.buscarMapaCompleto(1);
        expect(mapas.impactoMapa.value).toBeNull();
    });

    it("deve preservar o último snapshot do mapa ao invalidar, mas marcá-lo como stale", async () => {
        const codigoAtual = ref<number | null>(1);
        const mapas = useMapas(codigoAtual);
        const mapasStore = useMapasStore();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "snapshot preservado",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.buscarMapaCompleto(1);
        mapas.invalidar(1);

        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
        expect(mapasStore.dadosMapaValidos(1)).toBe(false);
    });

    it("deve voltar a buscar o mapa após invalidação explícita", async () => {
        const mapas = useMapas();
        const mapaInicial: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "antes",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        const mapaAtualizado: MapaCompleto = {
            ...mapaInicial,
            observacoes: "depois",
        };
        vi.mocked(service.obterMapaCompleto)
            .mockResolvedValueOnce(mapaInicial)
            .mockResolvedValueOnce(mapaAtualizado);

        await mapas.buscarMapaCompleto(1);
        mapas.invalidar(1);
        await mapas.buscarMapaCompleto(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledTimes(2);
        expect(mapas.mapaCompleto.value).toEqual(mapaAtualizado);
    });

    it("deve limpar totalmente o store ao resetar", async () => {
        const mapas = useMapas();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "limpar",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.buscarMapaCompleto(1);
        mapas.resetar();

        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapas.obterMapaCompletoCache(1)).toBeNull();
        expect(mapas.dadosMapaValidos(1)).toBe(false);
    });
});


import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import apiClient from "@/axios-setup";
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from "@/mappers/mapas";
import * as mapaService from "@/services/mapaService";
import {AxiosError} from "axios";

vi.mock("@/axios-setup");
vi.mock("@/mappers/mapas");

describe("mapaService", () => {
    const mockedApiClient = apiClient as Mocked<typeof apiClient>;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("obterMapaVisualizacao deve chamar o endpoint correto", async () => {
        mockedApiClient.get.mockResolvedValue({data: {}});
        await mapaService.obterMapaVisualizacao(1);
        expect(mockedApiClient.get).toHaveBeenCalledWith(
            "/subprocessos/1/mapa-visualizacao",
        );
    });

    it("verificarImpactosMapa deve chamar o endpoint correto e mapear a resposta", async () => {
        mockedApiClient.get.mockResolvedValue({data: {}});
        await mapaService.verificarImpactosMapa(1);
        expect(mockedApiClient.get).toHaveBeenCalledWith(
            "/subprocessos/1/impactos-mapa",
        );
        expect(mapImpactoMapaDtoToModel).toHaveBeenCalled();
    });

    it("obterMapaCompleto deve chamar o endpoint correto e mapear a resposta", async () => {
        mockedApiClient.get.mockResolvedValue({data: {}});
        await mapaService.obterMapaCompleto(1);
        expect(mockedApiClient.get).toHaveBeenCalledWith(
            "/subprocessos/1/mapa-completo",
        );
        expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
    });

    it("salvarMapaCompleto deve chamar o endpoint correto e mapear a resposta", async () => {
        mockedApiClient.post.mockResolvedValue({data: {}});
        await mapaService.salvarMapaCompleto(1, {});
        expect(mockedApiClient.post).toHaveBeenCalledWith(
            "/subprocessos/1/mapa-completo/salvar",
            {},
        );
        expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
    });

    it("obterMapaAjuste deve chamar o endpoint correto e mapear a resposta", async () => {
        mockedApiClient.get.mockResolvedValue({data: {}});
        await mapaService.obterMapaAjuste(1);
        expect(mockedApiClient.get).toHaveBeenCalledWith(
            "/subprocessos/1/mapa-ajuste",
        );
        expect(mapMapaAjusteDtoToModel).toHaveBeenCalled();
    });

    it("salvarMapaAjuste deve chamar o endpoint correto", async () => {
        mockedApiClient.post.mockResolvedValue({});
        await mapaService.salvarMapaAjuste(1, {});
        expect(mockedApiClient.post).toHaveBeenCalledWith(
            "/subprocessos/1/mapa-ajuste/salvar",
            {},
        );
    });

    it("verificarMapaVigente deve chamar o endpoint correto", async () => {
        mockedApiClient.get.mockResolvedValue({data: {temMapaVigente: true}});
        const result = await mapaService.verificarMapaVigente(1);
        expect(mockedApiClient.get).toHaveBeenCalledWith(
            "/unidades/1/mapa-vigente",
        );
        expect(result).toBe(true);
    });

    it("verificarMapaVigente deve retornar false em caso de 404", async () => {
        const error = new AxiosError("Not Found");
        error.response = { status: 404 } as any;
        mockedApiClient.get.mockRejectedValue(error);

        const result = await mapaService.verificarMapaVigente(1);
        expect(result).toBe(false);
    });

    it("verificarMapaVigente deve lançar exceção para outros erros", async () => {
        const error = new Error("Erro genérico");
        mockedApiClient.get.mockRejectedValue(error);

        await expect(mapaService.verificarMapaVigente(1)).rejects.toThrow(
            "Erro genérico",
        );
    });
});

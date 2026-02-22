import {describe, expect, it, vi} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import * as mapaService from "@/services/mapaService";
import {AxiosError} from "axios";

vi.mock("@/mappers/mapas");

describe("mapaService", () => {
    const { mockApi } = setupServiceTest();

    describe("obterMapaVisualizacao", () => {
        testGetEndpoint(
            () => mapaService.obterMapaVisualizacao(1),
            "/subprocessos/1/mapa-visualizacao",
            {}
        );
        testErrorHandling(() => mapaService.obterMapaVisualizacao(1), 'get');
    });

    describe("verificarImpactosMapa", () => {
        it("deve chamar o endpoint correto e mapear a resposta", async () => {
            mockApi.get.mockResolvedValue({ data: {} });
            await mapaService.verificarImpactosMapa(1);
            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/1/impactos-mapa");
            expect(mapImpactoMapaDtoToModel).toHaveBeenCalled();
        });
        testErrorHandling(() => mapaService.verificarImpactosMapa(1), 'get');
    });

    describe("obterMapaCompleto", () => {
        it("deve chamar o endpoint correto e mapear a resposta", async () => {
            mockApi.get.mockResolvedValue({ data: {} });
            await mapaService.obterMapaCompleto(1);
            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/1/mapa-completo");
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
        });
        testErrorHandling(() => mapaService.obterMapaCompleto(1), 'get');
    });

    describe("salvarMapaCompleto", () => {
        it("deve chamar o endpoint correto e mapear a resposta", async () => {
            mockApi.post.mockResolvedValue({ data: {} });
            await mapaService.salvarMapaCompleto(1, {});
            expect(mockApi.post).toHaveBeenCalledWith(
                "/subprocessos/1/mapa-completo",
                {}
            );
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
        });
        testErrorHandling(() => mapaService.salvarMapaCompleto(1, {}), 'post');
    });

    describe("obterMapaAjuste", () => {
        it("deve chamar o endpoint correto e mapear a resposta", async () => {
            mockApi.get.mockResolvedValue({ data: {} });
            await mapaService.obterMapaAjuste(1);
            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/1/mapa-ajuste");
            expect(mapMapaAjusteDtoToModel).toHaveBeenCalled();
        });
        testErrorHandling(() => mapaService.obterMapaAjuste(1), 'get');
    });

    describe("salvarMapaAjuste", () => {
        testPostEndpoint(
            () => mapaService.salvarMapaAjuste(1, {}),
            "/subprocessos/1/mapa-ajuste/atualizar",
            {},
            {}
        );
        testErrorHandling(() => mapaService.salvarMapaAjuste(1, {}), 'post');
    });

    describe("verificarMapaVigente", () => {
        it("deve chamar o endpoint correto", async () => {
            mockApi.get.mockResolvedValue({ data: { temMapaVigente: true } });
            const result = await mapaService.verificarMapaVigente(1);
            expect(mockApi.get).toHaveBeenCalledWith("/unidades/1/mapa-vigente");
            expect(result).toBe(true);
        });

        it("deve retornar false em caso de 404", async () => {
            const error = new AxiosError("Not Found");
            error.response = { status: 404 } as any;
            mockApi.get.mockRejectedValue(error);

            const result = await mapaService.verificarMapaVigente(1);
            expect(result).toBe(false);
        });

        it("deve retornar false se a resposta não contiver temMapaVigente", async () => {
            // This test covers the "?? false" branch in line 67
             mockApi.get.mockResolvedValue({ data: { } });
            const result = await mapaService.verificarMapaVigente(1);
            expect(result).toBe(false);
        });

        it("deve lançar exceção para outros erros", async () => {
            const error = new Error("Erro genérico");
            mockApi.get.mockRejectedValue(error);

            await expect(mapaService.verificarMapaVigente(1)).rejects.toThrow(
                "Erro genérico",
            );
        });
    });

    describe("disponibilizarMapa", () => {
        testPostEndpoint(
            () => mapaService.disponibilizarMapa(1, {} as any),
            "/subprocessos/1/disponibilizar-mapa",
            {}
        );
        testErrorHandling(() => mapaService.disponibilizarMapa(1, {} as any), 'post');
    });
});

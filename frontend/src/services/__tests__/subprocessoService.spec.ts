/// <reference types="vitest/globals" />
import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import apiClient from "@/axios-setup";
import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import * as subprocessoService from "@/services/subprocessoService";
import type {Competencia} from "@/types/tipos";

vi.mock("@/axios-setup");
vi.mock("@/mappers/mapas");

describe("subprocessoService", () => {
    const MOCK_ERROR = new Error("Service failed");
    const mockedApiClient = apiClient as Mocked<typeof apiClient>;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe("importarAtividades", () => {
        it("deve chamar o endpoint correto com o payload correto", async () => {
            mockedApiClient.post.mockResolvedValue({});
            await subprocessoService.importarAtividades(1, 2);
            expect(mockedApiClient.post).toHaveBeenCalledWith(
                "/subprocessos/1/importar-atividades",
                {subprocessoOrigemId: 2},
            );
        });

        it("deve lançar um erro em caso de falha", async () => {
            mockedApiClient.post.mockRejectedValue(MOCK_ERROR);
            await expect(subprocessoService.importarAtividades(1, 2)).rejects.toThrow(
                MOCK_ERROR,
            );
        });
    });

    describe("fetchSubprocessoDetalhe", () => {
        it("deve chamar o endpoint correto com os parâmetros corretos", async () => {
            mockedApiClient.get.mockResolvedValue({data: {}});
            await subprocessoService.fetchSubprocessoDetalhe(1, "perfil", 123);
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/1", {
                params: {perfil: "perfil", unidadeUsuario: 123},
            });
        });
    });

    describe("Competencia Actions", () => {
        const mockCompetencia: Competencia = {
            codigo: 1,
            descricao: "Teste",
            atividadesAssociadas: [],
        };
        const mockMapaCompleto = {id: 1, competencias: [mockCompetencia]};

        beforeEach(() => {
            (mapMapaCompletoDtoToModel as Mocked<any>).mockReturnValue(
                mockMapaCompleto,
            );
        });

        it("adicionarCompetencia deve chamar o endpoint correto e mapear a resposta", async () => {
            mockedApiClient.post.mockResolvedValue({data: {}});
            const result = await subprocessoService.adicionarCompetencia(
                1,
                mockCompetencia,
            );
            expect(mockedApiClient.post).toHaveBeenCalledWith(
                "/subprocessos/1/competencias",
                mockCompetencia,
            );
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
            expect(result).toEqual(mockMapaCompleto);
        });

        it("atualizarCompetencia deve chamar o endpoint correto e mapear a resposta", async () => {
            mockedApiClient.post.mockResolvedValue({data: {}});
            const result = await subprocessoService.atualizarCompetencia(
                1,
                mockCompetencia,
            );
            expect(mockedApiClient.post).toHaveBeenCalledWith(
                "/subprocessos/1/competencias/1/atualizar",
                mockCompetencia,
            );
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
            expect(result).toEqual(mockMapaCompleto);
        });

        it("removerCompetencia deve chamar o endpoint correto e mapear a resposta", async () => {
            mockedApiClient.post.mockResolvedValue({data: {}});
            const result = await subprocessoService.removerCompetencia(1, 1);
            expect(mockedApiClient.post).toHaveBeenCalledWith(
                "/subprocessos/1/competencias/1/remover",
            );
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
            expect(result).toEqual(mockMapaCompleto);
        });
    });
});

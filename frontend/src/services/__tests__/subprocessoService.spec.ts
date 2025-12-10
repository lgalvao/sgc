/// <reference types="vitest/globals" />
import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import apiClient from "@/axios-setup";
import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import {mapAtividadeVisualizacaoToModel} from "@/mappers/atividades";
import * as subprocessoService from "@/services/subprocessoService";
import type {Competencia} from "@/types/tipos";

vi.mock("@/axios-setup");
vi.mock("@/mappers/mapas");
vi.mock("@/mappers/atividades");

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
                {codSubprocessoOrigem: 2},
            );
        });

        it("deve lançar um erro em caso de falha", async () => {
            mockedApiClient.post.mockRejectedValue(MOCK_ERROR);
            await expect(subprocessoService.importarAtividades(1, 2)).rejects.toThrow(
                MOCK_ERROR,
            );
        });
    });
    
    describe("listarAtividades", () => {
        it("deve chamar o endpoint correto e mapear os resultados", async () => {
            const mockAtividades = [{ id: 1 }];
            mockedApiClient.get.mockResolvedValue({ data: mockAtividades });
            (mapAtividadeVisualizacaoToModel as Mocked<any>).mockImplementation((a: any) => a);

            await subprocessoService.listarAtividades(1);
            
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/1/atividades");
            expect(mapAtividadeVisualizacaoToModel).toHaveBeenCalledTimes(1);
        });
    });

    describe("obterPermissoes", () => {
        it("deve chamar o endpoint correto", async () => {
            const mockPermissoes = { podeEditar: true };
            mockedApiClient.get.mockResolvedValue({ data: mockPermissoes });

            const result = await subprocessoService.obterPermissoes(1);
            
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/1/permissoes");
            expect(result).toEqual(mockPermissoes);
        });
    });

    describe("validarCadastro", () => {
        it("deve chamar o endpoint correto", async () => {
            const mockValidacao = { valido: true };
            mockedApiClient.get.mockResolvedValue({ data: mockValidacao });

            const result = await subprocessoService.validarCadastro(1);
            
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/1/validar-cadastro");
            expect(result).toEqual(mockValidacao);
        });
    });
    
    describe("obterStatus", () => {
        it("deve chamar o endpoint correto", async () => {
            const mockStatus = { status: "EM_ANDAMENTO" };
            mockedApiClient.get.mockResolvedValue({ data: mockStatus });

            const result = await subprocessoService.obterStatus(1);
            
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/1/status");
            expect(result).toEqual(mockStatus);
        });
    });
    
    describe("buscarSubprocessoPorProcessoEUnidade", () => {
        it("deve chamar o endpoint correto com query params", async () => {
            const mockResponse = { id: 100 };
            mockedApiClient.get.mockResolvedValue({ data: mockResponse });

            const result = await subprocessoService.buscarSubprocessoPorProcessoEUnidade(10, "UNID");
            
            expect(mockedApiClient.get).toHaveBeenCalledWith("/subprocessos/buscar", {
                params: { codProcesso: 10, siglaUnidade: "UNID" }
            });
            expect(result).toEqual(mockResponse);
        });
    });

    describe("buscarSubprocessoDetalhe", () => {
        it("deve chamar o endpoint correto com os parâmetros corretos", async () => {
            mockedApiClient.get.mockResolvedValue({data: {}});
            await subprocessoService.buscarSubprocessoDetalhe(1, "perfil", 123);
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
                {
                    descricao: mockCompetencia.descricao,
                    atividadesIds: mockCompetencia.atividadesAssociadas,
                },
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
                {
                    descricao: mockCompetencia.descricao,
                    atividadesIds: mockCompetencia.atividadesAssociadas,
                },
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
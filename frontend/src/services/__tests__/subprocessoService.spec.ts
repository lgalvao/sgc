import { describe, expect, it, vi } from "vitest";
import { setupServiceTest, testPostEndpoint, testGetEndpoint } from "../../test-utils/serviceTestHelpers";
import { mapMapaCompletoDtoToModel } from "@/mappers/mapas";
import { mapAtividadeVisualizacaoToModel } from "@/mappers/atividades";
import * as subprocessoService from "@/services/subprocessoService";
import type { Competencia } from "@/types/tipos";

vi.mock("@/mappers/mapas");
vi.mock("@/mappers/atividades");

describe("subprocessoService", () => {
    const { mockApi } = setupServiceTest();
    const MOCK_ERROR = new Error("Service failed");

    describe("importarAtividades", () => {
        testPostEndpoint(
            () => subprocessoService.importarAtividades(1, 2),
            "/subprocessos/1/importar-atividades",
            { codSubprocessoOrigem: 2 },
            {}
        );

        it("deve lançar um erro em caso de falha", async () => {
            mockApi.post.mockRejectedValue(MOCK_ERROR);
            await expect(subprocessoService.importarAtividades(1, 2)).rejects.toThrow(
                MOCK_ERROR,
            );
        });
    });

    describe("listarAtividades", () => {
        it("deve chamar o endpoint correto e mapear os resultados", async () => {
            const mockAtividades = [{ id: 1 }];
            mockApi.get.mockResolvedValue({ data: mockAtividades });
            vi.mocked(mapAtividadeVisualizacaoToModel).mockImplementation((a: any) => a);

            await subprocessoService.listarAtividades(1);

            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/1/atividades");
            expect(mapAtividadeVisualizacaoToModel).toHaveBeenCalledTimes(1);
        });
    });

    describe("obterPermissoes", () => {
        testGetEndpoint(
            () => subprocessoService.obterPermissoes(1),
            "/subprocessos/1/permissoes",
            { podeEditar: true }
        );
    });

    describe("validarCadastro", () => {
        testGetEndpoint(
            () => subprocessoService.validarCadastro(1),
            "/subprocessos/1/validar-cadastro",
            { valido: true }
        );
    });

    describe("obterStatus", () => {
        testGetEndpoint(
            () => subprocessoService.obterStatus(1),
            "/subprocessos/1/status",
            { status: "EM_ANDAMENTO" }
        );
    });

    describe("buscarSubprocessoPorProcessoEUnidade", () => {
        it("deve chamar o endpoint correto com query params", async () => {
            const mockResponse = { id: 100 };
            mockApi.get.mockResolvedValue({ data: mockResponse });

            const result = await subprocessoService.buscarSubprocessoPorProcessoEUnidade(10, "UNID");

            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/buscar", {
                params: { codProcesso: 10, siglaUnidade: "UNID" }
            });
            expect(result).toEqual(mockResponse);
        });
    });

    describe("buscarSubprocessoDetalhe", () => {
        it("deve chamar o endpoint correto com os parâmetros corretos", async () => {
            mockApi.get.mockResolvedValue({ data: {} });
            await subprocessoService.buscarSubprocessoDetalhe(1, "perfil", 123);
            expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/1", {
                params: { perfil: "perfil", unidadeUsuario: 123 },
            });
        });
    });

    describe("Competencia Actions", () => {
        const mockCompetencia: Competencia = {
            codigo: 1,
            descricao: "Teste",
            atividadesAssociadas: [],
        };
        const mockMapaCompleto = { id: 1, competencias: [mockCompetencia] };

        it("adicionarCompetencia deve chamar o endpoint correto e mapear a resposta", async () => {
            vi.mocked(mapMapaCompletoDtoToModel).mockReturnValue(mockMapaCompleto as any);
            mockApi.post.mockResolvedValue({ data: {} });

            const result = await subprocessoService.adicionarCompetencia(
                1,
                mockCompetencia,
            );
            expect(mockApi.post).toHaveBeenCalledWith(
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
            vi.mocked(mapMapaCompletoDtoToModel).mockReturnValue(mockMapaCompleto as any);
            mockApi.post.mockResolvedValue({ data: {} });

            const result = await subprocessoService.atualizarCompetencia(
                1,
                mockCompetencia,
            );
            expect(mockApi.post).toHaveBeenCalledWith(
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
            vi.mocked(mapMapaCompletoDtoToModel).mockReturnValue(mockMapaCompleto as any);
            mockApi.post.mockResolvedValue({ data: {} });

            const result = await subprocessoService.removerCompetencia(1, 1);
            expect(mockApi.post).toHaveBeenCalledWith(
                "/subprocessos/1/competencias/1/remover",
            );
            expect(mapMapaCompletoDtoToModel).toHaveBeenCalled();
            expect(result).toEqual(mockMapaCompleto);
        });
    });
});

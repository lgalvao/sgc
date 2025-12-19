import { describe, expect, it } from "vitest";
import { setupServiceTest, testGetEndpoint, testPostEndpoint } from "../../test-utils/serviceTestHelpers";
import { diagnosticoService } from "../diagnosticoService";

describe("diagnosticoService", () => {
    const { mockApi } = setupServiceTest();

    describe("buscarDiagnostico", () => {
        testGetEndpoint(
            () => diagnosticoService.buscarDiagnostico(123),
            "/diagnosticos/123",
            { codigo: 1, subprocessoCodigo: 123, situacao: "EM_ANDAMENTO" }
        );
    });

    describe("salvarAvaliacao", () => {
        testPostEndpoint(
            () => diagnosticoService.salvarAvaliacao(123, 10, "ALTA", "DOMINIO_TOTAL", "Obs"),
            "/diagnosticos/123/avaliacoes",
            {
                competenciaCodigo: 10,
                importancia: "ALTA",
                dominio: "DOMINIO_TOTAL",
                observacoes: "Obs",
            },
            { codigo: 1, competenciaCodigo: 10, importancia: "ALTA" }
        );
    });

    describe("buscarMinhasAvaliacoes", () => {
        it("deve buscar minhas avaliações sem servidorTitulo", async () => {
            const mockAvaliacoes = [{ codigo: 1 }];
            mockApi.get.mockResolvedValue({ data: mockAvaliacoes });

            const result = await diagnosticoService.buscarMinhasAvaliacoes(123);

            expect(mockApi.get).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes/minhas", { params: {} });
            expect(result).toEqual(mockAvaliacoes);
        });

        it("deve buscar minhas avaliações com servidorTitulo", async () => {
            const mockAvaliacoes = [{ codigo: 1 }];
            mockApi.get.mockResolvedValue({ data: mockAvaliacoes });

            const result = await diagnosticoService.buscarMinhasAvaliacoes(123, "12345678900");

            expect(mockApi.get).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes/minhas", {
                params: { servidorTitulo: "12345678900" },
            });
            expect(result).toEqual(mockAvaliacoes);
        });
    });

    describe("concluirAutoavaliacao", () => {
        testPostEndpoint(
            () => diagnosticoService.concluirAutoavaliacao(123, "Atraso"),
            "/diagnosticos/123/avaliacoes/concluir",
            { justificativaAtraso: "Atraso" },
            {}
        );
    });

    describe("salvarOcupacao", () => {
        testPostEndpoint(
            () => diagnosticoService.salvarOcupacao(123, "12345678900", 10, "CRITICA"),
            "/diagnosticos/123/ocupacoes",
            {
                servidorTitulo: "12345678900",
                competenciaCodigo: 10,
                situacao: "CRITICA",
            },
            { codigo: 1, competenciaCodigo: 10 }
        );
    });

    describe("buscarOcupacoes", () => {
        testGetEndpoint(
            () => diagnosticoService.buscarOcupacoes(123),
            "/diagnosticos/123/ocupacoes",
            [{ codigo: 1 }]
        );
    });

    describe("concluirDiagnostico", () => {
        testPostEndpoint(
            () => diagnosticoService.concluirDiagnostico(123, "Justificativa"),
            "/diagnosticos/123/concluir",
            { justificativa: "Justificativa" },
            { codigo: 1, situacao: "CONCLUIDO" }
        );
    });
});

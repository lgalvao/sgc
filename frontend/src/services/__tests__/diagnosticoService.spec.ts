import {beforeEach, describe, expect, it, vi} from "vitest";
import {diagnosticoService} from "../diagnosticoService";
import apiClient from "../../axios-setup";

vi.mock("../../axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("diagnosticoService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve buscar diagnóstico corretamente", async () => {
        const mockData = {codigo: 1, subprocessoCodigo: 123, situacao: "EM_ANDAMENTO"};
        (apiClient.get as any).mockResolvedValue({data: mockData});

        const result = await diagnosticoService.buscarDiagnostico(123);

        expect(apiClient.get).toHaveBeenCalledWith("/diagnosticos/123");
        expect(result).toEqual(mockData);
    });

    it("deve salvar avaliação corretamente", async () => {
        const mockAvaliacao = {codigo: 1, competenciaCodigo: 10, importancia: "ALTA"};
        (apiClient.post as any).mockResolvedValue({data: mockAvaliacao});

        const result = await diagnosticoService.salvarAvaliacao(123, 10, "ALTA", "DOMINIO_TOTAL", "Obs");

        expect(apiClient.post).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes", {
            competenciaCodigo: 10,
            importancia: "ALTA",
            dominio: "DOMINIO_TOTAL",
            observacoes: "Obs",
        });
        expect(result).toEqual(mockAvaliacao);
    });

    it("deve buscar minhas avaliações sem servidorTitulo", async () => {
        const mockAvaliacoes = [{codigo: 1}];
        (apiClient.get as any).mockResolvedValue({data: mockAvaliacoes});

        const result = await diagnosticoService.buscarMinhasAvaliacoes(123);

        expect(apiClient.get).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes/minhas", {params: {}});
        expect(result).toEqual(mockAvaliacoes);
    });

    it("deve buscar minhas avaliações com servidorTitulo", async () => {
        const mockAvaliacoes = [{codigo: 1}];
        (apiClient.get as any).mockResolvedValue({data: mockAvaliacoes});

        const result = await diagnosticoService.buscarMinhasAvaliacoes(123, "12345678900");

        expect(apiClient.get).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes/minhas", {
            params: {servidorTitulo: "12345678900"},
        });
        expect(result).toEqual(mockAvaliacoes);
    });

    it("deve concluir autoavaliação corretamente", async () => {
        (apiClient.post as any).mockResolvedValue({});

        await diagnosticoService.concluirAutoavaliacao(123, "Atraso");

        expect(apiClient.post).toHaveBeenCalledWith("/diagnosticos/123/avaliacoes/concluir", {
            justificativaAtraso: "Atraso",
        });
    });

    it("deve salvar ocupação crítica corretamente", async () => {
        const mockOcupacao = {codigo: 1, competenciaCodigo: 10};
        (apiClient.post as any).mockResolvedValue({data: mockOcupacao});

        const result = await diagnosticoService.salvarOcupacao(123, "12345678900", 10, "CRITICA");

        expect(apiClient.post).toHaveBeenCalledWith("/diagnosticos/123/ocupacoes", {
            servidorTitulo: "12345678900",
            competenciaCodigo: 10,
            situacao: "CRITICA",
        });
        expect(result).toEqual(mockOcupacao);
    });

    it("deve buscar ocupações críticas corretamente", async () => {
        const mockOcupacoes = [{codigo: 1}];
        (apiClient.get as any).mockResolvedValue({data: mockOcupacoes});

        const result = await diagnosticoService.buscarOcupacoes(123);

        expect(apiClient.get).toHaveBeenCalledWith("/diagnosticos/123/ocupacoes");
        expect(result).toEqual(mockOcupacoes);
    });

    it("deve concluir diagnóstico corretamente", async () => {
        const mockDiagnostico = {codigo: 1, situacao: "CONCLUIDO"};
        (apiClient.post as any).mockResolvedValue({data: mockDiagnostico});

        const result = await diagnosticoService.concluirDiagnostico(123, "Justificativa");

        expect(apiClient.post).toHaveBeenCalledWith("/diagnosticos/123/concluir", {
            justificativa: "Justificativa",
        });
        expect(result).toEqual(mockDiagnostico);
    });
});

import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {buscarTodasAtribuicoes, criarAtribuicaoTemporaria,} from "../atribuicaoTemporariaService";

vi.mock("@/axios-setup");

describe("atribuicaoTemporariaService", () => {
    it("buscarTodasAtribuicoes should make a GET request", async () => {
        const mockData = [{id: 1}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarTodasAtribuicoes();

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/atribuicoes");
        expect(result).toEqual(mockData);
    });

    it("criarAtribuicaoTemporaria should make a POST request", async () => {
        const request = {
            tituloEleitoralServidor: "123",
            dataTermino: "2025-12-31",
            justificativa: "teste",
        };
        vi.mocked(apiClient.post).mockResolvedValue({});

        await criarAtribuicaoTemporaria(1, request);

        expect(apiClient.post).toHaveBeenCalledWith(
            "/unidades/1/atribuicoes-temporarias",
            request,
        );
    });
});

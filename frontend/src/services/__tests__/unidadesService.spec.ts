import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {
    buscarArvoreComElegibilidade,
    buscarTodasUnidades,
    buscarUnidadePorSigla,
} from "../unidadesService";

vi.mock("@/axios-setup");

describe("unidadesService", () => {
    it("buscarTodasUnidades should make a GET request", async () => {
        const mockData = [{id: 1}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarTodasUnidades();

        expect(apiClient.get).toHaveBeenCalledWith("/unidades");
        expect(result).toEqual(mockData);
    });

    it("buscarUnidadePorSigla should make a GET request", async () => {
        const mockData = {id: 1};
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarUnidadePorSigla("TESTE");

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/sigla/TESTE");
        expect(result).toEqual(mockData);
    });

    it("buscarArvoreComElegibilidade should make a GET request", async () => {
        const mockData = [{id: 1}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarArvoreComElegibilidade("MAPEAMENTO", 1);

        expect(apiClient.get).toHaveBeenCalledWith(
            "/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=1",
        );
        expect(result).toEqual(mockData);
    });
});

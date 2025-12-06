import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarSubordinadas,
    buscarSuperior,
    buscarTodasUnidades,
    buscarUnidadePorCodigo,
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

    it("buscarUnidadePorCodigo should make a GET request", async () => {
        const mockData = {id: 1, nome: "Unit 1"};
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarUnidadePorCodigo(1);

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/1");
        expect(result).toEqual(mockData);
    });

    it("buscarArvoreComElegibilidade should make a GET request with code", async () => {
        const mockData = [{id: 1}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarArvoreComElegibilidade("MAPEAMENTO", 1);

        expect(apiClient.get).toHaveBeenCalledWith(
            "/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=1",
        );
        expect(result).toEqual(mockData);
    });

    it("buscarArvoreComElegibilidade should make a GET request without code", async () => {
        const mockData = [{id: 1}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarArvoreComElegibilidade("MAPEAMENTO");

        expect(apiClient.get).toHaveBeenCalledWith(
            "/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO",
        );
        expect(result).toEqual(mockData);
    });

    it("buscarArvoreUnidade should make a GET request", async () => {
        const mockData = {id: 10, children: []};
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarArvoreUnidade(10);

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/10/arvore");
        expect(result).toEqual(mockData);
    });

    it("buscarSubordinadas should make a GET request", async () => {
        const mockData = [{id: 11}, {id: 12}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarSubordinadas("SIGLA");

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/sigla/SIGLA/subordinadas");
        expect(result).toEqual(mockData);
    });

    it("buscarSuperior should make a GET request", async () => {
        const mockData = {id: 9};
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

        const result = await buscarSuperior("SIGLA");

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/sigla/SIGLA/superior");
        expect(result).toEqual(mockData);
    });

    it("buscarSuperior should return null if response data is missing", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({data: null});

        const result = await buscarSuperior("SIGLA");

        expect(apiClient.get).toHaveBeenCalledWith("/unidades/sigla/SIGLA/superior");
        expect(result).toBeNull();
    });
});

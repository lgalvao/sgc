import { describe, expect, it } from "vitest";
import { setupServiceTest, testErrorHandling, testGetEndpoint } from "../../test-utils/serviceTestHelpers";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarSubordinadas,
    buscarSuperior,
    buscarTodasUnidades,
    buscarUnidadePorCodigo,
    buscarUnidadePorSigla,
} from "../unidadesService";

describe("unidadesService", () => {
    const { mockApi } = setupServiceTest();

    describe("buscarTodasUnidades", () => {
        testGetEndpoint(
            () => buscarTodasUnidades(),
            "/unidades",
            [{ id: 1 }]
        );
        testErrorHandling(() => buscarTodasUnidades());
    });

    describe("buscarUnidadePorSigla", () => {
        testGetEndpoint(
            () => buscarUnidadePorSigla("TESTE"),
            "/unidades/sigla/TESTE",
            { id: 1 }
        );
        testErrorHandling(() => buscarUnidadePorSigla("TESTE"));
    });

    describe("buscarUnidadePorCodigo", () => {
        testGetEndpoint(
            () => buscarUnidadePorCodigo(1),
            "/unidades/1",
            { id: 1, nome: "Unit 1" }
        );
        testErrorHandling(() => buscarUnidadePorCodigo(1));
    });

    describe("buscarArvoreComElegibilidade", () => {
        it("deve fazer GET com código do processo", async () => {
            const mockData = [{ id: 1 }];
            mockApi.get.mockResolvedValue({ data: mockData });

            const result = await buscarArvoreComElegibilidade("MAPEAMENTO", 1);

            expect(mockApi.get).toHaveBeenCalledWith(
                "/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO&codProcesso=1",
            );
            expect(result).toEqual(mockData);
        });

        it("deve fazer GET sem código do processo", async () => {
            const mockData = [{ id: 1 }];
            mockApi.get.mockResolvedValue({ data: mockData });

            const result = await buscarArvoreComElegibilidade("MAPEAMENTO");

            expect(mockApi.get).toHaveBeenCalledWith(
                "/unidades/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO",
            );
            expect(result).toEqual(mockData);
        });

        testErrorHandling(() => buscarArvoreComElegibilidade("MAPEAMENTO"));
    });

    describe("buscarArvoreUnidade", () => {
        testGetEndpoint(
            () => buscarArvoreUnidade(10),
            "/unidades/10/arvore",
            { id: 10, children: [] }
        );
        testErrorHandling(() => buscarArvoreUnidade(10));
    });

    describe("buscarSubordinadas", () => {
        testGetEndpoint(
            () => buscarSubordinadas("SIGLA"),
            "/unidades/sigla/SIGLA/subordinadas",
            [{ id: 11 }, { id: 12 }]
        );
        testErrorHandling(() => buscarSubordinadas("SIGLA"));
    });

    describe("buscarSuperior", () => {
        testGetEndpoint(
            () => buscarSuperior("SIGLA"),
            "/unidades/sigla/SIGLA/superior",
            { id: 9 }
        );

        it("deve retornar null se a resposta for vazia", async () => {
            mockApi.get.mockResolvedValue({ data: null });

            const result = await buscarSuperior("SIGLA");

            expect(mockApi.get).toHaveBeenCalledWith("/unidades/sigla/SIGLA/superior");
            expect(result).toBeNull();
        });

        testErrorHandling(() => buscarSuperior("SIGLA"));
    });
});

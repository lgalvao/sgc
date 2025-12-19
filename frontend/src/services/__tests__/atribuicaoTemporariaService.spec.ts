import { describe } from "vitest";
import { setupServiceTest, testGetEndpoint, testPostEndpoint } from "../../test-utils/serviceTestHelpers";
import { buscarTodasAtribuicoes, criarAtribuicaoTemporaria } from "../atribuicaoTemporariaService";

describe("atribuicaoTemporariaService", () => {
    setupServiceTest();

    describe("buscarTodasAtribuicoes", () => {
        testGetEndpoint(
            () => buscarTodasAtribuicoes(),
            "/unidades/atribuicoes",
            [{ id: 1 }]
        );
    });

    describe("criarAtribuicaoTemporaria", () => {
        const request = {
            tituloEleitoralUsuario: "123",
            dataTermino: "2025-12-31",
            justificativa: "teste",
        };

        testPostEndpoint(
            () => criarAtribuicaoTemporaria(1, request),
            "/unidades/1/atribuicoes-temporarias",
            request,
            {}
        );
    });
});

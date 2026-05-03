import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
import {criarAtribuicaoTemporaria} from "../atribuicaoTemporariaService";

describe("atribuicaoTemporariaService", () => {
    setupServiceTest();

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
        testErrorHandling(() => criarAtribuicaoTemporaria(1, request), 'post');
    });
});

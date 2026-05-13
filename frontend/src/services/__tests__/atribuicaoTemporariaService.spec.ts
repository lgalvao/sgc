import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
import {
    atualizarAtribuicaoTemporaria,
    buscarAtribuicoesTemporariasPorUnidade,
    criarAtribuicaoTemporaria,
    removerAtribuicaoTemporaria
} from "../atribuicaoTemporariaService";

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

    describe("buscarAtribuicoesTemporariasPorUnidade", () => {
        testGetEndpoint(
            () => buscarAtribuicoesTemporariasPorUnidade(1),
            "/unidades/1/atribuicoes-temporarias",
            []
        );
    });

    describe("atualizarAtribuicaoTemporaria", () => {
        const request = {
            tituloEleitoralUsuario: "123",
            dataInicio: "2025-01-01",
            dataTermino: "2025-12-31",
            justificativa: "teste",
        };

        testPostEndpoint(
            () => atualizarAtribuicaoTemporaria(1, 9, request),
            "/unidades/1/atribuicoes-temporarias/9/atualizar",
            request,
            {}
        );
        testErrorHandling(() => atualizarAtribuicaoTemporaria(1, 9, request), "post");
    });

    describe("removerAtribuicaoTemporaria", () => {
        testPostEndpoint(
            () => removerAtribuicaoTemporaria(1, 9),
            "/unidades/1/atribuicoes-temporarias/9/excluir",
            undefined,
            {}
        );
        testErrorHandling(() => removerAtribuicaoTemporaria(1, 9), "post");
    });
});

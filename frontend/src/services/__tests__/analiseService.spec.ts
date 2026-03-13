import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint} from "@/test-utils/serviceTestHelpers";
import * as analiseService from "@/services/analiseService";

describe("analiseService", () => {
    setupServiceTest();

    describe("listarAnalisesCadastro", () => {
        testGetEndpoint(
            () => analiseService.listarAnalisesCadastro(1),
            "/subprocessos/1/historico-cadastro"
        );
        testErrorHandling(() => analiseService.listarAnalisesCadastro(1), 'get');
    });

    describe("listarAnalisesValidacao", () => {
        testGetEndpoint(
            () => analiseService.listarAnalisesValidacao(1),
            "/subprocessos/1/historico-validacao"
        );
        testErrorHandling(() => analiseService.listarAnalisesValidacao(1), 'get');
    });
});

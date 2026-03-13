import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint} from "@/test-utils/serviceTestHelpers";
import * as mapaService from "@/services/mapaService";

describe("mapaService", () => {
    setupServiceTest();

    describe("obterMapaVisualizacao", () => {
        testGetEndpoint(
            () => mapaService.obterMapaVisualizacao(1),
            "/subprocessos/1/mapa-visualizacao"
        );
        testErrorHandling(() => mapaService.obterMapaVisualizacao(1), 'get');
    });
});

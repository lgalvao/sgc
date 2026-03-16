import {describe, expect, it} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import * as service from "../mapaService";

describe("mapaService", () => {
    const {mockApi} = setupServiceTest();

    it("obterMapaVisualizacao deve buscar o mapa de visualização", async () => {
        const dto = {codigo: 1, competencias: []};
        mockApi.get.mockResolvedValue({data: dto});

        const result = await service.obterMapaVisualizacao(123);

        expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/123/mapa-visualizacao");
        expect(result).toEqual(dto);
    });

    describe("Tratamento de erros", () => {
        testErrorHandling(() => service.obterMapaVisualizacao(123));
    });
});

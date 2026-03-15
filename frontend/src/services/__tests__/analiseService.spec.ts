import {describe, expect, it} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import * as service from "../analiseService";

describe("analiseService", () => {
    const {mockApi} = setupServiceTest();

    it("listarAnalisesCadastro deve buscar histórico de cadastro", async () => {
        const dtoList = [{codigo: 1, observacoes: "OK"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarAnalisesCadastro(123);

        expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/123/historico-cadastro");
        expect(result).toEqual(dtoList);
    });

    it("listarAnalisesValidacao deve buscar histórico de validação", async () => {
        const dtoList = [{codigo: 2, observacoes: "Validado"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarAnalisesValidacao(123);

        expect(mockApi.get).toHaveBeenCalledWith("/subprocessos/123/historico-validacao");
        expect(result).toEqual(dtoList);
    });

    describe("Tratamento de erros", () => {
        testErrorHandling(() => service.listarAnalisesCadastro(123));
        testErrorHandling(() => service.listarAnalisesValidacao(123));
    });
});

import {describe, expect, it, vi} from "vitest";
import {setupServiceTest, testGetEndpoint} from "@/test-utils/serviceTestHelpers";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {listarAnalisesCadastro, listarAnalisesValidacao,} from "../analiseService";

describe("analiseService", () => {
    const {mockApi} = setupServiceTest();

    describe("listarAnalisesCadastro", () => {
        const subprocessoId = 1;
        const responseData: AnaliseCadastro[] = [
            {
                codigo: 1,
                dataHora: new Date().toISOString(),
                observacoes: "Obs",
                acao: "ACEITE",
                unidadeSigla: "Unidade",
                analista: "Usuario",
                resultado: "APROVADO",
                codSubrocesso: 1,
            },
        ];

        testGetEndpoint(
            () => listarAnalisesCadastro(subprocessoId),
            `/subprocessos/${subprocessoId}/historico-cadastro`,
            responseData
        );

        it("deve lançar um erro em caso de falha", async () => {
            const error = new Error("Request failed");
            mockApi.get.mockRejectedValue(error);

            await expect(listarAnalisesCadastro(subprocessoId)).rejects.toThrow();
        });
    });

    describe("listarAnalisesValidacao", () => {
        const subprocessoId = 1;
        const responseData: AnaliseValidacao[] = [
            {
                codigo: 1,
                dataHora: new Date().toISOString(),
                observacoes: "Obs",
                acao: "DEVOLUCAO",
                unidade: "Unidade",
                analista: "Usuario",
                resultado: "REPROVADO",
                codSubrocesso: 1,
            },
        ];

        testGetEndpoint(
            () => listarAnalisesValidacao(subprocessoId),
            `/subprocessos/${subprocessoId}/historico-validacao`,
            responseData
        );

        it("deve lançar um erro em caso de falha", async () => {
            const error = new Error("Request failed");
            mockApi.get.mockRejectedValue(error);

            await expect(listarAnalisesValidacao(subprocessoId)).rejects.toThrow();
        });
    });
});

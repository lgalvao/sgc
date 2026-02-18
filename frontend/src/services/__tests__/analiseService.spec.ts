import {describe} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint} from "@/test-utils/serviceTestHelpers";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {listarAnalisesCadastro, listarAnalisesValidacao,} from "../analiseService";

describe("analiseService", () => {
    setupServiceTest();

    describe("listarAnalisesCadastro", () => {
        const subprocessoId = 1;
        const responseData: AnaliseCadastro[] = [
            {
                dataHora: new Date().toISOString(),
                observacoes: "Obs",
                acao: "ACEITE_MAPEAMENTO",
                unidadeSigla: "Unidade",
                unidadeNome: "Unidade Nome",
                analistaUsuarioTitulo: "123456",
                motivo: "",
                tipo: "CADASTRO"
            },
        ];

        testGetEndpoint(
            () => listarAnalisesCadastro(subprocessoId),
            `/subprocessos/${subprocessoId}/historico-cadastro`,
            responseData
        );

        testErrorHandling(() => listarAnalisesCadastro(subprocessoId));
    });

    describe("listarAnalisesValidacao", () => {
        const subprocessoId = 1;
        const responseData: AnaliseValidacao[] = [
            {
                dataHora: new Date().toISOString(),
                observacoes: "Obs",
                acao: "DEVOLUCAO_MAPEAMENTO",
                unidadeSigla: "Unidade",
                unidadeNome: "Unidade Nome",
                analistaUsuarioTitulo: "123456",
                motivo: "Motivo",
                tipo: "VALIDACAO"
            },
        ];

        testGetEndpoint(
            () => listarAnalisesValidacao(subprocessoId),
            `/subprocessos/${subprocessoId}/historico-validacao`,
            responseData
        );

        testErrorHandling(() => listarAnalisesValidacao(subprocessoId));
    });
});

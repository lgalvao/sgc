import { describe, expect, it } from "vitest";
import { setupServiceTest, testGetEndpoint, testPostEndpoint, testErrorHandling } from "../../test-utils/serviceTestHelpers";
import { type AtualizarProcessoRequest, type CriarProcessoRequest, TipoProcesso } from "@/types/tipos";
import * as service from "../processoService";

describe("processoService", () => {
    const { mockApi } = setupServiceTest();

    describe("iniciarProcesso", () => {
        const payload = {
            tipo: TipoProcesso.REVISAO,
            unidades: [10, 20],
        };
        testPostEndpoint(
            () => service.iniciarProcesso(1, TipoProcesso.REVISAO, [10, 20]),
            "/processos/1/iniciar",
            payload
        );
    });

    describe("finalizarProcesso", () => {
        testPostEndpoint(
            () => service.finalizarProcesso(1),
            "/processos/1/finalizar"
        );
    });

    describe("excluirProcesso", () => {
        testPostEndpoint(
            () => service.excluirProcesso(1),
            "/processos/1/excluir"
        );
    });

    describe("buscarProcessosFinalizados", () => {
        testGetEndpoint(
            () => service.buscarProcessosFinalizados(),
            "/processos/finalizados",
            []
        );
    });

    describe("obterProcessoPorCodigo", () => {
        testGetEndpoint(
            () => service.obterProcessoPorCodigo(1),
            "/processos/1",
            {}
        );
    });

    describe("atualizarProcesso", () => {
        const request: AtualizarProcessoRequest = {
            codigo: 1,
            tipo: TipoProcesso.MAPEAMENTO,
            unidades: [],
            descricao: "teste",
            dataLimiteEtapa1: "2025-12-31",
        };
        testPostEndpoint(
            () => service.atualizarProcesso(request.codigo, request),
            `/processos/${request.codigo}/atualizar`,
            request,
            {}
        );
    });

    describe("obterDetalhesProcesso", () => {
        testGetEndpoint(
            () => service.obterDetalhesProcesso(1),
            "/processos/1/detalhes",
            {}
        );
    });

    describe("processarAcaoEmBloco", () => {
        const payload = {
            codProcesso: 1,
            unidades: ["A"],
            tipoAcao: "aceitar" as "aceitar" | "homologar",
            unidadeUsuario: "B",
        };
        testPostEndpoint(
            () => service.processarAcaoEmBloco(payload),
            "/processos/1/acoes-em-bloco",
            payload
        );
    });

    describe("buscarSubprocessosElegiveis", () => {
        testGetEndpoint(
            () => service.buscarSubprocessosElegiveis(1),
            "/processos/1/subprocessos-elegiveis",
            []
        );
    });

    describe("alterarDataLimiteSubprocesso", () => {
        const payload = { novaData: "2026-01-01" };
        testPostEndpoint(
            () => service.alterarDataLimiteSubprocesso(1, payload),
            "/processos/alterar-data-limite",
            { id: 1, ...payload }
        );
    });

    describe("apresentarSugestoes", () => {
        const payload = { sugestoes: "sugestoes" };
        testPostEndpoint(
            () => service.apresentarSugestoes(1, payload),
            "/subprocessos/1/apresentar-sugestoes",
            payload
        );
    });

    describe("validarMapa", () => {
        testPostEndpoint(
            () => service.validarMapa(1),
            "/subprocessos/1/validar-mapa"
        );
    });

    describe("buscarSubprocessos", () => {
        testGetEndpoint(
            () => service.buscarSubprocessos(1),
            "/processos/1/subprocessos",
            []
        );
    });

    describe("criarProcesso", () => {
        it("deve lançar erro em caso de falha", async () => {
            const request: CriarProcessoRequest = {
                descricao: "teste",
                tipo: TipoProcesso.MAPEAMENTO,
                dataLimiteEtapa1: "2025-12-31",
                unidades: [1],
            };
            mockApi.post.mockRejectedValue(new Error("Failed"));
            await expect(service.criarProcesso(request)).rejects.toThrow();
        });
    });

    describe("Casos de Borda e Erros", () => {
        describe("obterProcessoPorCodigo", () => {
            testErrorHandling(() => service.obterProcessoPorCodigo(999), 'get');
        });

        describe("iniciarProcesso", () => {
            testErrorHandling(() => service.iniciarProcesso(1, TipoProcesso.REVISAO, []), 'post');
        });
    });
});

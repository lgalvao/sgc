import {beforeEach, describe, expect, it, vi} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import {createPinia, setActivePinia} from "pinia";
import type {Conhecimento} from "@/types/tipos";
import * as service from "../atividadeService";

describe("atividadeService", () => {
    const { mockApi } = setupServiceTest();

    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("listarAtividades deve buscar atividades", async () => {
        const dtoList = [{codigo: 1, descricao: "Atividade DTO"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarAtividades();

        expect(mockApi.get).toHaveBeenCalledWith("/atividades");
        expect(result[0].codigo).toBe(1);
    });

    it("obterAtividadePorCodigo deve buscar uma atividade", async () => {
        const dto = {codigo: 1, descricao: "Atividade DTO"};
        mockApi.get.mockResolvedValue({data: dto});

        const result = await service.obterAtividadePorCodigo(1);

        expect(mockApi.get).toHaveBeenCalledWith("/atividades/1");
        expect(result.codigo).toBe(1);
    });

    it("criarAtividade deve enviar POST", async () => {
        const request = {descricao: "Nova"};
        const responseDto = {
            atividade: {codigo: 1, descricao: "Nova", conhecimentos: []},
            subprocesso: {codigo: 123, situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO", situacaoLabel: "Cadastro em andamento"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarAtividade(request, 100);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades", expect.objectContaining({ ...request, mapaCodigo: 100 }));
        expect(result.atividade).toBeDefined();
        expect(result.subprocesso).toBeDefined();
    });

    it("atualizarAtividade deve enviar POST", async () => {
        const request = {codigo: 1, descricao: "Editada", conhecimentos: []} as any;
        const responseDto = {
            atividade: request,
            subprocesso: {codigo: 123, situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO", situacaoLabel: "Cadastro em andamento"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.atualizarAtividade(1, request);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/atualizar", expect.objectContaining({ descricao: "Editada" }));
        expect(result.atividade).toBeDefined();
        expect(result.subprocesso).toBeDefined();
    });

    it("excluirAtividade deve enviar POST", async () => {
        const responseDto = {
            atividade: null,
            subprocesso: {codigo: 123, situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO", situacaoLabel: "Cadastro em andamento"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.excluirAtividade(1);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/excluir");
        expect(result.atividade).toBeNull();
        expect(result.subprocesso).toBeDefined();
    });

    it("listarConhecimentos deve buscar conhecimentos", async () => {
        const dtoList = [{codigo: 1, descricao: "Conhecimento DTO"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarConhecimentos(1);

        expect(mockApi.get).toHaveBeenCalledWith("/atividades/1/conhecimentos");
        expect(result[0].codigo).toBe(1);
    });

    it("criarConhecimento deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request = {descricao: "Novo Conhecimento"};
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: [{codigo: 2, ...request}]},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarConhecimento(1, request);

        expect(mockApi.post).toHaveBeenCalledWith(
            "/atividades/1/conhecimentos",
            expect.objectContaining({ ...request, atividadeCodigo: 1 }),
        );
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("atualizarConhecimento deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request: Conhecimento = {
            codigo: 1,
            descricao: "Conhecimento Atualizado",
        };
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: [{...request}]},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.atualizarConhecimento(1, request.codigo, request);

        expect(mockApi.post).toHaveBeenCalledWith(
            `/atividades/1/conhecimentos/${request.codigo}/atualizar`,
            expect.objectContaining({ descricao: "Conhecimento Atualizado" }),
        );
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("excluirConhecimento deve chamar POST e retornar AtividadeOperacaoResponse", async () => {
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: []},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});
        const result = await service.excluirConhecimento(1, 1);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/atividades/1/conhecimentos/1/excluir",
        );
        expect(result).toHaveProperty("subprocesso");
    });

    describe("Tratamento de erros", () => {
        testErrorHandling(() => service.listarAtividades());
        testErrorHandling(() => service.obterAtividadePorCodigo(1));
        testErrorHandling(() => service.criarAtividade({descricao: "Nova"}, 123), 'post');
        testErrorHandling(() => service.atualizarAtividade(1, {codigo: 1} as any), 'post');
        testErrorHandling(() => service.excluirAtividade(1), 'post');
        testErrorHandling(() => service.listarConhecimentos(1));
        testErrorHandling(() => service.criarConhecimento(1, {descricao: "Novo"}), 'post');
        testErrorHandling(() => service.atualizarConhecimento(1, 1, {codigo: 1} as any), 'post');
        testErrorHandling(() => service.excluirConhecimento(1, 1), 'post');
    });
});

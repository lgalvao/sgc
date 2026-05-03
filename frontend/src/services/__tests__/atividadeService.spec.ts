import {beforeEach, describe, expect, it} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import {createPinia, setActivePinia} from "pinia";
import type {Conhecimento} from "@/types/tipos";
import * as service from "../atividadeService";

describe("atividadeService", () => {
    const {mockApi} = setupServiceTest();

    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("criarAtividade deve enviar POST", async () => {
        const request = {descricao: "Nova"};
        const responseDto = {
            atividade: {codigo: 1, descricao: "Nova", conhecimentos: []},
            subprocesso: {
                codigo: 123,
                situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                situacaoLabel: "Cadastro em andamento"
            },
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarAtividade(request, 100);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades", {...request, mapaCodigo: 100});
        expect(result).toEqual(responseDto);
    });

    it("atualizarAtividade deve enviar POST", async () => {
        const request = {codigo: 1, descricao: "Editada", conhecimentos: []} as any;
        const responseDto = {
            atividade: request,
            subprocesso: {
                codigo: 123,
                situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                situacaoLabel: "Cadastro em andamento"
            },
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.atualizarAtividade(1, request);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/atualizar", request);
        expect(result).toEqual(responseDto);
    });

    it("excluirAtividade deve enviar POST", async () => {
        const responseDto = {
            atividade: null,
            subprocesso: {
                codigo: 123,
                situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
                situacaoLabel: "Cadastro em andamento"
            },
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.excluirAtividade(1);

        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/excluir");
        expect(result).toEqual(responseDto);
    });

    it("criarConhecimento deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request = {descricao: "Novo conhecimento"};
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: [{codigo: 2, ...request}]},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"},
            atividadesAtualizadas: []
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarConhecimento(1, request);

        expect(mockApi.post).toHaveBeenCalledWith(
            "/atividades/1/conhecimentos",
            {...request, atividadeCodigo: 1},
        );
        expect(result).toEqual(responseDto);
    });

    it("atualizarConhecimento deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request: Conhecimento = {
            codigo: 1,
            descricao: "Conhecimento atualizado",
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
            {descricao: request.descricao},
        );
        expect(result).toEqual(responseDto);
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
        expect(result).toEqual(responseDto);
    });

    describe("Tratamento de erros", () => {
        testErrorHandling(() => service.criarAtividade({descricao: "Nova"}, 123), 'post');
        testErrorHandling(() => service.atualizarAtividade(1, {codigo: 1} as any), 'post');
        testErrorHandling(() => service.excluirAtividade(1), 'post');
        testErrorHandling(() => service.criarConhecimento(1, {descricao: "Novo"}), 'post');
        testErrorHandling(() => service.atualizarConhecimento(1, 1, {codigo: 1} as any), 'post');
        testErrorHandling(() => service.excluirConhecimento(1, 1), 'post');
    });
});

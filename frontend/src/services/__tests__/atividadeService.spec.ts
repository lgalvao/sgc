import {describe, expect, it, vi, beforeEach} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import { createPinia, setActivePinia } from "pinia";
import * as mappers from "@/mappers/atividades";
import type {Atividade, Conhecimento} from "@/types/tipos";
import * as service from "../atividadeService";

// Mocking mappers as they are used in the service
vi.mock("@/mappers/atividades", () => ({
    mapAtividadeDtoToModel: vi.fn((dto) => ({...dto, mapped: true})),
    mapConhecimentoDtoToModel: vi.fn((dto) => ({...dto, mapped: true})),
    mapCriarAtividadeRequestToDto: vi.fn((req) => ({...req, mapped: true})),
    mapCriarConhecimentoRequestToDto: vi.fn((req) => ({...req, mapped: true})),
}));

describe("atividadeService", () => {
    const { mockApi } = setupServiceTest();

    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("listarAtividades deve buscar e mapear atividades", async () => {
        const dtoList = [{id: 1, descricao: "Atividade DTO"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarAtividades();

        expect(mockApi.get).toHaveBeenCalledWith("/atividades");
        expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalled();
        expect((mappers.mapAtividadeDtoToModel as any).mock.calls[0][0]).toEqual(
            dtoList[0],
        );
        expect(result[0]).toHaveProperty("mapped", true);
    });

    it("obterAtividadePorCodigo deve buscar e mapear uma atividade", async () => {
        const dto = {id: 1, descricao: "Atividade DTO"};
        mockApi.get.mockResolvedValue({data: dto});

        const result = await service.obterAtividadePorCodigo(1);

        expect(mockApi.get).toHaveBeenCalledWith("/atividades/1");
        expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(dto);
        expect(result).toHaveProperty("mapped", true);
    });

    it("criarAtividade deve mapear a requisição, enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request = {descricao: "Nova Atividade"};
        const requestDto = {...request, mapped: true};
        const responseDto = {
            atividade: {id: 2, ...requestDto},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarAtividade(request, 123);

        expect(mappers.mapCriarAtividadeRequestToDto).toHaveBeenCalledWith(
            request,
            123,
        );
        expect(mockApi.post).toHaveBeenCalledWith("/atividades", requestDto);
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("atualizarAtividade deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request: Atividade = {
            codigo: 1,
            descricao: "Atividade Atualizada",
            conhecimentos: [],
        };
        const responseDto = {
            atividade: {...request},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.atualizarAtividade(request.codigo, request);

        const expectedPayload = {
            codigo: request.codigo,
            descricao: request.descricao,
            mapaCodigo: request.mapaCodigo,
        };

        expect(mockApi.post).toHaveBeenCalledWith(
            `/atividades/${request.codigo}/atualizar`,
            expectedPayload,
        );
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("excluirAtividade deve chamar POST e retornar AtividadeOperacaoResponse", async () => {
        const responseDto = {
            atividade: null,
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});
        const result = await service.excluirAtividade(1);
        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/excluir");
        expect(result).toHaveProperty("subprocesso");
    });

    it("listarConhecimentos deve buscar e mapear conhecimentos", async () => {
        const dtoList = [{id: 1, descricao: "Conhecimento DTO"}];
        mockApi.get.mockResolvedValue({data: dtoList});

        const result = await service.listarConhecimentos(1);

        expect(mockApi.get).toHaveBeenCalledWith("/atividades/1/conhecimentos");
        expect(mappers.mapConhecimentoDtoToModel).toHaveBeenCalled();
        expect((mappers.mapConhecimentoDtoToModel as any).mock.calls[0][0]).toEqual(
            dtoList[0],
        );
        expect(result[0]).toHaveProperty("mapped", true);
    });

    it("criarConhecimento deve mapear a requisição, enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request = {descricao: "Novo Conhecimento"};
        const requestDto = {...request, mapped: true};
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: [{id: 2, ...requestDto}]},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.criarConhecimento(1, request);

        expect(mappers.mapCriarConhecimentoRequestToDto).toHaveBeenCalledWith(
            request,
            1
        );
        expect(mockApi.post).toHaveBeenCalledWith(
            "/atividades/1/conhecimentos",
            requestDto,
        );
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("atualizarConhecimento deve enviar POST e retornar AtividadeOperacaoResponse", async () => {
        const request: Conhecimento = {
            id: 1,
            descricao: "Conhecimento Atualizado",
        };
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: [{...request}]},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});

        const result = await service.atualizarConhecimento(1, request.id, request);

        const expectedPayload = {
            codigo: request.id,
            atividadeCodigo: 1,
            descricao: request.descricao,
        };

        expect(mockApi.post).toHaveBeenCalledWith(
            `/atividades/1/conhecimentos/${request.id}/atualizar`,
            expectedPayload,
        );
        expect(result).toHaveProperty("atividade");
        expect(result).toHaveProperty("subprocesso");
    });

    it("excluirConhecimento deve chamar POST e retornar AtividadeOperacaoResponse", async () => {
        const responseDto = {
            atividade: {codigo: 1, descricao: "Atividade", conhecimentos: []},
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
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
        testErrorHandling(() => service.atualizarConhecimento(1, 1, {id: 1} as any), 'post');
        testErrorHandling(() => service.excluirConhecimento(1, 1), 'post');
    });
});

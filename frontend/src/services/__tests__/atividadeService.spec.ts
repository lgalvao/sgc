import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import * as mappers from "@/mappers/atividades";
import type {Atividade, Conhecimento} from "@/types/tipos";
import * as service from "../atividadeService";

vi.mock("@/axios-setup", () => {
    return {
        default: {
            get: vi.fn(),
            post: vi.fn(),
            put: vi.fn(),
            delete: vi.fn(),
        },
    };
});

const mockApi = apiClient as any;

vi.mock("@/mappers/atividades", () => ({
  mapAtividadeDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapConhecimentoDtoToModel: vi.fn((dto) => ({ ...dto, mapped: true })),
  mapCriarAtividadeRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
  mapCriarConhecimentoRequestToDto: vi.fn((req) => ({ ...req, mapped: true })),
}));

describe("atividadeService", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
      vi.clearAllMocks();
      mockApi.get.mockClear();
      mockApi.post.mockClear();
      mockApi.put.mockClear();
      mockApi.delete.mockClear();
  });

    it("listarAtividades should fetch and map atividades", async () => {
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

    it("obterAtividadePorCodigo should fetch and map an atividade", async () => {
        const dto = {id: 1, descricao: "Atividade DTO"};
        mockApi.get.mockResolvedValue({data: dto});

        const result = await service.obterAtividadePorCodigo(1);

        expect(mockApi.get).toHaveBeenCalledWith("/atividades/1");
        expect(mappers.mapAtividadeDtoToModel).toHaveBeenCalledWith(dto);
        expect(result).toHaveProperty("mapped", true);
    });

    it("criarAtividade should map request, post, and return AtividadeOperacaoResponse", async () => {
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

    it("atualizarAtividade should post and return AtividadeOperacaoResponse", async () => {
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

    it("excluirAtividade should call post and return AtividadeOperacaoResponse", async () => {
        const responseDto = {
            atividade: null,
            subprocesso: {codigo: 123, situacao: "CADASTRO_EM_ANDAMENTO", situacaoLabel: "CADASTRO_EM_ANDAMENTO"}
        };
        mockApi.post.mockResolvedValue({data: responseDto});
        const result = await service.excluirAtividade(1);
        expect(mockApi.post).toHaveBeenCalledWith("/atividades/1/excluir");
        expect(result).toHaveProperty("subprocesso");
    });

    it("listarConhecimentos should fetch and map conhecimentos", async () => {
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

    it("criarConhecimento should map request, post, and return AtividadeOperacaoResponse", async () => {
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

    it("atualizarConhecimento should post and return AtividadeOperacaoResponse", async () => {
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

    it("excluirConhecimento should call post and return AtividadeOperacaoResponse", async () => {
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

    // Error handling tests
    it("listarAtividades should throw error on failure", async () => {
        mockApi.get.mockRejectedValue(new Error("Failed"));
        await expect(service.listarAtividades()).rejects.toThrow("Failed");
    });

    it("obterAtividadePorCodigo should throw error on failure", async () => {
        mockApi.get.mockRejectedValue(new Error("Failed"));
        await expect(service.obterAtividadePorCodigo(1)).rejects.toThrow("Failed");
    });

    it("criarAtividade should throw error on failure", async () => {
        const request = {descricao: "Nova Atividade"};
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.criarAtividade(request, 123)).rejects.toThrow(
            "Failed",
        );
    });

    it("atualizarAtividade should throw error on failure", async () => {
        const request: Atividade = {
            codigo: 1,
            descricao: "Atividade Atualizada",
            conhecimentos: [],
        };
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.atualizarAtividade(1, request)).rejects.toThrow(
            "Failed",
        );
    });

    it("excluirAtividade should throw error on failure", async () => {
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.excluirAtividade(1)).rejects.toThrow("Failed");
    });

    it("listarConhecimentos should throw error on failure", async () => {
        mockApi.get.mockRejectedValue(new Error("Failed"));
        await expect(service.listarConhecimentos(1)).rejects.toThrow("Failed");
    });

    it("criarConhecimento should throw error on failure", async () => {
        const request = {descricao: "Novo Conhecimento"};
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.criarConhecimento(1, request)).rejects.toThrow(
            "Failed",
        );
    });

    it("atualizarConhecimento should throw error on failure", async () => {
        const request: Conhecimento = {
            id: 1,
            descricao: "Conhecimento Atualizado",
        };
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.atualizarConhecimento(1, 1, request)).rejects.toThrow(
            "Failed",
        );
    });

    it("excluirConhecimento should throw error on failure", async () => {
        mockApi.post.mockRejectedValue(new Error("Failed"));
        await expect(service.excluirConhecimento(1, 1)).rejects.toThrow("Failed");
    });
});

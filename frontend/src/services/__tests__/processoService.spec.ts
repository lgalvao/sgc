import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeAll, beforeEach, describe, expect, it, type Mock, vi,} from "vitest";
import apiClient from "@/axios-setup";
import {type AtualizarProcessoRequest, type CriarProcessoRequest, TipoProcesso,} from "@/types/tipos";
import * as service from "../processoService";

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

describe("processoService", () => {
    let mockedMappers: typeof import("@/mappers/processos"); // Declarar a variável aqui

    beforeAll(() => {
        vi.doMock("@/mappers/processos", () => ({
            mapProcessoDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
            mapProcessoDetalheDtoToFrontend: vi.fn((dto) => ({
                ...dto,
                mapped: true,
            })),
        }));
    });

    beforeEach(async () => {
        setActivePinia(createPinia());
        mockedMappers = await import("@/mappers/processos");
        vi.spyOn(mockedMappers, "mapProcessoDtoToFrontend") as Mock;
        vi.spyOn(mockedMappers, "mapProcessoDetalheDtoToFrontend") as Mock;
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("iniciarProcesso deve enviar POST com os parâmetros corretos", async () => {
        mockApi.post.mockResolvedValue({});
        await service.iniciarProcesso(1, TipoProcesso.REVISAO, [10, 20]);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/1/iniciar",
            {
                tipo: TipoProcesso.REVISAO,
                unidades: [10, 20],
            },
        );
    });

    it("finalizarProcesso deve enviar POST para o endpoint correto", async () => {
        mockApi.post.mockResolvedValue({});
        await service.finalizarProcesso(1);
        expect(mockApi.post).toHaveBeenCalledWith("/processos/1/finalizar");
    });

    it("excluirProcesso deve chamar o POST", async () => {
        mockApi.post.mockResolvedValue({});
        await service.excluirProcesso(1);
        expect(mockApi.post).toHaveBeenCalledWith("/processos/1/excluir");
    });

    it("buscarProcessosFinalizados deve obter do endpoint correto", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.buscarProcessosFinalizados();
        expect(mockApi.get).toHaveBeenCalledWith("/processos/finalizados");
    });

    it("obterProcessoPorCodigo deve obter do endpoint correto", async () => {
        mockApi.get.mockResolvedValue({data: {}});
        await service.obterProcessoPorCodigo(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1");
    });

    it("atualizarProcesso deve enviar POST para o endpoint correto", async () => {
        const request: AtualizarProcessoRequest = {
            codigo: 1,
            tipo: TipoProcesso.MAPEAMENTO,
            unidades: [],
            descricao: "teste",
            dataLimiteEtapa1: "2025-12-31",
        };
        mockApi.post.mockResolvedValue({data: {}});
        await service.atualizarProcesso(request.codigo, request);
        expect(mockApi.post).toHaveBeenCalledWith(
            `/processos/${request.codigo}/atualizar`,
            request,
        );
    });

    it("obterDetalhesProcesso deve obter do endpoint correto", async () => {
        mockApi.get.mockResolvedValue({data: {}});
        await service.obterDetalhesProcesso(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1/detalhes");
    });

    it("processarAcaoEmBloco deve enviar POST para o endpoint correto", async () => {
        const payload = {
            codProcesso: 1,
            unidades: ["A"],
            tipoAcao: "aceitar" as "aceitar" | "homologar",
            unidadeUsuario: "B",
        };
        mockApi.post.mockResolvedValue({});
        await service.processarAcaoEmBloco(payload);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/1/acoes-em-bloco",
            payload,
        );
    });

    it("buscarSubprocessosElegiveis deve obter do endpoint correto", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.buscarSubprocessosElegiveis(1);
        expect(mockApi.get).toHaveBeenCalledWith(
            "/processos/1/subprocessos-elegiveis",
        );
    });

    it("alterarDataLimiteSubprocesso deve enviar POST para o endpoint correto", async () => {
        const payload = {novaData: "2026-01-01"};
        mockApi.post.mockResolvedValue({});
        await service.alterarDataLimiteSubprocesso(1, payload);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/alterar-data-limite",
            {id: 1, ...payload},
        );
    });

    it("apresentarSugestoes deve enviar POST para o endpoint correto", async () => {
        const payload = {sugestoes: "sugestoes"};
        mockApi.post.mockResolvedValue({});
        await service.apresentarSugestoes(1, payload);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/subprocessos/1/apresentar-sugestoes",
            payload,
        );
    });

    it("validarMapa deve enviar POST para o endpoint correto", async () => {
        mockApi.post.mockResolvedValue({});
        await service.validarMapa(1);
        expect(mockApi.post).toHaveBeenCalledWith("/subprocessos/1/validar-mapa");
    });

    it("buscarSubprocessos deve obter do endpoint correto", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.buscarSubprocessos(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1/subprocessos");
    });

    // Tratamento de erros
    it("criarProcesso deve lançar erro em caso de falha", async () => {
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

import {createPinia, setActivePinia} from "pinia";
import {
    afterEach,
    beforeAll,
    beforeEach,
    describe,
    expect,
    it,
    type Mock,
    vi,
} from "vitest";
import apiClient from "@/axios-setup";
import {
    type AtualizarProcessoRequest,
    type CriarProcessoRequest,
    TipoProcesso,
} from "@/types/tipos";
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

    it("iniciarProcesso should post with correct params", async () => {
        mockApi.post.mockResolvedValue({});
        await service.iniciarProcesso(1, TipoProcesso.REVISAO, [10, 20]);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/1/iniciar?tipo=REVISAO",
            [10, 20],
        );
    });

    it("finalizarProcesso should post to the correct endpoint", async () => {
        mockApi.post.mockResolvedValue({});
        await service.finalizarProcesso(1);
        expect(mockApi.post).toHaveBeenCalledWith("/processos/1/finalizar");
    });

    it("excluirProcesso should call post", async () => {
        mockApi.post.mockResolvedValue({});
        await service.excluirProcesso(1);
        expect(mockApi.post).toHaveBeenCalledWith("/processos/1/excluir");
    });

    it("fetchProcessosFinalizados should get from the correct endpoint", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.fetchProcessosFinalizados();
        expect(mockApi.get).toHaveBeenCalledWith("/processos/finalizados");
    });

    it("obterProcessoPorId should get from the correct endpoint", async () => {
        mockApi.get.mockResolvedValue({data: {}});
        await service.obterProcessoPorId(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1");
    });

    it("atualizarProcesso should post to the correct endpoint", async () => {
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

    it("obterDetalhesProcesso should get from the correct endpoint", async () => {
        mockApi.get.mockResolvedValue({data: {}});
        await service.obterDetalhesProcesso(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1/detalhes");
    });

    it("processarAcaoEmBloco should post to the correct endpoint", async () => {
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

    it("fetchSubprocessosElegiveis should get from the correct endpoint", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.fetchSubprocessosElegiveis(1);
        expect(mockApi.get).toHaveBeenCalledWith(
            "/processos/1/subprocessos-elegiveis",
        );
    });

    it("alterarDataLimiteSubprocesso should post to the correct endpoint", async () => {
        const payload = {novaData: "2026-01-01"};
        mockApi.post.mockResolvedValue({});
        await service.alterarDataLimiteSubprocesso(1, payload);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/alterar-data-limite",
            {id: 1, ...payload},
        );
    });

    it("apresentarSugestoes should post to the correct endpoint", async () => {
        const payload = {sugestoes: "sugestoes"};
        mockApi.post.mockResolvedValue({});
        await service.apresentarSugestoes(1, payload);
        expect(mockApi.post).toHaveBeenCalledWith(
            "/processos/apresentar-sugestoes",
            {id: 1, ...payload},
        );
    });

    it("validarMapa should post to the correct endpoint", async () => {
        mockApi.post.mockResolvedValue({});
        await service.validarMapa(1);
        expect(mockApi.post).toHaveBeenCalledWith("/processos/validar-mapa", {
            id: 1,
        });
    });

    it("buscarSubprocessos should get from the correct endpoint", async () => {
        mockApi.get.mockResolvedValue({data: []});
        await service.buscarSubprocessos(1);
        expect(mockApi.get).toHaveBeenCalledWith("/processos/1/subprocessos");
    });

    // Error handling
    it("criarProcesso should throw error on failure", async () => {
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

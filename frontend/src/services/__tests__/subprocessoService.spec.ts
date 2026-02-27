import {describe, expect, it, vi} from "vitest";
import apiClient from "../../axios-setup";
import * as service from "../subprocessoService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

vi.mock("@/mappers/mapas", () => ({
    mapMapaCompletoDtoToModel: vi.fn(d => d)
}));

vi.mock("@/mappers/atividades", () => ({
    mapAtividadeVisualizacaoToModel: vi.fn(d => d)
}));

describe("subprocessoService", () => {
    it("importarAtividades chama o endpoint correto", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
        await service.importarAtividades(1, 2);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/importar-atividades", { codSubprocessoOrigem: 2 });
    });

    it("listarAtividades chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: { atividadesDisponiveis: [] } });
        await service.listarAtividades(1);
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/1/contexto-edicao");
    });


    it("validarCadastro chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: {} });
        await service.validarCadastro(1);
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/1/validar-cadastro");
    });

    it("obterStatus chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: {} });
        await service.obterStatus(1);
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/1/status");
    });

    it("buscarSubprocessoDetalhe chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: {} });
        await service.buscarSubprocessoDetalhe(1, "ADMIN", 10);
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/1", expect.objectContaining({
            params: { perfil: "ADMIN", unidadeUsuario: 10 }
        }));
    });

    it("buscarSubprocessoPorProcessoEUnidade chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: {} });
        await service.buscarSubprocessoPorProcessoEUnidade(1, "TEST");
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/buscar", expect.objectContaining({
            params: { codProcesso: 1, siglaUnidade: "TEST" }
        }));
    });

    it("adicionarCompetencia chama o endpoint correto", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
        await service.adicionarCompetencia(1, { descricao: "D", atividadesAssociadas: [10] } as any);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/competencia", {
            descricao: "D",
            atividadesIds: [10]
        });
    });

    it("removerCompetencia chama o endpoint correto", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
        await service.removerCompetencia(1, 50);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/competencia/50/remover");
    });

    it("buscarContextoEdicao chama o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({ data: {} });
        await service.buscarContextoEdicao(1, "ADMIN", 10);
        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/1/contexto-edicao", expect.objectContaining({
            params: { perfil: "ADMIN", unidadeUsuario: 10 }
        }));
    });

    it("atualizarCompetencia chama o endpoint correto", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
        await service.atualizarCompetencia(1, 50, { descricao: "D2", atividadesIds: [20] });
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/competencia/50", {
            descricao: "D2",
            atividadesIds: [20]
        });
    });

    it("acoes em bloco chamam os endpoints corretos", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({ data: {} });
        const payload = { unidadeCodigos: [1] };

        await service.aceitarCadastroEmBloco(1, payload);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/aceitar-cadastro-bloco", {
            acao: 'ACEITAR',
            subprocessos: [1]
        });

        await service.homologarCadastroEmBloco(1, payload);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/homologar-cadastro-bloco", {
            acao: 'HOMOLOGAR',
            subprocessos: [1]
        });

        await service.aceitarValidacaoEmBloco(1, payload);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/aceitar-validacao-bloco", {
            acao: 'ACEITAR_VALIDACAO',
            subprocessos: [1]
        });

        await service.homologarValidacaoEmBloco(1, payload);
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/homologar-validacao-bloco", {
            acao: 'HOMOLOGAR_VALIDACAO',
            subprocessos: [1]
        });

        await service.disponibilizarMapaEmBloco(1, { ...payload, dataLimite: '2025-01-01' });
        expect(apiClient.post).toHaveBeenCalledWith("/subprocessos/1/disponibilizar-mapa-bloco", {
            acao: 'DISPONIBILIZAR',
            subprocessos: [1],
            dataLimite: '2025-01-01'
        });
    });
});

import {beforeEach, describe, expect, it, vi} from "vitest";
import {useRelatorioAndamentoTela} from "../useRelatorioAndamentoTela";
import * as painelService from "@/services/painelService";

const mockNotify = vi.fn();

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({
        notify: mockNotify,
    }),
}));

vi.mock("@/services/painelService", () => ({
    listarProcessos: vi.fn(),
}));

describe("useRelatorioAndamentoTela", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve carregar processos com sucesso", async () => {
        const mockResponse = {
            content: [{codigo: 1, descricao: "Processo 1"}],
        };
        vi.mocked(painelService.listarProcessos).mockResolvedValue(mockResponse as any);

        const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
        expect(processosDisponiveis.value).toEqual([]);

        await carregarProcessos();

        expect(painelService.listarProcessos).toHaveBeenCalledWith({page: 0, size: 100});
        expect(processosDisponiveis.value).toEqual(mockResponse.content);
        expect(mockNotify).not.toHaveBeenCalled();
    });

    it("deve lidar com resposta vazia ou nula", async () => {
        vi.mocked(painelService.listarProcessos).mockResolvedValue(null as any);

        const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
        await carregarProcessos();

        expect(processosDisponiveis.value).toEqual([]);
        expect(mockNotify).not.toHaveBeenCalled();
    });

    it("deve notificar erro em caso de falha no carregamento", async () => {
        vi.mocked(painelService.listarProcessos).mockRejectedValue(new Error("Erro do servidor"));

        const {processosDisponiveis, carregarProcessos} = useRelatorioAndamentoTela();
        await carregarProcessos();

        expect(processosDisponiveis.value).toEqual([]);
        expect(mockNotify).toHaveBeenCalledWith("Erro ao carregar processos", "danger");
    });
});

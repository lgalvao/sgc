import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {Alerta} from "@/types/tipos";
import {useAlertasStore} from "../alertas";
import {normalizeError} from "@/utils/apiError";

// Mock dos serviços
vi.mock("@/services/painelService");
vi.mock("@/services/alertaService");

// Mock do perfilStore para garantir que a mesma instância seja usada
const mockPerfilStoreValues = {
    usuarioCodigo: "123" as string | null,
    unidadeSelecionada: "456" as string | null,
};
vi.mock("../perfil", () => ({
    usePerfilStore: vi.fn(() => mockPerfilStoreValues),
}));

describe("useAlertasStore", () => {
    const context = setupStoreTest(useAlertasStore);
    let painelService: Mocked<typeof import("@/services/painelService")>;
    let alertaService: Mocked<typeof import("@/services/alertaService")>;

    const mockAlerta: Alerta = {
        codigo: 1,
        codProcesso: 1,
        unidadeOrigem: "Origem",
        unidadeDestino: "Destino",
        descricao: "Alerta Teste",
        dataHora: "2025-01-01T10:00:00",
        dataHoraLeitura: null,
        mensagem: "Mensagem Teste",
        dataHoraFormatada: "01/01/2025 10:00",
        origem: "Origem",
        processo: "Processo Teste",
    };

    beforeEach(async () => {
        // Resetar o estado do mock antes de cada teste
        mockPerfilStoreValues.usuarioCodigo = "123";
        mockPerfilStoreValues.unidadeSelecionada = "456";

        painelService = (await import("@/services/painelService")) as Mocked<
            typeof import("@/services/painelService")
        >;
        alertaService = (await import("@/services/alertaService")) as Mocked<
            typeof import("@/services/alertaService")
        >;

        // Reset calls
        vi.clearAllMocks();
    });

    it("deve inicializar com alertas simulados e datas formatadas", () => {
        expect(context.store.alertas).toEqual([]);
        expect(context.store.alertasPage).toEqual({});
    });

    it("deve limpar o erro com clearError", () => {
        context.store.lastError = normalizeError(new Error("Erro de teste"));
        context.store.clearError();
        expect(context.store.lastError).toBeNull();
    });

    describe("actions", () => {
        describe("buscarAlertas", () => {
            it("deve chamar painelService e atualizar o estado", async () => {
                const mockPage = {
                    content: [mockAlerta],
                    totalPages: 1,
                    totalElements: 1,
                    number: 0,
                    size: 10,
                    first: true,
                    last: true,
                    empty: false,
                };
                painelService.listarAlertas.mockResolvedValue(mockPage);

                await context.store.buscarAlertas("123", 456, 0, 10);

                expect(painelService.listarAlertas).toHaveBeenCalledWith(
                    "123",
                    456,
                    0,
                    10,
                    undefined,
                    undefined,
                );
                expect(context.store.alertas).toEqual(mockPage.content);
                expect(context.store.alertasPage).toEqual(mockPage);
                expect(context.store.lastError).toBeNull();
            });

            it("deve lidar com erros no serviço", async () => {
                const error = new Error("Erro na busca");
                painelService.listarAlertas.mockRejectedValue(error);

                await expect(
                    context.store.buscarAlertas("123", 456, 0, 10)
                ).rejects.toThrow("Erro na busca");

                expect(context.store.lastError).toEqual(normalizeError(error));
            });
        });

        describe("marcarAlertaComoLido", () => {
            it("deve realizar update otimista e chamar alertaService sem recarregar tudo", async () => {
                // Setup initial state
                context.store.alertas = [JSON.parse(JSON.stringify(mockAlerta))];
                alertaService.marcarComoLido.mockResolvedValue();

                const result = await context.store.marcarAlertaComoLido(1);

                expect(result).toBe(true);
                // Check optimistic update: dataHoraLeitura should be set (it was null in mockAlerta)
                expect(context.store.alertas[0].dataHoraLeitura).not.toBeNull();

                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                // Should NOT reload
                expect(painelService.listarAlertas).not.toHaveBeenCalled();
            });

            it("deve reverter estado em caso de falha do serviço", async () => {
                // Setup initial state
                context.store.alertas = [JSON.parse(JSON.stringify(mockAlerta))];

                alertaService.marcarComoLido.mockRejectedValue(
                    new Error("Falha no serviço"),
                );

                const result = await context.store.marcarAlertaComoLido(1);

                expect(result).toBe(false);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                // Revert check: dataHoraLeitura should be null again
                expect(context.store.alertas[0].dataHoraLeitura).toBeNull();
                expect(context.store.lastError).not.toBeNull();
            });

            it("deve lidar com alerta não encontrado na lista local", async () => {
                context.store.alertas = []; // Lista vazia
                alertaService.marcarComoLido.mockResolvedValue();

                const result = await context.store.marcarAlertaComoLido(999);

                expect(result).toBe(true);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(999);
                // Como não estava na lista, nada muda localmente, mas chama o serviço
            });
        });
    });
});

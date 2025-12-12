import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import {initPinia} from "@/test-utils/helpers";
import type {Alerta} from "@/types/tipos";
import {useAlertasStore} from "../alertas";

// Mock dos serviços
vi.mock("@/services/painelService");
vi.mock("@/services/alertaService");

// Mock do perfilStore para garantir que a mesma instância seja usada
const mockPerfilStoreValues = {
    servidorId: 123 as number | null,
    unidadeSelecionada: "456" as string | null,
};
vi.mock("../perfil", () => ({
    usePerfilStore: vi.fn(() => mockPerfilStoreValues),
}));

describe("useAlertasStore", () => {
    let alertasStore: ReturnType<typeof useAlertasStore>;
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
        initPinia();
        alertasStore = useAlertasStore();

        // Resetar o estado do mock antes de cada teste
        mockPerfilStoreValues.servidorId = 123;
        mockPerfilStoreValues.unidadeSelecionada = "456";

        painelService = (await import("@/services/painelService")) as Mocked<
            typeof import("@/services/painelService")
        >;
        alertaService = (await import("@/services/alertaService")) as Mocked<
            typeof import("@/services/alertaService")
        >;

        vi.clearAllMocks();
    });

    it("should initialize with mock alerts and parsed dates", () => {
        expect(alertasStore.alertas).toEqual([]);
        expect(alertasStore.alertasPage).toEqual({});
    });

    describe("actions", () => {
        it("buscarAlertas should call painelService and update state", async () => {
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

            await alertasStore.buscarAlertas(123, 456, 0, 10);

            expect(painelService.listarAlertas).toHaveBeenCalledWith(
                123,
                456,
                0,
                10,
                undefined,
                undefined,
            );
            expect(alertasStore.alertas).toEqual(mockPage.content);
            expect(alertasStore.alertasPage).toEqual(mockPage);
        });

        describe("marcarAlertaComoLido", () => {
            it("should call alertaService and reload alerts on success", async () => {
                alertaService.marcarComoLido.mockResolvedValue();
                const mockReloadPage = {
                    content: [
                        {...mockAlerta, codigo: 2, descricao: "Alerta Recarregado"},
                    ],
                    totalPages: 1,
                    totalElements: 1,
                    number: 0,
                    size: 10,
                    first: true,
                    last: true,
                    empty: false,
                };
                painelService.listarAlertas.mockResolvedValue(mockReloadPage);

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(true);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                expect(painelService.listarAlertas).toHaveBeenCalledWith(
                    123,
                    456,
                    0,
                    20,
                    undefined,
                    undefined,
                );
                expect(alertasStore.alertas).toEqual(mockReloadPage.content);
            });

            it("should return false on service failure", async () => {
                alertaService.marcarComoLido.mockRejectedValue(
                    new Error("Falha no serviço"),
                );

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(false);
                expect(painelService.listarAlertas).not.toHaveBeenCalled();
            });

            it("should handle missing perfilStore data", async () => {
                mockPerfilStoreValues.servidorId = null;
                mockPerfilStoreValues.unidadeSelecionada = null;

                alertaService.marcarComoLido.mockResolvedValue();

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(true);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                expect(painelService.listarAlertas).not.toHaveBeenCalled();
            });
        });
    });
});

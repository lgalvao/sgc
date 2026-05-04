import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {relatoriosService} from "@/services/relatoriosService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
    },
}));

describe("relatoriosService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        // Mock URL.createObjectURL and URL.revokeObjectURL
        global.URL.createObjectURL = vi.fn(() => "mock-url");
        global.URL.revokeObjectURL = vi.fn();
    });

    it("deve chamar obterRelatorioAndamento com o código do processo correto", async () => {
        const mockData = [{siglaUnidade: "UNIT1", nomeUnidade: "Unit 1", situacaoAtual: "OK"}];
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockData});

        const result = await relatoriosService.obterRelatorioAndamento(123);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/andamento/123");
        expect(result).toEqual(mockData);
    });

    it("deve chamar obterRelatorioMapas com os códigos das unidades corretos", async () => {
        const mockData = [{
            codigoUnidade: 1,
            siglaUnidade: "SEC",
            nomeUnidade: "Secretaria",
            totalCompetencias: 1,
            competencias: []
        }];
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockData});

        const result = await relatoriosService.obterRelatorioMapas([123, 456]);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas", {
            params: {
                codUnidade: [123, 456]
            }
        });
        expect(result).toEqual(mockData);
    });

    it("deve chamar downloadRelatorioMapasPdf com os códigos das unidades quando fornecidos", async () => {
        const mockBlob = new Blob(["pdf content"], {type: "application/pdf"});
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockBlob});

        const mockLink = {
            href: "",
            setAttribute: vi.fn(),
            click: vi.fn(),
            remove: vi.fn(),
        };
        vi.spyOn(document, "createElement").mockReturnValue(mockLink as any);
        vi.spyOn(document.body, "appendChild").mockImplementation(() => mockLink as any);

        await relatoriosService.downloadRelatorioMapasPdf([123, 456]);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas/exportar", {
            params: {
                codUnidade: [123, 456]
            },
            responseType: "blob",
        });
        expect(document.createElement).toHaveBeenCalledWith("a");
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "relatorio-mapas-vigentes.pdf");
        expect(mockLink.click).toHaveBeenCalled();
        expect(mockLink.remove).toHaveBeenCalled();
    });

    it("deve chamar downloadRelatorioAndamentoPdf e disparar o download", async () => {
        const mockBlob = new Blob(["pdf content"], {type: "application/pdf"});
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockBlob});

        const mockLink = {
            href: "",
            setAttribute: vi.fn(),
            click: vi.fn(),
            remove: vi.fn(),
        };
        vi.spyOn(document, "createElement").mockReturnValue(mockLink as any);
        vi.spyOn(document.body, "appendChild").mockImplementation(() => mockLink as any);

        await relatoriosService.downloadRelatorioAndamentoPdf(123);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/andamento/123/exportar", {
            responseType: "blob",
        });
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "relatorio-andamento-123.pdf");
    });
});

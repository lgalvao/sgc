import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {relatoriosService} from "@/services/relatoriosService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
    },
}));

vi.mock("@/utils/date", () => ({
    obterHojeFormatado: vi.fn(() => "2026-05-07"),
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

    it("deve chamar obterRelatorioMapaAtual com o subprocesso correto", async () => {
        const mockData = {
            codigoUnidade: 1,
            siglaUnidade: "SEC",
            nomeUnidade: "Secretaria",
            totalCompetencias: 1,
            competencias: []
        };
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockData});

        const result = await relatoriosService.obterRelatorioMapaAtual(321);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas/subprocessos/321");
        expect(result).toEqual(mockData);
    });

    it("deve chamar obterRelatorioMapaVigenteUnidade com a unidade correta", async () => {
        const mockData = {
            codigoUnidade: 1,
            siglaUnidade: "SEC",
            nomeUnidade: "Secretaria",
            totalCompetencias: 1,
            competencias: []
        };
        vi.mocked(apiClient.get).mockResolvedValueOnce({data: mockData});

        const result = await relatoriosService.obterRelatorioMapaVigenteUnidade(123);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas-vigentes/unidades/123");
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
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-mapas-2026-05-07.pdf");
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
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-andamento-2026-05-07.pdf");
    });

    it("deve chamar downloadRelatorioMapaAtualPdf e disparar o download", async () => {
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

        await relatoriosService.downloadRelatorioMapaAtualPdf(456);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas/subprocessos/456/exportar", {
            responseType: "blob",
        });
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-mapa-atual-2026-05-07.pdf");
    });

    it("deve chamar downloadRelatorioMapaVigenteUnidadePdf e disparar o download", async () => {
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

        await relatoriosService.downloadRelatorioMapaVigenteUnidadePdf(123);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas-vigentes/unidades/123/exportar", {
            responseType: "blob",
        });
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-mapa-vigente-2026-05-07.pdf");
    });

    it("deve gerar CSV do mapa atual a partir do JSON do relatório", async () => {
        vi.mocked(apiClient.get).mockResolvedValueOnce({
            data: {
                codigoUnidade: 1,
                siglaUnidade: "SEC",
                nomeUnidade: "Secretaria",
                totalCompetencias: 1,
                competencias: [{
                    codigo: 10,
                    descricao: "Competência 1",
                    atividades: [{
                        codigo: 20,
                        descricao: 'Atividade "A"',
                        conhecimentos: [{codigo: 30, descricao: "Conhecimento 1"}]
                    }]
                }]
            }
        });

        const mockLink = {
            href: "",
            setAttribute: vi.fn(),
            click: vi.fn(),
            remove: vi.fn(),
        };
        vi.spyOn(document, "createElement").mockReturnValue(mockLink as any);
        vi.spyOn(document.body, "appendChild").mockImplementation(() => mockLink as any);

        await relatoriosService.downloadRelatorioMapaAtualCsv(456);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas/subprocessos/456");
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-mapa-atual-2026-05-07.csv");

        const blob = vi.mocked(global.URL.createObjectURL).mock.calls[0]?.[0] as Blob;
        await expect(blob.text()).resolves.toContain('"Competência";"Atividade";"Conhecimento"');
        await expect(blob.text()).resolves.toContain('"Competência 1";"Atividade ""A""";"Conhecimento 1"');
        await expect(blob.text()).resolves.not.toContain('"SEC";"Secretaria"');
    });

    it("deve gerar CSV do mapa vigente da unidade a partir do JSON do relatório", async () => {
        vi.mocked(apiClient.get).mockResolvedValueOnce({
            data: {
                codigoUnidade: 1,
                siglaUnidade: "SEC",
                nomeUnidade: "Secretaria",
                totalCompetencias: 1,
                competencias: [{
                    codigo: 10,
                    descricao: "Competência 1",
                    atividades: []
                }]
            }
        });

        const mockLink = {
            href: "",
            setAttribute: vi.fn(),
            click: vi.fn(),
            remove: vi.fn(),
        };
        vi.spyOn(document, "createElement").mockReturnValue(mockLink as any);
        vi.spyOn(document.body, "appendChild").mockImplementation(() => mockLink as any);

        await relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv(123);

        expect(apiClient.get).toHaveBeenCalledWith("/relatorios/mapas-vigentes/unidades/123");
        expect(mockLink.setAttribute).toHaveBeenCalledWith("download", "sgc-rel-mapa-vigente-2026-05-07.csv");

        const blob = vi.mocked(global.URL.createObjectURL).mock.calls[0]?.[0] as Blob;
        await expect(blob.text()).resolves.toContain('"Competência";"Atividade";"Conhecimento"');
        await expect(blob.text()).resolves.toContain('"Competência 1";"";""');
        await expect(blob.text()).resolves.not.toContain('"SEC";"Secretaria"');
    });
});

import {beforeEach, describe, expect, it, vi} from "vitest";
import * as unidadesService from "@/services/unidadeService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {Unidade} from "@/types/tipos";
import {useUnidadesStore} from "../unidades";

const mockUnidades: Unidade[] = [
    {
        sigla: "ADMIN",
        nome: "Administração",
        tipo: "INTEROPERACIONAL",
        usuarioCodigo: 7,
        responsavel: null,
        codigo: 1,
        filhas: [
            {
                sigla: "SGP",
                nome: "Secretaria de Gestao de Pessoas",
                tipo: "INTERMEDIARIA",
                usuarioCodigo: 2,
                responsavel: null,
                filhas: [],
                codigo: 3,
            },
            {
                sigla: "COEDE",
                nome: "Coordenadoria de Educação Especial",
                tipo: "INTERMEDIARIA",
                usuarioCodigo: 3,
                responsavel: null,
                filhas: [
                    {
                        sigla: "SEMARE",
                        nome: "Seção Magistrados e Requisitados",
                        tipo: "OPERACIONAL",
                        usuarioCodigo: 4,
                        responsavel: null,
                        filhas: [],
                        codigo: 5,
                    },
                ],
                codigo: 4,
            },
        ],
    },
];

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(() => Promise.resolve(mockUnidades)),
    buscarUnidadePorSigla: vi.fn(),
    buscarUnidadePorCodigo: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(() => Promise.resolve(mockUnidades)),
    buscarArvoreUnidade: vi.fn(),
    buscarSubordinadas: vi.fn(),
    buscarSuperior: vi.fn(),
}));

describe("useUnidadesStore", () => {
    const context = setupStoreTest(useUnidadesStore);

    beforeEach(() => {
        context.store.unidades = mockUnidades;
    });

    it("deve inicializar com unidades simuladas", () => {
        expect(context.store.unidades.length).toBeGreaterThan(0);
        expect(context.store.unidades[0].sigla).toBeDefined();
    });

    describe("actions", () => {
        it("deve buscar e definir unidades", async () => {
            context.store.unidades = [];
            await context.store.buscarUnidadesParaProcesso("MAPEAMENTO");
            expect(
                unidadesService.buscarArvoreComElegibilidade,
            ).toHaveBeenCalledTimes(1);
            expect(context.store.unidades.length).toBeGreaterThan(0);
        });

        it("deve lidar com erro em buscarUnidadesParaProcesso", async () => {
            vi.mocked(
                unidadesService.buscarArvoreComElegibilidade,
            ).mockRejectedValueOnce(new Error("API Error"));
            await expect(
                context.store.buscarUnidadesParaProcesso("MAPEAMENTO"),
            ).rejects.toThrow("API Error");
            expect(context.store.error).toContain("API Error");
        });

        it("buscarUnidade deve definir estado da unidade", async () => {
            const mockUnit = { sigla: "TEST", nome: "Test Unit" };
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue(
                mockUnit as any,
            );

            await context.store.buscarUnidade("TEST");

            expect(unidadesService.buscarUnidadePorSigla).toHaveBeenCalledWith(
                "TEST",
            );
            expect(context.store.unidade).toEqual(mockUnit);
        });

        it("buscarUnidade deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(context.store.buscarUnidade("TEST")).rejects.toThrow(
                "Fail",
            );
            expect(context.store.error).toContain("Fail");
        });

        it("buscarUnidadePorCodigo deve definir estado da unidade", async () => {
            const mockUnit = { codigo: 123, nome: "Test Unit" };
            vi.mocked(unidadesService.buscarUnidadePorCodigo).mockResolvedValue(
                mockUnit as any,
            );

            await context.store.buscarUnidadePorCodigo(123);

            expect(unidadesService.buscarUnidadePorCodigo).toHaveBeenCalledWith(
                123,
            );
            expect(context.store.unidade).toEqual(mockUnit);
        });

        it("buscarUnidadePorCodigo deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarUnidadePorCodigo).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(
                context.store.buscarUnidadePorCodigo(123),
            ).rejects.toThrow("Fail");
            expect(context.store.error).toContain("Fail");
        });

        it("buscarArvoreUnidade deve chamar serviço e definir unidade", async () => {
            const mockUnit = { codigo: 1, filhas: [] };
            vi.mocked(unidadesService.buscarArvoreUnidade).mockResolvedValue(
                mockUnit as any,
            );

            await context.store.buscarArvoreUnidade(1);

            expect(unidadesService.buscarArvoreUnidade).toHaveBeenCalledWith(1);
            expect(context.store.unidade).toEqual(mockUnit);
        });

        it("buscarArvoreUnidade deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarArvoreUnidade).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(context.store.buscarArvoreUnidade(1)).rejects.toThrow(
                "Fail",
            );
            expect(context.store.error).toContain("Fail");
        });

        it("obterUnidadesSubordinadas deve chamar serviço", async () => {
            const mockSubordinadas = ["A", "B"];
            vi.mocked(unidadesService.buscarSubordinadas).mockResolvedValue(
                mockSubordinadas,
            );

            const result = await context.store.obterUnidadesSubordinadas("TEST");

            expect(unidadesService.buscarSubordinadas).toHaveBeenCalledWith(
                "TEST",
            );
            expect(result).toEqual(mockSubordinadas);
        });

        it("obterUnidadesSubordinadas deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarSubordinadas).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(
                context.store.obterUnidadesSubordinadas("TEST"),
            ).rejects.toThrow("Fail");
            expect(context.store.error).toContain("Fail");
        });

        it("obterUnidadeSuperior deve chamar serviço", async () => {
            const mockSuperior = "SUP";
            vi.mocked(unidadesService.buscarSuperior).mockResolvedValue(
                mockSuperior,
            );

            const result = await context.store.obterUnidadeSuperior("TEST");

            expect(unidadesService.buscarSuperior).toHaveBeenCalledWith("TEST");
            expect(result).toEqual(mockSuperior);
        });

        it("obterUnidadeSuperior deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarSuperior).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(
                context.store.obterUnidadeSuperior("TEST"),
            ).rejects.toThrow("Fail");
            expect(context.store.error).toContain("Fail");
        });

        it("buscarTodasAsUnidades deve definir unidades", async () => {
            context.store.unidades = [];
            await context.store.buscarTodasAsUnidades();
            expect(unidadesService.buscarTodasUnidades).toHaveBeenCalled();
            expect(context.store.unidades.length).toBeGreaterThan(0);
        });

        it("buscarTodasAsUnidades deve lidar com erro", async () => {
            vi.mocked(unidadesService.buscarTodasUnidades).mockRejectedValueOnce(new Error("API Error"));
            await expect(context.store.buscarTodasAsUnidades()).rejects.toThrow("API Error");
            expect(context.store.error).toContain("API Error");
        });

        it("clearError deve limpar erros", () => {
            context.store.error = "Algo deu errado";
            context.store.clearError();
            expect(context.store.error).toBeNull();
        });
    });
});


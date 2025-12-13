import {beforeEach, describe, expect, it, vi} from "vitest";
import * as unidadesService from "@/services/unidadesService";
import {initPinia} from "@/test-utils/helpers";

import type {Unidade} from "@/types/tipos";
import {useUnidadesStore} from "../unidades";

const mockUnidades: Unidade[] = [
    {
        sigla: "SEDOC",
        nome: "Seção de Desenvolvimento Organizacional e Capacitação",
        tipo: "INTEROPERACIONAL",
        idServidorTitular: 7,
        responsavel: null,
        codigo: 1,
        filhas: [
            {
                sigla: "SGP",
                nome: "Secretaria de Gestao de Pessoas",
                tipo: "INTERMEDIARIA",
                idServidorTitular: 2,
                responsavel: null,
                filhas: [],
                codigo: 3,
            },
            {
                sigla: "COEDE",
                nome: "Coordenadoria de Educação Especial",
                tipo: "INTERMEDIARIA",
                idServidorTitular: 3,
                responsavel: null,
                filhas: [
                    {
                        sigla: "SEMARE",
                        nome: "Seção Magistrados e Requisitados",
                        tipo: "OPERACIONAL",
                        idServidorTitular: 4,
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

vi.mock("@/services/unidadesService", () => ({
    buscarTodasUnidades: vi.fn(() => Promise.resolve(mockUnidades)),
    buscarUnidadePorSigla: vi.fn(),
    buscarUnidadePorCodigo: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(() => Promise.resolve(mockUnidades)),
    buscarArvoreUnidade: vi.fn(),
    buscarSubordinadas: vi.fn(),
    buscarSuperior: vi.fn(),
}));

describe("useUnidadesStore", () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;

    beforeEach(() => {
        initPinia();
        unidadesStore = useUnidadesStore();
        unidadesStore.unidades = mockUnidades;
        vi.clearAllMocks();
    });

    it("should initialize with mock unidades", () => {
        expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        expect(unidadesStore.unidades[0].sigla).toBeDefined();
    });

    describe("actions", () => {
        it("should fetch and set unidades", async () => {
            unidadesStore.unidades = [];
            await unidadesStore.buscarUnidadesParaProcesso("MAPEAMENTO");
            expect(
                unidadesService.buscarArvoreComElegibilidade,
            ).toHaveBeenCalledTimes(1);
            expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        });

        it("should handle error in buscarUnidadesParaProcesso", async () => {
            vi.mocked(
                unidadesService.buscarArvoreComElegibilidade,
            ).mockRejectedValueOnce(new Error("API Error"));
            await unidadesStore.buscarUnidadesParaProcesso("MAPEAMENTO");
            expect(unidadesStore.error).toContain("API Error");
        });

        it("buscarUnidade should set unidade state", async () => {
            const mockUnit = {sigla: "TEST", nome: "Test Unit"};
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue(
                mockUnit as any,
            );

            await unidadesStore.buscarUnidade("TEST");

            expect(unidadesService.buscarUnidadePorSigla).toHaveBeenCalledWith(
                "TEST",
            );
            expect(unidadesStore.unidade).toEqual(mockUnit);
        });

        it("buscarUnidade should handle error", async () => {
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockRejectedValue(
                new Error("Fail"),
            );
            await unidadesStore.buscarUnidade("TEST");
            expect(unidadesStore.error).toContain("Fail");
        });

        it("buscarUnidadePorCodigo should set unidade state", async () => {
            const mockUnit = {codigo: 123, nome: "Test Unit"};
            vi.mocked(unidadesService.buscarUnidadePorCodigo).mockResolvedValue(
                mockUnit as any,
            );

            await unidadesStore.buscarUnidadePorCodigo(123);

            expect(unidadesService.buscarUnidadePorCodigo).toHaveBeenCalledWith(123);
            expect(unidadesStore.unidade).toEqual(mockUnit);
        });

        it("buscarUnidadePorCodigo should handle error", async () => {
            vi.mocked(unidadesService.buscarUnidadePorCodigo).mockRejectedValue(
                new Error("Fail"),
            );
            await unidadesStore.buscarUnidadePorCodigo(123);
            expect(unidadesStore.error).toContain("Fail");
        });

        it("buscarArvoreUnidade should call service and set unidade", async () => {
            const mockUnit = {codigo: 1, filhas: []};
            vi.mocked(unidadesService.buscarArvoreUnidade).mockResolvedValue(mockUnit as any);

            await unidadesStore.buscarArvoreUnidade(1);

            expect(unidadesService.buscarArvoreUnidade).toHaveBeenCalledWith(1);
            expect(unidadesStore.unidade).toEqual(mockUnit);
        });

        it("buscarArvoreUnidade should handle error", async () => {
            vi.mocked(unidadesService.buscarArvoreUnidade).mockRejectedValue(new Error("Fail"));
            await unidadesStore.buscarArvoreUnidade(1);
            expect(unidadesStore.error).toContain("Fail");
        });

        it("obterUnidadesSubordinadas should call service", async () => {
            const mockSubordinadas = ["A", "B"];
            vi.mocked(unidadesService.buscarSubordinadas).mockResolvedValue(mockSubordinadas);

            const result = await unidadesStore.obterUnidadesSubordinadas("TEST");

            expect(unidadesService.buscarSubordinadas).toHaveBeenCalledWith("TEST");
            expect(result).toEqual(mockSubordinadas);
        });

        it("obterUnidadesSubordinadas should handle error", async () => {
            vi.mocked(unidadesService.buscarSubordinadas).mockRejectedValue(new Error("Fail"));
            const result = await unidadesStore.obterUnidadesSubordinadas("TEST");
            expect(unidadesStore.error).toContain("Fail");
            expect(result).toEqual([]);
        });

        it("obterUnidadeSuperior should call service", async () => {
            const mockSuperior = "SUP";
            vi.mocked(unidadesService.buscarSuperior).mockResolvedValue(mockSuperior);

            const result = await unidadesStore.obterUnidadeSuperior("TEST");

            expect(unidadesService.buscarSuperior).toHaveBeenCalledWith("TEST");
            expect(result).toEqual(mockSuperior);
        });

        it("obterUnidadeSuperior should handle error", async () => {
            vi.mocked(unidadesService.buscarSuperior).mockRejectedValue(new Error("Fail"));
            const result = await unidadesStore.obterUnidadeSuperior("TEST");
            expect(unidadesStore.error).toContain("Fail");
            expect(result).toBeNull();
        });
    });
});

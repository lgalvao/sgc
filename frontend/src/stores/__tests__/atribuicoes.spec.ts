import {beforeEach, describe, expect, it, vi} from "vitest";
import * as atribuicaoTemporariaService from "@/services/atribuicaoTemporariaService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {AtribuicaoTemporaria} from "@/types/tipos";
import {useAtribuicaoTemporariaStore} from "../atribuicoes";
import {normalizeError} from "@/utils/apiError";
import {logger} from "@/utils";

// Define mock data shape that matches what the service returns
const serviceMockData = [
    {
        codigo: 1,
        unidade: { codigo: 1, nome: "A", sigla: "A" },
        usuario: {
            codigo: 1,
            nome: "Servidor 1",
            tituloEleitoral: "123",
            unidade: { codigo: 1, nome: "A", sigla: "A" },
        },
        dataInicio: "2025-01-01",
        dataTermino: "2025-01-31",
        justificativa: "J1",
    },
    {
        codigo: 2,
        unidade: { codigo: 2, nome: "B", sigla: "B" },
        servidor: { // Different field name to test 'usuario || servidor' logic
            codigo: 2,
            nome: "Servidor 2",
            tituloEleitoral: "456",
            unidade: { codigo: 2, nome: "B", sigla: "B" },
        },
        dataInicio: "2025-02-01",
        dataTermino: "2025-02-28",
        justificativa: "J2",
    },
];

vi.mock("@/services/atribuicaoTemporariaService", () => ({
    buscarTodasAtribuicoes: vi.fn(),
}));

// Mock logger
vi.mock("@/utils", async (importOriginal) => {
    const actual = await importOriginal<any>();
    return {
        ...actual,
        logger: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    }
});

describe("useAtribuicaoTemporariaStore", () => {
    const context = setupStoreTest(useAtribuicaoTemporariaStore);

    beforeEach(() => {
        vi.clearAllMocks();
        context.store.atribuicoes = [];
    });

    it("deve limpar o erro com clearError", () => {
        context.store.lastError = normalizeError(new Error("Erro de teste"));
        context.store.error = "Erro de teste";
        context.store.clearError();
        expect(context.store.lastError).toBeNull();
        expect(context.store.error).toBeNull();
    });

    describe("actions", () => {
        it("buscarAtribuicoes deve buscar e mapear atribuições corretamente (array direto)", async () => {
            (atribuicaoTemporariaService.buscarTodasAtribuicoes as any).mockResolvedValue(serviceMockData);

            await context.store.buscarAtribuicoes();

            expect(atribuicaoTemporariaService.buscarTodasAtribuicoes).toHaveBeenCalledTimes(1);
            expect(context.store.atribuicoes.length).toBe(2);
            expect(context.store.atribuicoes[0].codigo).toBe(1);
            expect(context.store.atribuicoes[0].usuario.nome).toBe("Servidor 1");
            expect(context.store.atribuicoes[1].codigo).toBe(2);
            expect(context.store.atribuicoes[1].usuario.nome).toBe("Servidor 2");
        });

        it("buscarAtribuicoes deve lidar com resposta encapsulada em .data", async () => {
            (atribuicaoTemporariaService.buscarTodasAtribuicoes as any).mockResolvedValue({
                data: serviceMockData
            });

            await context.store.buscarAtribuicoes();

            expect(context.store.atribuicoes.length).toBe(2);
        });

        it("buscarAtribuicoes deve lidar com resposta inválida (não array)", async () => {
            (atribuicaoTemporariaService.buscarTodasAtribuicoes as any).mockResolvedValue({
                foo: 'bar'
            });

            await context.store.buscarAtribuicoes();

            expect(context.store.atribuicoes).toEqual([]);
            expect(logger.error).toHaveBeenCalledWith("Expected array but got:", undefined);
        });

        it("buscarAtribuicoes deve lidar com erros", async () => {
            const error = new Error("Failed");
            (atribuicaoTemporariaService.buscarTodasAtribuicoes as any).mockRejectedValue(error);

            await expect(context.store.buscarAtribuicoes()).rejects.toThrow("Failed");

            expect(context.store.error).toBe("Failed");
            expect(context.store.lastError).toEqual(normalizeError(error));
            expect(context.store.isLoading).toBe(false);
        });
    });

    describe("getters", () => {
        // Populating store manually for getter tests
        beforeEach(() => {
             context.store.atribuicoes = [
                 {
                     codigo: 1,
                     unidade: { codigo: 1, nome: "A", sigla: "A" },
                     usuario: { codigo: 1, nome: "S1" },
                     dataInicio: "2025-01-01",
                     dataFim: "2025-01-31",
                     dataTermino: "2025-01-31",
                     justificativa: "J1"
                 },
                 {
                     codigo: 2,
                     unidade: { codigo: 2, nome: "B", sigla: "B" },
                     usuario: { codigo: 2, nome: "S2" },
                     dataInicio: "2025-02-01",
                     dataFim: "2025-02-28",
                     dataTermino: "2025-02-28",
                     justificativa: "J2"
                 },
                  {
                     codigo: 3,
                     unidade: { codigo: 3, nome: "C", sigla: "C" },
                     usuario: { codigo: 1, nome: "S1" },
                     dataInicio: "2025-03-01",
                     dataFim: "2025-03-31",
                     dataTermino: "2025-03-31",
                     justificativa: "J3"
                 },
             ] as unknown as AtribuicaoTemporaria[];
        });

        it("obterAtribuicoesPorServidor deve retornar as atribuições corretas por ID do servidor", () => {
            const servidorAtribuicoes = context.store.obterAtribuicoesPorServidor(1);
            expect(servidorAtribuicoes.length).toBe(2);
            expect(servidorAtribuicoes[0].codigo).toBe(1);
            expect(servidorAtribuicoes[1].codigo).toBe(3);
        });

        it("obterAtribuicoesPorServidor deve retornar uma lista vazia se nenhum servidor correspondente for encontrado", () => {
            const servidorAtribuicoes = context.store.obterAtribuicoesPorServidor(999);
            expect(servidorAtribuicoes.length).toBe(0);
        });

        it("obterAtribuicoesPorUnidade deve retornar as atribuições corretas pela sigla da unidade", () => {
            const unidadeAtribuicoes = context.store.obterAtribuicoesPorUnidade("A");
            expect(unidadeAtribuicoes.length).toBe(1);
            expect(unidadeAtribuicoes[0].codigo).toBe(1);
        });

        it("obterAtribuicoesPorUnidade deve retornar uma lista vazia se nenhuma unidade correspondente for encontrada", () => {
            const unidadeAtribuicoes = context.store.obterAtribuicoesPorUnidade("NONEXISTENT");
            expect(unidadeAtribuicoes.length).toBe(0);
        });
    });
});

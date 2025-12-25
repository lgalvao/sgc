import {beforeEach, describe, expect, it, vi} from "vitest";
import * as atribuicaoTemporariaService from "@/services/atribuicaoTemporariaService";
import {setupStoreTest} from "../../test-utils/storeTestHelpers";
import type {AtribuicaoTemporaria} from "@/types/tipos";
import {useAtribuicaoTemporariaStore} from "../atribuicoes";

const mockAtribuicoes: AtribuicaoTemporaria[] = [
    {
        codigo: 1,
        unidade: { codigo: 1, nome: "A", sigla: "A" },
        usuario: {
            codigo: 1,
            nome: "Servidor 1",
            tituloEleitoral: "123",
            unidade: { codigo: 1, nome: "A", sigla: "A" },
            email: "",
            ramal: "",
        },
        dataInicio: "2025-01-01",
        dataFim: "2025-01-31",
        dataTermino: "2025-01-31",
        justificativa: "J1",
    },
    {
        codigo: 2,
        unidade: { codigo: 2, nome: "B", sigla: "B" },
        usuario: {
            codigo: 2,
            nome: "Servidor 2",
            tituloEleitoral: "456",
            unidade: { codigo: 2, nome: "B", sigla: "B" },
            email: "",
            ramal: "",
        },
        dataInicio: "2025-02-01",
        dataFim: "2025-02-28",
        dataTermino: "2025-02-28",
        justificativa: "J2",
    },
    {
        codigo: 3,
        unidade: { codigo: 3, nome: "C", sigla: "C" },
        usuario: {
            codigo: 1,
            nome: "Servidor 1",
            tituloEleitoral: "123",
            unidade: { codigo: 3, nome: "C", sigla: "C" },
            email: "",
            ramal: "",
        },
        dataInicio: "2025-03-01",
        dataFim: "2025-03-31",
        dataTermino: "2025-03-31",
        justificativa: "J3",
    },
];

vi.mock("@/services/atribuicaoTemporariaService", () => ({
    buscarTodasAtribuicoes: vi.fn(() =>
        Promise.resolve({ data: mockAtribuicoes }),
    ),
}));

describe("useAtribuicaoTemporariaStore", () => {
    const context = setupStoreTest(useAtribuicaoTemporariaStore);

    beforeEach(() => {
        // Inicializa com dados simulados para os testes de getters
        context.store.atribuicoes = mockAtribuicoes;
    });

    it("deve inicializar com atribuições simuladas", () => {
        expect(context.store.atribuicoes.length).toBe(3);
        expect(context.store.atribuicoes[0].codigo).toBe(1);
    });

    describe("actions", () => {
        it("buscarAtribuicoes deve buscar e definir atribuições", async () => {
            context.store.atribuicoes = [];
            await context.store.buscarAtribuicoes();
            expect(
                atribuicaoTemporariaService.buscarTodasAtribuicoes,
            ).toHaveBeenCalledTimes(1);
            expect(context.store.atribuicoes.length).toBe(3);
        });

        it("buscarAtribuicoes deve lidar com erros", async () => {
            (
                atribuicaoTemporariaService.buscarTodasAtribuicoes as any
            ).mockRejectedValue(new Error("Failed"));
            await expect(
                context.store.buscarAtribuicoes(),
            ).rejects.toThrow("Failed");
            expect(context.store.error).toContain("Failed");
        });
    });

    describe("getters", () => {
        it("obterAtribuicoesPorServidor deve retornar as atribuições corretas por ID do servidor", () => {
            const servidorAtribuicoes =
                context.store.obterAtribuicoesPorServidor(1);
            expect(servidorAtribuicoes.length).toBe(2);
            expect(servidorAtribuicoes[0].codigo).toBe(1);
            expect(servidorAtribuicoes[1].codigo).toBe(3);
        });

        it("obterAtribuicoesPorServidor deve retornar uma lista vazia se nenhum servidor correspondente for encontrado", () => {
            const servidorAtribuicoes =
                context.store.obterAtribuicoesPorServidor(999);
            expect(servidorAtribuicoes.length).toBe(0);
        });

        it("obterAtribuicoesPorUnidade deve retornar as atribuições corretas pela sigla da unidade", () => {
            const unidadeAtribuicoes =
                context.store.obterAtribuicoesPorUnidade("A");
            expect(unidadeAtribuicoes.length).toBe(1);
            expect(unidadeAtribuicoes[0].codigo).toBe(1);
        });

        it("obterAtribuicoesPorUnidade deve retornar uma lista vazia se nenhuma unidade correspondente for encontrada", () => {
            const unidadeAtribuicoes =
                context.store.obterAtribuicoesPorUnidade("NONEXISTENT");
            expect(unidadeAtribuicoes.length).toBe(0);
        });
    });
});

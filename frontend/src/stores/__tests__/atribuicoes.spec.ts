import {beforeEach, describe, expect, it, vi} from "vitest";
import * as atribuicaoTemporariaService from "@/services/atribuicaoTemporariaService";
import {initPinia} from "@/test-utils/helpers";
import type {AtribuicaoTemporaria} from "@/types/tipos";
import {useAtribuicaoTemporariaStore} from "../atribuicoes";

const mockAtribuicoes: AtribuicaoTemporaria[] = [
    {
        codigo: 1,
        unidade: {codigo: 1, nome: "A", sigla: "A"},
        servidor: {
            codigo: 1,
            nome: "Servidor 1",
            tituloEleitoral: "123",
            unidade: {codigo: 1, nome: "A", sigla: "A"},
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
        unidade: {codigo: 2, nome: "B", sigla: "B"},
        servidor: {
            codigo: 2,
            nome: "Servidor 2",
            tituloEleitoral: "456",
            unidade: {codigo: 2, nome: "B", sigla: "B"},
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
        unidade: {codigo: 3, nome: "C", sigla: "C"},
        servidor: {
            codigo: 1,
            nome: "Servidor 1",
            tituloEleitoral: "123",
            unidade: {codigo: 3, nome: "C", sigla: "C"},
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
        Promise.resolve({data: mockAtribuicoes}),
    ),
}));

describe("useAtribuicaoTemporariaStore", () => {
    let atribuicaoTemporariaStore: ReturnType<
        typeof useAtribuicaoTemporariaStore
    >;

    beforeEach(() => {
        initPinia();
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        atribuicaoTemporariaStore.atribuicoes = mockAtribuicoes;
        vi.clearAllMocks();
    });

    it("should initialize with mock atribuicoes", () => {
        expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(3);
        expect(atribuicaoTemporariaStore.atribuicoes[0].codigo).toBe(1);
    });

    describe("actions", () => {
        it("buscarAtribuicoes should fetch and set atribuicoes", async () => {
            atribuicaoTemporariaStore.atribuicoes = [];
            await atribuicaoTemporariaStore.buscarAtribuicoes();
            expect(
                atribuicaoTemporariaService.buscarTodasAtribuicoes,
            ).toHaveBeenCalledTimes(1);
            expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(3);
        });

        it("buscarAtribuicoes should handle errors", async () => {
            (
                atribuicaoTemporariaService.buscarTodasAtribuicoes as any
            ).mockRejectedValue(new Error("Failed"));
            await atribuicaoTemporariaStore.buscarAtribuicoes();
            expect(atribuicaoTemporariaStore.error).toContain(
                "Falha ao carregar atribuições",
            );
        });
    });

    describe("getters", () => {
        it("obterAtribuicoesPorServidor should return the correct atribuicoes by servidor ID", () => {
            const servidorAtribuicoes =
                atribuicaoTemporariaStore.obterAtribuicoesPorServidor(1);
            expect(servidorAtribuicoes.length).toBe(2);
            expect(servidorAtribuicoes[0].codigo).toBe(1);
            expect(servidorAtribuicoes[1].codigo).toBe(3);
        });

        it("obterAtribuicoesPorServidor should return an empty array if no matching servidor is found", () => {
            const servidorAtribuicoes =
                atribuicaoTemporariaStore.obterAtribuicoesPorServidor(999);
            expect(servidorAtribuicoes.length).toBe(0);
        });

        it("obterAtribuicoesPorUnidade should return the correct atribuicoes by unidade sigla", () => {
            const unidadeAtribuicoes =
                atribuicaoTemporariaStore.obterAtribuicoesPorUnidade("A");
            expect(unidadeAtribuicoes.length).toBe(1);
            expect(unidadeAtribuicoes[0].codigo).toBe(1);
        });

        it("obterAtribuicoesPorUnidade should return an empty array if no matching unidade is found", () => {
            const unidadeAtribuicoes =
                atribuicaoTemporariaStore.obterAtribuicoesPorUnidade("NONEXISTENT");
            expect(unidadeAtribuicoes.length).toBe(0);
        });
    });
});

import {describe, expect, it, vi} from "vitest";
import * as analiseService from "@/services/analiseService";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {useAnalisesStore} from "../analises";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";

// Mock do service
vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
    listarAnalisesValidacao: vi.fn(),
}));

describe("useAnalisesStore", () => {
    // Inicializa a store usando o helper centralizado
    const context = setupStoreTest(useAnalisesStore);

    it("deve inicializar com um mapa vazio para análises", () => {
        expect(context.store.analisesPorSubprocesso).toBeInstanceOf(Map);
        expect(context.store.analisesPorSubprocesso.size).toBe(0);
    });

    describe("getters", () => {
        it("obterAnalisesPorSubprocesso deve retornar uma lista vazia se nenhuma análise estiver presente para o subprocesso", () => {
            const result = context.store.obterAnalisesPorSubprocesso(123);
            expect(result).toEqual([]);
        });

        it("obterAnalisesPorSubprocesso deve retornar as análises corretas para um dado subprocesso", () => {
            const mockAnalises: (AnaliseCadastro | AnaliseValidacao)[] = [
                {
                    codigo: 1,
                    dataHora: "2023-01-01T12:00:00Z",
                    observacoes: "Obs 1",
                    acao: "ACEITE",
                    unidadeSigla: "ABC",
                    analista: "Analista 1",
                    resultado: "APROVADO",
                    codSubrocesso: 123,
                },
                {
                    codigo: 2,
                    dataHora: "2023-01-02T12:00:00Z",
                    observacoes: "Obs 2",
                    acao: "DEVOLUCAO",
                    unidade: "DEF",
                    analista: "Analista 2",
                    resultado: "REPROVADO",
                    codSubrocesso: 123,
                },
            ];
            const codSubrocesso = 123;
            context.store.analisesPorSubprocesso.set(codSubrocesso, mockAnalises);

            const result = context.store.obterAnalisesPorSubprocesso(codSubrocesso);
            expect(result).toEqual(mockAnalises);
        });
    });

    describe("actions", () => {
        const codSubrocesso = 123;
        const mockAnalisesCadastro: AnaliseCadastro[] = [
            {
                codigo: 1,
                dataHora: "2023-01-01T10:00:00Z",
                observacoes: "Cadastro 1",
                acao: "ACEITE",
                unidadeSigla: "ABC",
                analista: "Analista 1",
                resultado: "APROVADO",
                codSubrocesso: 123,
            },
        ];
        const mockAnalisesValidacao: AnaliseValidacao[] = [
            {
                codigo: 2,
                dataHora: "2023-01-02T10:00:00Z",
                observacoes: "Validacao 1",
                acao: "DEVOLUCAO",
                unidade: "DEF",
                analista: "Analista 2",
                resultado: "REPROVADO",
                codSubrocesso: 123,
            },
        ];

        it("buscarAnalisesCadastro deve chamar o serviço e atualizar o estado", async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue(
                mockAnalisesCadastro,
            );

            await context.store.buscarAnalisesCadastro(codSubrocesso);

            expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(
                codSubrocesso,
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual(
                mockAnalisesCadastro,
            );
        });

        it("buscarAnalisesValidacao deve chamar o serviço e atualizar o estado", async () => {
            vi.mocked(analiseService.listarAnalisesValidacao).mockResolvedValue(
                mockAnalisesValidacao,
            );

            await context.store.buscarAnalisesValidacao(codSubrocesso);

            expect(analiseService.listarAnalisesValidacao).toHaveBeenCalledWith(
                codSubrocesso,
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual(
                mockAnalisesValidacao,
            );
        });

        it("deve mesclar resultados ao buscar análises de cadastro e validação", async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue(
                mockAnalisesCadastro,
            );
            vi.mocked(analiseService.listarAnalisesValidacao).mockResolvedValue(
                mockAnalisesValidacao,
            );

            // Fetch cadastro first
            await context.store.buscarAnalisesCadastro(codSubrocesso);
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual(
                mockAnalisesCadastro,
            );

            // Then fetch validacao
            await context.store.buscarAnalisesValidacao(codSubrocesso);

            const expected = [...mockAnalisesCadastro, ...mockAnalisesValidacao];
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual(
                expect.arrayContaining(expected),
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso).length).toBe(2);
        });

        it("deve lidar com erro em buscarAnalisesCadastro", async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockRejectedValue(
                new Error("Fail"),
            );
            expect(
                context.store.buscarAnalisesCadastro(codSubrocesso),
            ).rejects.toThrow("Fail");
            // Verifica se o estado permanece limpo ou inalterado em caso de erro inicial
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual([]);
        });

        it("deve lidar com erro em buscarAnalisesValidacao", async () => {
            vi.mocked(analiseService.listarAnalisesValidacao).mockRejectedValue(
                new Error("Fail"),
            );
            expect(
                context.store.buscarAnalisesValidacao(codSubrocesso),
            ).rejects.toThrow("Fail");
            expect(context.store.obterAnalisesPorSubprocesso(codSubrocesso)).toEqual([]);
        });
    });
});

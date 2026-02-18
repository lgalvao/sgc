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
                    dataHora: "2023-01-01T12:00:00Z",
                    observacoes: "Obs 1",
                    acao: "ACEITE_MAPEAMENTO",
                    unidadeSigla: "ABC",
                    unidadeNome: "Unidade ABC",
                    analistaUsuarioTitulo: "123456",
                    motivo: "Motivo 1",
                    tipo: "CADASTRO"
                },
                {
                    dataHora: "2023-01-02T12:00:00Z",
                    observacoes: "Obs 2",
                    acao: "DEVOLUCAO_MAPEAMENTO",
                    unidadeSigla: "DEF",
                    unidadeNome: "Unidade DEF",
                    analistaUsuarioTitulo: "654321",
                    motivo: "Motivo 2",
                    tipo: "CADASTRO"
                },
            ];
            const codSubprocesso = 123;
            context.store.analisesPorSubprocesso.set(codSubprocesso, mockAnalises);

            const result = context.store.obterAnalisesPorSubprocesso(codSubprocesso);
            expect(result).toEqual(mockAnalises);
        });
    });

    describe("actions", () => {
        const codSubprocesso = 123;
        const mockAnalisesCadastro: AnaliseCadastro[] = [
            {
                dataHora: "2023-01-01T10:00:00Z",
                observacoes: "Cadastro 1",
                acao: "ACEITE_MAPEAMENTO",
                unidadeSigla: "ABC",
                unidadeNome: "Unidade ABC",
                analistaUsuarioTitulo: "123456",
                motivo: "",
                tipo: "CADASTRO"
            },
        ];
        const mockAnalisesValidacao: AnaliseValidacao[] = [
            {
                dataHora: "2023-01-02T10:00:00Z",
                observacoes: "Validacao 1",
                acao: "DEVOLUCAO_MAPEAMENTO",
                unidadeSigla: "DEF",
                unidadeNome: "Unidade DEF",
                analistaUsuarioTitulo: "654321",
                motivo: "Motivo",
                tipo: "VALIDACAO"
            },
        ];

        it("buscarAnalisesCadastro deve chamar o serviço e atualizar o estado", async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue(
                mockAnalisesCadastro,
            );

            await context.store.buscarAnalisesCadastro(codSubprocesso);

            expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual(
                mockAnalisesCadastro,
            );
        });

        it("buscarAnalisesValidacao deve chamar o serviço e atualizar o estado", async () => {
            vi.mocked(analiseService.listarAnalisesValidacao).mockResolvedValue(
                mockAnalisesValidacao,
            );

            await context.store.buscarAnalisesValidacao(codSubprocesso);

            expect(analiseService.listarAnalisesValidacao).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual(
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
            await context.store.buscarAnalisesCadastro(codSubprocesso);
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual(
                mockAnalisesCadastro,
            );

            // Then fetch validacao
            await context.store.buscarAnalisesValidacao(codSubprocesso);

            const expected = [...mockAnalisesCadastro, ...mockAnalisesValidacao];
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual(
                expect.arrayContaining(expected),
            );
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso).length).toBe(2);
        });

        it("deve lidar com erro em buscarAnalisesCadastro", async () => {
            vi.mocked(analiseService.listarAnalisesCadastro).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(
                context.store.buscarAnalisesCadastro(codSubprocesso),
            ).rejects.toThrow("Fail");
            // Verifica se o estado permanece limpo ou inalterado em caso de erro inicial
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual([]);
        });

        it("deve lidar com erro em buscarAnalisesValidacao", async () => {
            vi.mocked(analiseService.listarAnalisesValidacao).mockRejectedValue(
                new Error("Fail"),
            );
            await expect(
                context.store.buscarAnalisesValidacao(codSubprocesso),
            ).rejects.toThrow("Fail");
            expect(context.store.obterAnalisesPorSubprocesso(codSubprocesso)).toEqual([]);
        });
    });
});

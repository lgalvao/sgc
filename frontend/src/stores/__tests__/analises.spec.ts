import {describe, expect, it, vi} from "vitest";
import * as subprocessoService from "@/services/subprocessoService";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {useAnalisesStore} from "../analises";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";

// Mock do service
vi.mock("@/services/subprocessoService", () => ({
    listarAnalisesCadastro: vi.fn(),
    listarAnalisesValidacao: vi.fn(),
}));

describe("useAnalisesStore", () => {
    // Inicializa a store usando o helper centralizado
    const context = setupStoreTest(useAnalisesStore);

    it("deve inicializar com listas vazias", () => {
        expect(context.store.analisesCadastro).toEqual([]);
        expect(context.store.analisesValidacao).toEqual([]);
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

        it("carregarHistorico deve chamar o serviço e atualizar o estado", async () => {
            vi.mocked(subprocessoService.listarAnalisesCadastro).mockResolvedValue(
                mockAnalisesCadastro,
            );
            vi.mocked(subprocessoService.listarAnalisesValidacao).mockResolvedValue(
                mockAnalisesValidacao,
            );

            await context.store.carregarHistorico(codSubprocesso);

            expect(subprocessoService.listarAnalisesCadastro).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(subprocessoService.listarAnalisesValidacao).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(context.store.analisesCadastro).toEqual(mockAnalisesCadastro);
            expect(context.store.analisesValidacao).toEqual(mockAnalisesValidacao);
        });

        it("deve lidar com erro em listarAnalisesCadastro", async () => {
            vi.mocked(subprocessoService.listarAnalisesCadastro).mockRejectedValue(
                new Error("Fail"),
            );

            await context.store.carregarHistorico(codSubprocesso);

            expect(context.store.erro).toBe("Fail");
            expect(context.store.analisesCadastro).toEqual([]);
        });
    });
});

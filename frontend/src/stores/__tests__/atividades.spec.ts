import {beforeEach, describe, expect, it, vi} from "vitest";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import {useAtividadesStore} from "../atividades";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";

vi.mock("@/services/atividadeService");
vi.mock("@/services/subprocessoService");

describe("useAtividadesStore", () => {
    const context = setupStoreTest(useAtividadesStore);

    beforeEach(() => {
        // Mocks específicos precisam ser limpos ou configurados,
        // mas o setupStoreTest já chama vi.clearAllMocks().
        // Como estamos usando vi.mock no topo, os métodos são vi.fn().
        // Mocks de retorno precisam ser definidos em cada teste ou num beforeEach se comum.
    });

    describe("buscarAtividadesParaSubprocesso", () => {
        it("deve buscar e mapear as atividades", async () => {
            const mockAtividades = [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ];
            vi.mocked(subprocessoService.listarAtividades).mockResolvedValue(mockAtividades as any);

            await context.store.buscarAtividadesParaSubprocesso(1);

            expect(subprocessoService.listarAtividades).toHaveBeenCalledWith(1);
            expect(context.store.atividadesPorSubprocesso.get(1)).toEqual(mockAtividades);
        });

        it("deve lidar com erros", async () => {
            vi.mocked(subprocessoService.listarAtividades).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(context.store.buscarAtividadesParaSubprocesso(1)).rejects.toThrow("Erro");
        });
    });

    describe("adicionarAtividade", () => {
        it("deve adicionar uma atividade", async () => {
            const novaAtividade = {
                codigo: 2,
                descricao: "Nova Atividade",
                conhecimentos: [],
            };
            vi.mocked(atividadeService.criarAtividade).mockResolvedValue({
                atividade: novaAtividade,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: [novaAtividade]
            });

            await context.store.adicionarAtividade(1, 999, {descricao: "Nova Atividade"});

            expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
                {descricao: "Nova Atividade"},
                999,
            );
            expect(context.store.atividadesPorSubprocesso.get(1)).toEqual([novaAtividade]);
        });

        it("deve lidar com erros ao adicionar atividade", async () => {
            vi.mocked(atividadeService.criarAtividade).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(context.store.adicionarAtividade(1, 999, {descricao: "Nova Atividade"})).rejects.toThrow("Erro");
        });
    });

    describe("removerAtividade", () => {
        it("deve remover uma atividade", async () => {
            context.store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({
                atividade: null,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: []
            });

            await context.store.removerAtividade(1, 1);

            expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
            expect(context.store.atividadesPorSubprocesso.get(1)).toEqual([]);
        });

        it("deve lidar com erros ao remover atividade", async () => {
            vi.mocked(atividadeService.excluirAtividade).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(context.store.removerAtividade(1, 1)).rejects.toThrow("Erro");
        });
    });

    describe("adicionarConhecimento", () => {
        it("deve adicionar um conhecimento", async () => {
            context.store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            const novoConhecimento = {codigo: 1, descricao: "Novo Conhecimento"};
            const atividadeComConhecimento = {
                codigo: 1, 
                descricao: "Atividade Teste", 
                conhecimentos: [novoConhecimento]
            };
            vi.mocked(atividadeService.criarConhecimento).mockResolvedValue({
                atividade: atividadeComConhecimento,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: [atividadeComConhecimento]
            });

            await context.store.adicionarConhecimento(1, 1, {
                descricao: "Novo Conhecimento",
            });

            expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, {
                descricao: "Novo Conhecimento",
            });
            expect(context.store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([
                novoConhecimento,
            ]);
        });

        it("deve lidar com erros ao adicionar conhecimento", async () => {
            vi.mocked(atividadeService.criarConhecimento).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                context.store.adicionarConhecimento(1, 1, {descricao: "Novo"}),
            ).rejects.toThrow("Erro");
        });
    });

    describe("removerConhecimento", () => {
        it("deve remover um conhecimento", async () => {
            context.store.atividadesPorSubprocesso.set(1, [
                {
                    codigo: 1,
                    descricao: "Atividade Teste",
                    conhecimentos: [{codigo: 1, descricao: "Conhecimento Teste"}],
                },
            ]);
            const atividadeSemConhecimento = {
                codigo: 1, 
                descricao: "Atividade Teste", 
                conhecimentos: []
            };
            vi.mocked(atividadeService.excluirConhecimento).mockResolvedValue({
                atividade: atividadeSemConhecimento,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: [atividadeSemConhecimento]
            });

            await context.store.removerConhecimento(1, 1, 1);

            expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 1);
            expect(context.store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual(
                [],
            );
        });

        it("deve lidar com erros ao remover conhecimento", async () => {
            vi.mocked(atividadeService.excluirConhecimento).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(context.store.removerConhecimento(1, 1, 1)).rejects.toThrow("Erro");
        });
    });

    describe("importarAtividades", () => {
        it("deve importar atividades", async () => {
            vi.mocked(subprocessoService.importarAtividades).mockResolvedValue(
                undefined,
            );
            vi.mocked(subprocessoService.listarAtividades).mockResolvedValue(
                [] as any
            );

            await context.store.importarAtividades(1, 2);

            expect(subprocessoService.importarAtividades).toHaveBeenCalledWith(1, 2);
        });

        it("deve lidar com erros ao importar atividades", async () => {
            vi.mocked(subprocessoService.importarAtividades).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(context.store.importarAtividades(1, 2)).rejects.toThrow("Erro");
        });
    });

    describe("atualizarAtividade", () => {
        it("deve atualizar uma atividade", async () => {
            const atividadeAtualizada = {
                codigo: 1,
                descricao: "Atividade Atualizada",
                conhecimentos: [],
            };
            context.store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            vi.mocked(atividadeService.atualizarAtividade).mockResolvedValue({
                atividade: atividadeAtualizada,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: [atividadeAtualizada]
            });

            await context.store.atualizarAtividade(1, 1, atividadeAtualizada);

            expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(
                1,
                atividadeAtualizada,
            );
            expect(context.store.atividadesPorSubprocesso.get(1)).toEqual([
                atividadeAtualizada,
            ]);
        });

        it("deve lidar com erros ao atualizar atividade", async () => {
            vi.mocked(atividadeService.atualizarAtividade).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                context.store.atualizarAtividade(1, 1, {
                    codigo: 1,
                    descricao: "Teste",
                    conhecimentos: [],
                }),
            ).rejects.toThrow("Erro");
        });
    });

    describe("atualizarConhecimento", () => {
        it("deve atualizar um conhecimento", async () => {
            const conhecimentoAtualizado = {
                codigo: 1,
                descricao: "Conhecimento Atualizado",
            };
            const atividadeComConhecimentoAtualizado = {
                codigo: 1,
                descricao: "Atividade Teste",
                conhecimentos: [conhecimentoAtualizado]
            };

            context.store.atividadesPorSubprocesso.set(1, [
                {
                    codigo: 1,
                    descricao: "Atividade Teste",
                    conhecimentos: [{codigo: 1, descricao: "Conhecimento Teste"}],
                },
            ]);
            vi.mocked(atividadeService.atualizarConhecimento).mockResolvedValue({
                atividade: atividadeComConhecimentoAtualizado,
                subprocesso: {
                    codigo: 1,
                    situacao: "CADASTRO_EM_ANDAMENTO" as any,
                    situacaoLabel: "CADASTRO_EM_ANDAMENTO"
                },
                atividadesAtualizadas: [atividadeComConhecimentoAtualizado]
            });

            await context.store.atualizarConhecimento(1, 1, 1, conhecimentoAtualizado);

            expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(
                1,
                1,
                conhecimentoAtualizado,
            );
            expect(context.store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([
                conhecimentoAtualizado,
            ]);
        });

        it("deve lidar com erros ao atualizar conhecimento", async () => {
            vi.mocked(atividadeService.atualizarConhecimento).mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                context.store.atualizarConhecimento(1, 1, 1, {codigo: 1, descricao: "Teste"}),
            ).rejects.toThrow("Erro");
        });
    });
});

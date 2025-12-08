import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import * as atividadeService from "@/services/atividadeService";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import {useAtividadesStore} from "../atividades";

vi.mock("@/services/mapaService");
vi.mock("@/services/atividadeService");
vi.mock("@/services/subprocessoService");

describe("useAtividadesStore", () => {
    let store: ReturnType<typeof useAtividadesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useAtividadesStore();
        vi.restoreAllMocks();
    });

    describe("buscarAtividadesParaSubprocesso", () => {
        it("deve buscar e mapear as atividades", async () => {
            const mockMapaVisualizacao = {
                competencias: [
                    {
                        atividades: [
                            {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
                        ],
                    },
                ],
            };
            const spy = vi
                .spyOn(mapaService, "obterMapaVisualizacao")
                .mockResolvedValue(mockMapaVisualizacao as any);
            await store.buscarAtividadesParaSubprocesso(1);
            expect(spy).toHaveBeenCalledWith(1);
            const expectedAtividades = [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ];
            expect(store.atividadesPorSubprocesso.get(1)).toEqual(expectedAtividades);
        });

        it("deve lidar com erros", async () => {
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(store.buscarAtividadesParaSubprocesso(1)).rejects.toThrow("Erro");
        });
    });

    describe("adicionarAtividade", () => {
        it("deve adicionar uma atividade", async () => {
            const novaAtividade = {
                codigo: 2,
                descricao: "Nova Atividade",
                conhecimentos: [],
            };
            vi.spyOn(atividadeService, "criarAtividade").mockResolvedValue(
                novaAtividade,
            );
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [{atividades: [novaAtividade]}],
            } as any);

            await store.adicionarAtividade(1, 999, {descricao: "Nova Atividade"});

            expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
                {descricao: "Nova Atividade"},
                999,
            );
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([novaAtividade]);
        });

        it("deve lidar com erros ao adicionar atividade", async () => {
            vi.spyOn(atividadeService, "criarAtividade").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(store.adicionarAtividade(1, 999, {descricao: "Nova Atividade"})).rejects.toThrow("Erro");
        });
    });

    describe("removerAtividade", () => {
        it("deve remover uma atividade", async () => {
            store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            vi.spyOn(atividadeService, "excluirAtividade").mockResolvedValue(
                undefined,
            );
            // Mock re-fetch to return empty list
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [],
            } as any);

            await store.removerAtividade(1, 1);

            expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([]);
        });

        it("deve lidar com erros ao remover atividade", async () => {
            vi.spyOn(atividadeService, "excluirAtividade").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(store.removerAtividade(1, 1)).rejects.toThrow("Erro");
        });
    });

    describe("adicionarConhecimento", () => {
        it("deve adicionar um conhecimento", async () => {
            store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            const novoConhecimento = {id: 1, descricao: "Novo Conhecimento"};
            vi.spyOn(atividadeService, "criarConhecimento").mockResolvedValue(
                novoConhecimento,
            );
            // Mock re-fetch to return activity with new knowledge
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [{
                    atividades: [{
                        codigo: 1,
                        descricao: "Atividade Teste",
                        conhecimentos: [novoConhecimento]
                    }]
                }],
            } as any);

            await store.adicionarConhecimento(1, 1, {
                descricao: "Novo Conhecimento",
            });

            expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, {
                descricao: "Novo Conhecimento",
            });
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([
                novoConhecimento,
            ]);
        });

        it("deve lidar com erros ao adicionar conhecimento", async () => {
            vi.spyOn(atividadeService, "criarConhecimento").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                store.adicionarConhecimento(1, 1, {descricao: "Novo"}),
            ).rejects.toThrow("Erro");
        });
    });

    describe("removerConhecimento", () => {
        it("deve remover um conhecimento", async () => {
            store.atividadesPorSubprocesso.set(1, [
                {
                    codigo: 1,
                    descricao: "Atividade Teste",
                    conhecimentos: [{id: 1, descricao: "Conhecimento Teste"}],
                },
            ]);
            vi.spyOn(atividadeService, "excluirConhecimento").mockResolvedValue(
                undefined,
            );
            // Mock re-fetch to return activity with no knowledge
             vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [{
                    atividades: [{
                        codigo: 1,
                        descricao: "Atividade Teste",
                        conhecimentos: []
                    }]
                }],
            } as any);

            await store.removerConhecimento(1, 1, 1);

            expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 1);
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual(
                [],
            );
        });

        it("deve lidar com erros ao remover conhecimento", async () => {
            vi.spyOn(atividadeService, "excluirConhecimento").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(store.removerConhecimento(1, 1, 1)).rejects.toThrow("Erro");
        });
    });

    describe("importarAtividades", () => {
        it("deve importar atividades", async () => {
            vi.spyOn(subprocessoService, "importarAtividades").mockResolvedValue(
                undefined,
            );
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [],
            } as any);

            await store.importarAtividades(1, 2);

            expect(subprocessoService.importarAtividades).toHaveBeenCalledWith(1, 2);
        });

        it("deve lidar com erros ao importar atividades", async () => {
            vi.spyOn(subprocessoService, "importarAtividades").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(store.importarAtividades(1, 2)).rejects.toThrow("Erro");
        });
    });

    describe("atualizarAtividade", () => {
        it("deve atualizar uma atividade", async () => {
            const atividadeAtualizada = {
                codigo: 1,
                descricao: "Atividade Atualizada",
                conhecimentos: [],
            };
            store.atividadesPorSubprocesso.set(1, [
                {codigo: 1, descricao: "Atividade Teste", conhecimentos: []},
            ]);
            vi.spyOn(atividadeService, "atualizarAtividade").mockResolvedValue(
                atividadeAtualizada,
            );
            // Mock re-fetch
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [{atividades: [atividadeAtualizada]}],
            } as any);

            await store.atualizarAtividade(1, 1, atividadeAtualizada);

            expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(
                1,
                atividadeAtualizada,
            );
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([
                atividadeAtualizada,
            ]);
        });

        it("deve lidar com erros ao atualizar atividade", async () => {
            vi.spyOn(atividadeService, "atualizarAtividade").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                store.atualizarAtividade(1, 1, {
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
                id: 1,
                descricao: "Conhecimento Atualizado",
            };
            const atividadeComConhecimentoAtualizado = {
                 codigo: 1,
                 descricao: "Atividade Teste",
                 conhecimentos: [conhecimentoAtualizado]
            };

            store.atividadesPorSubprocesso.set(1, [
                {
                    codigo: 1,
                    descricao: "Atividade Teste",
                    conhecimentos: [{id: 1, descricao: "Conhecimento Teste"}],
                },
            ]);
            vi.spyOn(atividadeService, "atualizarConhecimento").mockResolvedValue(
                conhecimentoAtualizado,
            );
            // Mock re-fetch
            vi.spyOn(mapaService, "obterMapaVisualizacao").mockResolvedValue({
                competencias: [{atividades: [atividadeComConhecimentoAtualizado]}],
            } as any);

            await store.atualizarConhecimento(1, 1, 1, conhecimentoAtualizado);

            expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(
                1,
                1,
                conhecimentoAtualizado,
            );
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([
                conhecimentoAtualizado,
            ]);
        });

        it("deve lidar com erros ao atualizar conhecimento", async () => {
            vi.spyOn(atividadeService, "atualizarConhecimento").mockRejectedValue(
                new Error("Erro"),
            );
            await expect(
                store.atualizarConhecimento(1, 1, 1, {id: 1, descricao: "Teste"}),
            ).rejects.toThrow("Erro");
        });
    });
});

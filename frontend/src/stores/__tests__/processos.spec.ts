import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {Processo} from "@/types/tipos";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import {useProcessosStore} from "../processos";
import {normalizeError} from "@/utils/apiError";

// Mocks
vi.mock("@/services/painelService");
vi.mock("@/services/processoService");
vi.mock("../unidades", () => ({useUnidadesStore: vi.fn(() => ({}))}));
vi.mock("../alertas", () => ({useAlertasStore: vi.fn(() => ({}))}));

describe("useProcessosStore", () => {
    // Usando o helper centralizado
    const context = setupStoreTest(useProcessosStore);

    // Services mocks precisam ser carregados
    let painelService: Mocked<typeof import("@/services/painelService")>;
    let processoService: Mocked<typeof import("@/services/processoService")>;

    const MOCK_ERROR = new Error("Service failed");
    const MOCK_PROCESSO_DETALHE: Processo = {
        codigo: 1,
        descricao: "Teste",
        tipo: TipoProcesso.MAPEAMENTO,
        situacao: SituacaoProcesso.EM_ANDAMENTO,
        dataLimite: "2025-12-31",
        dataCriacao: "2025-01-01",
        unidades: [],
        resumoSubprocessos: [],
    };

    // Setup específico dos services que não cabe no setup genérico
    beforeEach(async () => {
        vi.clearAllMocks();
        painelService = (await import("@/services/painelService")) as Mocked<
            typeof import("@/services/painelService")
        >;
        processoService = (await import("@/services/processoService")) as Mocked<
            typeof import("@/services/processoService")
        >;
    });

    it("deve inicializar com o estado padrão", () => {
        expect(context.store.processosPainel).toEqual([]);
        expect(context.store.processoDetalhe).toBeNull();
    });

    it("deve limpar o erro com clearError", () => {
        context.store.lastError = normalizeError(new Error("Erro de teste"));
        context.store.clearError();
        expect(context.store.lastError).toBeNull();
    });

    describe("Actions", () => {
        describe("buscarProcessosPainel", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockPage = {content: [{codigo: 1}], totalPages: 1};
                painelService.listarProcessos.mockResolvedValue(mockPage as any);
                await context.store.buscarProcessosPainel("perfil", 1, 0, 10);
                const calls = painelService.listarProcessos.mock.calls[0];
                expect(calls[0]).toBe("perfil");
                expect(calls[1]).toBe(1);
                expect(calls[2]).toBe(0);
                expect(calls[3]).toBe(10);
                expect(context.store.processosPainel).toEqual(mockPage.content);
            });

            it("deve respeitar ordenacao personalizada", async () => {
                const mockPage = {content: [{codigo: 2}], totalPages: 1};
                painelService.listarProcessos.mockResolvedValue(mockPage as any);
                await context.store.buscarProcessosPainel("perfil", 1, 0, 10, "descricao", "asc");
                expect(painelService.listarProcessos).toHaveBeenCalledWith(
                    "perfil",
                    1,
                    0,
                    10,
                    "descricao",
                    "asc",
                );
                expect(context.store.processosPainel).toEqual(mockPage.content);
            });

            it("não deve atualizar o estado em caso de falha", async () => {
                painelService.listarProcessos.mockRejectedValue(MOCK_ERROR);
                await expect(
                    context.store.buscarProcessosPainel("perfil", 1, 0, 10),
                ).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("buscarContextoCompleto", () => {
            it("deve atualizar processoDetalhe e subprocessosElegiveis", async () => {
                const mockData = {
                    processo: MOCK_PROCESSO_DETALHE,
                    elegiveis: [{codigo: 1}]
                };
                processoService.buscarContextoCompleto.mockResolvedValue(mockData as any);

                await context.store.buscarContextoCompleto(1);

                expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
                expect(context.store.processoDetalhe).toEqual(MOCK_PROCESSO_DETALHE);
                expect(context.store.subprocessosElegiveis).toEqual([{codigo: 1}]);
            });

            it("deve lidar com erros", async () => {
                processoService.buscarContextoCompleto.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.buscarContextoCompleto(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("buscarProcessoDetalhe", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.buscarProcessoDetalhe(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
                expect(context.store.processoDetalhe).toEqual(MOCK_PROCESSO_DETALHE);
            });

            it("não deve atualizar o estado em caso de falha", async () => {
                processoService.obterDetalhesProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.buscarProcessoDetalhe(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.processoDetalhe).toBeNull();
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("criarProcesso", () => {
            const payload = {
                descricao: "Novo",
                tipo: TipoProcesso.MAPEAMENTO,
                dataLimiteEtapa1: "2025-01-01",
                unidades: [1],
            };
            it("deve chamar o processoService", async () => {
                processoService.criarProcesso.mockResolvedValue({} as any);
                await context.store.criarProcesso(payload);
                expect(processoService.criarProcesso).toHaveBeenCalledWith(payload);
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.criarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.criarProcesso(payload)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("atualizarProcesso", () => {
            const payload = {
                codigo: 1,
                descricao: "Atualizado",
                tipo: TipoProcesso.MAPEAMENTO,
                dataLimiteEtapa1: "2025-01-01",
                unidades: [1],
            };
            it("deve chamar o processoService", async () => {
                processoService.atualizarProcesso.mockResolvedValue({} as any);
                await context.store.atualizarProcesso(1, payload);
                expect(processoService.atualizarProcesso).toHaveBeenCalledWith(
                    1,
                    payload,
                );
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.atualizarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.atualizarProcesso(1, payload)).rejects.toThrow(
                    MOCK_ERROR,
                );
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("removerProcesso", () => {
            it("deve chamar o processoService", async () => {
                processoService.excluirProcesso.mockResolvedValue(undefined);
                await context.store.removerProcesso(1);
                expect(processoService.excluirProcesso).toHaveBeenCalledWith(1);
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.excluirProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.removerProcesso(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("iniciarProcesso", () => {
            it("deve chamar o processoService corretamente", async () => {
                processoService.iniciarProcesso.mockResolvedValue(undefined);

                await context.store.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]);

                expect(processoService.iniciarProcesso).toHaveBeenCalledWith(
                    1,
                    TipoProcesso.MAPEAMENTO,
                    [10],
                );
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.iniciarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(
                    context.store.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]),
                ).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("buscarProcessosFinalizados", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockProcessos = [{codigo: 1}];
                processoService.buscarProcessosFinalizados.mockResolvedValue(
                    mockProcessos as any,
                );
                await context.store.buscarProcessosFinalizados();
                expect(processoService.buscarProcessosFinalizados).toHaveBeenCalled();
                expect(context.store.processosFinalizados).toEqual(mockProcessos);
            });

            it("deve lançar erro em caso de falha", async () => {
                processoService.buscarProcessosFinalizados.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.buscarProcessosFinalizados()).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("buscarSubprocessosElegiveis", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockSubprocessos = [{codigo: 1}];
                processoService.buscarSubprocessosElegiveis.mockResolvedValue(
                    mockSubprocessos as any,
                );
                await context.store.buscarSubprocessosElegiveis(1);
                expect(processoService.buscarSubprocessosElegiveis).toHaveBeenCalledWith(
                    1,
                );
                expect(context.store.subprocessosElegiveis).toEqual(mockSubprocessos);
            });

            it("deve lançar erro em caso de falha", async () => {
                processoService.buscarSubprocessosElegiveis.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.buscarSubprocessosElegiveis(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("finalizarProcesso", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                processoService.finalizarProcesso.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.finalizarProcesso(1);
                expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

            it("deve lançar erro em caso de falha", async () => {
                processoService.finalizarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.finalizarProcesso(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("processarCadastroBloco", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                const payload = {
                    codProcesso: 1,
                    unidades: ["1"],
                    tipoAcao: "aceitar",
                    unidadeUsuario: "1",
                } as any;
                processoService.processarAcaoEmBloco.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.processarCadastroBloco(payload);
                expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith(
                    payload,
                );
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

            it("deve lançar erro em caso de falha", async () => {
                const payload = { codProcesso: 1 } as any;
                processoService.processarAcaoEmBloco.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.processarCadastroBloco(payload)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("alterarDataLimiteSubprocesso", () => {
            it("deve chamar o processoService e recarregar os detalhes se disponíveis", async () => {
                context.store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                const payload = {novaData: "2026-01-01"};
                processoService.alterarDataLimiteSubprocesso.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.alterarDataLimiteSubprocesso(1, payload);
                expect(
                    processoService.alterarDataLimiteSubprocesso,
                ).toHaveBeenCalledWith(1, payload);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

             it("não deve recarregar detalhes se processoDetalhe for nulo", async () => {
                context.store.processoDetalhe = null;
                const payload = {novaData: "2026-01-01"};
                processoService.alterarDataLimiteSubprocesso.mockResolvedValue(undefined);

                await context.store.alterarDataLimiteSubprocesso(1, payload);

                expect(processoService.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(1, payload);
                expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
            });

            it("deve lançar erro em caso de falha", async () => {
                const payload = {novaData: "2026-01-01"};
                processoService.alterarDataLimiteSubprocesso.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.alterarDataLimiteSubprocesso(1, payload)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("apresentarSugestoes", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                context.store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                const payload = {sugestoes: "sugestoes"};
                processoService.apresentarSugestoes.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.apresentarSugestoes(1, payload);
                expect(processoService.apresentarSugestoes).toHaveBeenCalledWith(
                    1,
                    payload,
                );
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

            it("não deve recarregar detalhes se processoDetalhe for nulo", async () => {
                context.store.processoDetalhe = null;
                const payload = {sugestoes: "sugestoes"};
                processoService.apresentarSugestoes.mockResolvedValue(undefined);

                await context.store.apresentarSugestoes(1, payload);

                expect(processoService.apresentarSugestoes).toHaveBeenCalledWith(1, payload);
                expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
            });

            it("deve lançar erro em caso de falha", async () => {
                const payload = {sugestoes: "sugestoes"};
                processoService.apresentarSugestoes.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.apresentarSugestoes(1, payload)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("validarMapa", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                context.store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                processoService.validarMapa.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.validarMapa(1);
                expect(processoService.validarMapa).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

             it("não deve recarregar detalhes se processoDetalhe for nulo", async () => {
                context.store.processoDetalhe = null;
                processoService.validarMapa.mockResolvedValue(undefined);

                await context.store.validarMapa(1);

                expect(processoService.validarMapa).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
            });

            it("deve lançar erro em caso de falha", async () => {
                processoService.validarMapa.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.validarMapa(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("homologarValidacao", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                context.store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                processoService.homologarValidacao.mockResolvedValue(undefined);
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await context.store.homologarValidacao(1);
                expect(processoService.homologarValidacao).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

             it("não deve recarregar detalhes se processoDetalhe for nulo", async () => {
                context.store.processoDetalhe = null;
                processoService.homologarValidacao.mockResolvedValue(undefined);

                await context.store.homologarValidacao(1);

                expect(processoService.homologarValidacao).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
            });

            it("deve lançar erro em caso de falha", async () => {
                processoService.homologarValidacao.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.homologarValidacao(1)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });

        describe("aceitarValidacao", () => {
             it("deve chamar service corretamente", async () => {
                 context.store.processoDetalhe = { codigo: 1 } as any;
                 processoService.aceitarValidacao.mockResolvedValue(undefined);
                 processoService.obterDetalhesProcesso.mockResolvedValue(MOCK_PROCESSO_DETALHE);
                 await context.store.aceitarValidacao(10, { observacoes: 'Obs' });
                 expect(processoService.aceitarValidacao).toHaveBeenCalledWith(10, { observacoes: 'Obs' });
                 expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
             });

             it("não deve recarregar detalhes se processoDetalhe for nulo", async () => {
                context.store.processoDetalhe = null;
                processoService.aceitarValidacao.mockResolvedValue(undefined);

                await context.store.aceitarValidacao(10, { observacoes: 'Obs' });

                expect(processoService.aceitarValidacao).toHaveBeenCalledWith(10, { observacoes: 'Obs' });
                expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
            });

             it("deve lançar erro em caso de falha", async () => {
                processoService.aceitarValidacao.mockRejectedValue(MOCK_ERROR);
                await expect(context.store.aceitarValidacao(10)).rejects.toThrow(MOCK_ERROR);
                expect(context.store.lastError).toEqual(normalizeError(MOCK_ERROR));
            });
        });


        describe("obterUnidadesDoProcesso (Getter)", () => {
            it("deve retornar unidades do processo correto", () => {
                context.store.processoDetalhe = {
                    ...MOCK_PROCESSO_DETALHE,
                    codigo: 1,
                    resumoSubprocessos: [{codigo: 10}]
                } as any;

                const unidades = context.store.obterUnidadesDoProcesso(1);
                expect(unidades).toEqual([{codigo: 10}]);
            });

            it("deve retornar lista vazia se processo não corresponder", () => {
                context.store.processoDetalhe = {...MOCK_PROCESSO_DETALHE, codigo: 2} as any;

                const unidades = context.store.obterUnidadesDoProcesso(1);
                expect(unidades).toEqual([]);
            });

            it("deve retornar lista vazia se processoDetalhe for nulo", () => {
                context.store.processoDetalhe = null;

                const unidades = context.store.obterUnidadesDoProcesso(1);
                expect(unidades).toEqual([]);
            });
        });
    });
});

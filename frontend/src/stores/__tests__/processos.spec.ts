import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import {initPinia} from "@/test-utils/helpers";
import type {Processo} from "@/types/tipos";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import {useProcessosStore} from "../processos";

// Mocks
vi.mock("@/services/painelService");
vi.mock("@/services/processoService");
vi.mock("../unidades", () => ({useUnidadesStore: vi.fn(() => ({}))}));
vi.mock("../alertas", () => ({useAlertasStore: vi.fn(() => ({}))}));

describe("useProcessosStore", () => {
    let store: ReturnType<typeof useProcessosStore>;
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

    beforeEach(async () => {
        initPinia();
        store = useProcessosStore();
        painelService = (await import("@/services/painelService")) as Mocked<
            typeof import("@/services/painelService")
        >;
        processoService = (await import("@/services/processoService")) as Mocked<
            typeof import("@/services/processoService")
        >;
        vi.restoreAllMocks();
    });

    it("deve inicializar com o estado padrão", () => {
        expect(store.processosPainel).toEqual([]);
        expect(store.processoDetalhe).toBeNull();
    });

    describe("Actions", () => {
        describe("buscarProcessosPainel", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockPage = {content: [{id: 1}], totalPages: 1};
                painelService.listarProcessos.mockResolvedValue(mockPage as any);
                await store.buscarProcessosPainel("perfil", 1, 0, 10);
                const calls = painelService.listarProcessos.mock.calls[0];
                expect(calls[0]).toBe("perfil");
                expect(calls[1]).toBe(1);
                expect(calls[2]).toBe(0);
                expect(calls[3]).toBe(10);
                // sort and order may be undefined by default
                expect(calls[4]).toBeUndefined();
                expect(calls[5]).toBeUndefined();
                expect(store.processosPainel).toEqual(mockPage.content);
            });

            it("deve respeitar ordenacao personalizada", async () => {
                const mockPage = {content: [{id: 2}], totalPages: 1};
                painelService.listarProcessos.mockResolvedValue(mockPage as any);
                await store.buscarProcessosPainel("perfil", 1, 0, 10, "descricao", "asc");
                expect(painelService.listarProcessos).toHaveBeenCalledWith(
                    "perfil",
                    1,
                    0,
                    10,
                    "descricao",
                    "asc",
                );
                expect(store.processosPainel).toEqual(mockPage.content);
            });

            it("não deve atualizar o estado em caso de falha", async () => {
                painelService.listarProcessos.mockRejectedValue(MOCK_ERROR);
                await expect(
                    store.buscarProcessosPainel("perfil", 1, 0, 10),
                ).rejects.toThrow(MOCK_ERROR);
            });
        });

        describe("buscarProcessoDetalhe", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.buscarProcessoDetalhe(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
                expect(store.processoDetalhe).toEqual(MOCK_PROCESSO_DETALHE);
            });

            it("não deve atualizar o estado em caso de falha", async () => {
                processoService.obterDetalhesProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(store.buscarProcessoDetalhe(1)).rejects.toThrow(MOCK_ERROR);
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
                await store.criarProcesso(payload);
                expect(processoService.criarProcesso).toHaveBeenCalledWith(payload);
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.criarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(store.criarProcesso(payload)).rejects.toThrow(MOCK_ERROR);
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
                await store.atualizarProcesso(1, payload);
                expect(processoService.atualizarProcesso).toHaveBeenCalledWith(
                    1,
                    payload,
                );
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.atualizarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(store.atualizarProcesso(1, payload)).rejects.toThrow(
                    MOCK_ERROR,
                );
            });
        });

        describe("removerProcesso", () => {
            it("deve chamar o processoService", async () => {
                processoService.excluirProcesso.mockResolvedValue();
                await store.removerProcesso(1);
                expect(processoService.excluirProcesso).toHaveBeenCalledWith(1);
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.excluirProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(store.removerProcesso(1)).rejects.toThrow(MOCK_ERROR);
            });
        });

        describe("iniciarProcesso", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                processoService.iniciarProcesso.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]);
                expect(processoService.iniciarProcesso).toHaveBeenCalledWith(
                    1,
                    TipoProcesso.MAPEAMENTO,
                    [10],
                );
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });

            it("deve lançar um erro em caso de falha", async () => {
                processoService.iniciarProcesso.mockRejectedValue(MOCK_ERROR);
                await expect(
                    store.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]),
                ).rejects.toThrow(MOCK_ERROR);
            });
        });

        describe("buscarProcessosFinalizados", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockProcessos = [{id: 1}];
                processoService.buscarProcessosFinalizados.mockResolvedValue(
                    mockProcessos as any,
                );
                await store.buscarProcessosFinalizados();
                expect(processoService.buscarProcessosFinalizados).toHaveBeenCalled();
                expect(store.processosFinalizados).toEqual(mockProcessos);
            });
        });

        describe("buscarSubprocessosElegiveis", () => {
            it("deve atualizar o estado em caso de sucesso", async () => {
                const mockSubprocessos = [{id: 1}];
                processoService.buscarSubprocessosElegiveis.mockResolvedValue(
                    mockSubprocessos as any,
                );
                await store.buscarSubprocessosElegiveis(1);
                expect(processoService.buscarSubprocessosElegiveis).toHaveBeenCalledWith(
                    1,
                );
                expect(store.subprocessosElegiveis).toEqual(mockSubprocessos);
            });
        });

        describe("finalizarProcesso", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                processoService.finalizarProcesso.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.finalizarProcesso(1);
                expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
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
                processoService.processarAcaoEmBloco.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.processarCadastroBloco(payload);
                expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith(
                    payload,
                );
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });
        });

        describe("alterarDataLimiteSubprocesso", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                const payload = {novaData: "2026-01-01"};
                processoService.alterarDataLimiteSubprocesso.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.alterarDataLimiteSubprocesso(1, payload);
                expect(
                    processoService.alterarDataLimiteSubprocesso,
                ).toHaveBeenCalledWith(1, payload);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });
        });

        describe("apresentarSugestoes", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                const payload = {sugestoes: "sugestoes"};
                processoService.apresentarSugestoes.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.apresentarSugestoes(1, payload);
                expect(processoService.apresentarSugestoes).toHaveBeenCalledWith(
                    1,
                    payload,
                );
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });
        });

        describe("validarMapa", () => {
            it("deve chamar o processoService e recarregar os detalhes", async () => {
                store.processoDetalhe = MOCK_PROCESSO_DETALHE;
                processoService.validarMapa.mockResolvedValue();
                processoService.obterDetalhesProcesso.mockResolvedValue(
                    MOCK_PROCESSO_DETALHE,
                );
                await store.validarMapa(1);
                expect(processoService.validarMapa).toHaveBeenCalledWith(1);
                expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            });
        });
    });
});

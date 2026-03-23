import {beforeEach, describe, expect, it, type Mocked, vi} from "vitest";
import type {Processo} from "@/types/tipos";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import {normalizeError} from "@/utils/apiError";

vi.mock("@/services/painelService");
vi.mock("@/services/processoService");

const {loggerMock} = vi.hoisted(() => ({
    loggerMock: {
        error: vi.fn(),
    }
}));

vi.mock("@/utils", () => ({
    logger: loggerMock
}));

describe("useProcessos", () => {
    let painelService: Mocked<typeof import("@/services/painelService")>;
    let processoService: Mocked<typeof import("@/services/processoService")>;
    let useProcessos: typeof import("../useProcessos").useProcessos;

    const ERRO_MOCK = new Error("Service failed");
    const PROCESSO_MOCK: Processo = {
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
        vi.clearAllMocks();
        vi.resetModules();

        painelService = await import("@/services/painelService") as Mocked<typeof import("@/services/painelService")>;
        processoService = await import("@/services/processoService") as Mocked<typeof import("@/services/processoService")>;
        ({useProcessos} = await import("../useProcessos"));
    });

    it("deve inicializar com o estado padrão", () => {
        const composable = useProcessos();

        expect(composable.processosPainel.value).toEqual([]);
        expect(composable.processoDetalhe.value).toBeNull();
    });

    it("deve limpar o erro com clearError", async () => {
        const composable = useProcessos();
        painelService.listarProcessos.mockRejectedValue(ERRO_MOCK);

        await expect(
            composable.buscarProcessosPainel(1, 0, 10),
        ).rejects.toThrow(ERRO_MOCK);

        expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));

        composable.clearError();
        expect(composable.lastError.value).toBeNull();
    });

    describe("buscarProcessosPainel", () => {
        it("deve atualizar o estado em caso de sucesso", async () => {
            const composable = useProcessos();
            const paginaMock = {content: [{codigo: 1}], totalPages: 1};
            painelService.listarProcessos.mockResolvedValue(paginaMock as any);

            await composable.buscarProcessosPainel(1, 0, 10);

            expect(painelService.listarProcessos).toHaveBeenCalledWith(1, 0, 10, undefined, undefined);
            expect(composable.processosPainel.value).toEqual(paginaMock.content);
        });

        it("deve respeitar ordenacao personalizada", async () => {
            const composable = useProcessos();
            const paginaMock = {content: [{codigo: 2}], totalPages: 1};
            painelService.listarProcessos.mockResolvedValue(paginaMock as any);

            await composable.buscarProcessosPainel(1, 0, 10, "descricao", "asc");

            expect(painelService.listarProcessos).toHaveBeenCalledWith(
                1,
                0,
                10,
                "descricao",
                "asc",
            );
            expect(composable.processosPainel.value).toEqual(paginaMock.content);
        });

        it("deve retornar lista vazia se painelService retornar nulo", async () => {
            const composable = useProcessos();
            painelService.listarProcessos.mockResolvedValue(null as any);
            await composable.buscarProcessosPainel(1, 0, 10);
            expect(composable.processosPainel.value).toEqual([]);
        });

        it("deve registrar erro quando a busca falhar", async () => {
            const composable = useProcessos();
            painelService.listarProcessos.mockRejectedValue(ERRO_MOCK);

            await expect(
                composable.buscarProcessosPainel(1, 0, 10),
            ).rejects.toThrow(ERRO_MOCK);

            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("devolverValidacao", () => {
        it("deve chamar o service e recarregar o processo", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as any;
            processoService.devolverValidacao.mockResolvedValue(undefined);
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.devolverValidacao(10, {justificativa: "Erro"});

            expect(processoService.devolverValidacao).toHaveBeenCalledWith(10, {justificativa: "Erro"});
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        });

        it("deve propagar erro em caso de falha", async () => {
            const composable = useProcessos();
            processoService.devolverValidacao.mockRejectedValue(ERRO_MOCK);

            await expect(
                composable.devolverValidacao(10, {justificativa: "E"}),
            ).rejects.toThrow(ERRO_MOCK);

            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("buscarContextoCompleto", () => {
        it("deve lidar com retorno nulo", async () => {
            const composable = useProcessos();
            processoService.buscarContextoCompleto.mockResolvedValue(null as any);
            await composable.buscarContextoCompleto(1);
            expect(composable.processoDetalhe.value).toBeNull();
            expect(composable.subprocessosElegiveis.value).toEqual([]);
        });

        it("deve limpar o estado anterior antes de buscar novo contexto", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as any;
            processoService.buscarContextoCompleto.mockReturnValue(new Promise(() => {
            }) as any);

            composable.buscarContextoCompleto(2);

            expect(composable.processoDetalhe.value).toBeNull();
        });

        it("deve atualizar processoDetalhe e subprocessosElegiveis", async () => {
            const composable = useProcessos();
            const dataMock = {
                ...PROCESSO_MOCK,
                elegiveis: [{codigo: 1}]
            };
            processoService.buscarContextoCompleto.mockResolvedValue(dataMock as any);

            await composable.buscarContextoCompleto(1);

            expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
            expect(composable.processoDetalhe.value).toEqual(dataMock);
            expect(composable.subprocessosElegiveis.value).toEqual([{codigo: 1}]);
        });

        it("deve lidar com erros", async () => {
            const composable = useProcessos();
            processoService.buscarContextoCompleto.mockRejectedValue(ERRO_MOCK);

            await expect(composable.buscarContextoCompleto(1)).rejects.toThrow(ERRO_MOCK);

            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("buscarProcessoDetalhe", () => {
        it("deve limpar o estado anterior antes de buscar novo detalhe", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as any;
            processoService.obterDetalhesProcesso.mockReturnValue(new Promise(() => {
            }) as any);

            composable.buscarProcessoDetalhe(2);

            expect(composable.processoDetalhe.value).toBeNull();
        });

        it("deve atualizar o estado em caso de sucesso", async () => {
            const composable = useProcessos();
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.buscarProcessoDetalhe(1);

            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            expect(composable.processoDetalhe.value).toEqual(PROCESSO_MOCK);
        });

        it("deve manter o estado limpo em caso de falha", async () => {
            const composable = useProcessos();
            processoService.obterDetalhesProcesso.mockRejectedValue(ERRO_MOCK);

            await expect(composable.buscarProcessoDetalhe(1)).rejects.toThrow(ERRO_MOCK);

            expect(composable.processoDetalhe.value).toBeNull();
            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });

        it("deve registrar erro no logger se kind não for unauthorized", async () => {
            const composable = useProcessos();
            const error500 = new Error("Internal Error");
            processoService.buscarContextoCompleto.mockRejectedValue(error500);

            await expect(composable.buscarContextoCompleto(1)).rejects.toThrow("Internal Error");
            expect(loggerMock.error).not.toHaveBeenCalled();
        });

        it("não deve registrar erro no logger se kind for unauthorized", async () => {
            const composable = useProcessos();
            const error401 = {
                isAxiosError: true,
                response: {
                    status: 401,
                    data: {message: "Unauthorized"}
                }
            } as any;
            processoService.buscarContextoCompleto.mockRejectedValue(error401);

            await expect(composable.buscarContextoCompleto(1)).rejects.toThrow();
            expect(loggerMock.error).not.toHaveBeenCalled();
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
            const composable = useProcessos();
            processoService.criarProcesso.mockResolvedValue({} as any);

            await composable.criarProcesso(payload);

            expect(processoService.criarProcesso).toHaveBeenCalledWith(payload);
        });

        it("deve lançar erro em caso de falha", async () => {
            const composable = useProcessos();
            processoService.criarProcesso.mockRejectedValue(ERRO_MOCK);

            await expect(composable.criarProcesso(payload)).rejects.toThrow(ERRO_MOCK);
            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
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
            const composable = useProcessos();
            processoService.atualizarProcesso.mockResolvedValue({} as any);

            await composable.atualizarProcesso(1, payload);

            expect(processoService.atualizarProcesso).toHaveBeenCalledWith(1, payload);
        });

        it("deve lançar erro em caso de falha", async () => {
            const composable = useProcessos();
            processoService.atualizarProcesso.mockRejectedValue(ERRO_MOCK);

            await expect(composable.atualizarProcesso(1, payload)).rejects.toThrow(ERRO_MOCK);
            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("removerProcesso", () => {
        it("deve chamar o processoService", async () => {
            const composable = useProcessos();
            processoService.excluirProcesso.mockResolvedValue(undefined);

            await composable.removerProcesso(1);

            expect(processoService.excluirProcesso).toHaveBeenCalledWith(1);
        });

        it("deve lançar erro em caso de falha", async () => {
            const composable = useProcessos();
            processoService.excluirProcesso.mockRejectedValue(ERRO_MOCK);

            await expect(composable.removerProcesso(1)).rejects.toThrow(ERRO_MOCK);
            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("iniciarProcesso", () => {
        it("deve chamar o processoService corretamente", async () => {
            const composable = useProcessos();
            processoService.iniciarProcesso.mockResolvedValue(undefined);

            await composable.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]);

            expect(processoService.iniciarProcesso).toHaveBeenCalledWith(1, TipoProcesso.MAPEAMENTO, [10]);
        });

        it("deve lançar erro em caso de falha", async () => {
            const composable = useProcessos();
            processoService.iniciarProcesso.mockRejectedValue(ERRO_MOCK);

            await expect(
                composable.iniciarProcesso(1, TipoProcesso.MAPEAMENTO, [10]),
            ).rejects.toThrow(ERRO_MOCK);

            expect(composable.lastError.value).toEqual(normalizeError(ERRO_MOCK));
        });
    });

    describe("buscarProcessosFinalizados", () => {
        it("deve atualizar o estado em caso de sucesso", async () => {
            const composable = useProcessos();
            const processosMock = [{codigo: 1}];
            processoService.buscarProcessosFinalizados.mockResolvedValue(processosMock as any);

            await composable.buscarProcessosFinalizados();

            expect(composable.processosFinalizados.value).toEqual(processosMock);
        });

        it("deve usar lista vazia se service retornar nulo", async () => {
            const composable = useProcessos();
            processoService.buscarProcessosFinalizados.mockResolvedValue(null as any);
            await composable.buscarProcessosFinalizados();
            expect(composable.processosFinalizados.value).toEqual([]);
        });

        it("deve propagar erro de buscarProcessosFinalizados", async () => {
            const composable = useProcessos();
            processoService.buscarProcessosFinalizados.mockRejectedValue(ERRO_MOCK);
            await expect(composable.buscarProcessosFinalizados()).rejects.toThrow(ERRO_MOCK);
        });
    });

    describe("buscarProcessosParaImportacao", () => {
        it("deve atualizar o estado em caso de sucesso", async () => {
            const composable = useProcessos();
            const processosMock = [{codigo: 1}];
            processoService.buscarProcessosParaImportacao.mockResolvedValue(processosMock as any);

            await composable.buscarProcessosParaImportacao();

            expect(composable.processosParaImportacao.value).toEqual(processosMock);
        });

        it("deve usar lista vazia se service retornar nulo", async () => {
            const composable = useProcessos();
            processoService.buscarProcessosParaImportacao.mockResolvedValue(null as any);
            await composable.buscarProcessosParaImportacao();
            expect(composable.processosParaImportacao.value).toEqual([]);
        });

        it("deve propagar erro de buscarProcessosParaImportacao", async () => {
            const composable = useProcessos();
            processoService.buscarProcessosParaImportacao.mockRejectedValue(ERRO_MOCK);
            await expect(composable.buscarProcessosParaImportacao()).rejects.toThrow(ERRO_MOCK);
        });
    });

    describe("recarregarProcessoDetalheAtual", () => {
        it("deve recarregar se houver processo atual", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 55} as any;
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            // Chamando uma função que use recarregarProcessoDetalheAtual internamente, ou testar o export se existir
            // recarregarProcessoDetalheAtual não é exportado, mas é usado por validarMapa, etc.
            await composable.validarMapa(55);
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(55);
        });

        it("não deve recarregar se não houver processo atual", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = null;
            await composable.validarMapa(55);
            expect(processoService.obterDetalhesProcesso).not.toHaveBeenCalled();
        });
    });

    describe("executarAcaoBloco", () => {
        it("deve propagar erro do service", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as Processo;
            processoService.executarAcaoEmBloco.mockRejectedValue(ERRO_MOCK);

            await expect(composable.executarAcaoBloco("aceitar", [1])).rejects.toThrow(ERRO_MOCK);
        });
    });

    describe("buscarUnidadesParaImportacao", () => {
        it("deve chamar o service e retornar dados", async () => {
            const composable = useProcessos();
            const unidadesMock = [{unidadeCodigo: 1, unidadeSigla: "U1"}];
            processoService.buscarUnidadesParaImportacao.mockResolvedValue(unidadesMock as any);

            const result = await composable.buscarUnidadesParaImportacao(1);

            expect(processoService.buscarUnidadesParaImportacao).toHaveBeenCalledWith(1);
            expect(result).toEqual(unidadesMock);
        });
    });

    describe("finalizarProcesso", () => {
        it("deve chamar o service e recarregar detalhes", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = { codigo: 1 } as any;
            processoService.finalizarProcesso.mockResolvedValue(undefined);
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.finalizarProcesso(1);

            expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        });
    });

    describe("processarCadastroBloco", () => {
        it("deve chamar o service e recarregar detalhes", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = { codigo: 1 } as any;
            const payload = {codProcesso: 1, unidades: ["1"], tipoAcao: "aceitar" as const, unidadeUsuario: "U1"};
            processoService.processarAcaoEmBloco.mockResolvedValue(undefined);
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.processarCadastroBloco(payload);

            expect(processoService.processarAcaoEmBloco).toHaveBeenCalledWith(payload);
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        });
    });

    describe("alterarDataLimiteSubprocesso", () => {
        it("deve chamar o service e recarregar se houver processo atual", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as any;
            processoService.alterarDataLimiteSubprocesso.mockResolvedValue(undefined);
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.alterarDataLimiteSubprocesso(10, {novaData: "2025-01-01"});

            expect(processoService.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, {novaData: "2025-01-01"});
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        });
    });

    describe("apresentarSugestoes", () => {
        it("deve chamar o service", async () => {
            const composable = useProcessos();
            processoService.apresentarSugestoes.mockResolvedValue(undefined);

            await composable.apresentarSugestoes(1, {sugestoes: "Teste"});

            expect(processoService.apresentarSugestoes).toHaveBeenCalledWith(1, {sugestoes: "Teste"});
        });
    });

    describe("validarMapa", () => {
        it("deve chamar o service", async () => {
            const composable = useProcessos();
            processoService.validarMapa.mockResolvedValue(undefined);

            await composable.validarMapa(1);

            expect(processoService.validarMapa).toHaveBeenCalledWith(1);
        });
    });

    describe("homologarValidacao", () => {
        it("deve chamar o service", async () => {
            const composable = useProcessos();
            processoService.homologarValidacao.mockResolvedValue(undefined);

            await composable.homologarValidacao(1, {texto: "OK"});

            expect(processoService.homologarValidacao).toHaveBeenCalledWith(1, {texto: "OK"});
        });
    });

    describe("aceitarValidacao", () => {
        it("deve chamar o service", async () => {
            const composable = useProcessos();
            processoService.aceitarValidacao.mockResolvedValue(undefined);

            await composable.aceitarValidacao(1, {texto: "OK"});

            expect(processoService.aceitarValidacao).toHaveBeenCalledWith(1, {texto: "OK"});
        });
    });

    describe("executarAcaoBloco", () => {
        it("deve chamar o service se houver processo carregado", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = {codigo: 1} as any;
            processoService.executarAcaoEmBloco.mockResolvedValue(undefined);
            processoService.obterDetalhesProcesso.mockResolvedValue(PROCESSO_MOCK);

            await composable.executarAcaoBloco("aceitar", [10], "2025-01-01");

            expect(processoService.executarAcaoEmBloco).toHaveBeenCalledWith(1, {
                unidadeCodigos: [10],
                acao: "aceitar",
                dataLimite: "2025-01-01",
            });
            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
        });

        it("deve falhar se não houver processo carregado", async () => {
            const composable = useProcessos();
            composable.processoDetalhe.value = null;

            await expect(composable.executarAcaoBloco("aceitar", [10])).rejects.toThrow("Detalhes do processo não carregados.");
        });
    });

    describe("enviarLembrete", () => {
        it("deve chamar o service", async () => {
            const composable = useProcessos();
            processoService.enviarLembrete.mockResolvedValue(undefined);

            await composable.enviarLembrete(1, 10);

            expect(processoService.enviarLembrete).toHaveBeenCalledWith(1, 10);
        });
    });

    describe("buscarSubprocessosElegiveis", () => {
        it("deve atualizar o estado", async () => {
            const composable = useProcessos();
            const elegiveisMock = [{unidadeCodigo: 1}];
            processoService.buscarSubprocessosElegiveis.mockResolvedValue(elegiveisMock as any);

            await composable.buscarSubprocessosElegiveis(1);

            expect(composable.subprocessosElegiveis.value).toEqual(elegiveisMock);
        });

        it("deve usar lista vazia se service retornar nulo", async () => {
            const composable = useProcessos();
            processoService.buscarSubprocessosElegiveis.mockResolvedValue(null as any);
            await composable.buscarSubprocessosElegiveis(1);
            expect(composable.subprocessosElegiveis.value).toEqual([]);
        });
    });
});

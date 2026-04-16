import {beforeEach, describe, expect, it, vi} from "vitest";
import type {SubprocessoDetalhe} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import type {NormalizedError} from "@/utils/apiError";

vi.mock("@/services/subprocessoService");
vi.mock("@/utils", () => ({
    logger: {
        error: vi.fn(),
    },
}));

const permissoesPadrao = {
    podeEditarCadastro: false,
    podeDisponibilizarCadastro: false,
    podeDevolverCadastro: false,
    podeAceitarCadastro: false,
    podeHomologarCadastro: false,
    podeEditarMapa: false,
    podeDisponibilizarMapa: false,
    podeValidarMapa: false,
    podeApresentarSugestoes: false,
    podeVerSugestoes: false,
    podeDevolverMapa: false,
    podeAceitarMapa: false,
    podeHomologarMapa: false,
    podeVisualizarImpacto: false,
    podeAlterarDataLimite: false,
    podeReabrirCadastro: false,
    podeReabrirRevisao: false,
    podeEnviarLembrete: false,
    mesmaUnidade: false,
    habilitarAcessoCadastro: false,
    habilitarAcessoMapa: false,
    habilitarEditarCadastro: false,
    habilitarDisponibilizarCadastro: false,
    habilitarDevolverCadastro: false,
    habilitarAceitarCadastro: false,
    habilitarHomologarCadastro: false,
    habilitarEditarMapa: false,
    habilitarDisponibilizarMapa: false,
    habilitarValidarMapa: false,
    habilitarApresentarSugestoes: false,
    habilitarDevolverMapa: false,
    habilitarAceitarMapa: false,
    habilitarHomologarMapa: false,
};

describe("useSubprocessos", () => {
    let useSubprocessos: typeof import("../useSubprocessos").useSubprocessos;
    let service: typeof import("@/services/subprocessoService");

    const criarDetalheMinimo = (sobrescritas: Partial<SubprocessoDetalhe> = {}): SubprocessoDetalhe => ({
        codigo: 10,
        unidade: {codigo: 1, sigla: "U1", nome: "Unidade 1", tipo: "Setor", filhas: [], usuarioCodigo: 1, responsavel: null},
        titular: null,
        responsavel: null,
        situacao: SituacaoSubprocesso.NAO_INICIADO,
        localizacaoAtual: "U1",
        processoDescricao: "Processo",
        dataCriacaoProcesso: "2024-01-01T00:00:00",
        ultimaDataLimiteSubprocesso: "2025-01-01T00:00:00",
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        prazoEtapaAtual: "2025-01-01T00:00:00",
        isEmAndamento: false,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: permissoesPadrao,
        ...sobrescritas,
    });

    beforeEach(async () => {
        vi.clearAllMocks();
        vi.resetModules();

        service = await import("@/services/subprocessoService");
        vi.mocked(service.mapSubprocessoDetalheResponseParaModel).mockImplementation((dto) => ({
            codigo: dto.subprocesso.codigo,
            unidade: dto.subprocesso.unidade as SubprocessoDetalhe["unidade"],
            titular: dto.titular,
            responsavel: dto.responsavel,
            situacao: dto.subprocesso.situacao,
            localizacaoAtual: dto.localizacaoAtual,
            processoDescricao: dto.subprocesso.processoDescricao,
            dataCriacaoProcesso: dto.subprocesso.dataCriacaoProcesso,
            ultimaDataLimiteSubprocesso: dto.subprocesso.dataLimiteEtapa2 ?? dto.subprocesso.dataLimiteEtapa1,
            tipoProcesso: dto.subprocesso.tipoProcesso as TipoProcesso,
            prazoEtapaAtual: dto.subprocesso.dataLimiteEtapa2 ?? dto.subprocesso.dataLimiteEtapa1,
            isEmAndamento: dto.subprocesso.isEmAndamento,
            etapaAtual: dto.subprocesso.etapaAtual ?? 1,
            movimentacoes: dto.movimentacoes,
            elementosProcesso: [],
            permissoes: dto.permissoes,
        }));
        ({useSubprocessos} = await import("../useSubprocessos"));
    });

    it("deve buscar detalhe do subprocesso com sucesso", async () => {
        const store = useSubprocessos();
        const mockDto = {
            subprocesso: {
                codigo: 10,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                unidade: {sigla: "U1"},
                dataLimiteEtapa1: "2025-01-01T00:00:00",
                dataFimEtapa1: null,
                dataLimiteEtapa2: null,
                dataFimEtapa2: null,
                processoDescricao: "Processo",
                dataCriacaoProcesso: "2024-01-01T00:00:00",
                tipoProcesso: "MAPEAMENTO",
                isEmAndamento: true,
                etapaAtual: 1,
            },
            titular: null,
            responsavel: null,
            localizacaoAtual: "U1",
            movimentacoes: [],
            permissoes: permissoesPadrao,
        };
        vi.mocked(service.buscarSubprocessoDetalhe).mockResolvedValue(mockDto as never);

        await store.buscarSubprocessoDetalhe(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(store.subprocessoDetalhe?.localizacaoAtual).toBe("U1");
    });

    it("deve lidar com erro ao buscar detalhe", async () => {
        const store = useSubprocessos();
        vi.mocked(service.buscarSubprocessoDetalhe).mockRejectedValue(new Error("Falha"));

        await store.buscarSubprocessoDetalhe(10).catch(() => {});

        expect(store.subprocessoDetalhe).toBeNull();
        expect(store.lastError?.message).toBe("Falha");
    });

    it("preserva o detalhe atual durante uma recarga sem limpeza previa", async () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = criarDetalheMinimo({codigo: 10, situacao: SituacaoSubprocesso.NAO_INICIADO});

        let resolver: ((valor: unknown) => void) | null = null;
        vi.mocked(service.buscarSubprocessoDetalhe).mockImplementation(() =>
            new Promise((resolve) => {
                resolver = resolve;
            }) as never
        );

        const requisicao = store.buscarSubprocessoDetalhe(10, false);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.NAO_INICIADO);

        resolver?.({
            subprocesso: {
                codigo: 10,
                situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                unidade: {sigla: "U1"},
                dataLimiteEtapa1: "2025-01-01T00:00:00",
                dataFimEtapa1: null,
                dataLimiteEtapa2: null,
                dataFimEtapa2: null,
                processoDescricao: "Processo",
                dataCriacaoProcesso: "2024-01-01T00:00:00",
                tipoProcesso: "REVISAO",
                isEmAndamento: true,
                etapaAtual: 1,
            },
            titular: null,
            responsavel: null,
            localizacaoAtual: "U1",
            movimentacoes: [],
            permissoes: permissoesPadrao,
        });

        await requisicao;

        expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    });

    it("deve retornar null se buscarSubprocessoPorProcessoEUnidade falhar", async () => {
        const store = useSubprocessos();
        vi.mocked(service.buscarSubprocessoPorProcessoEUnidade).mockRejectedValue(new Error("Erro busca ID"));

        const id = await store.buscarSubprocessoPorProcessoEUnidade(1, "U1");

        expect(id).toBeNull();
    });

    it("não deve atualizar status local se não houver detalhe", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = null;

        store.atualizarStatusLocal({codigo: 10, situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO});

        expect(store.subprocessoDetalhe).toBeNull();
    });

    it("deve buscar contexto de edicao", async () => {
        const store = useSubprocessos();
        const mockContexto = {
            detalhes: {
                codigo: 10,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                localizacaoAtual: "U1",
                permissoes: permissoesPadrao,
            },
            mapa: {codigo: 20},
        };
        vi.mocked(service.buscarContextoEdicao).mockResolvedValue(mockContexto as never);

        const resultado = await store.buscarContextoEdicao(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(resultado).toEqual(mockContexto);
    });

    it("deve buscar contexto de edicao por processo e unidade", async () => {
        const store = useSubprocessos();
        const mockContexto = {
            detalhes: {
                codigo: 15,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                localizacaoAtual: "U1",
                permissoes: permissoesPadrao,
            },
            mapa: {codigo: 20},
        };
        vi.mocked(service.buscarContextoEdicaoPorProcessoEUnidade).mockResolvedValue(mockContexto as never);

        const resultado = await store.buscarContextoEdicaoPorProcessoEUnidade(1, "U1");

        expect(store.subprocessoDetalhe?.codigo).toBe(15);
        expect(resultado).toEqual(mockContexto);
    });

    it("deve buscar contexto enxuto do cadastro", async () => {
        const store = useSubprocessos();
        const mockContexto = {
            detalhes: {
                codigo: 16,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                localizacaoAtual: "U1",
                permissoes: permissoesPadrao,
            },
            mapa: {codigo: 21, subprocessoCodigo: 16},
            atividadesDisponiveis: [],
            unidade: {codigo: 1, sigla: "U1", nome: "Unidade 1"},
        };
        vi.mocked(service.buscarContextoCadastroAtividades).mockResolvedValue(mockContexto as never);

        const resultado = await store.buscarContextoCadastroAtividades(16);

        expect(store.subprocessoDetalhe?.codigo).toBe(16);
        expect(resultado).toEqual(mockContexto);
    });

    it("deve buscar contexto enxuto do cadastro por processo e unidade", async () => {
        const store = useSubprocessos();
        const mockContexto = {
            detalhes: {
                codigo: 17,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                localizacaoAtual: "U1",
                permissoes: permissoesPadrao,
            },
            mapa: {codigo: 22, subprocessoCodigo: 17},
            atividadesDisponiveis: [],
            unidade: {codigo: 1, sigla: "U1", nome: "Unidade 1"},
        };
        vi.mocked(service.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(mockContexto as never);

        const resultado = await store.buscarContextoCadastroAtividadesPorProcessoEUnidade(1, "U1");

        expect(store.subprocessoDetalhe?.codigo).toBe(17);
        expect(resultado).toEqual(mockContexto);
    });

    it("deve usar detalhes obrigatorios do contexto de edição", async () => {
        const store = useSubprocessos();
        const dataMock = {
            detalhes: {
                codigo: 123,
                situacao: SituacaoSubprocesso.NAO_INICIADO,
                localizacaoAtual: "U1",
                permissoes: permissoesPadrao,
            },
            mapa: {codigo: 1}
        };
        vi.mocked(service.buscarContextoEdicao).mockResolvedValue(dataMock as never);

        await store.buscarContextoEdicao(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(123);
    });

    it("deve buscar ID por processo e unidade", async () => {
        const store = useSubprocessos();
        vi.mocked(service.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 99} as never);

        const id = await store.buscarSubprocessoPorProcessoEUnidade(1, "U1");

        expect(id).toBe(99);
    });

    it("deve atualizar status local", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = criarDetalheMinimo({situacao: SituacaoSubprocesso.NAO_INICIADO});

        store.atualizarStatusLocal({
            codigo: 10,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            permissoes: {...permissoesPadrao, podeEditarCadastro: true}
        });

        expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        expect(store.subprocessoDetalhe?.permissoes).toEqual({...permissoesPadrao, podeEditarCadastro: true});
    });

    it("deve permitir setar erro e detalhe manualmente", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = criarDetalheMinimo({codigo: 5});
        expect(store.subprocessoDetalhe?.codigo).toBe(5);

        store.lastError = {kind: "unexpected", message: "Erro manual"} as NormalizedError;
        expect(store.lastError?.message).toBe("Erro manual");
    });

    it("deve mapear resposta detalhada do backend", async () => {
        const store = useSubprocessos();
        vi.mocked(service.buscarSubprocessoDetalhe).mockResolvedValue({
            subprocesso: {
                codigo: 10,
                situacao: SituacaoSubprocesso.NAO_INICIADO,
                unidade: {sigla: "U1"},
                dataLimiteEtapa1: "2025-01-01T00:00:00",
                dataFimEtapa1: null,
                dataLimiteEtapa2: "2025-02-01T00:00:00",
                dataFimEtapa2: null,
                processoDescricao: "Processo",
                dataCriacaoProcesso: "2024-01-01T00:00:00",
                tipoProcesso: "MAPEAMENTO",
                isEmAndamento: true,
                etapaAtual: null,
            },
            titular: null,
            responsavel: null,
            localizacaoAtual: "U1",
            movimentacoes: [],
            permissoes: permissoesPadrao
        } as never);

        await store.buscarSubprocessoDetalhe(10);
        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(store.subprocessoDetalhe?.localizacaoAtual).toBe("U1");
        expect(store.subprocessoDetalhe?.etapaAtual).toBe(1);
        expect(store.subprocessoDetalhe?.isEmAndamento).toBe(true);
        expect(store.subprocessoDetalhe?.movimentacoes).toEqual([]);
    });

    it("não deve atualizar status local se detalhe for nulo", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = null;
        store.atualizarStatusLocal({codigo: 1, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO});
        expect(store.subprocessoDetalhe).toBeNull();
    });
});

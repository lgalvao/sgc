import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {SituacaoSubprocesso} from "@/types/tipos";

vi.mock("@/services/subprocessoService");
vi.mock("@/stores/perfil");
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
};

describe("useSubprocessos", () => {
    let useSubprocessos: typeof import("../useSubprocessos").useSubprocessos;
    let service: any;
    let perfilStore: any;

    beforeEach(async () => {
        vi.clearAllMocks();
        vi.resetModules();

        service = await import("@/services/subprocessoService");
        perfilStore = {
            perfilSelecionado: "ADMIN",
            unidadeAtual: 1,
        };
        vi.mocked(usePerfilStore).mockReturnValue(perfilStore);

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
        service.buscarSubprocessoDetalhe.mockResolvedValue(mockDto);

        await store.buscarSubprocessoDetalhe(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(store.subprocessoDetalhe?.localizacaoAtual).toBe("U1");
    });

    it("deve lidar com erro ao buscar detalhe", async () => {
        const store = useSubprocessos();
        service.buscarSubprocessoDetalhe.mockRejectedValue(new Error("Falha"));

        await store.buscarSubprocessoDetalhe(10).catch(() => {});

        expect(store.subprocessoDetalhe).toBeNull();
        expect(store.lastError?.message).toBe("Falha");
    });

    it("deve retornar null se buscarContextoEdicao falhar por pre-condição", async () => {
        const store = useSubprocessos();
        perfilStore.perfilSelecionado = null;

        const resultado = await store.buscarContextoEdicao(10);

        expect(resultado).toBeUndefined();
        expect(store.lastError?.message).toBe("Informações de perfil ou unidade não disponíveis.");
    });

    it("deve retornar null se buscarSubprocessoPorProcessoEUnidade falhar", async () => {
        const store = useSubprocessos();
        service.buscarSubprocessoPorProcessoEUnidade.mockRejectedValue(new Error("Erro busca ID"));

        const id = await store.buscarSubprocessoPorProcessoEUnidade(1, "U1");

        expect(id).toBeNull();
    });

    it("não deve atualizar status local se não houver detalhe", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = null;

        store.atualizarStatusLocal({codigo: 10, situacao: SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO});

        expect(store.subprocessoDetalhe).toBeNull();
    });

    it("deve validar pre-condições de perfil", async () => {
        const store = useSubprocessos();
        perfilStore.perfilSelecionado = null;

        await store.buscarSubprocessoDetalhe(10);

        expect(store.lastError?.message).toBe("Informações de perfil ou unidade não disponíveis.");
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
        service.buscarContextoEdicao.mockResolvedValue(mockContexto);

        const resultado = await store.buscarContextoEdicao(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
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
        service.buscarContextoEdicao.mockResolvedValue(dataMock);

        await store.buscarContextoEdicao(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(123);
    });

    it("deve buscar ID por processo e unidade", async () => {
        const store = useSubprocessos();
        service.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue({codigo: 99});

        const id = await store.buscarSubprocessoPorProcessoEUnidade(1, "U1");

        expect(id).toBe(99);
    });

    it("deve atualizar status local", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = {codigo: 10, situacao: SituacaoSubprocesso.NAO_INICIADO} as any;

        store.atualizarStatusLocal({
            codigo: 10,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            permissoes: {...permissoesPadrao, podeEditarCadastro: true}
        });

        expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        expect(store.subprocessoDetalhe?.permissoes).toEqual({...permissoesPadrao, podeEditarCadastro: true});
    });

    it("deve lidar com o cenário sem perfil Global e sem unidade", async () => {
        const store = useSubprocessos();
        perfilStore.perfilSelecionado = "SERVIDOR";
        perfilStore.unidadeAtual = null;

        await store.buscarSubprocessoDetalhe(10);

        expect(store.lastError?.message).toBe("Informações de perfil ou unidade não disponíveis.");
    });

    it("deve permitir setar erro e detalhe manualmente", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = {codigo: 5} as any;
        expect(store.subprocessoDetalhe?.codigo).toBe(5);

        store.lastError = {message: "Erro manual"} as any;
        expect(store.lastError?.message).toBe("Erro manual");
    });

    it("deve mapear resposta detalhada do backend", async () => {
        const store = useSubprocessos();
        service.buscarSubprocessoDetalhe.mockResolvedValue({
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
        });

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

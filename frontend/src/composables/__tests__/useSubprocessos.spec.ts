import {beforeEach, describe, expect, it, vi} from "vitest";
import {usePerfilStore} from "@/stores/perfil";
import {useMapas} from "@/composables/useMapas";
import {SituacaoSubprocesso} from "@/types/tipos";

vi.mock("@/services/subprocessoService");
vi.mock("@/stores/perfil");
vi.mock("@/composables/useMapas");
vi.mock("@/utils", () => ({
    logger: {
        error: vi.fn(),
    },
}));

describe("useSubprocessos", () => {
    let useSubprocessos: typeof import("../useSubprocessos").useSubprocessos;
    let service: any;
    let perfilStore: any;
    let mapas: any;

    beforeEach(async () => {
        vi.clearAllMocks();
        vi.resetModules();

        service = await import("@/services/subprocessoService");
        perfilStore = {
            perfilSelecionado: "ADMIN",
            unidadeAtual: 1,
        };
        vi.mocked(usePerfilStore).mockReturnValue(perfilStore);

        mapas = {
            mapaCompleto: {value: null},
        };
        vi.mocked(useMapas).mockReturnValue(mapas);

        ({useSubprocessos} = await import("../useSubprocessos"));
    });

    it("deve buscar detalhe do subprocesso com sucesso", async () => {
        const store = useSubprocessos();
        const mockDto = {
            codigo: 10,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            unidade: {sigla: "U1"},
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
            detalhes: {codigo: 10, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO},
            mapa: {codigo: 20},
        };
        service.buscarContextoEdicao.mockResolvedValue(mockContexto);

        await store.buscarContextoEdicao(10);

        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(mapas.mapaCompleto.value).toEqual({codigo: 20});
    });

    it("deve usar data como fallback se detalhes estiver ausente no contexto de edição", async () => {
        const store = useSubprocessos();
        const dataMock = {
            codigo: 123,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
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
            permissoes: {podeEditar: true}
        });

        expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        expect(store.subprocessoDetalhe?.permissoes).toEqual({podeEditar: true});
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

    it("deve lidar com DTO minimalista no mapSubprocessoDetalheDtoToModel", async () => {
        const store = useSubprocessos();
        // DTO that triggers all fallback branches in mapSubprocessoDetalheDtoToModel
        service.buscarSubprocessoDetalhe.mockResolvedValue({
            codigo: 10,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            unidade: null,
            titular: null,
            responsavel: null,
            localizacaoAtual: null,
            processoDescricao: null,
            tipoProcesso: null,
            prazoEtapaAtual: null,
            isEmAndamento: null,
            etapaAtual: null,
            movimentacoes: null,
            permissoes: null
        });

        await store.buscarSubprocessoDetalhe(10);
        expect(store.subprocessoDetalhe?.codigo).toBe(10);
        expect(store.subprocessoDetalhe?.etapaAtual).toBe(1);
        expect(store.subprocessoDetalhe?.isEmAndamento).toBe(true);
        expect(store.subprocessoDetalhe?.movimentacoes).toEqual([]);
    });

    it("deve limpar detalhe se service retornar nulo", async () => {
        const store = useSubprocessos();
        service.buscarSubprocessoDetalhe.mockResolvedValue(null);

        await store.buscarSubprocessoDetalhe(10);
        expect(store.subprocessoDetalhe).toBeNull();
    });

    it("não deve atualizar status local se detalhe for nulo", () => {
        const store = useSubprocessos();
        store.subprocessoDetalhe = null;
        store.atualizarStatusLocal({codigo: 1, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO});
        expect(store.subprocessoDetalhe).toBeNull();
    });
});

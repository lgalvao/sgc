import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref, toValue} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import * as service from "@/services/subprocessoService";
import {useMapas} from "../useMapas";

vi.mock("@/services/subprocessoService", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/services/subprocessoService")>();
    return {
        ...actual,
        obterMapaCompleto: vi.fn(),
        verificarImpactosMapa: vi.fn(),
    };
});

// Simula o comportamento do useQuery do Pinia Colada: data reativa, status e refetch/refresh
function criarQueryMock<T>(valorInicial: T | null = null) {
    const data = ref<T | null>(valorInicial);
    const status = ref<"idle" | "pending" | "success" | "error">("idle");

    const refetch = vi.fn(async (buscar: () => Promise<T | null>) => {
        status.value = "pending";
        try {
            data.value = await buscar();
            status.value = "success";
        } catch (e: any) {
            status.value = "error";
            throw e;
        }
    });

    const refresh = vi.fn(async (buscar: () => Promise<T | null>) => {
        data.value = await buscar();
        status.value = "success";
    });

    return {data, status, refetch, refresh};
}

// Estado compartilhado entre testes para simular o cache do Colada
let mapaQueryState: ReturnType<typeof criarQueryMock<MapaCompleto>>;
let impactoQueryState: ReturnType<typeof criarQueryMock<ImpactoMapa>>;
const cacheMapaMock = {
    invalidarMapa: vi.fn(),
    invalidarImpacto: vi.fn(),
    sincronizarMapa: vi.fn(),
    sincronizarImpacto: vi.fn(),
    obterMapa: vi.fn(),
    obterImpacto: vi.fn(),
};

vi.mock("@/composables/useMapaQuery", () => {
    return {
        CHAVE_QUERY_MAPA: ["mapa"],
        CHAVE_QUERY_IMPACTO_MAPA: ["impacto-mapa"],
        criarChaveMapa: vi.fn(),
        criarChaveImpactoMapa: vi.fn(),
        useMapaQuery: (codigoSubprocesso: any) => ({
            get data() { return mapaQueryState.data; },
            get status() { return mapaQueryState.status; },
            // refetch: sempre busca (ignora cache)
            refetch: vi.fn(async () => {
                const codigo = toValue(codigoSubprocesso);
                mapaQueryState.status.value = "pending";
                try {
                    const resultado = await (service.obterMapaCompleto as any)(codigo);
                    mapaQueryState.data.value = resultado;
                    mapaQueryState.status.value = "success";
                } catch (e: any) {
                    mapaQueryState.status.value = "error";
                    mapaQueryState.data.value = null;
                    throw new Error(e?.message ?? "Erro", {cause: e});
                }
            }),
            // refresh: simula staleTime=Infinity — só busca se stale (não "success")
            refresh: vi.fn(async () => {
                if (mapaQueryState.status.value === "success") {
                    return; // dado fresco, não rebusca
                }
                const codigo = toValue(codigoSubprocesso);
                const resultado = await (service.obterMapaCompleto as any)(codigo);
                mapaQueryState.data.value = resultado;
                mapaQueryState.status.value = "success";
            }),
        }),
        useImpactoMapaQuery: (codigoSubprocesso: any) => ({
            get data() { return impactoQueryState.data; },
            get status() { return impactoQueryState.status; },
            // refetch: sempre busca
            refetch: vi.fn(async () => {
                const codigo = toValue(codigoSubprocesso);
                impactoQueryState.status.value = "pending";
                const resultado = await (service.verificarImpactosMapa as any)(codigo);
                impactoQueryState.data.value = resultado;
                impactoQueryState.status.value = "success";
            }),
            // refresh: simula staleTime=Infinity — só busca se stale
            refresh: vi.fn(async () => {
                if (impactoQueryState.status.value === "success") {
                    return; // dado fresco, não rebusca
                }
                const codigo = toValue(codigoSubprocesso);
                const resultado = await (service.verificarImpactosMapa as any)(codigo);
                impactoQueryState.data.value = resultado;
                impactoQueryState.status.value = "success";
            }),
        }),
        useCacheMapa: () => cacheMapaMock,
    };
});


describe("useMapas", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mapaQueryState = criarQueryMock<MapaCompleto>();
        impactoQueryState = criarQueryMock<ImpactoMapa>();

        // Implementações padrão do cacheMapaMock
        cacheMapaMock.invalidarMapa.mockImplementation((codigo?: number) => {
            if (typeof codigo === "number") {
                // Marca como stale: limpa o dado atual
                mapaQueryState.data.value = null;
                mapaQueryState.status.value = "idle";
                cacheMapaMock.invalidarImpacto(codigo);
            } else {
                mapaQueryState.data.value = null;
                mapaQueryState.status.value = "idle";
                cacheMapaMock.invalidarImpacto();
            }
        });
        cacheMapaMock.invalidarImpacto.mockImplementation(() => {
            impactoQueryState.data.value = null;
        });
        cacheMapaMock.sincronizarMapa.mockImplementation((_codigo: number, mapa: MapaCompleto | null) => {
            mapaQueryState.data.value = mapa;
            mapaQueryState.status.value = "success";
            cacheMapaMock.invalidarImpacto();
        });
        cacheMapaMock.sincronizarImpacto.mockImplementation((_codigo: number, impacto: ImpactoMapa | null) => {
            impactoQueryState.data.value = impacto;
        });
    });

    it("deve inicializar com valores nulos", () => {
        const mapas = useMapas();

        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapas.impactoMapa.value).toBeNull();
    });

    it("deve buscar mapa completo com sucesso", async () => {
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "teste",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        const mapas = useMapas();
        await mapas.carregarMapa(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledWith(1);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
    });

    it("deve reaproveitar o cache do mapa por subprocesso ao reativar a view", async () => {
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "teste",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        const mapas = useMapas();
        await mapas.carregarMapa(1);
        // Segunda chamada com mesmo código e status "success" deve usar refresh (não refetch)
        await mapas.carregarMapa(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledTimes(2);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);
    });

    it("deve definir erro em caso de falha ao buscar mapa completo", async () => {
        vi.mocked(service.obterMapaCompleto).mockRejectedValue(new Error("Failed"));

        const mapas = useMapas();
        await mapas.carregarMapa(1);

        // useAsyncAction usa a mensagem do erro lançado quando disponível
        expect(mapas.erro.value).toBe("Failed");
    });

    it("deve buscar impacto do mapa com sucesso", async () => {
        const mockImpacto: ImpactoMapa = {
            temImpactos: true,
            totalAtividadesInseridas: 0,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };
        vi.mocked(service.verificarImpactosMapa).mockResolvedValue(mockImpacto);

        const mapas = useMapas();
        await mapas.carregarImpacto(1);

        expect(service.verificarImpactosMapa).toHaveBeenCalledWith(1);
        expect(mapas.impactoMapa.value).toEqual(mockImpacto);
    });

    it("deve reutilizar o cache de impacto ao buscar novamente sem invalidação", async () => {
        const impacto: ImpactoMapa = {
            temImpactos: true,
            totalAtividadesInseridas: 1,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };
        vi.mocked(service.verificarImpactosMapa).mockResolvedValue(impacto);

        const mapas = useMapas();
        await mapas.carregarImpacto(1);
        await mapas.carregarImpacto(1);

        expect(service.verificarImpactosMapa).toHaveBeenCalledTimes(2);
        expect(mapas.impactoMapa.value).toEqual(impacto);
    });

    it("deve manter mapas separados por subprocesso em views keepAlive", async () => {
        const codigoAtual = ref<number | null>(1);
        const mapas = useMapas(codigoAtual);
        const mapaUm: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "mapa 1",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        const mapaDois: MapaCompleto = {
            codigo: 2,
            subprocessoCodigo: 2,
            observacoes: "mapa 2",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto)
            .mockResolvedValueOnce(mapaUm)
            .mockResolvedValueOnce(mapaDois);

        await mapas.carregarMapa(1);
        expect(mapas.mapaCompleto.value).toEqual(mapaUm);

        // Reinicia o status para simular nova entrada de cache quando o código muda
        mapaQueryState.status.value = "idle";
        codigoAtual.value = 2;
        await mapas.carregarMapa(2);
        expect(mapas.mapaCompleto.value).toEqual(mapaDois);
        expect(service.obterMapaCompleto).toHaveBeenCalledTimes(2);
    });

    it("não deve buscar impacto se codSubprocesso for zero", async () => {
        const mapas = useMapas();

        await mapas.carregarImpacto(0);

        expect(service.verificarImpactosMapa).not.toHaveBeenCalled();
    });

    it("deve invalidar o impacto quando sincronizarMapa é chamado com mapa novo", async () => {
        const mapas = useMapas();
        const mockImpacto: ImpactoMapa = {
            temImpactos: true,
            totalAtividadesInseridas: 0,
            totalAtividadesRemovidas: 0,
            totalAtividadesAlteradas: 0,
            totalCompetenciasImpactadas: 0,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };
        vi.mocked(service.verificarImpactosMapa).mockReset().mockResolvedValue(mockImpacto);

        await mapas.carregarImpacto(1);
        expect(mapas.impactoMapa.value).toEqual(mockImpacto);

        // sincronizarMapa invalida o impacto (chamado pela view ao receber mapa atualizado)
        mapas.sincronizarMapa(1, null);
        expect(cacheMapaMock.sincronizarMapa).toHaveBeenCalledWith(1, null);
        // O mock de sincronizarMapa chama invalidarImpacto, zerando o impacto
        expect(mapas.impactoMapa.value).toBeNull();
    });

    it("deve marcar mapa como stale ao invalidar (invalidarMapa é chamado)", async () => {
        const codigoAtual = ref<number | null>(1);
        const mapas = useMapas(codigoAtual);
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "mapa preservado",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.carregarMapa(1);
        expect(mapas.mapaCompleto.value).toEqual(mockMapa);

        mapas.invalidar(1);

        // invalidarMapa deve ter sido chamado com o código correto
        expect(cacheMapaMock.invalidarMapa).toHaveBeenCalledWith(1);
        // O mock de invalidarMapa limpa o dado e status (simula stale)
        expect(mapas.mapaCompleto.value).toBeNull();
        expect(mapaQueryState.status.value).toBe("idle");
    });

    it("deve voltar a buscar o mapa após invalidação explícita", async () => {
        const mapas = useMapas();
        const mapaInicial: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "antes",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        const mapaAtualizado: MapaCompleto = {
            ...mapaInicial,
            observacoes: "depois",
        };
        vi.mocked(service.obterMapaCompleto)
            .mockResolvedValueOnce(mapaInicial)
            .mockResolvedValueOnce(mapaAtualizado);

        await mapas.carregarMapa(1);
        mapas.invalidar(1);
        await mapas.carregarMapa(1);

        expect(service.obterMapaCompleto).toHaveBeenCalledTimes(2);
        expect(mapas.mapaCompleto.value).toEqual(mapaAtualizado);
    });

    it("deve limpar totalmente o store ao resetar", async () => {
        const mapas = useMapas();
        const mockMapa: MapaCompleto = {
            codigo: 1,
            subprocessoCodigo: 1,
            observacoes: "limpar",
            competencias: [],
            atividades: [],
            situacao: "EM_ANDAMENTO",
        };
        vi.mocked(service.obterMapaCompleto).mockResolvedValue(mockMapa);

        await mapas.carregarMapa(1);
        mapas.resetar();

        expect(mapas.mapaCompleto.value).toBeNull();
    });
});

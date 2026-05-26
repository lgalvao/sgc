import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia} from "pinia";
import {createApp, ref} from "vue";
import {PiniaColada} from "@pinia/colada";
import {
    useArvoreElegibilidadeQuery,
    useUnidade,
    useInvalidacaoUnidade,
} from "../useUnidadeQuery";
import * as unidadeService from "@/services/unidadeService";

vi.mock("@/services/unidadeService", () => ({
    buscarArvoreUnidade: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(),
    buscarReferenciaMapaVigente: vi.fn(),
}));

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => ({
        usuarioCodigo: "123",
        perfilSelecionado: "SERVIDOR",
        unidadeSelecionada: 10,
    }),
}));

function withSetup<T>(composable: () => T) {
    let result: T;
    const app = createApp({
        setup() {
            result = composable();
            return () => {};
        },
    });
    const pinia = createPinia();
    app.use(pinia);
    app.use(PiniaColada);
    app.mount(document.createElement("div"));
    return [result!, app] as const;
}

describe("useUnidadeQuery", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("useArvoreElegibilidadeQuery deve buscar dados usando o serviço correspondente", async () => {
        const mockData = [{codigo: 1, filhas: []}];
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockResolvedValue(mockData as any);

        const tipoProcesso = ref<string | null>("T1");
        const codProcesso = ref<number | undefined>(1);

        const [query, app] = withSetup(() => useArvoreElegibilidadeQuery(tipoProcesso, codProcesso));

        const res = await query.refetch();
        expect(res.data).toEqual(mockData);
        expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledWith("T1", 1);
        app.unmount();
    });

    it("useUnidade deve expor propriedades reativas corretas", async () => {
        const mockUnidade = {codigo: 1, nome: "U1", sigla: "U1", filhas: []};
        const mockMapa = {codigo: 99, dataInicio: "2026-01-01"};
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue(mockUnidade as any);
        vi.mocked(unidadeService.buscarReferenciaMapaVigente).mockResolvedValue(mockMapa as any);

        const [{unidade, mapaVigente, carregar}, app] = withSetup(() => useUnidade(1));

        await carregar();

        expect(unidade.value).toEqual(mockUnidade);
        expect(mapaVigente.value).toEqual(mockMapa);
        expect(unidadeService.buscarArvoreUnidade).toHaveBeenCalledWith(1);
        expect(unidadeService.buscarReferenciaMapaVigente).toHaveBeenCalledWith(1);
        app.unmount();
    });

    it("useInvalidacaoUnidade deve expor métodos de invalidação válidos", () => {
        const [invalidadores, app] = withSetup(() => useInvalidacaoUnidade());
        expect(invalidadores.invalidarUnidade).toBeTypeOf("function");
        expect(invalidadores.invalidarDadosTelaUnidade).toBeTypeOf("function");
        expect(invalidadores.invalidarArvoreElegibilidade).toBeTypeOf("function");
        app.unmount();
    });
});

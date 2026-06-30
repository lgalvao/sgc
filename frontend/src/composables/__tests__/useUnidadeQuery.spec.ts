import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia} from "pinia";
import {createApp, ref} from "vue";
import {PiniaColada} from "@pinia/colada";
import {
    useArvoreElegibilidadeQuery,
    useDadosTelaUnidadeQuery,
    useInvalidacaoUnidade,
    useUnidadeQuery,
} from "../useUnidadeQuery";
import * as unidadeService from "@/services/unidadeService";

vi.mock("@/services/unidadeService", () => ({
    buscarArvoreUnidade: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(),
    buscarReferenciaMapaVigente: vi.fn(),
}));

const mockUsuarioCodigo = ref<string | undefined>("123");
const mockPerfilSelecionado = ref<string | undefined>("SERVIDOR");
const mockUnidadeSelecionada = ref<number | undefined>(10);

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => ({
        get usuarioCodigo() {
            return mockUsuarioCodigo.value;
        },
        get perfilSelecionado() {
            return mockPerfilSelecionado.value;
        },
        get unidadeSelecionada() {
            return mockUnidadeSelecionada.value;
        },
    }),
}));

function withSetup<T>(composable: () => T) {
    let result: T;
    const app = createApp({
        setup() {
            result = composable();
            return () => {
            };
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

    it("useDadosTelaUnidadeQuery deve expor os dados da tela corretamente", async () => {
        const mockUnidade = {codigo: 1, nome: "U1", sigla: "U1", filhas: []};
        const mockMapa = {codigo: 99, dataInicio: "2026-01-01"};
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue(mockUnidade as any);
        vi.mocked(unidadeService.buscarReferenciaMapaVigente).mockResolvedValue(mockMapa as any);

        const [query, app] = withSetup(() => useDadosTelaUnidadeQuery(1));

        const res = await query.refetch(true);

        expect(res.data?.unidade).toEqual(mockUnidade);
        expect(res.data?.mapaVigente).toEqual(mockMapa);
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

    it("useUnidadeQuery deve buscar dados da unidade e respeitar o enabled", async () => {
        const mockUnidade = {codigo: 5, nome: "U5", sigla: "U5", filhas: []};
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue(mockUnidade as any);

        const [query, app] = withSetup(() => useUnidadeQuery(5));
        const res = await query.refetch();
        expect(res.data).toEqual(mockUnidade);
        expect(unidadeService.buscarArvoreUnidade).toHaveBeenCalledWith(5);

        app.unmount();
    });

    it("deve exercitar fallbacks do contexto de sessao e enabled com valores nulos", async () => {
        mockUsuarioCodigo.value = undefined;
        mockPerfilSelecionado.value = undefined;
        mockUnidadeSelecionada.value = undefined;

        const [query, app] = withSetup(() => useUnidadeQuery(0));
        expect(unidadeService.buscarArvoreUnidade).not.toHaveBeenCalled();

        app.unmount();
    });

    it("useArvoreElegibilidadeQuery deve cobrir os fallbacks quando tipoProcesso ou codProcesso forem nulos", async () => {
        const [query, app] = withSetup(() => useArvoreElegibilidadeQuery(null, undefined));
        const res = await query.refetch();
        expect(res.data).toEqual([]);

        app.unmount();
    });
});

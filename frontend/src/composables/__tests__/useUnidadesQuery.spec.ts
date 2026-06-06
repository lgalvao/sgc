import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia} from "pinia";
import {createApp, ref} from "vue";
import {PiniaColada} from "@pinia/colada";
import {useUnidadesQuery, useInvalidacaoUnidades, CHAVE_QUERY_UNIDADES} from "../useUnidadesQuery";
import * as unidadeService from "@/services/unidadeService";

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(),
}));

const mockPerfilSelecionado = ref<string | undefined>("SERVIDOR");

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => ({
        get perfilSelecionado() { return mockPerfilSelecionado.value; },
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

describe("useUnidadesQuery", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockPerfilSelecionado.value = "SERVIDOR";
    });

    it("deve buscar todas as unidades quando perfil selecionado estiver presente", async () => {
        const mockUnidades = [{codigo: 1, nome: "Unidade 1", sigla: "U1"}];
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue(mockUnidades as any);

        const [query, app] = withSetup(() => useUnidadesQuery());

        // Para exercitar o enabled()
        const enabledCallback = (query as any).enabled;
        const isEnabled = typeof enabledCallback === "function" ? enabledCallback() : enabledCallback;
        expect(isEnabled).toBe(true);

        // Para exercitar o key()
        const keyCallback = (query as any).key;
        const key = typeof keyCallback === "function" ? keyCallback() : keyCallback;
        expect(key).toEqual(CHAVE_QUERY_UNIDADES);

        const res = await query.refetch();
        expect(res.data).toEqual(mockUnidades);
        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();

        app.unmount();
    });

    it("não deve buscar quando perfil selecionado estiver ausente", async () => {
        mockPerfilSelecionado.value = undefined;

        const [query, app] = withSetup(() => useUnidadesQuery());

        const enabledCallback = (query as any).enabled;
        const isEnabled = typeof enabledCallback === "function" ? enabledCallback() : enabledCallback;
        expect(isEnabled).toBe(false);

        app.unmount();
    });

    it("useInvalidacaoUnidades deve expor métodos de invalidação válidos", () => {
        const [invalidadores, app] = withSetup(() => useInvalidacaoUnidades());
        expect(invalidadores.invalidarUnidades).toBeTypeOf("function");
        
        // Chamar o método para cobertura completa
        invalidadores.invalidarUnidades();
        
        app.unmount();
    });
});

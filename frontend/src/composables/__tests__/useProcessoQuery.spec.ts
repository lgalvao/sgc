import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia} from "pinia";
import {createApp} from "vue";
import {PiniaColada} from "@pinia/colada";
import {useProcessoQuery, useInvalidacaoProcesso, CHAVE_QUERY_PROCESSO} from "../useProcessoQuery";
import * as processoService from "@/services/processo";

vi.mock("@/services/processo", () => ({
    buscarContextoCompleto: vi.fn(),
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

describe("useProcessoQuery", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve buscar o processo com base no código", async () => {
        const mockProcesso = {codigo: 42, nome: "Processo 42"};
        vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue(mockProcesso as any);

        const [query, app] = withSetup(() => useProcessoQuery(42));

        // Testar key()
        const keyCallback = (query as any).key;
        const key = typeof keyCallback === "function" ? keyCallback() : keyCallback;
        expect(key).toEqual([...CHAVE_QUERY_PROCESSO, 42]);

        // Testar enabled (deve ser false por padrão no composable)
        const enabledCallback = (query as any).enabled;
        const isEnabled = typeof enabledCallback === "function" ? enabledCallback() : enabledCallback;
        expect(isEnabled).toBe(false);

        const res = await query.refetch();
        expect(res.data).toEqual(mockProcesso);
        expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(42);

        app.unmount();
    });

    it("useInvalidacaoProcesso deve expor métodos de invalidação válidos", () => {
        const [invalidadores, app] = withSetup(() => useInvalidacaoProcesso());
        expect(invalidadores.invalidarProcesso).toBeTypeOf("function");
        
        invalidadores.invalidarProcesso();
        
        app.unmount();
    });
});

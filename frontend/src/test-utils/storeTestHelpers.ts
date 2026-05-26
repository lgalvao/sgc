import {PiniaColada} from "@pinia/colada";
import {createPinia, setActivePinia} from "pinia";
import {createApp} from "vue";
import {beforeEach, vi} from "vitest";

export function criarPiniaDeTeste() {
    const pinia = createPinia();
    const app = createApp({});
    app.use(pinia);
    app.use(PiniaColada);
    setActivePinia(pinia);
    return pinia;
}

/**
 * Utilitário para configurar testes de Store com Pinia
 */
export function setupStoreTest<T>(useStore: () => T) {
    const context = {store: undefined as unknown as T};

    beforeEach(() => {
        criarPiniaDeTeste();
        context.store = useStore();
        vi.clearAllMocks();
    });

    return context;
}

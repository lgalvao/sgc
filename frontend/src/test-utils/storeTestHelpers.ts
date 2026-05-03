import {createPinia, setActivePinia} from "pinia";
import {beforeEach, vi} from "vitest";

/**
 * Utilitário para configurar testes de Store com Pinia
 */
export function setupStoreTest<T>(useStore: () => T) {
    const context = {store: undefined as unknown as T};

    beforeEach(() => {
        setActivePinia(createPinia());
        context.store = useStore();
        vi.clearAllMocks();
    });

    return context;
}


import {createPinia, setActivePinia} from "pinia";
import {beforeEach, expect, it, vi} from "vitest";

/**
 * Utilitário para configurar testes de Store com Pinia
 * @param useStore Função que retorna a store
 * @returns Objeto de contexto contendo a store (populada no beforeEach)
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

/**
 * Testa se uma action da store chama o método do serviço com os argumentos corretos
 */
export function testServiceCall<T>(
    action: () => Promise<T>,
    service: any,
    method: string,
    expectedArgs: any[]
) {
    it("deve chamar o service com os parâmetros corretos", async () => {
        await action();
        expect(service[method]).toHaveBeenCalledWith(...expectedArgs);
    });
}

/**
 * Testa se uma action da store lança erro quando o serviço falha
 */
export function testErrorHandling<T>(
    action: () => Promise<T>,
    errorType?: Error | any
) {
    it("deve lançar um erro em caso de falha", async () => {
        // Se errorType for passado, verifica o tipo, senão verifica apenas se lança
        if (errorType) {
            expect(action()).rejects.toThrow(errorType);
        } else {
            expect(action()).rejects.toThrow();
        }
    });
}

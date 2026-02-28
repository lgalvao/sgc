import {computed, ref, type Ref} from 'vue';
import {logger} from '@/utils';

/**
 * Gerenciador de múltiplos estados de loading
 */
interface LoadingManager {
    states: Record<string, Ref<boolean>>;
    start: (name: string) => void;
    stop: (name: string) => void;
    isLoading: (name: string) => boolean;
    anyLoading: Ref<boolean>;
    stopAll: () => void;
    withLoading: <T>(name: string, fn: () => Promise<T>) => Promise<T>;
}

/**
 * Composable para gerenciar múltiplos estados de loading
 *
 * Simplifica o gerenciamento de estados de carregamento em componentes
 * com múltiplas operações assíncronas.
 *
 * @param names - Lista de nomes de estados de loading
 * @returns Gerenciador de loading
 *
 * @example
 * ```ts
 * const loading = useLoadingManager(['fetch', 'save', 'delete']);
 *
 * // Iniciar loading
 * loading.start('fetch');
 *
 * // Verificar estado
 * if (loading.isLoading('fetch')) { ... }
 *
 * // Parar loading
 * loading.stop('fetch');
 *
 * // Usar wrapper para async
 * await loading.withLoading('save', async () => {
 *     await saveData();
 * });
 *
 * // Verificar se qualquer loading está ativo
 * if (loading.anyLoading.value) { ... }
 * ```
 */
export function useLoadingManager(names: string[]): LoadingManager {
    // Cria refs para cada estado de loading
    const states: Record<string, Ref<boolean>> = {};
    names.forEach(name => {
        states[name] = ref(false);
    });

    /**
     * Inicia um estado de loading
     */
    const start = (name: string) => {
        if (!states[name]) {
            logger.warn(`Estado de loading "${name}" não foi registrado`);
            return;
        }
        states[name].value = true;
    };

    /**
     * Para um estado de loading
     */
    const stop = (name: string) => {
        if (!states[name]) {
            logger.warn(`Estado de loading "${name}" não foi registrado`);
            return;
        }
        states[name].value = false;
    };

    /**
     * Verifica se um estado está carregando
     */
    const isLoading = (name: string): boolean => {
        return states[name]?.value ?? false;
    };

    /**
     * Computed que retorna true se qualquer estado estiver carregando
     */
    const anyLoading = computed(() => {
        return Object.values(states).some(state => state.value);
    });

    /**
     * Para todos os estados de loading
     */
    const stopAll = () => {
        Object.keys(states).forEach(name => stop(name));
    };

    /**
     * Wrapper para executar função assíncrona com loading automático
     */
    const withLoading = async <T>(name: string, fn: () => Promise<T>): Promise<T> => {
        try {
            start(name);
            return await fn();
        } finally {
            stop(name);
        }
    };

    return {
        states,
        start,
        stop,
        isLoading,
        anyLoading,
        stopAll,
        withLoading
    };
}

/**
 * Versão simplificada para um único estado de loading
 *
 * @param initialValue - Valor inicial do loading (padrão: false)
 *
 * @example
 * ```ts
 * const loading = useSingleLoading();
 *
 * loading.start();
 * if (loading.isLoading.value) { ... }
 * loading.stop();
 *
 * // Ou usar wrapper
 * await loading.withLoading(async () => {
 *     await fetchData();
 * });
 * ```
 */
export function useSingleLoading(initialValue = false) {
    const isLoading = ref(initialValue);

    const start = () => {
        isLoading.value = true;
    };

    const stop = () => {
        isLoading.value = false;
    };

    const toggle = () => {
        isLoading.value = !isLoading.value;
    };

    const withLoading = async <T>(fn: () => Promise<T>): Promise<T> => {
        try {
            start();
            return await fn();
        } finally {
            stop();
        }
    };

    return {
        isLoading,
        start,
        stop,
        toggle,
        withLoading
    };
}

import {ref, type Ref, watch} from 'vue';

/**
 * Composable para sincronizar ref com localStorage
 */
export function useLocalStorage<T>(key: string, defaultValue: T): Ref<T> {
    const readValue = (): T => {
        const item = localStorage.getItem(key);
        if (item === null) {
            return defaultValue;
        }

        try {
            return JSON.parse(item) as T;
        } catch {
            return item as unknown as T;
        }
    };

    // Cria ref com valor inicial do localStorage
    const storedValue = ref(readValue()) as Ref<T>;

    // Observa mudanças e sincroniza com localStorage
    watch(storedValue, (newValue) => {
        if (newValue === null || newValue === undefined) {
            localStorage.removeItem(key);
        } else {
            localStorage.setItem(key, JSON.stringify(newValue));
        }
    }, {deep: true});

    return storedValue;
}

/**
 * Composable para sincronizar múltiplas refs com localStorage
 */
export function useLocalStorageMultiple<T extends Record<string, any>>(
    items: T
): { [K in keyof T]: Ref<T[K]> } {
    const result = {} as { [K in keyof T]: Ref<T[K]> };

    for (const [key, defaultValue] of Object.entries(items)) {
        result[key as keyof T] = useLocalStorage(key, defaultValue);
    }

    return result;
}

/**
 * Remove item do localStorage
 */
export function removeFromLocalStorage(key: string): void {
    localStorage.removeItem(key);
}

/**
 * Remove múltiplos itens do localStorage
 */
export function removeMultipleFromLocalStorage(keys: string[]): void {
    keys.forEach(key => localStorage.removeItem(key));
}

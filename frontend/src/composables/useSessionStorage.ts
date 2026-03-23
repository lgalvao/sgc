import {ref, type Ref, watch} from 'vue';

/**
 * Composable para sincronizar ref com sessionStorage
 */
export function useSessionStorage<T>(key: string, defaultValue: T): Ref<T> {
    const readValue = (): T => {
        const item = sessionStorage.getItem(key);
        if (item === null) {
            return defaultValue;
        }

        try {
            return JSON.parse(item) as T;
        } catch {
            return item as unknown as T;
        }
    };

    // Cria ref com valor inicial do sessionStorage
    const storedValue = ref(readValue()) as Ref<T>;

    // Observa mudanças e sincroniza com sessionStorage
    watch(storedValue, (newValue) => {
        if (newValue === null || newValue === undefined) {
            sessionStorage.removeItem(key);
        } else {
            sessionStorage.setItem(key, JSON.stringify(newValue));
        }
    }, {deep: true});

    return storedValue;
}

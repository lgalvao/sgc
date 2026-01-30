import { ref, watch, type Ref } from 'vue';

/**
 * Composable para sincronizar ref com localStorage
 * 
 * @param key - Chave do localStorage
 * @param defaultValue - Valor padrão se não houver no localStorage
 * @returns Ref sincronizado com localStorage
 */
export function useLocalStorage<T>(key: string, defaultValue: T): Ref<T> {
    // Função para ler do localStorage
    const readValue = (): T => {
        const item = localStorage.getItem(key);
        if (item === null) {
            return defaultValue;
        }
        
        try {
            // Se for string simples, retorna direto
            if (typeof defaultValue === 'string') {
                return item as T;
            }
            // Se for número, faz parse
            if (typeof defaultValue === 'number') {
                return Number(item) as T;
            }
            // Para objetos e arrays, faz JSON.parse
            return JSON.parse(item) as T;
        } catch {
            return defaultValue;
        }
    };

    // Cria ref com valor inicial do localStorage
    const storedValue = ref(readValue()) as Ref<T>;

    // Observa mudanças e sincroniza com localStorage
    watch(storedValue, (newValue) => {
        if (newValue === null || newValue === undefined) {
            localStorage.removeItem(key);
        } else if (typeof newValue === 'string') {
            localStorage.setItem(key, newValue);
        } else if (typeof newValue === 'number') {
            localStorage.setItem(key, newValue.toString());
        } else {
            localStorage.setItem(key, JSON.stringify(newValue));
        }
    }, { deep: true });

    return storedValue;
}

/**
 * Composable para sincronizar múltiplas refs com localStorage
 * 
 * @param items - Mapa de chave -> valor padrão
 * @returns Mapa de chave -> Ref sincronizado
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

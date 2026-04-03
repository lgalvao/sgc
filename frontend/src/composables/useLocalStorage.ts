import type {Ref} from 'vue';
import {
    removerDoArmazenamento,
    removerMultiplosDoArmazenamento,
    useWebStorage
} from '@/composables/useWebStorage';

/**
 * Composable para sincronizar ref com localStorage
 */
export function useLocalStorage<T>(chave: string, valorPadrao: T): Ref<T> {
    return useWebStorage(localStorage, chave, valorPadrao);
}

/**
 * Composable para sincronizar múltiplas refs com localStorage
 */
export function useLocalStorageMultiple<T extends Record<string, any>>(
    itens: T
): { [K in keyof T]: Ref<T[K]> } {
    const resultado = {} as { [K in keyof T]: Ref<T[K]> };

    for (const [chave, valorPadrao] of Object.entries(itens)) {
        resultado[chave as keyof T] = useLocalStorage(chave, valorPadrao);
    }

    return resultado;
}

/**
 * Remove item do localStorage
 */
export function removeFromLocalStorage(chave: string): void {
    removerDoArmazenamento(localStorage, chave);
}

/**
 * Remove múltiplos itens do localStorage
 */
export function removeMultipleFromLocalStorage(chaves: string[]): void {
    removerMultiplosDoArmazenamento(localStorage, chaves);
}

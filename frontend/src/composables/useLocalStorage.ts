import type {Ref} from 'vue';
import {useWebStorage} from '@/composables/useWebStorage';

/**
 * Composable para sincronizar ref com localStorage
 */
export function useLocalStorage<T>(chave: string, valorPadrao: T): Ref<T> {
    return useWebStorage(localStorage, chave, valorPadrao);
}

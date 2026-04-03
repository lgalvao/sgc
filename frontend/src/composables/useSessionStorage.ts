import type {Ref} from 'vue';
import {useWebStorage} from '@/composables/useWebStorage';

/**
 * Composable para sincronizar ref com sessionStorage
 */
export function useSessionStorage<T>(chave: string, valorPadrao: T): Ref<T> {
    return useWebStorage(sessionStorage, chave, valorPadrao);
}

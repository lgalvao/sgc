import {ref} from 'vue';
import {type ErroNormalizado, normalizarErro} from '@/utils/apiError';

/**
 * Composable para tratamento centralizado de erros em stores.
 */
export function useErrorHandler() {
    const lastError = ref<ErroNormalizado | null>(null);

    /**
     * Limpa o último erro armazenado.
     */
    function clearError() {
        lastError.value = null;
    }

    /**
     * Executa uma função assíncrona com tratamento automático de erros.
     *
     * @throws Re-lança o erro após tratamento
     */
    async function withErrorHandling<T>(
        fn: () => Promise<T>,
        onError?: (error: ErroNormalizado) => void
    ): Promise<T> {
        lastError.value = null;
        try {
            return await fn();
        } catch (error) {
            const normalized = normalizarErro(error);
            lastError.value = normalized;

            if (onError) {
                onError(normalized);
            }

            throw error;
        }
    }

    return {
        lastError,
        clearError,
        withErrorHandling
    };
}

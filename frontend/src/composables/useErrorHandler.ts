import {ref} from 'vue';
import {type NormalizedError, normalizeError} from '@/utils/apiError';

/**
 * Composable para tratamento centralizado de erros em stores.
 */
export function useErrorHandler() {
  const lastError = ref<NormalizedError | null>(null);

  /**
   * Limpa o último erro armazenado.
   */
  function clearError() {
    lastError.value = null;
  }

  /**
   * Executa uma função assíncrona com tratamento automático de erros.
   * 
   * @param fn - Função assíncrona a ser executada
   * @param onError - Callback opcional executado quando ocorre erro
   * @returns Promise com resultado da função
   * @throws Re-lança o erro após tratamento
   */
  async function withErrorHandling<T>(
    fn: () => Promise<T>,
    onError?: (error: NormalizedError) => void
  ): Promise<T> {
    lastError.value = null;
    try {
      return await fn();
    } catch (error) {
      const normalized = normalizeError(error);
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

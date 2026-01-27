import { ref } from 'vue';
import { normalizeError, type NormalizedError } from '@/utils/apiError';

/**
 * Composable para tratamento centralizado de erros em stores.
 * 
 * Elimina duplicação de código de error handling em todos os stores,
 * fornecendo uma API consistente para gerenciar erros.
 * 
 * @example
 * ```typescript
 * export const useMyStore = defineStore('myStore', () => {
 *   const { lastError, clearError, withErrorHandling } = useErrorHandler();
 *   
 *   async function buscarDados() {
 *     return withErrorHandling(async () => {
 *       const dados = await apiService.buscar();
 *       // ... processar dados
 *       return dados;
 *     });
 *   }
 *   
 *   return { lastError, clearError, buscarDados };
 * });
 * ```
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

import {ref} from 'vue';
import {type ErroNormalizado, normalizarErro} from '@/utils/apiError';

/**
 * Composable para tratamento centralizado de erros em stores.
 */
export function useErrorHandler() {
    const ultimoErro = ref<ErroNormalizado | null>(null);

    /**
     * Limpa o último erro armazenado.
     */
    function limparErro() {
        ultimoErro.value = null;
    }

    /**
     * Executa uma função assíncrona com tratamento automático de erros.
     *
     * @throws Re-lança o erro após tratamento
     */
    async function executarComTratamentoDeErros<T>(
        fn: () => Promise<T>,
        aoOcorrerErro?: (error: ErroNormalizado) => void
    ): Promise<T> {
        ultimoErro.value = null;
        try {
            return await fn();
        } catch (error) {
            const normalizado = normalizarErro(error);
            ultimoErro.value = normalizado;

            if (aoOcorrerErro) {
                aoOcorrerErro(normalizado);
            }

            throw error;
        }
    }

    return {
        ultimoErro,
        limparErro,
        executarComTratamentoDeErros
    };
}

import {type ErroNormalizado} from '@/utils/apiError';
import {useAsyncAction} from '@/composables/useAsyncAction';

/**
 * Composable para tratamento centralizado de erros em stores.
 */
export function useErrorHandler() {
    const {erro: ultimoErro, limparErro, executar} = useAsyncAction();
    type OpcoesExecucao<T> = {
        relancarErro?: boolean;
        aoOcorrerErro?: (error: ErroNormalizado, causa: unknown) => void | Promise<void>;
        aoSucesso?: (resultado: T) => void | Promise<void>;
    };

    /**
     * Executa uma função assíncrona com tratamento automático de erros.
     *
     * @throws Re-lança o erro após tratamento
     */
    async function executarComTratamentoDeErros<T>(
        fn: () => Promise<T>,
        opcoes?: OpcoesExecucao<T> | ((error: ErroNormalizado) => void | Promise<void>)
    ): Promise<T>;
    async function executarComTratamentoDeErros<T>(
        fn: () => Promise<T>,
        opcoes: (OpcoesExecucao<T> & { relancarErro: false }) | ((error: ErroNormalizado) => void | Promise<void>)
    ): Promise<T | undefined>;
    async function executarComTratamentoDeErros<T>(
        fn: () => Promise<T>,
        opcoes: OpcoesExecucao<T> | ((error: ErroNormalizado) => void | Promise<void>) = {}
    ): Promise<T | undefined> {
        const opcoesNormalizadas = typeof opcoes === "function"
            ? {
                aoOcorrerErro: async (erro: ErroNormalizado) => {
                    await opcoes(erro);
                },
            }
            : opcoes;
        if (opcoesNormalizadas.relancarErro === false) {
            return executar(fn, undefined, {
                ...opcoesNormalizadas,
                relancarErro: false,
            });
        }
        return executar(fn, undefined, opcoesNormalizadas);
    }

    return {
        ultimoErro,
        limparErro,
        executarComTratamentoDeErros
    };
}

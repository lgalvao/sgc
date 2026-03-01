import {ref} from "vue";

/**
 * Composable que encapsula o padrão try/catch/loading/erro
 * usado repetidamente nas stores Pinia.
 *
 * @example
 * ```ts
 * const {carregando, erro, executar} = useAsyncAction();
 *
 * async function salvar(dados: any) {
 *     await executar(
 *         () => salvarService(dados),
 *         "Erro ao salvar"
 *     );
 * }
 * ```
 */
export function useAsyncAction() {
    const carregando = ref(false);
    const erro = ref<string | null>(null);

    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação"
    ): Promise<T | undefined> {
        carregando.value = true;
        erro.value = null;
        try {
            const resultado = await acao();
            return resultado;
        } catch (e: any) {
            erro.value = e.message || mensagemErro;
            throw e;
        } finally {
            carregando.value = false;
        }
    }

    /**
     * Versão que captura erros silenciosamente (não relança).
     * Útil para operações onde falha não deve interromper o fluxo.
     */
    async function executarSilencioso<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação"
    ): Promise<T | undefined> {
        carregando.value = true;
        erro.value = null;
        try {
            return await acao();
        } catch (e: any) {
            erro.value = e.message || mensagemErro;
            return undefined;
        } finally {
            carregando.value = false;
        }
    }

    return {carregando, erro, executar, executarSilencioso};
}

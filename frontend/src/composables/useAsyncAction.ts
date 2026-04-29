import {ref} from "vue";

/**
 * Composable que encapsula o padrão try/catch/loading/erro
 * usado repetidamente nas stores Pinia.
 *
 * @example
 * ```ts
 * const {carregando, erro, executar} = useAsyncAction();
 *
 * async function salvar(dados: {descricao: string}) {
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

    function obterMensagemErro(error: unknown, mensagemPadrao: string): string {
        if (error instanceof Error && error.message) {
            return error.message;
        }

        return mensagemPadrao;
    }

    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação"
    ): Promise<T | undefined> {
        carregando.value = true;
        erro.value = null;
        try {
            return await acao();
        } catch (error: unknown) {
            erro.value = obterMensagemErro(error, mensagemErro);
            throw error;
        } finally {
            carregando.value = false;
        }
    }

    /**
     * Versão que captura erros silenciosamente (não relança).
     * Útil para operações onde uma falha não deve interromper o fluxo.
     */
    async function executarSilencioso<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação"
    ): Promise<T | undefined> {
        carregando.value = true;
        erro.value = null;
        try {
            return await acao();
        } catch (error: unknown) {
            erro.value = obterMensagemErro(error, mensagemErro);
            return undefined;
        } finally {
            carregando.value = false;
        }
    }

    return {carregando, erro, executar, executarSilencioso};
}

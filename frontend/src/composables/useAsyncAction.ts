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

    type OpcoesExecucao = {
        relancarErro?: boolean;
    };

    function obterMensagemErro(error: unknown, mensagemPadrao: string): string {
        if (error instanceof Error && error.message) {
            return error.message;
        }

        return mensagemPadrao;
    }

    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro?: string
    ): Promise<T>;
    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro: string | undefined,
        opcoes: {relancarErro: false}
    ): Promise<T | undefined>;
    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação",
        opcoes: OpcoesExecucao = {}
    ): Promise<T | undefined> {
        carregando.value = true;
        erro.value = null;
        try {
            return await acao();
        } catch (error: unknown) {
            erro.value = obterMensagemErro(error, mensagemErro);
            if (opcoes.relancarErro === false) {
                return undefined;
            }
            throw error;
        } finally {
            carregando.value = false;
        }
    }

    return {carregando, erro, executar};
}

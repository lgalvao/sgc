import {ref} from "vue";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";

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
    const erro = ref<ErroNormalizado | null>(null);

    type OpcoesExecucao<T> = {
        relancarErro?: boolean;
        aoOcorrerErro?: (erro: ErroNormalizado, causa: unknown) => void | Promise<void>;
        aoSucesso?: (resultado: T) => void | Promise<void>;
    };

    function normalizarErroComFallback(error: unknown, mensagemPadrao: string): ErroNormalizado {
        const normalizado = normalizarErro(error);
        const manterMensagemPadrao = !(error instanceof Error)
            && normalizado.mensagem === 'Erro desconhecido ou não mapeado pela aplicação.';

        if (normalizado.mensagem?.trim() && !manterMensagemPadrao) {
            return normalizado;
        }
        return {
            ...normalizado,
            mensagem: mensagemPadrao,
        };
    }

    function limparErro() {
        erro.value = null;
    }

    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro?: string
    ): Promise<T>;
    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro: string | undefined,
        opcoes: OpcoesExecucao<T>
    ): Promise<T>;
    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro: string | undefined,
        opcoes: OpcoesExecucao<T> & { relancarErro: false }
    ): Promise<T | undefined>;
    async function executar<T>(
        acao: () => Promise<T>,
        mensagemErro = "Erro na operação",
        opcoes: OpcoesExecucao<T> = {}
    ): Promise<T | undefined> {
        carregando.value = true;
        limparErro();
        try {
            const resultado = await acao();
            await opcoes.aoSucesso?.(resultado);
            return resultado;
        } catch (error: unknown) {
            erro.value = normalizarErroComFallback(error, mensagemErro);
            await opcoes.aoOcorrerErro?.(erro.value, error);
            if (opcoes.relancarErro === false) {
                return undefined;
            }
            throw error;
        } finally {
            carregando.value = false;
        }
    }

    return {carregando, erro, limparErro, executar};
}

import {ref, type Ref} from "vue";
import type {Atividade, AtividadeOperacaoResponse} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import * as atividadeService from "@/services/atividadeService";
import type {VarianteAlerta} from "@/composables/useNotification";
import {useConhecimentoMutacoes} from "./useConhecimentoMutacoes";

export type DadosRemocaoCadastro = {
    tipo: "atividade" | "conhecimento";
    atividadeCodigo: number;
    conhecimentoCodigo?: number;
} | null;

interface UseCadastroAtividadesMutacoesParams {
    atividades: Ref<Atividade[]>;
    codigoSubprocesso: Ref<number | null>;
    codMapa: Ref<number | null>;
    executarComTratamentoDeErros: <T>(operacao: () => Promise<T>) => Promise<T>;
    ultimoErro: Ref<{ mensagem?: string } | null>;
    notify: (mensagem: string, variante: VarianteAlerta) => void;
    processarRespostaLocal: (response: AtividadeOperacaoResponse) => void;
    adicionarAtividadeAction: (codigoSubprocesso: number, codMapa: number) => Promise<AtividadeOperacaoResponse | null>;
}

export function useCadastroAtividadesMutacoes({
                                                  atividades,
                                                  codigoSubprocesso,
                                                  codMapa,
                                                  executarComTratamentoDeErros,
                                                  ultimoErro,
                                                  notify,
                                                  processarRespostaLocal,
                                                  adicionarAtividadeAction,
                                               }: UseCadastroAtividadesMutacoesParams) {
    const erroNovaAtividade = ref<string | null>(null);
    const dadosRemocao = ref<DadosRemocaoCadastro>(null);
    const loadingRemocao = ref(false);
    const mostrarModalConfirmacaoRemocao = ref(false);

    type ResultadoExecucao<T> =
        | { sucesso: true; resultado: T }
        | { sucesso: false };

    async function executarOperacaoAtividade<T>(
        operacao: () => Promise<T>,
        aoFalhar: (erro: unknown) => void
    ): Promise<ResultadoExecucao<T>> {
        try {
            return {
                sucesso: true,
                resultado: await executarComTratamentoDeErros(operacao),
            };
        } catch (erro) {
            aoFalhar(erro);
            return {sucesso: false};
        }
    }

    const executarAtualizacao = async (acao: () => Promise<AtividadeOperacaoResponse>, msg: string) => {
        const resultado = await executarOperacaoAtividade(async () => {
            processarRespostaLocal(await acao());
        },
            () => notify(msg, "danger"),
        );
        return resultado.sucesso;
    };

    const prepararRemocao = (tipo: "atividade" | "conhecimento", atividadeCodigo: number, conhecimentoCodigo?: number) => {
        if (!codigoSubprocesso.value) return;
        dadosRemocao.value = {tipo, atividadeCodigo, conhecimentoCodigo};
        mostrarModalConfirmacaoRemocao.value = true;
    };

    const {
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento
    } = useConhecimentoMutacoes(codigoSubprocesso, executarAtualizacao, prepararRemocao);

    async function adicionarAtividade(): Promise<boolean> {
        const codMapaVal = codMapa.value;
        const codSubprocessoVal = codigoSubprocesso.value;
        if (!codMapaVal || !codSubprocessoVal) return false;
        const resultado = await executarOperacaoAtividade(
            () => adicionarAtividadeAction(codSubprocessoVal, codMapaVal),
            () => {
                erroNovaAtividade.value = ultimoErro.value?.mensagem || TEXTOS.atividades.ERRO_ADICIONAR;
            },
        );
        if (!resultado.sucesso || !resultado.resultado) return false;
        processarRespostaLocal(resultado.resultado);
        erroNovaAtividade.value = null;
        return true;
    }

    async function confirmarRemocao() {
        if (!dadosRemocao.value || !codigoSubprocesso.value || loadingRemocao.value) return;
        const {tipo, atividadeCodigo, conhecimentoCodigo} = dadosRemocao.value;
        loadingRemocao.value = true;
        try {
            const resultado = await executarOperacaoAtividade(async () => {
                if (tipo === "atividade") {
                    return await atividadeService.excluirAtividade(atividadeCodigo);
                }
                return await atividadeService.excluirConhecimento(atividadeCodigo, conhecimentoCodigo ?? 0);
            }, (erro) => notify(ultimoErro.value?.mensagem || (erro instanceof Error ? erro.message : String(erro)) || TEXTOS.atividades.ERRO_REMOVER, "danger"));
            if (resultado.sucesso && resultado.resultado) {
                processarRespostaLocal(resultado.resultado);
                dadosRemocao.value = null;
            }
            mostrarModalConfirmacaoRemocao.value = false;
        } finally {
            loadingRemocao.value = false;
        }
    }

    async function salvarEdicaoAtividade(codigo: number, descricao: string) {
        if (!descricao.trim() || !codigoSubprocesso.value) return;
        const atividade = atividades.value.find((a) => a.codigo === codigo);
        if (atividade) await executarAtualizacao(() => atividadeService.atualizarAtividade(codigo, {
            ...atividade,
            descricao: descricao.trim()
        }), TEXTOS.atividades.ERRO_SALVAR_ATIVIDADE);
    }

    return {
        erroNovaAtividade,
        dadosRemocao,
        loadingRemocao,
        mostrarModalConfirmacaoRemocao,
        adicionarAtividade,
        removerAtividade: (c: number) => prepararRemocao("atividade", c),
        confirmarRemocao,
        salvarEdicaoAtividade,
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento,
    };
}

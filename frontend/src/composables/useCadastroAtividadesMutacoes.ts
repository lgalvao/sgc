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
    withErrorHandling: <T>(operacao: () => Promise<T>) => Promise<T>;
    lastError: Ref<{ mensagem?: string } | null>;
    notify: (mensagem: string, variante: VarianteAlerta) => void;
    processarRespostaLocal: (response: AtividadeOperacaoResponse) => void;
    adicionarAtividadeAction: (codigoSubprocesso: number, codMapa: number) => Promise<AtividadeOperacaoResponse | null>;
}

export function useCadastroAtividadesMutacoes({
    atividades,
    codigoSubprocesso,
    codMapa,
    withErrorHandling,
    lastError,
    notify,
    processarRespostaLocal,
    adicionarAtividadeAction,
}: UseCadastroAtividadesMutacoesParams) {
    const erroNovaAtividade = ref<string | null>(null);
    const dadosRemocao = ref<DadosRemocaoCadastro>(null);
    const loadingRemocao = ref(false);
    const mostrarModalConfirmacaoRemocao = ref(false);

    const executarAtualizacao = async (acao: () => Promise<AtividadeOperacaoResponse>, msg: string) => {
        try {
            await withErrorHandling(async () => processarRespostaLocal(await acao()));
            return true;
        } catch {
            notify(msg, "danger");
            return false;
        }
    };

    const prepararRemocao = (tipo: "atividade" | "conhecimento", atividadeCodigo: number, conhecimentoCodigo?: number) => {
        dadosRemocao.value = {tipo, atividadeCodigo, conhecimentoCodigo};
        mostrarModalConfirmacaoRemocao.value = true;
    };

    const {adicionarConhecimento, removerConhecimento, salvarEdicaoConhecimento} = useConhecimentoMutacoes(codigoSubprocesso, executarAtualizacao, prepararRemocao);

    async function adicionarAtividade(): Promise<boolean> {
        if (!codMapa.value || !codigoSubprocesso.value) return false;
        try {
            const resp = await withErrorHandling(() => adicionarAtividadeAction(codigoSubprocesso.value!, codMapa.value!));
            if (!resp) return false;
            processarRespostaLocal(resp);
            erroNovaAtividade.value = null;
            return true;
        } catch {
            erroNovaAtividade.value = lastError.value?.mensagem || TEXTOS.atividades.ERRO_ADICIONAR;
            return false;
        }
    }

    async function confirmarRemocao() {
        if (!dadosRemocao.value || !codigoSubprocesso.value || loadingRemocao.value) return;
        const {tipo, atividadeCodigo, conhecimentoCodigo} = dadosRemocao.value;
        loadingRemocao.value = true;
        try {
            await withErrorHandling(async () => {
                const resp = tipo === "atividade" ? await atividadeService.excluirAtividade(atividadeCodigo) : await atividadeService.excluirConhecimento(atividadeCodigo, conhecimentoCodigo!);
                processarRespostaLocal(resp);
            });
            mostrarModalConfirmacaoRemocao.value = false;
            dadosRemocao.value = null;
        } catch (e: any) {
            notify(lastError.value?.mensagem || (e as Error).message || TEXTOS.atividades.ERRO_REMOVER, "danger");
            mostrarModalConfirmacaoRemocao.value = false;
        } finally {
            loadingRemocao.value = false;
        }
    }

    async function salvarEdicaoAtividade(codigo: number, descricao: string) {
        if (!descricao.trim() || !codigoSubprocesso.value) return;
        const atividade = atividades.value.find((a) => a.codigo === codigo);
        if (atividade) await executarAtualizacao(() => atividadeService.atualizarAtividade(codigo, {...atividade, descricao: descricao.trim()}), TEXTOS.atividades.ERRO_SALVAR_ATIVIDADE);
    }

    return {
        erroNovaAtividade, dadosRemocao, loadingRemocao, mostrarModalConfirmacaoRemocao,
        adicionarAtividade, removerAtividade: (c: number) => prepararRemocao("atividade", c), confirmarRemocao, salvarEdicaoAtividade,
        adicionarConhecimento, removerConhecimento, salvarEdicaoConhecimento,
    };
}

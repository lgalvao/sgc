import {ref, type Ref} from "vue";
import {TEXTOS} from "@/constants/textos";
import * as atividadeService from "@/services/atividadeService";
import type {VarianteAlerta} from "@/composables/useNotification";
import type {AtividadeOperacaoResponse} from "@/types/tipos";

export function useConhecimentoMutacoes(
    codigoSubprocesso: Ref<number | null>,
    executarAtualizacao: (acao: () => Promise<AtividadeOperacaoResponse>, msg: string) => Promise<boolean>,
    prepararRemocao: (tipo: "conhecimento", atividadeCodigo: number, conhecimentoCodigo?: number) => void
) {
    async function adicionarConhecimento(atividadeCodigo: number, descricao: string) {
        if (!codigoSubprocesso.value || !descricao.trim()) return;
        await executarAtualizacao(
            () => atividadeService.criarConhecimento(atividadeCodigo, {descricao: descricao.trim()}),
            TEXTOS.atividades.ERRO_ADICIONAR_CONHECIMENTO,
        );
    }

    function removerConhecimento(atividadeCodigo: number, conhecimentoCodigo?: number) {
        if (!codigoSubprocesso.value) return;
        prepararRemocao("conhecimento", atividadeCodigo, conhecimentoCodigo);
    }

    async function salvarEdicaoConhecimento(atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) {
        if (!codigoSubprocesso.value || !descricao.trim()) return;
        await executarAtualizacao(
            () => atividadeService.atualizarConhecimento(atividadeCodigo, conhecimentoCodigo, {
                codigo: conhecimentoCodigo,
                descricao: descricao.trim(),
            }),
            TEXTOS.atividades.ERRO_ATUALIZAR_CONHECIMENTO,
        );
    }

    return {adicionarConhecimento, removerConhecimento, salvarEdicaoConhecimento};
}

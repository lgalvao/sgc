import {ref, type Ref} from "vue";
import type {Atividade, AtividadeOperacaoResponse} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import * as atividadeService from "@/services/atividadeService";
import type {VarianteAlerta} from "@/composables/useNotification";

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

type AcaoAtualizacaoCadastro = () => Promise<AtividadeOperacaoResponse>;

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

    async function executarAtualizacaoCadastro(
        acao: AcaoAtualizacaoCadastro,
        mensagemErro: string,
    ) {
        try {
            await withErrorHandling(async () => {
                const response = await acao();
                processarRespostaLocal(response);
            });
            return true;
        } catch {
            notify(mensagemErro, "danger");
            return false;
        }
    }

    async function adicionarAtividade(): Promise<boolean> {
        if (!codMapa.value || !codigoSubprocesso.value) {
            return false;
        }

        try {
            const response = await withErrorHandling(() =>
                adicionarAtividadeAction(codigoSubprocesso.value!, codMapa.value!)
            );

            if (!response) {
                return false;
            }

            processarRespostaLocal(response);
            erroNovaAtividade.value = null;
            return true;
        } catch {
            erroNovaAtividade.value = lastError.value?.mensagem || TEXTOS.atividades.ERRO_ADICIONAR;
            return false;
        }
    }

    function removerAtividade(codigo: number) {
        if (!codigoSubprocesso.value) return;
        dadosRemocao.value = {tipo: "atividade", atividadeCodigo: codigo};
        mostrarModalConfirmacaoRemocao.value = true;
    }

    async function confirmarRemocao() {
        if (!dadosRemocao.value || !codigoSubprocesso.value || loadingRemocao.value) return;

        const {tipo, atividadeCodigo, conhecimentoCodigo} = dadosRemocao.value;

        loadingRemocao.value = true;
        try {
            if (tipo === "atividade") {
                await withErrorHandling(async () => {
                    const response = await atividadeService.excluirAtividade(atividadeCodigo);
                    processarRespostaLocal(response);
                });
            } else if (conhecimentoCodigo !== undefined) {
                await withErrorHandling(async () => {
                    const response = await atividadeService.excluirConhecimento(atividadeCodigo, conhecimentoCodigo);
                    processarRespostaLocal(response);
                });
            }

            mostrarModalConfirmacaoRemocao.value = false;
            dadosRemocao.value = null;
        } catch (e: unknown) {
            const err = lastError.value?.mensagem || (e as Error).message;
            notify(err || TEXTOS.atividades.ERRO_REMOVER, "danger");
            mostrarModalConfirmacaoRemocao.value = false;
        } finally {
            loadingRemocao.value = false;
        }
    }

    async function salvarEdicaoAtividade(codigo: number, descricao: string) {
        if (!descricao.trim() || !codigoSubprocesso.value) return;

        const atividadeOriginal = atividades.value.find((atividade) => atividade.codigo === codigo);
        if (!atividadeOriginal) return;

        const descricaoAtualizada = descricao.trim();
        await executarAtualizacaoCadastro(
            () => atividadeService.atualizarAtividade(codigo, {
                ...atividadeOriginal,
                descricao: descricaoAtualizada,
            }),
            TEXTOS.atividades.ERRO_SALVAR_ATIVIDADE,
        );
    }

    async function adicionarConhecimento(atividadeCodigo: number, descricao: string) {
        if (!codigoSubprocesso.value || !descricao.trim()) return;

        await executarAtualizacaoCadastro(
            () => atividadeService.criarConhecimento(atividadeCodigo, {
                descricao: descricao.trim(),
            }),
            TEXTOS.atividades.ERRO_ADICIONAR_CONHECIMENTO,
        );
    }

    function removerConhecimento(atividadeCodigo: number, conhecimentoCodigo?: number) {
        if (!codigoSubprocesso.value) return;
        dadosRemocao.value = {tipo: "conhecimento", atividadeCodigo, conhecimentoCodigo};
        mostrarModalConfirmacaoRemocao.value = true;
    }

    async function salvarEdicaoConhecimento(atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) {
        if (!codigoSubprocesso.value || !descricao.trim()) return;

        const descricaoAtualizada = descricao.trim();
        await executarAtualizacaoCadastro(
            () => atividadeService.atualizarConhecimento(atividadeCodigo, conhecimentoCodigo, {
                codigo: conhecimentoCodigo,
                descricao: descricaoAtualizada,
            }),
            TEXTOS.atividades.ERRO_ATUALIZAR_CONHECIMENTO,
        );
    }

    return {
        erroNovaAtividade,
        dadosRemocao,
        loadingRemocao,
        mostrarModalConfirmacaoRemocao,
        executarAtualizacaoCadastro,
        adicionarAtividade,
        removerAtividade,
        confirmarRemocao,
        salvarEdicaoAtividade,
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento,
    };
}

import {defineStore} from "pinia";
import {ref} from "vue";
import type {Processo} from "@/types/tipos";
import {buscarContextoCompleto as serviceBuscarContextoCompleto} from "@/services/processoService";
import {logger} from "@/utils";
import {normalizeError} from "@/utils/apiError";
import {isErroCanceladoHttp} from "@/axios-setup";

/**
 * Dedupe de sessão para contexto completo de processo.
 *
 * O contexto completo carrega situação, permissões e ações disponíveis no momento.
 * Como esses dados mudam ao longo do workflow, não é seguro reutilizar snapshots
 * antigos entre navegações. Mantemos apenas deduplicação de requisições concorrentes
 * e o último payload recebido para consumo imediato da própria view.
 *
 * Estratégia: reaproveitar o último snapshot enquanto ele não foi invalidado.
 * A invalidação é explícita após ações de workflow ou eventos SSE. Isso evita
 * recargas repetidas ao reativar a view sem perder previsibilidade.
 */
export const useProcessoStore = defineStore("processo", () => {
    const contextoCompleto = ref<Processo | null>(null);
    const codProcessoCarregado = ref<number | null>(null);
    const contextoInvalido = ref(false);
    const carregamentosEmAndamento = new Map<number, Promise<Processo>>();

    function limparContextoAtual(): void {
        contextoCompleto.value = null;
        codProcessoCarregado.value = null;
        contextoInvalido.value = false;
    }

    /** Invalida o cache — deve ser chamado após qualquer ação de workflow que altera o processo. */
    function invalidar(): void {
        contextoInvalido.value = true;
        carregamentosEmAndamento.clear();
    }

    function resetar(): void {
        carregamentosEmAndamento.clear();
        limparContextoAtual();
    }

    function dadosValidos(codProcesso: number): boolean {
        return contextoCompleto.value?.codigo === codProcesso
            && codProcessoCarregado.value === codProcesso
            && !contextoInvalido.value;
    }

    /**
     * Retorna o contexto completo do processo, usando deduplicação de requisições concorrentes.
     * @returns o contexto ou null em caso de erro
     */
    async function garantirContextoCompleto(codProcesso: number): Promise<Processo | null> {
        if (dadosValidos(codProcesso)) {
            return contextoCompleto.value;
        }

        const carregamentoExistente = carregamentosEmAndamento.get(codProcesso);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        try {
            const promessaCarregamento = serviceBuscarContextoCompleto(codProcesso);
            carregamentosEmAndamento.set(codProcesso, promessaCarregamento);
            const data = await promessaCarregamento;
            contextoCompleto.value = data;
            codProcessoCarregado.value = codProcesso;
            contextoInvalido.value = false;
            return data;
        } catch (err) {
            const erroNormalizado = normalizeError(err);
            if (isErroCanceladoHttp(err) || erroNormalizado.code === "REQUEST_CANCELADA") {
                return null;
            }
            logger.error(`Erro ao buscar contexto completo do processo ${codProcesso}:`, err);
            throw err; // relança para que a view possa exibir erro inline
        } finally {
            carregamentosEmAndamento.delete(codProcesso);
        }
    }

    return {
        contextoCompleto,
        codProcessoCarregado,
        limparContextoAtual,
        invalidar,
        resetar,
        dadosValidos,
        garantirContextoCompleto,
    };
});

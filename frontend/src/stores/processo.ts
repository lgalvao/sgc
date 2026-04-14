import {defineStore} from "pinia";
import {ref} from "vue";
import type {Processo} from "@/types/tipos";
import {buscarContextoCompleto as serviceBuscarContextoCompleto} from "@/services/processoService";
import {logger} from "@/utils";

/**
 * Dedupe de sessão para contexto completo de processo.
 *
 * O contexto completo carrega situação, permissões e ações disponíveis no momento.
 * Como esses dados mudam ao longo do workflow, não é seguro reutilizar snapshots
 * antigos entre navegações. Mantemos apenas deduplicação de requisições concorrentes
 * e o último payload recebido para consumo imediato da própria view.
 *
 * Estratégia: nunca considerar o contexto "válido" para reuso entre ativações.
 */
export const useProcessoStore = defineStore("processo", () => {
    const contextoCompleto = ref<Processo | null>(null);
    const codProcessoCarregado = ref<number | null>(null);
    const invalido = ref(true);
    const carregamentosEmAndamento = new Map<number, Promise<Processo>>();

    /** Contexto ainda é válido para o processo dado? */
    function dadosValidos(_: number): boolean {
        return false;
    }

    /** Invalida o cache — deve ser chamado após qualquer ação de workflow que altera o processo. */
    function invalidar(): void {
        invalido.value = true;
        carregamentosEmAndamento.clear();
    }

    /**
     * Retorna o contexto completo do processo, usando cache quando disponível.
     * @returns o contexto (potencialmente do cache) ou null em caso de erro
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
            invalido.value = true;
            return data;
        } catch (err) {
            logger.error(`Erro ao buscar contexto completo do processo ${codProcesso}:`, err);
            throw err; // relança para que a view possa exibir erro inline
        } finally {
            carregamentosEmAndamento.delete(codProcesso);
        }
    }

    return {
        contextoCompleto,
        codProcessoCarregado,
        invalido,
        dadosValidos,
        invalidar,
        garantirContextoCompleto,
    };
});

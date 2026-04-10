import {defineStore} from "pinia";
import {ref} from "vue";
import type {Processo} from "@/types/tipos";
import {buscarContextoCompleto as serviceBuscarContextoCompleto} from "@/services/processoService";
import {logger} from "@/utils";

/**
 * Cache de sessão para contexto completo de processo.
 *
 * Problema: ProcessoDetalheView busca `processos/{codigo}/contexto-completo` em toda
 * ativação (onMounted + onActivated), mesmo quando o contexto não mudou. O endpoint
 * teve pico de 60ms e foi chamado 4 vezes na jornada do ciclo completo.
 *
 * Estratégia: cache por código de processo, invalidado após ações de workflow
 * (ação em bloco, finalização) que alteram o estado da tela.
 */
export const useProcessoStore = defineStore("processo", () => {
    const contextoCompleto = ref<Processo | null>(null);
    const codProcessoCarregado = ref<number | null>(null);
    const invalido = ref(true);

    /** Contexto ainda é válido para o processo dado? */
    function dadosValidos(codProcesso: number): boolean {
        return !invalido.value && codProcessoCarregado.value === codProcesso && contextoCompleto.value !== null;
    }

    /** Invalida o cache — deve ser chamado após qualquer ação de workflow que altera o processo. */
    function invalidar(): void {
        invalido.value = true;
    }

    /**
     * Retorna o contexto completo do processo, usando cache quando disponível.
     * @returns o contexto (potencialmente do cache) ou null em caso de erro
     */
    async function garantirContextoCompleto(codProcesso: number): Promise<Processo | null> {
        if (dadosValidos(codProcesso)) {
            return contextoCompleto.value;
        }

        try {
            const data = await serviceBuscarContextoCompleto(codProcesso);
            contextoCompleto.value = data;
            codProcessoCarregado.value = codProcesso;
            invalido.value = false;
            return data;
        } catch (err) {
            logger.error(`Erro ao buscar contexto completo do processo ${codProcesso}:`, err);
            throw err; // relança para que a view possa exibir erro inline
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

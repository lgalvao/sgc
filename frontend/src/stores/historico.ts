import {defineStore} from "pinia";
import {ref} from "vue";
import type {ProcessoResumo} from "@/types/tipos";

/**
 * Store de cache de sessão para o histórico de processos finalizados.
 *
 * Os dados são carregados uma vez por sessão e invalidados explicitamente
 * após a finalização de um processo.
 */
export const useHistoricoStore = defineStore("historico", () => {
    const processos = ref<ProcessoResumo[]>([]);
    const carregado = ref(false);

    function definirDados(novosProcessos: ProcessoResumo[]) {
        processos.value = novosProcessos;
        carregado.value = true;
    }

    function invalidar() {
        carregado.value = false;
    }

    function dadosValidos(): boolean {
        return carregado.value;
    }

    return {
        processos,
        carregado,
        definirDados,
        invalidar,
        dadosValidos,
    };
});

import {defineStore} from "pinia";
import {ref} from "vue";
import type {ProcessoResumo} from "@/types/tipos";
import {buscarProcessosFinalizados} from "@/services/processo";

/**
 * Store de cache de sessão para o histórico de processos finalizados.
 *
 * Os dados são carregados uma vez por sessão e invalidados explicitamente
 * após a finalização de um processo.
 */
export const useHistoricoStore = defineStore("historico", () => {
    const processos = ref<ProcessoResumo[]>([]);
    const carregado = ref(false);
    const carregando = ref(false);

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

    async function garantirDados(forcar = false): Promise<void> {
        if (!forcar && carregado.value) {
            return;
        }

        carregando.value = true;
        try {
            processos.value = await buscarProcessosFinalizados() ?? [];
            carregado.value = true;
        } catch {
            // Não marca carregado=true; permite retry na próxima navegação
            processos.value = [];
        } finally {
            carregando.value = false;
        }
    }

    return {
        processos,
        carregado,
        carregando,
        definirDados,
        invalidar,
        dadosValidos,
        garantirDados,
    };
});

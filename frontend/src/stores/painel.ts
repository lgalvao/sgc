import {defineStore} from "pinia";
import {ref} from "vue";
import type {Alerta, ProcessoResumo} from "@/types/tipos";

const TTL_PAINEL_MS = 5 * 60 * 1000; // 5 minutos

/**
 * Store de cache de sessão para o painel.
 *
 * Os dados são carregados uma vez por sessão e invalidados explicitamente após
 * ações de workflow que alteram o estado de processos ou alertas.
 * Um TTL de 5 minutos garante que dados muito antigos sejam recarregados em
 * segundo plano na próxima reativação do painel.
 */
export const usePainelStore = defineStore("painel", () => {
    const processos = ref<ProcessoResumo[]>([]);
    const alertas = ref<Alerta[]>([]);
    const carregado = ref(false);
    const carregadoEm = ref<number | null>(null);
    const codigosMarcadosComoLidos = ref(new Set<number>());

    function definirDados(novosProcessos: ProcessoResumo[], novosAlertas: Alerta[]) {
        processos.value = novosProcessos;
        alertas.value = novosAlertas;
        carregado.value = true;
        carregadoEm.value = Date.now();
    }

    function invalidar() {
        carregado.value = false;
        carregadoEm.value = null;
    }

    function resetar() {
        processos.value = [];
        alertas.value = [];
        carregado.value = false;
        carregadoEm.value = null;
        codigosMarcadosComoLidos.value = new Set();
    }

    function dadosValidos(): boolean {
        if (!carregado.value || carregadoEm.value === null) {
            return false;
        }
        return Date.now() - carregadoEm.value < TTL_PAINEL_MS;
    }

    function registrarLeitura(codigos: number[]) {
        codigos.forEach(c => codigosMarcadosComoLidos.value.add(c));
    }

    function isMarcadoComoLido(codigo: number): boolean {
        return codigosMarcadosComoLidos.value.has(codigo);
    }

    return {
        processos,
        alertas,
        carregado,
        carregadoEm,
        definirDados,
        invalidar,
        resetar,
        dadosValidos,
        registrarLeitura,
        isMarcadoComoLido
    };
});

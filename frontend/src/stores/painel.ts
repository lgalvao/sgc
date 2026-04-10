import {defineStore} from "pinia";
import {ref} from "vue";
import type {Alerta, ProcessoResumo} from "@/types/tipos";

/**
 * Store de cache de sessão para o painel.
 *
 * Os dados são carregados uma vez por sessão e invalidados explicitamente após
 * ações de workflow que alteram o estado de processos ou alertas.
 * A unidade selecionada não muda durante a sessão (só via logout/login), portanto
 * não há necessidade de granularidade por unidade.
 */
export const usePainelStore = defineStore("painel", () => {
    const processos = ref<ProcessoResumo[]>([]);
    const alertas = ref<Alerta[]>([]);
    const carregado = ref(false);
    const codigosMarcadosComoLidos = ref(new Set<number>());

    function definirDados(novosProcessos: ProcessoResumo[], novosAlertas: Alerta[]) {
        processos.value = novosProcessos;
        alertas.value = novosAlertas;
        carregado.value = true;
    }

    function invalidar() {
        carregado.value = false;
    }

    function dadosValidos(): boolean {
        return carregado.value;
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
        definirDados,
        invalidar,
        dadosValidos,
        registrarLeitura,
        isMarcadoComoLido
    };
});

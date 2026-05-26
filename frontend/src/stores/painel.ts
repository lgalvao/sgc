import {defineStore} from "pinia";
import {ref} from "vue";

/**
 * Estado local do painel que não pertence ao cache remoto.
 *
 * O carregamento de processos e alertas agora é feito via Pinia Colada.
 * Este store mantém apenas os alertas que já foram marcados como lidos
 * durante a sessão para evitar chamadas repetidas ao backend.
 */
export const usePainelStore = defineStore("painel", () => {
    const codigosMarcadosComoLidos = ref(new Set<number>());

    function invalidar() {
        codigosMarcadosComoLidos.value = new Set();
    }

    function resetar() {
        codigosMarcadosComoLidos.value = new Set();
    }

    function registrarLeitura(codigos: number[]) {
        codigos.forEach(c => codigosMarcadosComoLidos.value.add(c));
    }

    function isMarcadoComoLido(codigo: number): boolean {
        return codigosMarcadosComoLidos.value.has(codigo);
    }

    return {
        invalidar,
        resetar,
        registrarLeitura,
        isMarcadoComoLido
    };
});

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
    const precisaRecarregar = ref(false);

    function invalidar() {
        codigosMarcadosComoLidos.value = new Set();
        precisaRecarregar.value = true;
    }

    function resetar() {
        codigosMarcadosComoLidos.value = new Set();
        precisaRecarregar.value = false;
    }

    function marcarRecarregado() {
        precisaRecarregar.value = false;
    }

    function registrarLeitura(codigos: number[]) {
        codigos.forEach(c => codigosMarcadosComoLidos.value.add(c));
    }

    function isMarcadoComoLido(codigo: number): boolean {
        return codigosMarcadosComoLidos.value.has(codigo);
    }

    return {
        invalidar,
        marcarRecarregado,
        precisaRecarregar,
        resetar,
        registrarLeitura,
        isMarcadoComoLido
    };
});

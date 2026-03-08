import {ref} from 'vue';
import type {Unidade} from '@/types/tipos';

// Estado reativo compartilhado (módulo singleton)
const unidadeAtual = ref<Unidade | null>(null);

export function useUnidadeAtual() {
    function definirUnidadeAtual(unidade: Unidade | null) {
        unidadeAtual.value = unidade;
    }

    return {unidadeAtual, definirUnidadeAtual};
}

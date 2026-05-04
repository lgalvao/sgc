import {ref} from "vue";
import type {Unidade} from "@/types/tipos";

export function useArvoreExpansao() {
    const expandedUnits = ref<Set<number>>(new Set());

    function isExpanded(unidade: Unidade): boolean {
        return expandedUnits.value.has(unidade.codigo);
    }

    function toggleExpand(unidade: Unidade) {
        if (expandedUnits.value.has(unidade.codigo)) {
            expandedUnits.value.delete(unidade.codigo);
        } else {
            expandedUnits.value.add(unidade.codigo);
        }
    }

    function expandirRecursivo(unidades: Unidade[]) {
        unidades.forEach(u => {
            if (u.filhas && u.filhas.length > 0) {
                expandedUnits.value.add(u.codigo);
                expandirRecursivo(u.filhas);
            }
        });
    }

    function limparExpansao() {
        expandedUnits.value = new Set();
    }

    return {
        expandedUnits,
        isExpanded,
        toggleExpand,
        expandirRecursivo,
        limparExpansao
    };
}

import {storeToRefs} from 'pinia';
import {usePerfilStore} from '@/stores/perfil';
import type {Unidade} from '@/types/tipos';

export function useUnidadeAtual() {
    const perfilStore = usePerfilStore();
    const {unidadeAtualDetalhes} = storeToRefs(perfilStore);

    function definirUnidadeAtual(unidade: Unidade | null) {
        perfilStore.unidadeAtualDetalhes = unidade;
    }

    return {
        unidadeAtual: unidadeAtualDetalhes as import('vue').Ref<Unidade | null>,
        definirUnidadeAtual,
    };
}

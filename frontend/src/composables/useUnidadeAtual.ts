// @sgc-auditoria ignorar: arquivoMinusculo | Abstração deliberada: fornece acesso reativo + setter para unidadeAtualDetalhes; dois consumidores (useBreadcrumbs, useUnidadeTela) tornam o inline inadequado
import {storeToRefs} from 'pinia';
import type {Ref} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import type {Unidade} from '@/types/tipos';

export function useUnidadeAtual() {
    const perfilStore = usePerfilStore();
    const {unidadeAtualDetalhes} = storeToRefs(perfilStore);

    function definirUnidadeAtual(unidade: Unidade | null) {
        perfilStore.unidadeAtualDetalhes = unidade;
    }

    return {
        unidadeAtual: unidadeAtualDetalhes as Ref<Unidade | null>,
        definirUnidadeAtual,
    };
}

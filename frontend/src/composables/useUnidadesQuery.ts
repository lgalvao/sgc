import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import type {Unidade} from "@/types/tipos";
import {buscarTodasUnidades} from "@/services/unidadeService";

export const CHAVE_QUERY_UNIDADES = ["unidades"] as const;

export function useUnidadesQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<Unidade[]>({
        key: CHAVE_QUERY_UNIDADES,
        query: () => buscarTodasUnidades(),
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: Infinity,
    });
}

export function useInvalidacaoUnidades() {
    return {
        invalidarUnidades: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_UNIDADES}),
    };
}

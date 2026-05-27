import {useQuery, useQueryCache} from "@pinia/colada";
import type {Unidade} from "@/types/tipos";
import {buscarTodasUnidades} from "@/services/unidadeService";

export const CHAVE_QUERY_UNIDADES = ["unidades"] as const;

export function useUnidadesQuery() {
    return useQuery<Unidade[]>({
        key: CHAVE_QUERY_UNIDADES,
        query: () => buscarTodasUnidades(),
        staleTime: Infinity,
    });
}

export function useInvalidacaoUnidades() {
    return {
        invalidarUnidades: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_UNIDADES}),
    };
}

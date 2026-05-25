import {useQuery, useQueryCache} from "@pinia/colada";
import {buscarProcessosFinalizados} from "@/services/processo";
import type {ProcessoResumo} from "@/types/tipos";

export const CHAVE_QUERY_HISTORICO = ["historico"] as const;

export function useHistoricoQuery() {
    return useQuery<ProcessoResumo[], Error, ProcessoResumo[]>({
        key: CHAVE_QUERY_HISTORICO,
        query: () => buscarProcessosFinalizados(),
        initialData: () => [],
        staleTime: Infinity,
    });
}

export function useInvalidacaoHistorico() {
    const queryCache = useQueryCache();

    return {
        invalidarHistorico: () => queryCache.invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true}),
    };
}

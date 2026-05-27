import {useQuery, useQueryCache} from "@pinia/colada";
import {computed} from "vue";
import {buscarContextoCompleto} from "@/services/processo";
import type {Processo} from "@/types/tipos";

export const CHAVE_QUERY_PROCESSO = ["processo"] as const;

export function useProcessoQuery(codigoProcesso: number) {
    const chave = computed(() => [...CHAVE_QUERY_PROCESSO, codigoProcesso] as const);

    return useQuery<Processo | null, Error, Processo | null>({
        key: () => [...chave.value],
        query: async () => buscarContextoCompleto(codigoProcesso),
        enabled: () => Number.isFinite(codigoProcesso) && codigoProcesso > 0,
        staleTime: Infinity,
    });
}

export function useInvalidacaoProcesso() {
    return {
        invalidarProcesso: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_PROCESSO}),
    };
}

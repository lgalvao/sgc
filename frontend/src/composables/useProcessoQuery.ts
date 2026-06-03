// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
import {useQuery, useQueryCache} from "@pinia/colada";
import {computed} from "vue";
import {usePerfilStore} from "@/stores/perfil";
import {buscarContextoCompleto} from "@/services/processo";
import type {Processo} from "@/types/tipos";

export const CHAVE_QUERY_PROCESSO = ["processo"] as const;

export function useProcessoQuery(codigoProcesso: number) {
    const perfilStore = usePerfilStore();
    const chave = computed(() => [...CHAVE_QUERY_PROCESSO, codigoProcesso] as const);

    return useQuery<Processo | null, Error, Processo | null>({
        key: () => [...chave.value],
        query: async () => buscarContextoCompleto(codigoProcesso),
        enabled: false,
        staleTime: Infinity,
    });
}

export function useInvalidacaoProcesso() {
    return {
        invalidarProcesso: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_PROCESSO}),
    };
}

// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import {buscarProcessosFinalizados} from "@/services/processo";
import type {ProcessoResumo} from "@/types/tipos";

export const CHAVE_QUERY_HISTORICO = ["historico"] as const;

export function useHistoricoQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<ProcessoResumo[], Error, ProcessoResumo[]>({
        key: CHAVE_QUERY_HISTORICO,
        query: () => buscarProcessosFinalizados(),
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: Infinity,
    });
}

export function useInvalidacaoHistorico() {
    return {
        invalidarHistorico: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true}),
    };
}

// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
import {useQuery, useQueryCache} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
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
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}

export function useInvalidacaoUnidades() {
    return {
        invalidarUnidades: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_UNIDADES}),
    };
}

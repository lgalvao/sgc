// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
import {useQuery, useQueryCache} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
import {usePerfilStore} from "@/stores/perfil";
import {buscarDiagnosticoOrganizacional} from "@/services/unidadeService";
import type {DiagnosticoOrganizacional} from "@/types/tipos";

export const CHAVE_QUERY_DIAGNOSTICO_ORGANIZACIONAL = ["diagnostico-organizacional"] as const;

/**
 * Query de sessão para o diagnóstico organizacional.
 * Carrega apenas quando o perfil ativo tem permissão para exibir o diagnóstico.
 * Cache controlado por invalidação explícita — dados só são recarregados após mutações organizacionais.
 */
export function useDiagnosticoOrganizacionalQuery() {
    const perfilStore = usePerfilStore();
    return useQuery<DiagnosticoOrganizacional, Error, DiagnosticoOrganizacional>({
        key: CHAVE_QUERY_DIAGNOSTICO_ORGANIZACIONAL,
        query: () => buscarDiagnosticoOrganizacional(),
        enabled: () => perfilStore.permissoesSessao?.mostrarDiagnosticoOrganizacional === true,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}

export function useInvalidacaoDiagnosticoOrganizacional() {
    return {
        invalidarDiagnostico: () =>
            useQueryCache().invalidateQueries({key: CHAVE_QUERY_DIAGNOSTICO_ORGANIZACIONAL}),
    };
}

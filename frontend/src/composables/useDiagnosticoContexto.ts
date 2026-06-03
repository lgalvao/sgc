import {useQuery, useQueryCache} from '@pinia/colada';
import {usePerfilStore} from '@/stores/perfil';
import {obterContextoDiagnostico} from '@/services/diagnosticoService';
import type {DiagnosticoContexto} from '@/types/diagnostico-competencias';

/** Prefixo de chave compartilhado entre todos os composables de diagnóstico. */
export const CHAVE_DIAGNOSTICO = 'diagnostico-competencias';

export function chaveContexto(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'contexto', codSubprocesso] as const;
}

/**
 * Query do contexto de diagnóstico para um subprocesso.
 * Carrega situação, unidade e competências do mapa copiado.
 */
export function useDiagnosticoContexto(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    return useQuery<DiagnosticoContexto>({
        key: () => chaveContexto(codSubprocesso),
        query: () => obterContextoDiagnostico(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });
}

/** Invalida o contexto de diagnóstico após ações de fluxo. */
export function useInvalidacaoDiagnosticoContexto() {
    const cache = useQueryCache();
    return {
        invalidarContexto: (codSubprocesso: number) =>
            void cache.invalidateQueries({key: chaveContexto(codSubprocesso)}),
    };
}

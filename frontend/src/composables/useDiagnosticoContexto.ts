import {useQuery, useQueryCache} from '@pinia/colada';
import {usePerfilStore} from '@/stores/perfil';
import {obterContextoDiagnostico} from '@/services/diagnosticoService';
import type {DiagnosticoContexto} from '@/types/diagnostico-competencias';
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from '@/composables/cachePolicy';
import {
    CHAVE_DIAGNOSTICO,
    criarContextoSessaoDiagnostico,
    habilitarQueryDiagnostico,
} from '@/composables/diagnosticoQueryUtils';

export {
    CHAVE_DIAGNOSTICO,
    criarContextoSessaoDiagnostico,
    habilitarQueryDiagnostico,
};

type ContextoSessaoDiagnostico = ReturnType<typeof criarContextoSessaoDiagnostico>;

export function chaveContexto(codSubprocesso: number, contextoSessao: ContextoSessaoDiagnostico) {
    return [CHAVE_DIAGNOSTICO, 'contexto', ...contextoSessao, codSubprocesso] as const;
}

export function chaveAutoavaliacao(codSubprocesso: number, contextoSessao: ContextoSessaoDiagnostico) {
    return [CHAVE_DIAGNOSTICO, 'autoavaliacao', ...contextoSessao, codSubprocesso] as const;
}

export function chaveEquipe(codSubprocesso: number, contextoSessao: ContextoSessaoDiagnostico) {
    return [CHAVE_DIAGNOSTICO, 'equipe', ...contextoSessao, codSubprocesso] as const;
}

export function chaveUnidade(codSubprocesso: number, contextoSessao: ContextoSessaoDiagnostico) {
    return [CHAVE_DIAGNOSTICO, 'unidade', ...contextoSessao, codSubprocesso] as const;
}

export function chaveConsenso(
    codSubprocesso: number,
    contextoSessao: ContextoSessaoDiagnostico,
    servidorTitulo?: string,
) {
    return [CHAVE_DIAGNOSTICO, 'consenso', ...contextoSessao, codSubprocesso, servidorTitulo ?? 'usuario-logado'] as const;
}

/**
 * Query do contexto de diagnóstico para um subprocesso.
 * Carrega situação, unidade e competências do mapa copiado.
 */
export function useDiagnosticoContexto(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);
    return useQuery<DiagnosticoContexto>({
        key: () => chaveContexto(codSubprocesso, contextoSessao),
        query: () => obterContextoDiagnostico(codSubprocesso),
        enabled: () => habilitarQueryDiagnostico(perfilStore, codSubprocesso),
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}

/** Invalida o contexto de diagnóstico após ações de fluxo. */
export function useInvalidacaoDiagnosticoContexto() {
    const cache = useQueryCache();
    const perfilStore = usePerfilStore();
    return {
        invalidarContexto: (codSubprocesso: number) =>
            void cache.invalidateQueries({
                key: chaveContexto(codSubprocesso, criarContextoSessaoDiagnostico(perfilStore)),
                exact: true,
            }),
    };
}

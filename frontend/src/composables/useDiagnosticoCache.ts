import {useQueryCache} from '@pinia/colada';
import {usePerfilStore} from '@/stores/perfil';
import {
    chaveAutoavaliacao,
    chaveConsenso,
    chaveContexto,
    chaveEquipe,
    chaveUnidade,
    criarContextoSessaoDiagnostico,
} from '@/composables/useDiagnosticoContexto';

export function useCacheDiagnostico() {
    const cache = useQueryCache();
    const perfilStore = usePerfilStore();

    function contextoSessao() {
        return criarContextoSessaoDiagnostico(perfilStore);
    }

    function invalidarContexto(codSubprocesso: number) {
        void cache.invalidateQueries({key: chaveContexto(codSubprocesso, contextoSessao()), exact: true});
    }

    function invalidarAutoavaliacao(codSubprocesso: number) {
        void cache.invalidateQueries({key: chaveAutoavaliacao(codSubprocesso, contextoSessao()), exact: true});
    }

    function invalidarEquipe(codSubprocesso: number) {
        void cache.invalidateQueries({key: chaveEquipe(codSubprocesso, contextoSessao()), exact: true});
    }

    function invalidarUnidade(codSubprocesso: number) {
        void cache.invalidateQueries({key: chaveUnidade(codSubprocesso, contextoSessao()), exact: true});
    }

    function invalidarConsenso(codSubprocesso: number, servidorTitulo?: string) {
        void cache.invalidateQueries({key: chaveConsenso(codSubprocesso, contextoSessao(), servidorTitulo), exact: true});
    }

    function invalidarPermissoes(codSubprocesso: number) {
        void cache.invalidateQueries({key: ['subprocesso-contexto-edicao-diagnostico', codSubprocesso], exact: true});
    }

    function invalidarFluxoCompleto(codSubprocesso: number) {
        invalidarContexto(codSubprocesso);
        invalidarEquipe(codSubprocesso);
        invalidarUnidade(codSubprocesso);
        invalidarPermissoes(codSubprocesso);
    }

    return {
        invalidarContexto,
        invalidarAutoavaliacao,
        invalidarEquipe,
        invalidarUnidade,
        invalidarConsenso,
        invalidarFluxoCompleto,
    };
}

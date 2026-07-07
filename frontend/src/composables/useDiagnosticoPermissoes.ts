import {computed} from 'vue';
import {useQuery} from '@pinia/colada';
import {criarAcessosPermissao} from '@/composables/acessoPermissoes';
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from '@/composables/cachePolicy';
import {criarContextoSessaoDiagnostico, habilitarQueryDiagnostico,} from '@/composables/diagnosticoQueryUtils';
import {buscarPermissoesSubprocesso} from '@/services/subprocessoServiceContexto';
import {usePerfilStore} from '@/stores/perfil';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';
import type {PermissoesSubprocesso} from '@/types/tipos';

export function useDiagnosticoPermissoes(
    codSubprocesso: number,
    permissoesConhecidas?: PermissoesSubprocesso | null,
) {
    const perfilStore = usePerfilStore();
    const contextoSessao = computed(() => criarContextoSessaoDiagnostico(perfilStore));
    const usarPermissoesConhecidas = computed(() => !!permissoesConhecidas);

    const queryPermissoes = useQuery({
        key: () => ['subprocesso-permissoes-diagnostico', ...contextoSessao.value, codSubprocesso] as const,
        query: () => buscarPermissoesSubprocesso(codSubprocesso),
        enabled: () => !usarPermissoesConhecidas.value && habilitarQueryDiagnostico(perfilStore, codSubprocesso),
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });

    const permissoes = computed<PermissoesSubprocesso>(() =>
        permissoesConhecidas
        ?? queryPermissoes.data.value
        ?? PERMISSOES_SUBPROCESSO_VAZIAS,
    );
    const acesso = criarAcessosPermissao(permissoes);

    return {
        queryPermissoes,
        ...acesso,
    };
}

import {computed} from 'vue';
import {useQuery} from '@pinia/colada';
import {criarAcessosPermissao} from '@/composables/acessoPermissoes';
import {STALE_TIME_LEITURA_AUXILIAR} from '@/composables/cachePolicy';
import {
    criarContextoSessaoDiagnostico,
    habilitarQueryDiagnostico,
} from '@/composables/diagnosticoQueryUtils';
import {buscarPermissoesSubprocesso} from '@/services/subprocessoServiceContexto';
import {usePerfilStore} from '@/stores/perfil';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';
import type {PermissoesSubprocesso} from '@/types/tipos';

export function useDiagnosticoPermissoes(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const contextoSessao = computed(() => criarContextoSessaoDiagnostico(perfilStore));

    const queryPermissoes = useQuery({
        key: () => ['subprocesso-permissoes-diagnostico', ...contextoSessao.value, codSubprocesso] as const,
        query: () => buscarPermissoesSubprocesso(codSubprocesso),
        enabled: () => habilitarQueryDiagnostico(perfilStore, codSubprocesso),
        staleTime: STALE_TIME_LEITURA_AUXILIAR,
    });

    const permissoes = computed<PermissoesSubprocesso>(() => queryPermissoes.data.value ?? PERMISSOES_SUBPROCESSO_VAZIAS);
    const acesso = criarAcessosPermissao(permissoes);

    return {
        queryPermissoes,
        ...acesso,
    };
}

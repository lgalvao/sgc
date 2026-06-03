import {computed} from 'vue';
import {useQuery} from '@pinia/colada';
import {buscarContextoEdicao} from '@/services/subprocessoServiceContexto';
import {useAcesso} from '@/composables/acesso';

export function useDiagnosticoPermissoes(codSubprocesso: number) {
    const queryContextoEdicao = useQuery({
        key: () => ['subprocesso-contexto-edicao-diagnostico', codSubprocesso] as const,
        query: () => buscarContextoEdicao(codSubprocesso),
        enabled: () => codSubprocesso > 0,
        staleTime: 60_000,
    });

    const subprocesso = computed(() => queryContextoEdicao.data.value?.detalhes ?? null);
    const acesso = useAcesso(subprocesso);

    return {
        queryContextoEdicao,
        subprocesso,
        ...acesso,
    };
}

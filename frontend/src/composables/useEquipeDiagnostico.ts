import {useQuery} from '@pinia/colada';
import {computed, type MaybeRefOrGetter, toValue} from 'vue';
import {STALE_TIME_LEITURA_AUXILIAR} from '@/composables/cachePolicy';
import {usePerfilStore} from '@/stores/perfil';
import {obterEquipe} from '@/services/diagnosticoService';
import type {DiagnosticoEquipe} from '@/types/diagnostico-competencias';
import {
    chaveEquipe,
    criarContextoSessaoDiagnostico,
    habilitarQueryDiagnostico
} from '@/composables/useDiagnosticoContexto';

/**
 * Composable de acompanhamento da equipe no diagnóstico.
 * Carrega lista de servidores com suas situações de avaliação.
 * Apenas leitura — sem mutations.
 */
export function useEquipeDiagnostico(
    codSubprocesso: number,
    habilitado: MaybeRefOrGetter<boolean> = true,
) {
    const perfilStore = usePerfilStore();
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);

    const query = useQuery<DiagnosticoEquipe>({
        key: () => chaveEquipe(codSubprocesso, contextoSessao),
        query: () => obterEquipe(codSubprocesso),
        enabled: () => habilitarQueryDiagnostico(perfilStore, codSubprocesso, toValue(habilitado)),
        staleTime: STALE_TIME_LEITURA_AUXILIAR,
    });

    const itens = computed(() => query.data.value?.servidores ?? []);
    const carregando = computed(() => query.status.value === 'pending');
    const totalServidores = computed(() => itens.value.length);
    const pendentes = computed(
        () =>
            itens.value.filter(
                (i) =>
                    i.situacaoServidor !== 'CONSENSO_APROVADO' &&
                    i.situacaoServidor !== 'AVALIACAO_IMPOSSIBILITADA',
            ).length,
    );

    return {query, itens, carregando, totalServidores, pendentes};
}

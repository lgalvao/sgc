import {useQuery} from '@pinia/colada';
import {computed} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {obterEquipe} from '@/services/diagnosticoService';
import type {DiagnosticoEquipe} from '@/types/diagnostico-competencias';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

export function chaveEquipeDiagnostico(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'equipe', codSubprocesso] as const;
}

/**
 * Composable de acompanhamento da equipe no diagnóstico.
 * Carrega lista de servidores com suas situações de avaliação.
 * Apenas leitura — sem mutations.
 */
export function useEquipeDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();

    const query = useQuery<DiagnosticoEquipe>({
        key: () => chaveEquipeDiagnostico(codSubprocesso),
        query: () => obterEquipe(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: 30_000,
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

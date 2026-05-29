import {useQuery} from '@pinia/colada';
import {computed} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {obterDiagnosticoUnidade} from '@/services/diagnosticoService';
import type {DiagnosticoUnidade} from '@/types/diagnostico-competencias';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

function chaveUnidade(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'unidade', codSubprocesso] as const;
}

/**
 * Composable de monitoramento do diagnóstico da unidade.
 * Carrega dados completos: servidores, consenso, ocupações e movimentações.
 * Usado pelas views de monitoramento e análise da unidade pelo gestor/admin.
 */
export function useMonitoramentoDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();

    const query = useQuery<DiagnosticoUnidade>({
        key: () => chaveUnidade(codSubprocesso),
        query: () => obterDiagnosticoUnidade(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: 30_000,
    });

    const unidade = computed(() => query.data.value?.unidade);
    const servidores = computed(() => query.data.value?.servidores ?? []);
    const ocupacoesCriticas = computed(() => query.data.value?.ocupacoesCriticas ?? []);
    const movimentacoes = computed(() => query.data.value?.movimentacoes ?? []);
    const carregando = computed(() => query.status.value === 'pending');
    const erro = computed(() => query.error.value);
    const situacao = computed(() => unidade.value?.situacao ?? '');
    const totalPendentes = computed(
        () =>
            servidores.value.filter(
                (s) =>
                    s.situacaoServidor !== 'CONSENSO_APROVADO' &&
                    s.situacaoServidor !== 'AVALIACAO_IMPOSSIBILITADA',
            ).length,
    );

    return {
        query,
        unidade,
        servidores,
        ocupacoesCriticas,
        movimentacoes,
        carregando,
        erro,
        situacao,
        totalPendentes,
    };
}

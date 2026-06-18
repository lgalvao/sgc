import {useQuery} from '@pinia/colada';
import {computed} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {obterDiagnosticoUnidade} from '@/services/diagnosticoService';
import type {DiagnosticoUnidade, SituacaoDiagnostico} from '@/types/diagnostico-competencias';
import {
    chaveUnidade,
    criarContextoSessaoDiagnostico,
    useDiagnosticoContexto
} from '@/composables/useDiagnosticoContexto';

/**
 * Composable de leitura do diagnóstico da unidade.
 * Carrega dados completos: servidores, consenso, situações de capacitação e movimentações.
 */
export function useDiagnosticoUnidade(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const {data: contexto} = useDiagnosticoContexto(codSubprocesso);
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);

    const query = useQuery<DiagnosticoUnidade>({
        key: () => chaveUnidade(codSubprocesso, contextoSessao),
        query: () => obterDiagnosticoUnidade(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: 30_000,
    });

    const unidade = computed(() => query.data.value?.unidade);
    const servidores = computed(() => query.data.value?.servidores ?? []);
    const situacoesCapacitacao = computed(() => query.data.value?.situacoesCapacitacao ?? []);
    const movimentacoes = computed(() => query.data.value?.movimentacoes ?? []);
    const carregando = computed(() => query.status.value === 'pending');
    const erro = computed(() => query.error.value);
    const situacaoSubprocesso = computed(() => unidade.value?.situacaoSubprocesso ?? '');
    const situacao = computed<SituacaoDiagnostico>(
        () => query.data.value?.situacaoDiagnostico ?? contexto.value?.situacaoDiagnostico ?? 'EM_ANDAMENTO',
    );
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
        situacoesCapacitacao,
        movimentacoes,
        carregando,
        erro,
        situacaoSubprocesso,
        situacao,
        totalPendentes,
    };
}

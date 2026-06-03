import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {
    aprovarConsenso,
    impossibilitarAvaliacao,
    obterConsenso,
    obterConsensoServidor,
    salvarConsenso,
} from '@/services/diagnosticoService';
import type {AvaliacaoCompetencia, Consenso} from '@/types/diagnostico-competencias';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

function chaveConsenso(codSubprocesso: number, servidorTitulo?: string) {
    return [CHAVE_DIAGNOSTICO, 'consenso', codSubprocesso, servidorTitulo ?? 'usuario-logado'] as const;
}

function chaveEquipe(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'equipe', codSubprocesso] as const;
}

function chaveAutoavaliacao(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'autoavaliacao', codSubprocesso] as const;
}

/**
 * Composable de consenso de diagnóstico.
 * - Query do consenso do servidor logado.
 * - Mutation de salvar consenso (para chefia, passando servidorTitulo).
 * - Mutation de aprovar consenso (para o servidor logado).
 * - Mutation de impossibilitar avaliação (para chefia).
 */
export function useConsensoDiagnostico(codSubprocesso: number, servidorTitulo?: string) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();

    const query = useQuery<Consenso>({
        key: () => chaveConsenso(codSubprocesso, servidorTitulo),
        query: () => servidorTitulo
            ? obterConsensoServidor(codSubprocesso, servidorTitulo)
            : obterConsenso(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });

    // Estado local editável pela chefia
    const competenciasLocais = ref<AvaliacaoCompetencia[]>([]);
    const motivoReabertura = ref('');

    watch(
        () => query.data.value?.competencias,
        (novas) => {
            if (novas) {
                competenciasLocais.value = novas.map((c) => ({...c}));
            }
        },
        {immediate: true},
    );

    const mutacaoSalvar = useMutation({
        mutation: ({servidorTitulo, motivo}: {servidorTitulo: string; motivo?: string}) =>
            salvarConsenso(codSubprocesso, servidorTitulo, {
                competencias: competenciasLocais.value,
                motivoReabertura: motivo || undefined,
            }),
        onSuccess: () => {
            cache.invalidateQueries({key: chaveConsenso(codSubprocesso, servidorTitulo)});
            cache.invalidateQueries({key: chaveEquipe(codSubprocesso)});
        },
    });

    const mutacaoAprovar = useMutation({
        mutation: () => aprovarConsenso(codSubprocesso),
        onSuccess: () => {
            cache.invalidateQueries({key: chaveConsenso(codSubprocesso, servidorTitulo)});
            cache.invalidateQueries({key: chaveEquipe(codSubprocesso)});
            cache.invalidateQueries({key: chaveAutoavaliacao(codSubprocesso)});
        },
    });

    const mutacaoImpossibilitar = useMutation({
        mutation: ({servidorTitulo, justificativa}: {servidorTitulo: string; justificativa: string}) =>
            impossibilitarAvaliacao(codSubprocesso, servidorTitulo, {justificativa}),
        onSuccess: () => {
            cache.invalidateQueries({key: chaveEquipe(codSubprocesso)});
        },
    });

    function atualizarNota(competenciaCodigo: number, campo: 'importancia' | 'dominio', valor: number | null) {
        const item = competenciasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
        if (item) item[campo] = valor;
    }

    const situacaoServidor = computed(() => query.data.value?.situacaoServidor ?? 'AUTOAVALIACAO_NAO_REALIZADA');
    const carregando = computed(() => query.status.value === 'pending');
    const salvando = computed(() => mutacaoSalvar.isLoading.value);
    const aprovando = computed(() => mutacaoAprovar.isLoading.value);
    const impossibilitando = computed(() => mutacaoImpossibilitar.isLoading.value);

    return {
        query,
        competenciasLocais,
        motivoReabertura,
        situacaoServidor,
        carregando,
        salvando,
        aprovando,
        impossibilitando,
        erroSalvar: computed(() => mutacaoSalvar.error.value),
        erroAprovar: computed(() => mutacaoAprovar.error.value),
        erroImpossibilitar: computed(() => mutacaoImpossibilitar.error.value),
        atualizarNota,
        salvarConsenso: (servidorTitulo: string, motivo?: string) =>
            mutacaoSalvar.mutateAsync({servidorTitulo, motivo}),
        aprovarConsenso: () => mutacaoAprovar.mutateAsync(),
        impossibilitarAvaliacao: (servidorTitulo: string, justificativa: string) =>
            mutacaoImpossibilitar.mutateAsync({servidorTitulo, justificativa}),
    };
}

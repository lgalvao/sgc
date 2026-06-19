import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {obterDiagnosticoUnidade, salvarSituacoesCapacitacao} from '@/services/diagnosticoService';
import type {
    AvaliacaoCompetencia,
    DiagnosticoUnidade,
    SituacaoCapacitacaoItem,
    ValorSituacaoCapacitacao
} from '@/types/diagnostico-competencias';
import {chaveUnidade, criarContextoSessaoDiagnostico} from '@/composables/useDiagnosticoContexto';

/**
 * Composable de situação de capacitação do diagnóstico da unidade.
 * - Query: carrega dados completos da unidade (servidores + situações + movimentações).
 * - Mutation de salvar: autosave com debounce de 800ms.
 */
export function useSituacaoCapacitacaoDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);

    const query = useQuery<DiagnosticoUnidade>({
        key: () => chaveUnidade(codSubprocesso, contextoSessao),
        query: () => obterDiagnosticoUnidade(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });

    const situacoesLocais = ref<SituacaoCapacitacaoItem[]>([]);
    const salvandoAutomaticamente = ref(false);

    watch(
        () => query.data.value,
        (novoDiagnostico) => {
            if (novoDiagnostico) {
                situacoesLocais.value = montarSituacoesLocais(novoDiagnostico);
            }
        },
        {immediate: true},
    );

    const mutacaoSalvar = useMutation({
        mutation: (situacoes: SituacaoCapacitacaoItem[]) =>
            salvarSituacoesCapacitacao(codSubprocesso, {
                situacoes: situacoes.map((o) => ({
                    servidorTitulo: o.servidorTitulo,
                    competenciaCodigo: o.competenciaCodigo,
                    situacaoCapacitacao: o.situacaoCapacitacao,
                })),
            }),
        onSettled: () => {
            salvandoAutomaticamente.value = false;
        },
        onSuccess: () => {
            void cache.invalidateQueries({key: chaveUnidade(codSubprocesso, contextoSessao), exact: true});
        },
    });

    // Autosave com debounce nativo
    let timer: ReturnType<typeof setTimeout> | null = null;
    const dispararSalvamento = () => {
        if (timer !== null) clearTimeout(timer);
        timer = setTimeout(() => { mutacaoSalvar.mutate(situacoesLocais.value); }, 800);
    };

    function atualizarCapacitacao(
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: ValorSituacaoCapacitacao | null,
    ) {
        const item = situacoesLocais.value.find(
            (o) => o.servidorTitulo === servidorTitulo && o.competenciaCodigo === competenciaCodigo,
        );
        if (!item) return;
        item.situacaoCapacitacao = situacao;
        salvandoAutomaticamente.value = true;
        dispararSalvamento();
    }

    const servidores = computed(() => query.data.value?.servidores ?? []);
    const unidade = computed(() => query.data.value?.unidade);
    const movimentacoes = computed(() => query.data.value?.movimentacoes ?? []);
    const carregando = computed(() => query.status.value === 'pending');
    const erro = computed(() => query.error.value);
    const pendentes = computed(
        () => situacoesLocais.value.filter((o) => !o.situacaoCapacitacao).length,
    );

    return {
        query,
        situacoesLocais,
        servidores,
        unidade,
        movimentacoes,
        carregando,
        erro,
        salvandoAutomaticamente,
        pendentes,
        atualizarCapacitacao,
    };
}

function montarSituacoesLocais(diagnostico: DiagnosticoUnidade): SituacaoCapacitacaoItem[] {
    const situacoesExistentes = new Map(
        (diagnostico.situacoesCapacitacao ?? []).map((item) => [
            `${item.servidorTitulo}-${item.competenciaCodigo}`,
            {...item},
        ]),
    );

    const situacoesCompletas: SituacaoCapacitacaoItem[] = [];

    for (const servidor of diagnostico.servidores ?? []) {
        if (servidor.situacaoServidor !== 'CONSENSO_APROVADO') {
            continue;
        }

        for (const consenso of servidor.consenso ?? []) {
            situacoesCompletas.push(
                situacoesExistentes.get(`${servidor.servidorTitulo}-${consenso.competenciaCodigo}`)
                ?? criarSituacaoCapacitacaoPendente(servidor.servidorTitulo, servidor.servidorNome, consenso),
            );
        }
    }

    return situacoesCompletas;
}

function criarSituacaoCapacitacaoPendente(
    servidorTitulo: string,
    servidorNome: string,
    consenso: AvaliacaoCompetencia,
): SituacaoCapacitacaoItem {
    return {
        servidorTitulo,
        servidorNome,
        competenciaCodigo: consenso.competenciaCodigo,
        situacaoCapacitacao: null,
    };
}

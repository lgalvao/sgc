import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {
    DEBOUNCE_AUTOSAVE_PADRAO_MS,
    STALE_TIME_CONTROLADO_POR_INVALIDACAO,
} from '@/composables/cachePolicy';
import {usePerfilStore} from '@/stores/perfil';
import {obterDiagnosticoUnidade, salvarSituacoesCapacitacao} from '@/services/diagnosticoService';
import type {
    AvaliacaoCompetencia,
    DiagnosticoUnidade,
    SituacaoCapacitacaoItem,
    ValorSituacaoCapacitacao
} from '@/types/diagnostico-competencias';
import {chaveUnidade, criarContextoSessaoDiagnostico, habilitarQueryDiagnostico} from '@/composables/useDiagnosticoContexto';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';

/**
 * Composable de situação de capacitação do diagnóstico da unidade.
 * - Query: carrega dados completos da unidade (servidores + situações + movimentações).
 * - Mutation de salvar: autosave com debounce de 800ms.
 */
export function useSituacaoCapacitacaoDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);
    const {habilitarCriarConsenso} = useDiagnosticoPermissoes(codSubprocesso);

    const query = useQuery<DiagnosticoUnidade>({
        key: () => chaveUnidade(codSubprocesso, contextoSessao),
        query: () => obterDiagnosticoUnidade(codSubprocesso),
        enabled: () => habilitarQueryDiagnostico(perfilStore, codSubprocesso),
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
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
            const diagnosticoAtual = query.data.value;
            if (!diagnosticoAtual) {
                return;
            }

            cache.setQueryData(
                chaveUnidade(codSubprocesso, contextoSessao),
                {
                    ...diagnosticoAtual,
                    situacoesCapacitacao: situacoesLocais.value.map((item) => ({
                        servidorTitulo: item.servidorTitulo,
                        servidorNome: item.servidorNome,
                        competenciaCodigo: item.competenciaCodigo,
                        situacaoCapacitacao: item.situacaoCapacitacao,
                    })),
                } satisfies DiagnosticoUnidade,
            );
        },
    });

    // Autosave com debounce nativo
    let timer: ReturnType<typeof setTimeout> | null = null;
    const dispararSalvamento = () => {
        if (!habilitarCriarConsenso.value) return;
        if (timer !== null) clearTimeout(timer);
        timer = setTimeout(() => { mutacaoSalvar.mutate(situacoesLocais.value); }, DEBOUNCE_AUTOSAVE_PADRAO_MS);
    };

    function atualizarCapacitacao(
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: ValorSituacaoCapacitacao | null,
    ) {
        if (!habilitarCriarConsenso.value) return;
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
        habilitarCriarConsenso,
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

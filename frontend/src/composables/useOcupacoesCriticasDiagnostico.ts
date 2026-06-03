import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {obterDiagnosticoUnidade, salvarOcupacoesCriticas} from '@/services/diagnosticoService';
import type {DiagnosticoUnidade, OcupacaoCriticaItem, SituacaoCapacitacao} from '@/types/diagnostico-competencias';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

function chaveUnidade(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'unidade', codSubprocesso] as const;
}

/**
 * Composable de ocupações críticas do diagnóstico da unidade.
 * - Query: carrega dados completos da unidade (servidores + ocupações + movimentações).
 * - Mutation de salvar: autosave com debounce de 800ms.
 */
export function useOcupacoesCriticasDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();

    const query = useQuery<DiagnosticoUnidade>({
        key: () => chaveUnidade(codSubprocesso),
        query: () => obterDiagnosticoUnidade(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });

    const ocupacoesLocais = ref<OcupacaoCriticaItem[]>([]);
    const salvandoAutomaticamente = ref(false);
    const autoguardado = ref(false);

    watch(
        () => query.data.value?.ocupacoesCriticas,
        (novas) => {
            if (novas) {
                ocupacoesLocais.value = novas.map((o) => ({...o}));
            }
        },
        {immediate: true},
    );

    const mutacaoSalvar = useMutation({
        mutation: (ocupacoes: OcupacaoCriticaItem[]) =>
            salvarOcupacoesCriticas(codSubprocesso, {
                ocupacoes: ocupacoes.map((o) => ({
                    servidorTitulo: o.servidorTitulo,
                    competenciaCodigo: o.competenciaCodigo,
                    situacaoCapacitacao: o.situacaoCapacitacao,
                })),
            }),
        onSettled: () => {
            salvandoAutomaticamente.value = false;
        },
        onSuccess: () => {
            autoguardado.value = true;
            setTimeout(() => {
                autoguardado.value = false;
            }, 2000);
            void cache.invalidateQueries({key: chaveUnidade(codSubprocesso)});
        },
    });

    // Autosave com debounce nativo
    let timer: ReturnType<typeof setTimeout> | null = null;
    const dispararSalvamento = () => {
        if (timer !== null) clearTimeout(timer);
        timer = setTimeout(() => { mutacaoSalvar.mutate(ocupacoesLocais.value); }, 800);
    };

    function atualizarCapacitacao(
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: SituacaoCapacitacao,
    ) {
        const item = ocupacoesLocais.value.find(
            (o) => o.servidorTitulo === servidorTitulo && o.competenciaCodigo === competenciaCodigo,
        );
        if (!item) return;
        item.situacaoCapacitacao = situacao;
        salvandoAutomaticamente.value = true;
        autoguardado.value = false;
        dispararSalvamento();
    }

    const servidores = computed(() => query.data.value?.servidores ?? []);
    const unidade = computed(() => query.data.value?.unidade);
    const movimentacoes = computed(() => query.data.value?.movimentacoes ?? []);
    const carregando = computed(() => query.status.value === 'pending');
    const erro = computed(() => query.error.value);
    const pendentes = computed(
        () => ocupacoesLocais.value.filter((o) => !o.situacaoCapacitacao).length,
    );

    return {
        query,
        ocupacoesLocais,
        servidores,
        unidade,
        movimentacoes,
        carregando,
        erro,
        salvandoAutomaticamente,
        autoguardado,
        pendentes,
        atualizarCapacitacao,
    };
}

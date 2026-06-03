import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {
    concluirAutoavaliacao,
    obterAutoavaliacao,
    salvarAutoavaliacao,
} from '@/services/diagnosticoService';
import type {AvaliacaoCompetencia, Autoavaliacao} from '@/types/diagnostico-competencias';
import {CHAVE_DIAGNOSTICO} from '@/composables/useDiagnosticoContexto';

function chaveAutoavaliacao(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'autoavaliacao', codSubprocesso] as const;
}

function chaveEquipe(codSubprocesso: number) {
    return [CHAVE_DIAGNOSTICO, 'equipe', codSubprocesso] as const;
}

/**
 * Composable de autoavaliação do servidor logado.
 * - Query: carrega competências e situação.
 * - Mutation de salvar: acionada por autosave com debounce de 800ms.
 * - Mutation de concluir: finaliza a autoavaliação.
 */
export function useAutoavaliacaoDiagnostico(codSubprocesso: number) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();

    const query = useQuery<Autoavaliacao>({
        key: () => chaveAutoavaliacao(codSubprocesso),
        query: () => obterAutoavaliacao(codSubprocesso),
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });

    // Estado local de edição para evitar chamadas de rede a cada keypress
    const competenciasLocais = ref<AvaliacaoCompetencia[]>([]);
    const salvandoAutomaticamente = ref(false);
    const autoguardado = ref(false);

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
        mutation: (competencias: AvaliacaoCompetencia[]) =>
            salvarAutoavaliacao(codSubprocesso, {competencias}),
        onSettled: () => {
            salvandoAutomaticamente.value = false;
        },
        onSuccess: () => {
            autoguardado.value = true;
            setTimeout(() => {
                autoguardado.value = false;
            }, 2000);
        },
    });

    const mutacaoConcluir = useMutation({
        mutation: () => concluirAutoavaliacao(codSubprocesso),
        onSuccess: () => {
            void cache.invalidateQueries({key: chaveAutoavaliacao(codSubprocesso)});
            void cache.invalidateQueries({key: chaveEquipe(codSubprocesso)});
        },
    });

    // Autosave com debounce nativo
    let timer: ReturnType<typeof setTimeout> | null = null;
    function dispararSalvamento() {
        if (timer !== null) clearTimeout(timer);
        timer = setTimeout(() => { mutacaoSalvar.mutate(competenciasLocais.value); }, 800);
    }

    function atualizarNota(competenciaCodigo: number, campo: 'importancia' | 'dominio', valor: number | null) {
        const item = competenciasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
        if (!item) return;
        item[campo] = valor;
        salvandoAutomaticamente.value = true;
        autoguardado.value = false;
        dispararSalvamento();
    }

    const situacaoServidor = computed(() => query.data.value?.situacaoServidor ?? 'AUTOAVALIACAO_NAO_REALIZADA');
    const carregando = computed(() => query.status.value === 'pending');
    const erro = computed(() => query.error.value);
    const concluindo = computed(() => mutacaoConcluir.isLoading.value);
    const erroConcluir = computed(() => mutacaoConcluir.error.value);

    return {
        query,
        competenciasLocais,
        situacaoServidor,
        carregando,
        erro,
        salvandoAutomaticamente,
        autoguardado,
        concluindo,
        erroConcluir,
        atualizarNota,
        concluirAutoavaliacao: () => mutacaoConcluir.mutateAsync(),
    };
}

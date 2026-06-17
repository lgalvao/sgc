import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {
    aprovarConsenso,
    obterConsenso,
    obterConsensoServidor,
    salvarConsenso,
} from '@/services/diagnosticoService';
import type {Consenso, ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';
import {
    chaveAutoavaliacao,
    chaveConsenso,
    chaveEquipe,
    criarContextoSessaoDiagnostico,
} from '@/composables/useDiagnosticoContexto';

/**
 * Composable de consenso de diagnóstico.
 * - Query do consenso do servidor logado (ou de um servidor específico passado pela chefia).
 * - Autosave: qualquer edição dispara salvamento após debounce de 800ms.
 *   O autosave só ocorre quando o consenso NÃO está aprovado (CDU-44).
 * - Mutation de aprovar consenso (para o servidor logado).
 */
export function useConsensoDiagnostico(codSubprocesso: number, servidorTitulo?: string) {
    const perfilStore = usePerfilStore();
    const cache = useQueryCache();
    const contextoSessao = criarContextoSessaoDiagnostico(perfilStore);
    const consensoDoServidorLogado = computed(() =>
        servidorTitulo != null && String(servidorTitulo) === String(perfilStore.usuarioCodigo ?? '')
    );

    const query = useQuery<Consenso>({
        key: () => chaveConsenso(codSubprocesso, contextoSessao, servidorTitulo),
        query: () => servidorTitulo && !consensoDoServidorLogado.value
            ? obterConsensoServidor(codSubprocesso, servidorTitulo)
            : obterConsenso(codSubprocesso),
        enabled: () => codSubprocesso > 0,
        staleTime: Infinity,
    });

    // Estado local editável pela chefia
    const competenciasLocais = ref<ConsensoCompetenciaDetalhada[]>([]);

    // Estado de autosave
    const salvandoAutomaticamente = ref(false);
    let timer: ReturnType<typeof setTimeout> | null = null;

    watch(
        () => query.data.value,
        (novoConsenso) => {
            if (novoConsenso) {
                if (salvandoAutomaticamente.value) {
                    return;
                }
                competenciasLocais.value = novoConsenso.competencias.map((c) => ({...c}));
            }
        },
        {immediate: true},
    );

    const situacaoServidor = computed(() => query.data.value?.situacaoServidor ?? 'AUTOAVALIACAO_NAO_INICIADA');
    const podeEditar = computed(() => query.data.value?.podeEditar ?? false);
    const podeConcluirAvaliacao = computed(() => query.data.value?.podeConcluirAvaliacao ?? false);
    const habilitarConcluirAvaliacao = computed(() => query.data.value?.habilitarConcluirAvaliacao ?? false);
    const podeAprovarConsenso = computed(() => query.data.value?.podeAprovarConsenso ?? false);
    const habilitarAprovarConsenso = computed(() => query.data.value?.habilitarAprovarConsenso ?? false);
    const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');

    const mutacaoSalvar = useMutation({
        mutation: (titulo: string) =>
            salvarConsenso(codSubprocesso, titulo, {
                competencias: competenciasLocais.value.map((c) => ({...c})),
            }),
        onSettled: () => {
            salvandoAutomaticamente.value = false;
        },
        onSuccess: () => {
            const anterior = cache.getQueryData<Consenso>(chaveConsenso(codSubprocesso, contextoSessao, servidorTitulo));
            cache.setQueryData(
                chaveConsenso(codSubprocesso, contextoSessao, servidorTitulo),
                {
                    competencias: competenciasLocais.value.map((c) => ({...c})),
                    situacaoServidor: 'CONSENSO_CRIADO',
                    podeEditar: anterior?.podeEditar ?? false,
                    podeConcluirAvaliacao: anterior?.podeConcluirAvaliacao ?? false,
                    habilitarConcluirAvaliacao: anterior?.habilitarConcluirAvaliacao ?? false,
                    podeAprovarConsenso: anterior?.podeAprovarConsenso ?? false,
                    habilitarAprovarConsenso: anterior?.habilitarAprovarConsenso ?? false,
                } satisfies Consenso,
            );
            invalidarQueriesConsenso({cache, codSubprocesso, contextoSessao, servidorTitulo});
        },
    });

    const mutacaoAprovar = useMutation({
        mutation: () => aprovarConsenso(codSubprocesso),
        onSuccess: () => {
            invalidarQueriesAprovar({cache, codSubprocesso, contextoSessao, servidorTitulo});
        },
    });

    /** Dispara autosave com debounce. Só salva se não estiver aprovado. */
    function agendarAutosave() {
        if (ehConsensoAprovado.value) return;
        salvandoAutomaticamente.value = true;
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => {
            void salvarConsensoAgora();
        }, 800);
    }

    async function salvarConsensoAgora() {
        if (ehConsensoAprovado.value || !servidorTitulo) return;
        if (timer) {
            clearTimeout(timer);
            timer = null;
        }
        salvandoAutomaticamente.value = true;
        await mutacaoSalvar.mutateAsync(servidorTitulo);
    }

    function atualizarNotaDetalhada(
        competenciaCodigo: number,
        atualizacao: {
            origem: 'chefia' | 'consenso';
            campo: 'importancia' | 'dominio';
            valor: number | null;
        },
    ) {
        const item = competenciasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
        if (!item) return;

        if (atualizacao.origem === 'chefia') {
            if (atualizacao.campo === 'importancia') item.chefiaImportancia = atualizacao.valor;
            if (atualizacao.campo === 'dominio') item.chefiaDominio = atualizacao.valor;

            // Autopreenchimento do consenso quando chefia coincide com servidor (CDU-44)
            const chefiaImportancia = atualizacao.campo === 'importancia' ? atualizacao.valor : item.chefiaImportancia;
            const chefiaDominio = atualizacao.campo === 'dominio' ? atualizacao.valor : item.chefiaDominio;
            if (item.autoimportancia === chefiaImportancia && item.autodominio === chefiaDominio) {
                item.consensoImportancia = chefiaImportancia;
                item.consensoDominio = chefiaDominio;
            }
        } else {
            if (atualizacao.campo === 'importancia') item.consensoImportancia = atualizacao.valor;
            if (atualizacao.campo === 'dominio') item.consensoDominio = atualizacao.valor;
        }

        agendarAutosave();
    }

    const carregando = computed(() => query.status.value === 'pending');
    const aprovando = computed(() => mutacaoAprovar.isLoading.value);

    return {
        query,
        competenciasLocais,
        situacaoServidor,
        podeEditar,
        podeConcluirAvaliacao,
        habilitarConcluirAvaliacao,
        podeAprovarConsenso,
        habilitarAprovarConsenso,
        ehConsensoAprovado,
        carregando,
        salvandoAutomaticamente,
        aprovando,
        erroAprovar: computed(() => mutacaoAprovar.error.value),
        atualizarNotaDetalhada,
        salvarConsensoAgora,
        aprovarConsenso: () => mutacaoAprovar.mutateAsync(),
    };
}

interface InvalidaQueriesContext {
    cache: ReturnType<typeof useQueryCache>;
    codSubprocesso: number;
    contextoSessao: ReturnType<typeof criarContextoSessaoDiagnostico>;
    servidorTitulo?: string;
}

function invalidarQueriesConsenso(ctx: InvalidaQueriesContext) {
    void ctx.cache.invalidateQueries({key: chaveConsenso(ctx.codSubprocesso, ctx.contextoSessao, ctx.servidorTitulo), exact: true});
    void ctx.cache.invalidateQueries({key: chaveEquipe(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
}

function invalidarQueriesAprovar(ctx: InvalidaQueriesContext) {
    void ctx.cache.invalidateQueries({key: chaveConsenso(ctx.codSubprocesso, ctx.contextoSessao, ctx.servidorTitulo), exact: true});
    void ctx.cache.invalidateQueries({key: chaveEquipe(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
    void ctx.cache.invalidateQueries({key: chaveAutoavaliacao(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
}

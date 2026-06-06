import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {
    aprovarConsenso,
    obterConsenso,
    obterConsensoServidor,
    salvarConsenso,
} from '@/services/diagnosticoService';
import type {AvaliacaoCompetencia, Consenso, ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';
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
        enabled: () => !!perfilStore.usuarioCodigo && codSubprocesso > 0,
        staleTime: Infinity,
    });

    // Estado local editável pela chefia
    const competenciasLocais = ref<AvaliacaoCompetencia[]>([]);
    const competenciasDetalhadasLocais = ref<ConsensoCompetenciaDetalhada[]>([]);

    // Estado de autosave
    const salvandoAutomaticamente = ref(false);
    let timer: ReturnType<typeof setTimeout> | null = null;

    watch(
        () => query.data.value,
        (novoConsenso) => {
            if (novoConsenso) {
                competenciasLocais.value = novoConsenso.competencias.map((c) => ({...c}));
                competenciasDetalhadasLocais.value = (novoConsenso.competenciasDetalhadas ?? []).map((c) => ({...c}));
            }
        },
        {immediate: true},
    );

    const situacaoServidor = computed(() => query.data.value?.situacaoServidor ?? 'AUTOAVALIACAO_NAO_INICIADA');
    const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');

        const mutacaoSalvar = useMutation({
        mutation: (titulo: string) =>
            salvarConsenso(codSubprocesso, titulo, {
                competencias: obterCompetenciasPayload(competenciasDetalhadasLocais.value, competenciasLocais.value),
                competenciasDetalhadas: competenciasDetalhadasLocais.value.length > 0
                    ? competenciasDetalhadasLocais.value
                    : undefined,
            }),
        onSettled: () => {
            salvandoAutomaticamente.value = false;
        },
        onSuccess: () => {
            cache.setQueryData(
                chaveConsenso(codSubprocesso, contextoSessao, servidorTitulo),
                {
                    competencias: obterCompetenciasPayload(competenciasDetalhadasLocais.value, competenciasLocais.value),
                    competenciasDetalhadas: obterDetalhadasCopia(competenciasDetalhadasLocais.value),
                    situacaoServidor: 'CONSENSO_CRIADO',
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
            if (servidorTitulo) {
                mutacaoSalvar.mutate(servidorTitulo);
            }
        }, 800);
    }

    function atualizarNota(competenciaCodigo: number, campo: 'importancia' | 'dominio', valor: number | null) {
        const item = competenciasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
        if (item) {
            item[campo] = valor;
            agendarAutosave();
        }
    }

    function atualizarNotaDetalhada(
        competenciaCodigo: number,
        atualizacao: {
            origem: 'chefia' | 'consenso';
            campo: 'importancia' | 'dominio';
            valor: number | null;
        },
    ) {
        const item = competenciasDetalhadasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
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

        // Sincroniza lista simples com o consenso final
        const simples = competenciasLocais.value.find((c) => c.competenciaCodigo === competenciaCodigo);
        if (simples) {
            simples.importancia = item.consensoImportancia;
            simples.dominio = item.consensoDominio;
        }

        agendarAutosave();
    }

    const carregando = computed(() => query.status.value === 'pending');
    const aprovando = computed(() => mutacaoAprovar.isLoading.value);

    return {
        query,
        competenciasLocais,
        competenciasDetalhadasLocais,
        situacaoServidor,
        ehConsensoAprovado,
        carregando,
        salvandoAutomaticamente,
        aprovando,
        erroAprovar: computed(() => mutacaoAprovar.error.value),
        atualizarNota,
        atualizarNotaDetalhada,
        aprovarConsenso: () => mutacaoAprovar.mutateAsync(),
    };
}

function obterCompetenciasPayload(
    detalhadas: ConsensoCompetenciaDetalhada[],
    simples: AvaliacaoCompetencia[],
): AvaliacaoCompetencia[] {
    return detalhadas.length > 0
        ? detalhadas.map((item) => ({
            competenciaCodigo: item.competenciaCodigo,
            importancia: item.consensoImportancia,
            dominio: item.consensoDominio,
        }))
        : simples.map((item) => ({...item}));
}

function obterDetalhadasCopia(detalhadas: ConsensoCompetenciaDetalhada[]): ConsensoCompetenciaDetalhada[] | undefined {
    return detalhadas.length > 0 ? detalhadas.map((item) => ({...item})) : undefined;
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

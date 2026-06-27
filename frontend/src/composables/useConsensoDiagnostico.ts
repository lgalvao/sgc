import {useMutation, useQuery, useQueryCache} from '@pinia/colada';
import {computed, ref, watch} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {
    aprovarConsenso,
    concluirConsenso,
    obterConsenso,
    obterConsensoServidor,
    salvarConsenso,
} from '@/services/diagnosticoService';
import type {Consenso, ConsensoCompetenciaDetalhada} from '@/types/diagnostico-competencias';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
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
    const {podeCriarConsenso, queryContextoEdicao} = useDiagnosticoPermissoes(codSubprocesso);
    const carregandoPermissoes = computed(() => queryContextoEdicao.status.value === 'pending');
    const deveConsultarConsensoServidor = computed(() =>
        Boolean(servidorTitulo) && podeCriarConsenso.value
    );
    const chaveConsultaConsenso = computed(() => servidorTitulo
        ? (deveConsultarConsensoServidor.value ? servidorTitulo : 'usuario-logado')
        : 'usuario-logado',
    );
    const podeCarregarConsenso = computed(() =>
        codSubprocesso > 0
        && !!perfilStore.usuarioCodigo
        && (!servidorTitulo || !carregandoPermissoes.value),
    );

    const query = useQuery<Consenso>({
        key: () => chaveConsenso(codSubprocesso, contextoSessao, chaveConsultaConsenso.value),
        query: () => deveConsultarConsensoServidor.value
            ? obterConsensoServidor(codSubprocesso, servidorTitulo!)
            : obterConsenso(codSubprocesso),
        enabled: () => podeCarregarConsenso.value,
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
                competenciasLocais.value = novoConsenso.competencias.map(normalizarCompetenciaConsenso);
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
    const obterChaveConsensoAtual = () => chaveConsenso(codSubprocesso, contextoSessao, chaveConsultaConsenso.value);

    const {mutacaoSalvar, mutacaoConcluir, mutacaoAprovar} = criarMutacoesConsenso({
        cache,
        codSubprocesso,
        contextoSessao,
        servidorTitulo,
        obterChaveConsensoAtual,
        obterCompetenciasLocais: () => competenciasLocais.value,
        finalizarAutosave: () => {
            salvandoAutomaticamente.value = false;
        },
        obterChaveConsultaConsenso: () => chaveConsultaConsenso.value,
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
            aplicarAutopreenchimentoConsenso(item);
        } else {
            if (atualizacao.campo === 'importancia') item.consensoImportancia = atualizacao.valor;
            if (atualizacao.campo === 'dominio') item.consensoDominio = atualizacao.valor;
        }

        agendarAutosave();
    }

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
        carregando: computed(() => query.status.value === 'pending'),
        carregandoPermissoes,
        salvandoAutomaticamente,
        concluindo: computed(() => mutacaoConcluir.isLoading.value),
        aprovando: computed(() => mutacaoAprovar.isLoading.value),
        erroConcluir: computed(() => mutacaoConcluir.error.value),
        erroAprovar: computed(() => mutacaoAprovar.error.value),
        atualizarNotaDetalhada,
        salvarConsensoAgora,
        concluirAvaliacao: () => servidorTitulo ? mutacaoConcluir.mutateAsync(servidorTitulo) : Promise.resolve(),
        aprovarConsenso: () => mutacaoAprovar.mutateAsync(),
    };
}

interface InvalidaQueriesContext {
    cache: ReturnType<typeof useQueryCache>;
    codSubprocesso: number;
    contextoSessao: ReturnType<typeof criarContextoSessaoDiagnostico>;
    servidorTitulo?: string;
    chaveConsultaConsenso?: string;
}

type CriarMutacoesConsensoParams = {
    cache: ReturnType<typeof useQueryCache>;
    codSubprocesso: number;
    contextoSessao: ReturnType<typeof criarContextoSessaoDiagnostico>;
    servidorTitulo?: string;
    obterChaveConsensoAtual: () => ReturnType<typeof chaveConsenso>;
    obterCompetenciasLocais: () => ConsensoCompetenciaDetalhada[];
    finalizarAutosave: () => void;
    obterChaveConsultaConsenso: () => string;
};

function criarMutacoesConsenso(params: CriarMutacoesConsensoParams) {
    const mutacaoSalvar = useMutation({
        mutation: (titulo: string) =>
            salvarConsenso(params.codSubprocesso, titulo, {
                competencias: clonarCompetencias(params.obterCompetenciasLocais()),
            }),
        onSettled: params.finalizarAutosave,
        onSuccess: () => {
            atualizarCacheAposSalvar(params);
            invalidarQueriesConsenso(params);
        },
    });

    const mutacaoConcluir = useMutation({
        mutation: (titulo: string) => concluirConsenso(params.codSubprocesso, titulo),
        onSuccess: () => {
            atualizarCacheAposConcluir(params);
            invalidarQueriesConsenso(params);
            void params.cache.invalidateQueries({key: chaveAutoavaliacao(params.codSubprocesso, params.contextoSessao), exact: true});
        },
    });

    const mutacaoAprovar = useMutation({
        mutation: () => aprovarConsenso(params.codSubprocesso),
        onSuccess: () => {
            atualizarCacheAposAprovar(params);
            invalidarQueriesAprovar({
                ...params,
                chaveConsultaConsenso: params.obterChaveConsultaConsenso(),
            });
        },
    });

    return {mutacaoSalvar, mutacaoConcluir, mutacaoAprovar};
}

function atualizarCacheAposSalvar(params: CriarMutacoesConsensoParams) {
    const anterior = obterConsensoAnterior(params);
    const estadoAnterior = obterEstadoAnteriorConsenso(anterior);
    params.cache.setQueryData(
        params.obterChaveConsensoAtual(),
        {
            competencias: clonarCompetencias(params.obterCompetenciasLocais()),
            ...estadoAnterior,
        } satisfies Consenso,
    );
}

function atualizarCacheAposConcluir(params: CriarMutacoesConsensoParams) {
    const anterior = obterConsensoAnterior(params);
    if (!anterior) {
        return;
    }
    params.cache.setQueryData(
        params.obterChaveConsensoAtual(),
        {
            ...anterior,
            situacaoServidor: 'CONSENSO_CRIADO',
            podeEditar: false,
            podeConcluirAvaliacao: true,
            habilitarConcluirAvaliacao: false,
            podeAprovarConsenso: anterior.podeAprovarConsenso,
            habilitarAprovarConsenso: anterior.habilitarAprovarConsenso,
        } satisfies Consenso,
    );
}

function atualizarCacheAposAprovar(params: CriarMutacoesConsensoParams) {
    const anterior = obterConsensoAnterior(params);
    if (!anterior) {
        return;
    }
    params.cache.setQueryData(
        params.obterChaveConsensoAtual(),
        {
            ...anterior,
            situacaoServidor: 'CONSENSO_APROVADO',
            podeEditar: false,
            podeConcluirAvaliacao: false,
            habilitarConcluirAvaliacao: false,
            podeAprovarConsenso: true,
            habilitarAprovarConsenso: false,
        } satisfies Consenso,
    );
}

function obterConsensoAnterior(params: CriarMutacoesConsensoParams) {
    return params.cache.getQueryData<Consenso>(params.obterChaveConsensoAtual());
}

function obterEstadoAnteriorConsenso(anterior?: Consenso) {
    const estadoPadrao = {
        situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
        podeEditar: false,
        podeConcluirAvaliacao: false,
        habilitarConcluirAvaliacao: false,
        podeAprovarConsenso: false,
        habilitarAprovarConsenso: false,
    } satisfies Omit<Consenso, "competencias">;
    if (!anterior) {
        return estadoPadrao;
    }
    return {
        ...estadoPadrao,
        ...anterior,
    } satisfies Omit<Consenso, "competencias">;
}

function clonarCompetencias(competencias: ConsensoCompetenciaDetalhada[]) {
    return competencias.map((competencia) => ({...competencia}));
}

function invalidarQueriesConsenso(ctx: InvalidaQueriesContext) {
    void ctx.cache.invalidateQueries({key: chaveConsenso(ctx.codSubprocesso, ctx.contextoSessao, ctx.servidorTitulo), exact: true});
    void ctx.cache.invalidateQueries({key: chaveEquipe(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
}

function invalidarQueriesAprovar(ctx: InvalidaQueriesContext) {
    void ctx.cache.invalidateQueries({key: chaveConsenso(ctx.codSubprocesso, ctx.contextoSessao, ctx.chaveConsultaConsenso ?? ctx.servidorTitulo), exact: true});
    void ctx.cache.invalidateQueries({key: chaveEquipe(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
    void ctx.cache.invalidateQueries({key: chaveAutoavaliacao(ctx.codSubprocesso, ctx.contextoSessao), exact: true});
}

function normalizarCompetenciaConsenso(competencia: ConsensoCompetenciaDetalhada): ConsensoCompetenciaDetalhada {
    const item: ConsensoCompetenciaDetalhada = {...competencia};
    aplicarAutopreenchimentoConsenso(item);
    return item;
}

function aplicarAutopreenchimentoConsenso(item: ConsensoCompetenciaDetalhada) {
    item.consensoImportancia = obterValorAutopreenchidoConsenso(
        item.servidorImportancia,
        item.chefiaImportancia,
        item.consensoImportancia,
    );
    item.consensoDominio = obterValorAutopreenchidoConsenso(
        item.servidorDominio,
        item.chefiaDominio,
        item.consensoDominio,
    );
}

function obterValorAutopreenchidoConsenso(
    valorServidor: number | null,
    valorChefia: number | null,
    valorConsenso: number | null,
): number | null {
    if (valorChefia === null) return null;
    if (valorConsenso !== null) return valorConsenso;
    if (valorServidor !== valorChefia) return valorConsenso;
    return valorChefia;
}

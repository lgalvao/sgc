import {computed, ref, watch} from 'vue';
import {useQuery} from '@pinia/colada';
import type {ColorVariant} from 'bootstrap-vue-next';
import {STALE_TIME_LEITURA_AUXILIAR} from '@/composables/cachePolicy';
import {possuiCodSubprocessoValido} from '@/composables/diagnosticoQueryUtils';
import {useDiagnosticoUnidade} from '@/composables/useDiagnosticoUnidade';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {listarAnalisesDiagnostico} from '@/services/analiseService';
import {buscarSubprocessoDetalhe} from '@/services/subprocessoServiceContexto';
import {formatSituacaoSubprocesso} from '@/utils/formatters';
import {TEXTOS} from '@/constants/textos';
import {useToast} from '@/composables/useToast';
import type {Analise, Movimentacao, SubprocessoDetalhe} from '@/types/tipos';
import type {
    DiagnosticoContexto,
    ValorSituacaoCapacitacao,
} from '@/types/diagnostico-competencias';
import {useAsyncAction} from '@/composables/useAsyncAction';
import {
    formatarNota,
    formatarSituacaoCapacitacao,
    formatarSituacaoCapacitacaoResumida,
    formatarSituacaoServidor,
    formatTipoResponsabilidade,
    formatDataSimples,
    varianteSituacaoServidor,
} from '@/views/diagnosticoUnidadeViewUtils';

type RetornoFluxo = { mensagem: string; variante: 'danger' };

interface DiagnosticoUnidadeViewProps {
    codSubprocesso: number;
    siglaUnidade: string;
}

interface CompetenciaServidorSelecionado {
    competenciaCodigo: number;
    competenciaDescricao: string;
    importancia: number | null;
    dominio: number | null;
    situacaoCapacitacao: ValorSituacaoCapacitacao | null;
}

export function useDiagnosticoUnidadeView(props: DiagnosticoUnidadeViewProps) {
    const {exibirSucesso} = useToast();
    const contextoQuery = useDiagnosticoContexto(props.codSubprocesso);
    const {data: contexto} = contextoQuery;
    const {
        queryPermissoes,
        podeValidarDiagnostico,
        podeDevolverDiagnostico,
        podeHomologarDiagnostico,
        habilitarValidarDiagnostico,
        habilitarDevolverDiagnostico,
        habilitarHomologarDiagnostico,
    } = useDiagnosticoPermissoes(props.codSubprocesso);
    const querySubprocessoDetalhe = useQuery({
        key: () => ['subprocesso-detalhes-diagnostico', props.codSubprocesso] as const,
        query: () => buscarSubprocessoDetalhe(props.codSubprocesso),
        enabled: () => possuiCodSubprocessoValido(props.codSubprocesso),
        staleTime: STALE_TIME_LEITURA_AUXILIAR,
    });
    const subprocessoDetalhe = computed(() => querySubprocessoDetalhe.data.value ?? null);

    const {
        unidade,
        servidores,
        situacoesCapacitacao,
        movimentacoes,
        carregando: carregandoUnidade,
        situacao,
    } = useDiagnosticoUnidade(props.codSubprocesso);

    const {
        validando,
        devolvendo,
        homologando,
        erroValidar,
        erroValidacaoValidar,
        erroDevolver,
        erroValidacaoDevolver,
        erroHomologar,
        erroValidacaoHomologar,
        validarAcaoValidarDiagnostico,
        validarAcaoDevolverDiagnostico,
        validarAcaoHomologarDiagnostico,
        validarDiagnostico,
        devolverDiagnostico,
        homologarDiagnostico,
    } = useFluxoDiagnostico(props.codSubprocesso);

    const retornoFluxo = ref<RetornoFluxo | null>(null);
    const acaoTela = useAsyncAction();
    const modalValidarAberto = ref(false);
    const modalDevolverAberto = ref(false);
    const modalHomologarAberto = ref(false);
    const observacoesValidar = ref('');
    const justificativaDevolver = ref('');
    const observacoesHomologar = ref('');
    const tentouDevolverSemJustificativa = ref(false);
    const modalHistoricoAberto = ref(false);
    const carregandoHistorico = ref(false);
    const historicoAnalises = ref<Analise[]>([]);

    const podeValidar = computed(() => podeValidarDiagnostico.value);
    const podeDevolver = computed(() => podeDevolverDiagnostico.value);
    const podeHomologar = computed(() => podeHomologarDiagnostico.value);
    const habilitarValidar = computed(() => habilitarValidarDiagnostico.value);
    const habilitarDevolver = computed(() => habilitarDevolverDiagnostico.value);
    const habilitarHomologar = computed(() => habilitarHomologarDiagnostico.value);
    const carregando = computed(() =>
        carregandoUnidade.value
        || contextoQuery.isPending.value
        || contextoQuery.isLoading.value
        || queryPermissoes.isPending.value
        || queryPermissoes.isLoading.value
        || querySubprocessoDetalhe.isPending.value
        || querySubprocessoDetalhe.isLoading.value,
    );
    const contextoObrigatorio = computed<DiagnosticoContexto>(() => {
        const valor = contexto.value;
        if (
            !valor
            || valor.processoCodigo == null
            || valor.subprocessoCodigo == null
            || valor.unidadeSigla == null
            || valor.unidadeNome == null
        ) {
            throw new Error('Contexto de diagnóstico incompleto: processo, subprocesso e unidade são obrigatórios.');
        }
        return valor;
    });
    const subprocessoDetalheObrigatorio = computed<SubprocessoDetalhe>(() => {
        const valor = subprocessoDetalhe.value;
        if (
            !valor
            || !valor.processoDescricao
            || !valor.unidade?.sigla
            || !valor.unidade?.nome
            || !valor.localizacaoAtual
            || !valor.titular?.nome
        ) {
            throw new Error('Contexto de subprocesso incompleto: processo, unidade, localização e titular são obrigatórios.');
        }
        return valor;
    });

    async function abrirModalValidar() {
        observacoesValidar.value = '';
        await acaoTela.executar(
            () => validarAcaoValidarDiagnostico(),
            erroValidacaoValidar.value?.message || TEXTOS.diagnostico.ERRO_SALVAR,
            {
                relancarErro: false,
                aoSucesso: () => {
                    modalValidarAberto.value = true;
                },
                aoOcorrerErro: (erro) => {
                    registrarErro(erro.mensagem ?? erroValidacaoValidar.value?.message);
                },
            },
        );
    }

    async function abrirModalDevolver() {
        justificativaDevolver.value = '';
        tentouDevolverSemJustificativa.value = false;
        await acaoTela.executar(
            () => validarAcaoDevolverDiagnostico(),
            erroValidacaoDevolver.value?.message || TEXTOS.diagnostico.ERRO_SALVAR,
            {
                relancarErro: false,
                aoSucesso: () => {
                    modalDevolverAberto.value = true;
                },
                aoOcorrerErro: (erro) => {
                    registrarErro(erro.mensagem ?? erroValidacaoDevolver.value?.message);
                },
            },
        );
    }

    async function abrirModalHomologar() {
        observacoesHomologar.value = '';
        await acaoTela.executar(
            () => validarAcaoHomologarDiagnostico(),
            erroValidacaoHomologar.value?.message || TEXTOS.diagnostico.ERRO_SALVAR,
            {
                relancarErro: false,
                aoSucesso: () => {
                    modalHomologarAberto.value = true;
                },
                aoOcorrerErro: (erro) => {
                    registrarErro(erro.mensagem ?? erroValidacaoHomologar.value?.message);
                },
            },
        );
    }

    function limparRetornoFluxo() {
        retornoFluxo.value = null;
    }

    async function abrirHistoricoAnalise() {
        carregandoHistorico.value = true;
        modalHistoricoAberto.value = true;
        try {
            const resultado = await acaoTela.executar(
                () => listarAnalisesDiagnostico(props.codSubprocesso),
                TEXTOS.diagnostico.ERRO_SALVAR,
                {relancarErro: false},
            );
            if (resultado === undefined) {
                registrarErro(TEXTOS.diagnostico.ERRO_SALVAR);
            } else {
                historicoAnalises.value = resultado;
            }
        } finally {
            carregandoHistorico.value = false;
        }
    }

    function registrarRetornoFluxo(mensagem?: string | null) {
        retornoFluxo.value = {
            variante: 'danger',
            mensagem: mensagem?.trim() || TEXTOS.diagnostico.ERRO_SALVAR,
        };
    }

    function registrarErro(mensagem?: string | null) {
        registrarRetornoFluxo(mensagem);
    }

    function normalizarTextoOpcional(texto: string) {
        const textoLimpo = texto.trim();
        return textoLimpo.length > 0 ? textoLimpo : undefined;
    }

    const mensagemErroJustificativaDevolver = computed(() =>
        tentouDevolverSemJustificativa.value
            ? TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA
            : '',
    );

    async function executarAcaoFluxo(
        config: {
            acao: () => Promise<void>;
            aoConcluir: () => void;
            mensagemErro?: string | null;
        },
    ) {
        await acaoTela.executar(
            config.acao,
            config.mensagemErro || TEXTOS.diagnostico.ERRO_SALVAR,
            {
                relancarErro: false,
                aoSucesso: () => {
                    config.aoConcluir();
                },
                aoOcorrerErro: (erro) => {
                    registrarErro(config.mensagemErro || erro.mensagem);
                },
            },
        );
    }

    async function confirmarValidar() {
        await executarAcaoFluxo({
            acao: () => validarDiagnostico(normalizarTextoOpcional(observacoesValidar.value)),
            aoConcluir: () => {
                modalValidarAberto.value = false;
                exibirSucesso(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_VALIDADO);
            },
            mensagemErro: erroValidar.value?.message,
        });
    }

    async function confirmarDevolver() {
        const justificativa = normalizarTextoOpcional(justificativaDevolver.value);
        if (!justificativa) {
            tentouDevolverSemJustificativa.value = true;
            return;
        }
        tentouDevolverSemJustificativa.value = false;
        await executarAcaoFluxo({
            acao: () => devolverDiagnostico(justificativa),
            aoConcluir: () => {
                modalDevolverAberto.value = false;
                exibirSucesso(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_DEVOLVIDO);
            },
            mensagemErro: erroDevolver.value?.message,
        });
    }

    async function confirmarHomologar() {
        await executarAcaoFluxo({
            acao: () => homologarDiagnostico(normalizarTextoOpcional(observacoesHomologar.value)),
            aoConcluir: () => {
                modalHomologarAberto.value = false;
                exibirSucesso(TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_HOMOLOGADO);
            },
            mensagemErro: erroHomologar.value?.message,
        });
    }

    const varianteSituacao = computed<ColorVariant>(() => {
        switch (situacao.value) {
            case 'CONCLUIDO':
                return 'success';
            case 'VALIDADO':
                return 'info';
            case 'HOMOLOGADO':
                return 'primary';
            default:
                return 'warning';
        }
    });

    const servidoresExibidos = computed(() => {
        const responsavelTitulo = unidade.value?.responsavelTitulo;
        if (!responsavelTitulo) {
            return servidores.value;
        }
        return servidores.value.filter((item) => item.servidorTitulo !== responsavelTitulo);
    });

    const servidorSelecionadoTitulo = ref<string>('');

    watch(servidoresExibidos, (novosServidores) => {
        if (!servidorSelecionadoTitulo.value) {
            return;
        }
        const servidorAtualAindaExiste = novosServidores.some(
            (item) => item.servidorTitulo === servidorSelecionadoTitulo.value,
        );
        if (servidorAtualAindaExiste) {
            return;
        }
        servidorSelecionadoTitulo.value = '';
    }, {immediate: true});

    const servidorSelecionado = computed(() =>
        servidoresExibidos.value.find(
            (item) => item.servidorTitulo === servidorSelecionadoTitulo.value,
        ) ?? null,
    );

    const mapaSituacaoCapacitacao = computed(() =>
        Object.fromEntries(
            situacoesCapacitacao.value.map((item) => [
                `${item.servidorTitulo}:${item.competenciaCodigo}`,
                item.situacaoCapacitacao ?? null,
            ]),
        ),
    );

    const consensoServidorSelecionadoPorCompetencia = computed(() =>
        new Map(
            (servidorSelecionado.value?.consenso ?? []).map((item) => [item.competenciaCodigo, item]),
        ),
    );

    const competenciasServidorSelecionado = computed<CompetenciaServidorSelecionado[]>(() =>
        contextoObrigatorio.value.competencias.map((competencia) => ({
            competenciaCodigo: competencia.competenciaCodigo,
            competenciaDescricao: competencia.descricao,
            importancia: consensoServidorSelecionadoPorCompetencia.value.get(
                competencia.competenciaCodigo,
            )?.importancia ?? null,
            dominio: consensoServidorSelecionadoPorCompetencia.value.get(
                competencia.competenciaCodigo,
            )?.dominio ?? null,
            situacaoCapacitacao: servidorSelecionado.value
                ? mapaSituacaoCapacitacao.value[
                `${servidorSelecionado.value.servidorTitulo}:${competencia.competenciaCodigo}`
                ] ?? null
                : null,
        })),
    );

    const possuiDadosCompetenciasServidorSelecionado = computed(() =>
        competenciasServidorSelecionado.value.some((item) =>
            item.importancia !== null || item.dominio !== null,
        ),
    );

    const movimentacoesFormatadas = computed<Movimentacao[]>(() => movimentacoes.value);

    const colunasCompetenciasServidor = [
        {key: 'competenciaDescricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
        {
            key: 'importancia',
            label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA,
            thClass: 'text-center',
            tdClass: 'text-center'
        },
        {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO, thClass: 'text-center', tdClass: 'text-center'},
        {key: 'situacaoCapacitacao', label: TEXTOS.diagnostico.COLUNA_CAPACITACAO},
    ];

    return {
        contexto,
        contextoObrigatorio,
        subprocessoDetalheObrigatorio,
        unidade,
        carregando,
        situacao,
        retornoFluxo,
        limparRetornoFluxo,
        modalHistoricoAberto,
        carregandoHistorico,
        historicoAnalises,
        modalValidarAberto,
        modalDevolverAberto,
        modalHomologarAberto,
        observacoesValidar,
        justificativaDevolver,
        observacoesHomologar,
        mensagemErroJustificativaDevolver,
        podeValidar,
        podeDevolver,
        podeHomologar,
        habilitarValidar,
        habilitarDevolver,
        habilitarHomologar,
        abrirHistoricoAnalise,
        abrirModalValidar,
        abrirModalDevolver,
        abrirModalHomologar,
        confirmarValidar,
        confirmarDevolver,
        confirmarHomologar,
        validando,
        devolvendo,
        homologando,
        varianteSituacao,
        varianteSituacaoServidor,
        formatDataSimples,
        formatSituacaoSubprocesso,
        formatTipoResponsabilidade,
        formatarSituacaoCapacitacaoResumida,
        formatarSituacaoCapacitacao,
        formatarSituacaoServidor,
        formatarNota,
        servidoresExibidos,
        servidorSelecionado,
        servidorSelecionadoTitulo,
        competenciasServidorSelecionado,
        possuiDadosCompetenciasServidorSelecionado,
        movimentacoesFormatadas,
        colunasCompetenciasServidor,
    };
}

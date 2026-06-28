import {computed, ref, watch} from 'vue';
import type {ColorVariant} from 'bootstrap-vue-next';
import {normalizarErro} from '@/utils/apiError/normalizer';
import {useDiagnosticoUnidade} from '@/composables/useDiagnosticoUnidade';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {listarAnalisesDiagnostico} from '@/services/analiseService';
import {formatarDataBR} from '@/utils';
import {formatSituacaoSubprocesso} from '@/utils/formatters';
import {TEXTOS} from '@/constants/textos';
import {useToast} from '@/composables/useToast';
import type {Analise, Movimentacao, ResponsavelDto, SubprocessoDetalhe} from '@/types/tipos';
import type {
    DiagnosticoContexto,
    SituacaoAvaliacaoServidor,
    ValorSituacaoCapacitacao,
} from '@/types/diagnostico-competencias';

type RetornoFluxo = {mensagem: string; variante: 'danger'};

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
        queryContextoEdicao,
        subprocesso: subprocessoDetalhe,
        podeValidarDiagnostico,
        podeDevolverDiagnostico,
        podeHomologarDiagnostico,
        habilitarValidarDiagnostico,
        habilitarDevolverDiagnostico,
        habilitarHomologarDiagnostico,
    } = useDiagnosticoPermissoes(props.codSubprocesso);

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
        || queryContextoEdicao.isPending.value
        || queryContextoEdicao.isLoading.value,
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

    function formatDataSimples(dataStr: string | null): string {
        return dataStr ? formatarDataBR(dataStr) : '';
    }

    function formatTipoResponsabilidade(resp: ResponsavelDto | null): string {
        if (!resp?.tipo) return '';
        if (resp.tipo === 'Substituição' && resp.dataFim) {
            return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
        }
        if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
            return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
        }
        return resp.tipo;
    }

    async function abrirModalValidar() {
        observacoesValidar.value = '';
        try {
            await validarAcaoValidarDiagnostico();
            modalValidarAberto.value = true;
        } catch (erro) {
            registrarErro(normalizarErro(erro).mensagem ?? erroValidacaoValidar.value?.message);
        }
    }

    async function abrirModalDevolver() {
        justificativaDevolver.value = '';
        tentouDevolverSemJustificativa.value = false;
        try {
            await validarAcaoDevolverDiagnostico();
            modalDevolverAberto.value = true;
        } catch (erro) {
            registrarErro(normalizarErro(erro).mensagem ?? erroValidacaoDevolver.value?.message);
        }
    }

    async function abrirModalHomologar() {
        observacoesHomologar.value = '';
        try {
            await validarAcaoHomologarDiagnostico();
            modalHomologarAberto.value = true;
        } catch (erro) {
            registrarErro(normalizarErro(erro).mensagem ?? erroValidacaoHomologar.value?.message);
        }
    }

    function limparRetornoFluxo() {
        retornoFluxo.value = null;
    }

    async function abrirHistoricoAnalise() {
        carregandoHistorico.value = true;
        modalHistoricoAberto.value = true;
        try {
            historicoAnalises.value = await listarAnalisesDiagnostico(props.codSubprocesso);
        } catch {
            registrarErro(TEXTOS.diagnostico.ERRO_SALVAR);
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
        try {
            await config.acao();
            config.aoConcluir();
        } catch {
            registrarErro(config.mensagemErro);
        }
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
            case 'CONCLUIDO': return 'success';
            case 'VALIDADO': return 'info';
            case 'HOMOLOGADO': return 'primary';
            default: return 'warning';
        }
    });

    function formatarNota(valor: number | null): string {
        if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
        if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
        return String(valor);
    }

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

    const competenciasServidorSelecionado = computed<CompetenciaServidorSelecionado[]>(() =>
        contextoObrigatorio.value.competencias.map((competencia) => ({
            competenciaCodigo: competencia.competenciaCodigo,
            competenciaDescricao: competencia.descricao,
            importancia: servidorSelecionado.value?.consenso.find(
                (item) => item.competenciaCodigo === competencia.competenciaCodigo,
            )?.importancia ?? null,
            dominio: servidorSelecionado.value?.consenso.find(
                (item) => item.competenciaCodigo === competencia.competenciaCodigo,
            )?.dominio ?? null,
            situacaoCapacitacao: servidorSelecionado.value
                ? mapaSituacaoCapacitacao.value[
                    `${servidorSelecionado.value.servidorTitulo}:${competencia.competenciaCodigo}`
                ] ?? null
                : null,
        })),
    );

    function formatarSituacaoCapacitacaoResumida(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
        if (!situacaoCapacitacao) {
            return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
        }
        return `${situacaoCapacitacao} - ${formatarSituacaoCapacitacao(situacaoCapacitacao)}`;
    }

    function formatarSituacaoCapacitacao(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
        switch (situacaoCapacitacao) {
            case 'NA': return TEXTOS.diagnostico.CAPACITACAO_NA;
            case 'AC': return TEXTOS.diagnostico.CAPACITACAO_AC;
            case 'EC': return TEXTOS.diagnostico.CAPACITACAO_EC;
            case 'C': return TEXTOS.diagnostico.CAPACITACAO_C;
            case 'I': return TEXTOS.diagnostico.CAPACITACAO_I;
            default: return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
        }
    }

    function varianteSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): ColorVariant {
        switch (situacaoServidor) {
            case 'CONSENSO_APROVADO': return 'success';
            case 'AVALIACAO_IMPOSSIBILITADA': return 'secondary';
            case 'CONSENSO_CRIADO': return 'warning';
            case 'AUTOAVALIACAO_CONCLUIDA': return 'info';
            default: return 'light';
        }
    }

    function formatarSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): string {
        const mapa: Record<SituacaoAvaliacaoServidor, string> = {
            AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
            AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
            CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
            CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
            AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
        };
        return mapa[situacaoServidor] ?? situacaoServidor;
    }

    const movimentacoesFormatadas = computed<Movimentacao[]>(() => movimentacoes.value);

    const colunasCompetenciasServidor = [
        {key: 'competenciaDescricao', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
        {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA, thClass: 'text-center', tdClass: 'text-center'},
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
        movimentacoesFormatadas,
        colunasCompetenciasServidor,
    };
}

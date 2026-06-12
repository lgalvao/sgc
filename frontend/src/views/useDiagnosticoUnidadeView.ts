import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import type {ColorVariant} from 'bootstrap-vue-next';
import {useMonitoramentoDiagnostico} from '@/composables/useMonitoramentoDiagnostico';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {listarAnalisesDiagnostico} from '@/services/analiseService';
import {TEXTOS} from '@/constants/textos';
import type {Analise} from '@/types/tipos';
import type {AvaliacaoCompetencia, SituacaoAvaliacaoServidor, ValorSituacaoCapacitacao} from '@/types/diagnostico-competencias';

type RetornoFluxo = {mensagem: string; variante: 'danger' | 'success'};

interface DiagnosticoUnidadeViewProps {
    codSubprocesso: number;
    siglaUnidade: string;
}

interface MatrizCompetenciaServidor {
    competenciaCodigo: number;
    competenciaDescricao: string;
    avaliacoesPorServidor: Array<{
        servidorTitulo: string;
        importancia: number | null;
        dominio: number | null;
        situacaoCapacitacao: ValorSituacaoCapacitacao | null;
    }>;
}

export function useDiagnosticoUnidadeView(props: DiagnosticoUnidadeViewProps) {
    const router = useRouter();
    const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
    const {
        habilitarValidarDiagnostico,
        habilitarDevolverDiagnostico,
        habilitarHomologarDiagnostico,
    } = useDiagnosticoPermissoes(props.codSubprocesso);

    const {
        unidade,
        servidores,
        situacoesCapacitacao,
        movimentacoes,
        carregando,
        situacao,
        totalPendentes,
    } = useMonitoramentoDiagnostico(props.codSubprocesso);

    const {
        validando,
        devolvendo,
        homologando,
        erroValidar,
        erroDevolver,
        erroHomologar,
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

    const podeValidar = computed(() => habilitarValidarDiagnostico.value);
    const podeDevolver = computed(() => habilitarDevolverDiagnostico.value);
    const podeHomologar = computed(() => habilitarHomologarDiagnostico.value);

    function abrirModalValidar() {
        observacoesValidar.value = '';
        modalValidarAberto.value = true;
    }

    function abrirModalDevolver() {
        justificativaDevolver.value = '';
        tentouDevolverSemJustificativa.value = false;
        modalDevolverAberto.value = true;
    }

    function abrirModalHomologar() {
        observacoesHomologar.value = '';
        modalHomologarAberto.value = true;
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

    function registrarRetornoFluxo(variante: RetornoFluxo['variante'], mensagem?: string | null) {
        retornoFluxo.value = {
            variante,
            mensagem: mensagem?.trim() || TEXTOS.diagnostico.ERRO_SALVAR,
        };
    }

    function registrarSucesso(mensagem: string) {
        registrarRetornoFluxo('success', mensagem);
    }

    function registrarErro(mensagem?: string | null) {
        registrarRetornoFluxo('danger', mensagem);
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
            mensagemSucesso: string;
            mensagemErro?: string | null;
        },
    ) {
        try {
            await config.acao();
            config.aoConcluir();
            registrarSucesso(config.mensagemSucesso);
        } catch {
            registrarErro(config.mensagemErro);
        }
    }

    async function confirmarValidar() {
        await executarAcaoFluxo({
            acao: () => validarDiagnostico(normalizarTextoOpcional(observacoesValidar.value)),
            aoConcluir: () => {
                modalValidarAberto.value = false;
            },
            mensagemSucesso: TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_VALIDADO,
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
            },
            mensagemSucesso: TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_DEVOLVIDO,
            mensagemErro: erroDevolver.value?.message,
        });
    }

    async function confirmarHomologar() {
        await executarAcaoFluxo({
            acao: () => homologarDiagnostico(normalizarTextoOpcional(observacoesHomologar.value)),
            aoConcluir: () => {
                modalHomologarAberto.value = false;
            },
            mensagemSucesso: TEXTOS.diagnostico.SUCESSO_DIAGNOSTICO_HOMOLOGADO,
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

    function varianteSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): ColorVariant {
        const mapa: Record<SituacaoAvaliacaoServidor, ColorVariant> = {
            CONSENSO_APROVADO: 'success',
            AVALIACAO_IMPOSSIBILITADA: 'secondary',
            CONSENSO_CRIADO: 'warning',
            AUTOAVALIACAO_CONCLUIDA: 'info',
            AUTOAVALIACAO_NAO_INICIADA: 'light',
        };
        return mapa[situacaoServidor];
    }

    function formatarSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): string {
        return {
            AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
            AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
            CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
            CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
            AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
        }[situacaoServidor];
    }

    function varianteCapacitacao(situacaoCapacitacao: ValorSituacaoCapacitacao | null): ColorVariant {
        if (situacaoCapacitacao === null) return 'light';
        const mapa: Record<ValorSituacaoCapacitacao, ColorVariant> = {NA: 'secondary', AC: 'danger', EC: 'warning', C: 'success', I: 'primary'};
        return mapa[situacaoCapacitacao];
    }

    function formatarCapacitacao(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
        if (situacaoCapacitacao === null) return '-';
        return {
            NA: TEXTOS.diagnostico.CAPACITACAO_NA,
            AC: TEXTOS.diagnostico.CAPACITACAO_AC,
            EC: TEXTOS.diagnostico.CAPACITACAO_EC,
            C: TEXTOS.diagnostico.CAPACITACAO_C,
            I: TEXTOS.diagnostico.CAPACITACAO_I,
        }[situacaoCapacitacao];
    }

    function formatarNota(valor: number | null): string {
        if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
        if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
        return String(valor);
    }

    function calcularGap(item: AvaliacaoCompetencia): number | null {
        if (item.importancia === null || item.dominio === null || item.importancia === 0 || item.dominio === 0) return null;
        return item.importancia - item.dominio;
    }

    function obterGapInfo(item: AvaliacaoCompetencia): {texto: string; variante: string} | null {
        const gap = calcularGap(item);
        if (gap === null) return null;
        return {
            texto: gap > 0 ? `+${gap}` : String(gap),
            variante: gap > 0 ? 'text-danger fw-bold' : 'text-success',
        };
    }

    const mapaDescricaoCompetencia = computed(() =>
        Object.fromEntries((contexto.value?.competencias ?? []).map((competencia) => [competencia.competenciaCodigo, competencia.descricao])),
    );

    const servidoresExibidos = computed(() => {
        const responsavelTitulo = unidade.value?.responsavelTitulo;
        if (!responsavelTitulo) {
            return servidores.value;
        }
        return servidores.value.filter((item) => item.servidorTitulo !== responsavelTitulo);
    });

    const mapaSituacaoCapacitacao = computed(() =>
        Object.fromEntries(
            situacoesCapacitacao.value.map((item) => [
                `${item.servidorTitulo}:${item.competenciaCodigo}`,
                item.situacaoCapacitacao ?? null,
            ]),
        ),
    );

    const matrizCompetencias = computed<MatrizCompetenciaServidor[]>(() =>
        (contexto.value?.competencias ?? []).map((competencia) => ({
            competenciaCodigo: competencia.competenciaCodigo,
            competenciaDescricao: competencia.descricao,
            avaliacoesPorServidor: servidoresExibidos.value.map((servidorItem) => {
                const avaliacao = servidorItem.consenso.find(
                    (item) => item.competenciaCodigo === competencia.competenciaCodigo,
                );
                return {
                    servidorTitulo: servidorItem.servidorTitulo,
                    importancia: avaliacao?.importancia ?? null,
                    dominio: avaliacao?.dominio ?? null,
                    situacaoCapacitacao: mapaSituacaoCapacitacao.value[
                        `${servidorItem.servidorTitulo}:${competencia.competenciaCodigo}`
                    ] ?? null,
                };
            }),
        })),
    );

    const situacoesComDescricao = computed(() =>
        situacoesCapacitacao.value.map((situacaoItem) => ({
            ...situacaoItem,
            nomeCompetencia: mapaDescricaoCompetencia.value[situacaoItem.competenciaCodigo] ?? `Competência ${situacaoItem.competenciaCodigo}`,
        })),
    );

    function formatarSituacaoCapacitacaoResumida(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
        return situacaoCapacitacao ?? TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
    }

    const colunasCompetencias = [
        {key: 'competenciaCodigo', label: 'Código'},
        {key: 'importancia', label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA},
        {key: 'dominio', label: TEXTOS.diagnostico.COLUNA_DOMINIO},
        {key: 'gap', label: TEXTOS.diagnostico.COLUNA_GAP},
    ];

    const colunasSituacoes = [
        {key: 'servidorTitulo', label: TEXTOS.diagnostico.COLUNA_SERVIDOR},
        {key: 'nomeCompetencia', label: TEXTOS.diagnostico.COLUNA_COMPETENCIA},
        {key: 'situacaoCapacitacao', label: TEXTOS.diagnostico.COLUNA_CAPACITACAO},
    ];

    return {
        router,
        unidade,
        servidores,
        situacoesCapacitacao,
        movimentacoes,
        carregando,
        situacao,
        totalPendentes,
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
        formatarSituacaoServidor,
        varianteCapacitacao,
        formatarCapacitacao,
        formatarSituacaoCapacitacaoResumida,
        formatarNota,
        obterGapInfo,
        servidoresExibidos,
        matrizCompetencias,
        situacoesComDescricao,
        colunasCompetencias,
        colunasSituacoes,
    };
}

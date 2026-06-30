import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {useMapaQuery} from '@/composables/useMapaQuery';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useDiagnosticoPermissoes} from '@/composables/useDiagnosticoPermissoes';
import {useAutoavaliacaoDiagnostico} from '@/composables/useAutoavaliacaoDiagnostico';
import {useEquipeDiagnostico} from '@/composables/useEquipeDiagnostico';
import {useToast} from '@/composables/useToast';
import {useFluxoDiagnostico} from '@/composables/useFluxoDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {Atividade, Conhecimento} from '@/types/mapa-modelos';
import type {ItemEquipeDiagnostico, SituacaoAvaliacaoServidor} from '@/types/diagnostico-competencias';
import {normalizarErro} from '@/utils/apiError';

type RetornoFluxo = { mensagem: string; variante: 'danger' };

interface AutoavaliacaoDiagnosticoViewProps {
    codSubprocesso: number;
    siglaUnidade: string;
}

export function useAutoavaliacaoDiagnosticoView(props: AutoavaliacaoDiagnosticoViewProps) {
    const router = useRouter();
    const {registrarPendente} = useToast();

    const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);
    const {queryPermissoes, podeCriarConsenso} = useDiagnosticoPermissoes(props.codSubprocesso);
    const queryMapa = useMapaQuery(props.codSubprocesso);
    const {
        competenciasLocais,
        situacaoServidor,
        carregando,
        salvandoAutomaticamente,
        concluindo,
        podeEditar,
        podeConcluirAutoavaliacao,
        habilitarConcluirAutoavaliacao,
        atualizarNota,
        concluirAutoavaliacao,
    } = useAutoavaliacaoDiagnostico(props.codSubprocesso);
    const {
        impossibilitando,
        impossibilitarAvaliacao,
    } = useFluxoDiagnostico(props.codSubprocesso);

    const ehChefe = computed(() => podeCriarConsenso.value);
    const {itens: itensEquipe, pendentes} = useEquipeDiagnostico(props.codSubprocesso, ehChefe);
    const ehAutoavaliacaoConcluida = computed(() => situacaoServidor.value === 'AUTOAVALIACAO_CONCLUIDA');
    const ehConsensoAprovado = computed(() => situacaoServidor.value === 'CONSENSO_APROVADO');
    const retornoFluxo = ref<RetornoFluxo | null>(null);
    const modalConcluirAberto = ref(false);
    const modalImpossibilitarAberto = ref(false);
    const detalhesCompetenciaAbertos = ref<Record<number, boolean>>({});
    const servidorParaImpossibilitar = ref<ItemEquipeDiagnostico | null>(null);
    const justificativaImpossibilidade = ref('');
    const tentouImpossibilitarSemJustificativa = ref(false);

    function abrirModalConcluir() {
        limparRetornoFluxo();
        modalConcluirAberto.value = true;
    }

    function abrirModalImpossibilitar(servidor: ItemEquipeDiagnostico) {
        servidorParaImpossibilitar.value = servidor;
        justificativaImpossibilidade.value = '';
        tentouImpossibilitarSemJustificativa.value = false;
        modalImpossibilitarAberto.value = true;
    }

    function fecharModalImpossibilitar() {
        modalImpossibilitarAberto.value = false;
    }

    function limparRetornoFluxo() {
        retornoFluxo.value = null;
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

    const mensagemErroJustificativa = computed(() =>
        tentouImpossibilitarSemJustificativa.value
            ? TEXTOS.diagnostico.ERRO_JUSTIFICATIVA_OBRIGATORIA
            : '',
    );

    async function executarAcaoFluxo(
        acao: () => Promise<void>,
        aoSucesso?: () => void,
    ) {
        try {
            await acao();
            aoSucesso?.();
        } catch (err) {
            registrarErro(normalizarErro(err).mensagem);
        }
    }

    async function confirmarConcluir() {
        try {
            await concluirAutoavaliacao();
            modalConcluirAberto.value = false;
            registrarPendente(TEXTOS.diagnostico.SUCESSO_AUTOAVALIACAO_CONCLUIDA);
            if (contexto.value?.processoCodigo) {
                await router.push({
                    name: 'Subprocesso',
                    params: {
                        codProcesso: contexto.value.processoCodigo,
                        siglaUnidade: props.siglaUnidade,
                    },
                    query: {
                        codSubprocesso: String(props.codSubprocesso),
                    },
                });
            }
        } catch (err) {
            modalConcluirAberto.value = false;
            registrarErro(normalizarErro(err).mensagem);
        }
    }

    async function confirmarImpossibilitar() {
        const justificativa = normalizarTextoOpcional(justificativaImpossibilidade.value);
        if (!justificativa) {
            tentouImpossibilitarSemJustificativa.value = true;
            return;
        }
        const servidor = servidorParaImpossibilitar.value;
        tentouImpossibilitarSemJustificativa.value = false;
        if (!servidor) return;

        await executarAcaoFluxo(
            () => impossibilitarAvaliacao(servidor.servidorTitulo, justificativa),
            fecharModalImpossibilitar,
        );
    }

    function navegarParaConsenso(servidorTitulo: string) {
        void router.push({
            name: 'ConsensoDiagnostico',
            params: {
                codSubprocesso: props.codSubprocesso,
                siglaUnidade: props.siglaUnidade,
                servidorTitulo,
            },
        });
    }

    function voltar() {
        router.back();
    }

    function alternarDetalhesCompetencia(competenciaCodigo: number) {
        detalhesCompetenciaAbertos.value[competenciaCodigo] = !detalhesCompetenciaAbertos.value[competenciaCodigo];
    }

    function varianteSituacaoServidor(situacao: SituacaoAvaliacaoServidor) {
        switch (situacao) {
            case 'CONSENSO_APROVADO':
                return 'success';
            case 'AVALIACAO_IMPOSSIBILITADA':
                return 'secondary';
            case 'CONSENSO_CRIADO':
                return 'warning';
            case 'AUTOAVALIACAO_CONCLUIDA':
                return 'info';
            default:
                return 'light';
        }
    }

    function formatarSituacaoServidor(situacao: SituacaoAvaliacaoServidor): string {
        const mapa: Record<SituacaoAvaliacaoServidor, string> = {
            AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
            AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
            CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
            CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
            AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
        };
        return mapa[situacao];
    }

    function formatarNota(valor: number | null): string {
        if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
        if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
        return String(valor);
    }

    function formatarConhecimentos(conhecimentos: Conhecimento[]): string {
        if (conhecimentos.length === 0) return '-';
        return conhecimentos.map((conhecimento) => conhecimento.descricao).join(', ');
    }

    const colunas = [
        {
            key: 'descricao',
            label: TEXTOS.diagnostico.COLUNA_COMPETENCIA,
            tdClass: 'align-top',
            thStyle: {width: '400px'},
        },
        {
            key: 'importancia',
            label: TEXTOS.diagnostico.COLUNA_IMPORTANCIA,
            thClass: 'text-center',
            tdClass: 'align-top text-center',
            thStyle: {width: '120px'},
        },
        {
            key: 'dominio',
            label: TEXTOS.diagnostico.COLUNA_DOMINIO,
            thClass: 'text-center',
            tdClass: 'align-top text-center',
            thStyle: {width: '120px'},
        },
    ];

    const competenciasComDescricao = computed(() => {
        const mapaAtividades = new Map<number, Atividade[]>(
            (queryMapa.data.value?.competencias ?? []).map((competencia) => [competencia.codigo, competencia.atividades ?? []]),
        );
        return competenciasLocais.value.map((competencia) => ({
            ...competencia,
            descricao: competencia.competenciaDescricao,
            atividades: mapaAtividades.get(competencia.competenciaCodigo) ?? [],
        }));
    });

    const opcoesNota = [0, 1, 2, 3, 4, 5, 6].map((value) => ({value, text: value === 0 ? 'NA' : String(value)}));

    function normalizarValorNota(valor: unknown): number | null {
        if (valor === null || valor === undefined || valor === '') return null;
        if (typeof valor === 'number') return Number.isNaN(valor) ? null : valor;
        const numero = Number(valor);
        return Number.isNaN(numero) ? null : numero;
    }

    return {
        contexto,
        carregando: computed(() =>
            carregando.value
            || queryPermissoes.status.value === 'pending'
            || queryMapa.status.value === 'pending',
        ),
        retornoFluxo,
        limparRetornoFluxo,
        salvandoAutomaticamente,
        ehAutoavaliacaoConcluida,
        ehConsensoAprovado,
        ehChefe,
        podeConcluirAutoavaliacao,
        habilitarConcluirAutoavaliacao,
        podeEditar,
        colunas,
        competenciasComDescricao,
        detalhesCompetenciaAbertos,
        alternarDetalhesCompetencia,
        formatarConhecimentos,
        opcoesNota,
        atualizarNota,
        normalizarValorNota,
        confirmarConcluir,
        concluirAutoavaliacao: confirmarConcluir,
        modalConcluirAberto,
        abrirModalConcluir,
        concluindo,
        itensEquipe,
        pendentes,
        varianteSituacaoServidor,
        formatarSituacaoServidor,
        navegarParaConsenso,
        abrirModalImpossibilitar,
        modalImpossibilitarAberto,
        fecharModalImpossibilitar,
        servidorParaImpossibilitar,
        justificativaImpossibilidade,
        mensagemErroJustificativa,
        confirmarImpossibilitar,
        impossibilitando,
        voltar,
        formatarNota,
    };
}

import {computed, ref, watch, type Ref} from "vue";
import * as processoService from "@/services/processo";
import * as subprocessoService from "@/services/subprocessoService";
import {
    type Atividade,
    type AtividadeOperacaoResponse,
    type ProcessoResumo,
    type UnidadeImportacao,
} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useErrorHandler} from "@/composables/useErrorHandler";
import logger from "@/utils/logger";

export function useImportarAtividadesTela(
    mostrar: Ref<boolean>,
    codSubprocessoDestino: Ref<number | null | undefined>,
    onFechar: () => void,
    onImportar: (resultado: AtividadeOperacaoResponse) => void,
) {
    const {validarSubmissao, resetarValidacao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();
    const {executarComTratamentoDeErros} = useErrorHandler();

    const resultadoImportacao = ref<AtividadeOperacaoResponse | null>(null);
    const erroImportacao = ref<string | null>(null);
    const importando = ref(false);
    const processosParaImportacao = ref<ProcessoResumo[]>([]);
    const processoSelecionado = ref<ProcessoResumo | null>(null);
    const processoSelecionadoId = ref<number | null>(null);
    const unidadesParticipantes = ref<UnidadeImportacao[]>([]);
    const unidadeSelecionada = ref<UnidadeImportacao | null>(null);
    const unidadeSelecionadaId = ref<number | null>(null);
    const atividadesParaImportar = ref<Atividade[]>([]);
    const atividadesSelecionadas = ref<Atividade[]>([]);

    const mensagemErroProcesso = computed(() =>
        deveExibirErro(!processoSelecionadoId.value) ? "Selecione o processo de origem." : ""
    );
    const mensagemErroUnidade = computed(() =>
        deveExibirErro(!unidadeSelecionadaId.value) ? "Selecione a unidade de origem." : ""
    );
    const mensagemErroAtividades = computed(() =>
        deveExibirErro(atividadesSelecionadas.value.length === 0) ? TEXTOS.atividades.importacao.SELECIONE_ATIVIDADE : ""
    );
    const isFormularioValido = computed(() =>
        Boolean(processoSelecionadoId.value && unidadeSelecionadaId.value && atividadesSelecionadas.value.length > 0)
    );

    watch(mostrar, async (aberto) => {
        if (aberto) {
            resetModal();
            await carregarProcessosParaImportacao();
        }
    }, {immediate: true});

    watch(processoSelecionadoId, async (newId) => {
        if (newId) {
            const processo = processosParaImportacao.value.find((p) => p.codigo === Number(newId));
            if (processo) await selecionarProcesso(processo);
        } else {
            await selecionarProcesso(null);
        }
    });

    watch(unidadeSelecionadaId, (newId) => {
        if (newId) {
            const unidade = unidadesParticipantes.value.find((u) => u.codUnidade === Number(newId));
            if (unidade) selecionarUnidade(unidade);
        } else {
            selecionarUnidade(null);
        }
    });

    function resetModal() {
        processoSelecionado.value = null;
        processoSelecionadoId.value = null;
        unidadesParticipantes.value = [];
        unidadeSelecionada.value = null;
        unidadeSelecionadaId.value = null;
        atividadesParaImportar.value = [];
        atividadesSelecionadas.value = [];
        resetarValidacao();
    }

    function limparErroImportacao() {
        erroImportacao.value = null;
    }

    function registrarErroImportacao(mensagem: string) {
        erroImportacao.value = mensagem;
    }

    async function executarComTratamentoErro<T>(acao: () => Promise<T>, aplicarResultado: (resultado: T) => void) {
        try {
            aplicarResultado(await executarComTratamentoDeErros(acao, (erro) => {
                registrarErroImportacao(erro.mensagem);
            }));
        } catch (error) {
            logger.error("Erro ao carregar dados de importação de atividades", error);
        }
    }

    async function carregarProcessosParaImportacao() {
        processosParaImportacao.value = [];
        await executarComTratamentoErro(
            () => processoService.buscarProcessosParaImportacao(),
            (processos) => { processosParaImportacao.value = processos; },
        );
    }

    function selecionarTodasAtividades() {
        atividadesSelecionadas.value = [...atividadesParaImportar.value];
    }

    function limparSelecaoAtividades() {
        atividadesSelecionadas.value = [];
    }

    async function selecionarProcesso(processo: ProcessoResumo | null) {
        processoSelecionado.value = processo;
        atividadesSelecionadas.value = [];
        limparErroImportacao();
        unidadesParticipantes.value = [];
        if (processo) {
            await executarComTratamentoErro(
                () => processoService.buscarUnidadesParaImportacao(processo.codigo),
                (unidades) => { unidadesParticipantes.value = unidades; },
            );
        }
        unidadeSelecionada.value = null;
        unidadeSelecionadaId.value = null;
    }

    async function selecionarUnidade(unidadePu: UnidadeImportacao | null) {
        atividadesSelecionadas.value = [];
        unidadeSelecionada.value = unidadePu;
        limparErroImportacao();
        atividadesParaImportar.value = [];
        if (unidadePu) {
            await executarComTratamentoErro(
                () => subprocessoService.listarAtividadesParaImportacao(unidadePu.codSubprocesso),
                (atividadesDaOutraUnidade) => { atividadesParaImportar.value = [...atividadesDaOutraUnidade]; },
            );
        }
    }

    async function importar() {
        if (!validarSubmissao(isFormularioValido.value)) {
            await focarPrimeiroErroInvalido();
            return;
        }

        limparErroImportacao();
        if (!codSubprocessoDestino.value || !unidadeSelecionada.value) return;

        const idsAtividades = atividadesSelecionadas.value.map((a) => a.codigo);
        importando.value = true;
        try {
            resultadoImportacao.value = await executarComTratamentoDeErros(
                () => subprocessoService.importarAtividades(
                    codSubprocessoDestino.value!,
                    unidadeSelecionada.value!.codSubprocesso,
                    idsAtividades,
                ),
                (erro) => { registrarErroImportacao(erro.mensagem); },
            );
            onImportar(resultadoImportacao.value);
            onFechar();
        } catch (error) {
            resultadoImportacao.value = null;
            logger.error("Erro ao importar atividades", error);
        } finally {
            importando.value = false;
        }
    }

    return {
        erroImportacao,
        importando,
        processosParaImportacao,
        processoSelecionado,
        processoSelecionadoId,
        unidadesParticipantes,
        unidadeSelecionada,
        unidadeSelecionadaId,
        atividadesParaImportar,
        atividadesSelecionadas,
        mensagemErroProcesso,
        mensagemErroUnidade,
        mensagemErroAtividades,
        limparErroImportacao,
        selecionarTodasAtividades,
        limparSelecaoAtividades,
        importar,
    };
}

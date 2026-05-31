import {computed, nextTick, onMounted, reactive, ref, toRefs, watch} from "vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useFluxoSubprocesso} from "@/composables/useFluxoSubprocesso";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {usePerfilStore} from "@/stores/perfil";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAcesso} from "@/composables/acesso";
import {useCadastroAtividadesMutacoes} from "@/composables/useCadastroAtividadesMutacoes";
import {useCadastroRevisaoSemMudancas} from "@/composables/useCadastroRevisaoSemMudancas";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useCadastroOrquestracao} from "@/composables/useCadastroOrquestracao";
import {useCadastroAnaliseFluxo} from "@/views/cadastroAnaliseFluxo";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import {
    type Atividade,
    type AtividadeOperacaoResponse,
    type ErroValidacao,
    Perfil,
    type PermissoesSubprocesso,
    SituacaoSubprocesso,
    TipoProcesso
} from "@/types/tipos";
import {calcularAssinaturaCadastro, formatSituacaoSubprocesso} from "@/utils/formatters";
import {normalizarPermissoesSubprocesso} from "@/utils/permissoesSubprocesso";
import {normalizarErro} from "@/utils/apiError";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {TEXTOS} from "@/constants/textos";
import {extrairTextoPlanoHtml} from "@/utils/textoFormatado";

interface CadastroTelaProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

export function useCadastroTela(props: CadastroTelaProps) {
    const atividades = ref<Atividade[]>([]);

    const orquestracao = useCadastroOrquestracao(props, atividades);
    const carregandoInicial = orquestracao.carregandoInicial;
    const codigoSubprocesso = orquestracao.codigoSubprocesso;
    const atividadesSnapshotInicial = orquestracao.atividadesSnapshotInicial;
    const unidade = orquestracao.unidade;
    const codMapa = orquestracao.codMapa;
    const carregarContextoInicial = orquestracao.carregarContextoInicial;
    const processarRespostaLocal = orquestracao.processarRespostaLocal;

    const subprocessoStore = useSubprocessoStore();
    const perfilStore = usePerfilStore();
    const mapasStore = useMapas(codigoSubprocesso);
    const fluxoSubprocesso = useFluxoSubprocesso();
    const {notify, notificacao, clear} = useNotification();
    const {
        validarSubmissao,
        resetarValidacao,
        deveExibirErro,
        focarPrimeiroErroInvalido
    } = useValidacaoFormulario();
    const {impactoMapa: impactos} = mapasStore;

    const subprocesso = computed(() => subprocessoStore.contextoCadastro?.detalhes ?? null);
    const acesso = useAcesso(subprocesso);
    const {
        podeEditarCadastro,
        podeVisualizarImpacto,
        podeDevolverCadastro,
        mostrarDevolverCadastro,
        mostrarImportarAtividades,
        mostrarDisponibilizarCadastro,
        habilitarEditarCadastro,
        habilitarDevolverCadastro,
        podeDisponibilizarCadastro,
        acaoPrincipalCadastro
    } = acesso;

    const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
    const esconderEdicaoCadastroParaChefe = computed(() =>
        perfilStore.perfilSelecionado === Perfil.CHEFE
        && podeEditarCadastro.value
        && !habilitarEditarCadastro.value
    );
    const mostrarControlesEdicaoCadastro = computed(() => podeEditarCadastro.value && !esconderEdicaoCadastroParaChefe.value);

    const permissoesUI = computed<PermissoesSubprocesso>(() => ({
        ...normalizarPermissoesSubprocesso(subprocesso.value?.permissoes),
        podeEditarCadastro: podeEditarCadastro.value,
        podeDisponibilizarCadastro: podeDisponibilizarCadastro.value,
        podeDevolverCadastro: podeDevolverCadastro.value,
        habilitarEditarCadastro: habilitarEditarCadastro.value,
        habilitarDevolverCadastro: habilitarDevolverCadastro.value,
    }));

    const assinaturaCadastroAtual = computed(() => calcularAssinaturaCadastro(atividades.value));
    const houveAlteracaoCadastro = computed(() => assinaturaCadastroAtual.value !== atividadesSnapshotInicial.value);

    const atividadesOrdenadas = computed(() => {
        return [...atividades.value].sort((a, b) => (b.codigo || 0) - (a.codigo || 0));
    });

    const situacaoAtual = computed(() => subprocesso.value?.situacao);
    const {
        disponibilizacaoSemMudancas,
        checkboxSemMudancasDesabilitado,
        loadingInicioRevisao,
        sincronizarDisponibilizacaoSemMudancasInicial
    } = useCadastroRevisaoSemMudancas({
        codigoSubprocesso,
        isRevisao,
        situacaoAtual,
        houveAlteracaoCadastro,
        fluxoSubprocesso
    });

    const {executarComTratamentoDeErros, ultimoErro} = useErrorHandler();

    const loadingValidacao = ref(false);
    const loadingDisponibilizacao = ref(false);
    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);
    const erroTick = ref(0);

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        for (const erro of errosValidacao.value) {
            if (!erro.atividadeCodigo) continue;
            const atividade = atividades.value.find((item) => item.codigo === erro.atividadeCodigo);
            if (!atividade?.conhecimentos || atividade.conhecimentos.length === 0) {
                mapa.set(erro.atividadeCodigo, erro.mensagem);
            }
        }
        return mapa;
    });

    function limparErrosValidacao() {
        errosValidacao.value = [];
        erroGlobal.value = null;
    }

    function definirErroGlobal(mensagem: string) {
        limparErrosValidacao();
        erroGlobal.value = mensagem;
    }

    function aplicarErrosValidacao(erros: ErroValidacao[]) {
        limparErrosValidacao();
        erroTick.value++;
        errosValidacao.value = erros;
        erroGlobal.value = erros.find((erro) => !erro.atividadeCodigo)?.mensagem ?? null;
    }

    function obterErroParaAtividade(atividadeCodigo: number) {
        return mapaErros.value.get(atividadeCodigo);
    }

    const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();

    watch(novaAtividade, (valorAtual, valorAnterior) => {
        if (valorAtual !== valorAnterior && erroNovaAtividade.value) {
            erroNovaAtividade.value = null;
        }
    });

    watch(assinaturaCadastroAtual, (valorAtual, valorAnterior) => {
        if (valorAtual !== valorAnterior && (errosValidacao.value.length > 0 || erroGlobal.value)) {
            limparErrosValidacao();
        }
    });

    const estadoModais = reactive({
        mostrarModalImportar: false,
        mostrarModalConfirmacao: false,
        mostrarModalHistorico: false,
        mostrarModalValidarAnalise: false,
        mostrarModalDevolverAnalise: false,
    });
    const {
        mostrarModalImportar,
        mostrarModalConfirmacao,
        mostrarModalHistorico,
        mostrarModalValidarAnalise,
        mostrarModalDevolverAnalise,
    } = toRefs(estadoModais);

    const {
        mostrarModalImpacto,
        loadingImpacto,
        abrirModalImpacto,
        fecharModalImpacto,
    } = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.carregarImpacto(codigo));

    const atividadeRefs = new Map<number, Element>();
    const atividadeFormRef = ref<InstanceType<typeof CadAtividadeForm> | null>(null);

    const {
        erroNovaAtividade,
        dadosRemocao,
        loadingRemocao,
        mostrarModalConfirmacaoRemocao,
        adicionarAtividade,
        removerAtividade,
        confirmarRemocao,
        salvarEdicaoAtividade,
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento
    } = useCadastroAtividadesMutacoes({
        atividades,
        codigoSubprocesso,
        codMapa,
        executarComTratamentoDeErros,
        ultimoErro,
        notify,
        processarRespostaLocal,
        adicionarAtividadeAction
    });

    async function aoImportarAtividades(resultado: AtividadeOperacaoResponse) {
        mostrarModalImportar.value = false;
        clear();
        await nextTick();
        processarRespostaLocal(resultado);
        if (resultado.aviso) {
            notify(TEXTOS.atividades.AVISO_IMPORTACAO_DUPLICATAS, 'warning');
        } else {
            notify(TEXTOS.atividades.SUCESSO_IMPORTACAO, 'success');
        }
    }

    function setAtividadeRef(atividadeCodigo: number, el: unknown) {
        if (el && el instanceof Element) {
            atividadeRefs.set(atividadeCodigo, el);
        }
    }

    function scrollParaPrimeiroErro() {
        if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeCodigo) {
            const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeCodigo);
            if (primeiraAtividadeComErro) {
                primeiraAtividadeComErro.scrollIntoView({
                    behavior: "instant" as ScrollBehavior,
                    block: "center",
                });
            }
        }
    }

    async function disponibilizarCadastro() {
        if (loadingValidacao.value) return;

        limparErrosValidacao();
        const validacaoLocal = _validarLocalmente(
            atividades,
            isRevisao,
            houveAlteracaoCadastro,
            disponibilizacaoSemMudancas,
            situacaoAtual,
        );

        if (validacaoLocal.tipo === "erro-validacao") {
            aplicarErrosValidacao(validacaoLocal.erros);
            await nextTick();
            return;
        }
        if (validacaoLocal.tipo === "acao-nao-permitida") {
            definirErroGlobal(validacaoLocal.mensagem);
            return;
        }

        loadingValidacao.value = true;
        try {
            const resultado = await fluxoSubprocesso.validarCadastro(codigoSubprocesso.value!);
            if (resultado.valido) {
                mostrarModalConfirmacao.value = true;
                return;
            }
            aplicarErrosValidacao(resultado.erros);
            await nextTick();
            scrollParaPrimeiroErro();
        } catch (error) {
            definirErroGlobal(normalizarErro(error).mensagem);
        } finally {
            loadingValidacao.value = false;
        }
    }

    async function confirmarDisponibilizacao() {
        if (loadingDisponibilizacao.value) return;

        loadingDisponibilizacao.value = true;
        try {
            if (isRevisao.value) {
                await fluxoSubprocesso.disponibilizarRevisaoCadastro(codigoSubprocesso.value!);
            } else {
                await fluxoSubprocesso.disponibilizarCadastro(codigoSubprocesso.value!);
            }
        } finally {
            loadingDisponibilizacao.value = false;
        }
        mostrarModalConfirmacao.value = false;
    }

    const erroGlobalFormatado = computed(() =>
        erroGlobal.value ? {mensagem: erroGlobal.value} : null
    );

    const erroCampoObservacaoDevolucao = computed(() => {
        const erros = fluxoSubprocesso.ultimoErro.value?.erros;
        if (!erros) return "";
        return erros.find((erro) =>
            ["justificativa", "texto", "observacoes"].includes(erro.campo || "")
        )?.mensagem || "";
    });

    const erroFluxoCadastro = computed(() =>
        fluxoSubprocesso.ultimoErro.value?.tipo === "validacao"
            ? undefined
            : fluxoSubprocesso.ultimoErro.value?.mensagem
    );

    const mensagemErroObservacaoDevolucao = computed(() =>
        erroCampoObservacaoDevolucao.value
            || (deveExibirErro(!extrairTextoPlanoHtml(observacaoDevolucao.value))
                ? TEXTOS.atividades.ERRO_DEVOLUCAO_JUSTIFICATIVA
                : "")
    );

    const {
        historicoAnalises,
        loadingAnaliseCadastro,
        loadingDevolucaoAnalise,
        observacaoValidacao,
        observacaoDevolucao,
        abrirModalHistorico,
        abrirModalDevolverAnalise,
        abrirModalValidarAnalise,
        confirmarValidacaoAnalise,
        confirmarDevolucaoAnalise,
    } = useCadastroAnaliseFluxo({
        codigoSubprocesso,
        codProcesso: props.codProcesso,
        sigla: props.sigla,
        isRevisao,
        acaoPrincipalCadastro,
        mostrarModalHistorico,
        mostrarModalValidarAnalise,
        mostrarModalDevolverAnalise,
        resetarValidacao,
        validarSubmissao,
        focarPrimeiroErroInvalido,
        listarAnalisesCadastro,
        homologarCadastro: fluxoSubprocesso.homologarCadastro,
        homologarRevisaoCadastro: fluxoSubprocesso.homologarRevisaoCadastro,
        aceitarCadastro: fluxoSubprocesso.aceitarCadastro,
        aceitarRevisaoCadastro: fluxoSubprocesso.aceitarRevisaoCadastro,
        devolverCadastro: fluxoSubprocesso.devolverCadastro,
        devolverRevisaoCadastro: fluxoSubprocesso.devolverRevisaoCadastro,
    });

    async function adicionarNovaAtividade() {
        const sucesso = await adicionarAtividade();
        await nextTick();
        if (sucesso || erroNovaAtividade.value) atividadeFormRef.value?.inputRef?.$el?.focus();
    }

    onMounted(async () => {
        await carregarContextoInicial();
        sincronizarDisponibilizacaoSemMudancasInicial();
    });

    watch(() => atividades.value?.length, (newLen, oldLen) => {
        if (podeEditarCadastro.value && newLen === 0 && oldLen === undefined) {
            nextTick(() => atividadeFormRef.value?.inputRef?.$el?.focus());
        }
    }, {immediate: true});

    return {
        atividades,
        carregandoInicial,
        codigoSubprocesso,
        atividadesSnapshotInicial,
        unidade,
        permissoesUI,
        isRevisao,
        mostrarControlesEdicaoCadastro,
        disponibilizacaoSemMudancas,
        checkboxSemMudancasDesabilitado,
        loadingInicioRevisao,
        erroGlobalFormatado,
        erroTick,
        erroGlobal,
        notificacao,
        clear,
        novaAtividade,
        erroNovaAtividade,
        loadingAdicionar,
        atividadesOrdenadas,
        obterErroParaAtividade,
        dadosRemocao,
        erroFluxoCadastro,
        historicoAnalises,
        impactos,
        loadingAnaliseCadastro,
        loadingDevolucaoAnalise,
        loadingDisponibilizacao,
        loadingImpacto,
        loadingRemocao,
        mostrarModalConfirmacao,
        mostrarModalConfirmacaoRemocao,
        mostrarModalDevolverAnalise,
        mostrarModalHistorico,
        mostrarModalImpacto,
        mostrarModalImportar,
        mostrarModalValidarAnalise,
        observacaoDevolucao,
        mensagemErroObservacaoDevolucao,
        observacaoValidacao,
        habilitarEditarCadastro,
        mostrarDevolverCadastro,
        mostrarDisponibilizarCadastro,
        mostrarImportarAtividades,
        podeVisualizarImpacto,
        acaoPrincipalCadastro,
        loadingValidacao,
        atividadeFormRef,
        setAtividadeRef,
        aoImportarAtividades,
        confirmarDevolucaoAnalise,
        confirmarDisponibilizacao,
        confirmarRemocao,
        confirmarValidacaoAnalise,
        fecharModalImpacto,
        adicionarNovaAtividade,
        salvarEdicaoAtividade,
        removerAtividade,
        adicionarConhecimento,
        salvarEdicaoConhecimento,
        removerConhecimento,
        abrirModalHistorico,
        abrirModalDevolverAnalise,
        abrirModalValidarAnalise,
        abrirModalImpacto,
        disponibilizarCadastro,
        limparErrosValidacao,
        processarRespostaLocal,
        carregarContextoInicial,
        notify,
        errosValidacao,
        scrollParaPrimeiroErro,
        adicionarAtividade,
        houveAlteracaoCadastro,
        podeEditarCadastro,
        esconderEdicaoCadastroParaChefe
    };
}

type _ResultadoValidacaoLocal =
    | {tipo: "pode-validar"}
    | {tipo: "erro-validacao"; erros: ErroValidacao[]}
    | {tipo: "acao-nao-permitida"; mensagem: string};

function _validarLocalmente(
    atividades: {value: Atividade[]},
    isRevisao: {value: boolean},
    houveAlteracaoCadastro: {value: boolean},
    disponibilizacaoSemMudancas: {value: boolean},
    situacaoAtual: {value: SituacaoSubprocesso | string | undefined}
): _ResultadoValidacaoLocal {
    const cadastroIncompleto = atividades.value.length === 0
        || atividades.value.some((atividade) => !atividade.conhecimentos || atividade.conhecimentos.length === 0);

    if (cadastroIncompleto) {
        return {tipo: "erro-validacao", erros: [{tipo: "PRE_VALIDACAO", mensagem: TEXTOS.atividades.ERRO_CADASTRO_INCOMPLETO}]};
    }
    if (isRevisao.value && !houveAlteracaoCadastro.value && !disponibilizacaoSemMudancas.value) {
        return {tipo: "erro-validacao", erros: [{tipo: "PRE_VALIDACAO", mensagem: TEXTOS.atividades.ERRO_REVISAO_SEM_ALTERACAO}]};
    }

    const situacaoReferencia = isRevisao.value
        ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
        : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

    if (situacaoAtual.value !== situacaoReferencia) {
        return {
            tipo: "acao-nao-permitida",
            mensagem: TEXTOS.comum.ACAO_NAO_PERMITIDA_SITUACAO(formatSituacaoSubprocesso(situacaoReferencia))
        };
    }

    return {tipo: "pode-validar"};
}
